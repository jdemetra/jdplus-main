/*
 * Copyright 2018 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.toolkit.base.tsp.cube;

import jdplus.toolkit.base.tsp.util.ShortLivedCache;
import jdplus.toolkit.base.tsp.util.ShortLivedCaching;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.io.IOIterator;
import nbbrd.io.Resource;
import nbbrd.io.function.IORunnable;
import nbbrd.design.NonNegative;
import org.jspecify.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static internal.toolkit.base.tsp.cube.CubeRepository.checkLeaf;
import static internal.toolkit.base.tsp.cube.CubeRepository.checkNode;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class BulkCubeConnection implements CubeConnection {

    @NonNull
    public static CubeConnection of(@NonNull CubeConnection delegate, @NonNull BulkCube options, @NonNull ShortLivedCaching cacheFactory) {
        return options.isCacheEnabled()
                ? new BulkCubeConnection(delegate, options.getDepth(), cacheFactory.ofTtl(options.getTtl()))
                : delegate;
    }

    @lombok.NonNull
    private final CubeConnection delegate;

    @NonNegative
    private final int depth;

    @lombok.NonNull
    private final ShortLivedCache<CubeId, List<CubeSeriesWithData>> cache;

    private int getCacheLevel() throws IOException {
        return Math.max(0, delegate.getRoot().getMaxLevel() - depth);
    }

    @Override
    public @NonNull Stream<CubeId> getChildren(@NonNull CubeId node) throws IOException {
        checkNode(node);
        List<CubeSeriesWithData> list = peekList(node);
        if (list != null) {
            return list
                    .stream()
                    .map(HasCubeId::getId)
                    .filter(node::isAncestorOf)
                    .map(node.getDepth() == 1 ? (item -> item) : item -> item.getAncestor(node.getLevel() + 1))
                    .filter(Objects::nonNull)
                    .distinct();
        }
        return delegate.getChildren(node);
    }

    @Override
    public @NonNull Optional<CubeSeries> getSeries(@NonNull CubeId leaf) throws IOException {
        checkLeaf(leaf);
        List<CubeSeriesWithData> list = peekList(leaf);
        if (list != null) {
            return list
                    .stream()
                    .filter(onIdEqualTo(leaf))
                    .map(CubeSeriesWithData::withoutData)
                    .findFirst();
        }
        return delegate.getSeries(leaf);
    }

    @Override
    public @NonNull Stream<CubeSeries> getAllSeries(@NonNull CubeId node) throws IOException {
        checkNode(node);
        List<CubeSeriesWithData> list = peekList(node);
        if (list != null) {
            return list
                    .stream()
                    .filter(onIdDescendantOf(node))
                    .map(CubeSeriesWithData::withoutData);
        }
        return delegate.getAllSeries(node);
    }

    @Override
    public @NonNull Optional<CubeSeriesWithData> getSeriesWithData(@NonNull CubeId leaf) throws IOException {
        checkLeaf(leaf);
        try (var stream = getCloseableStream(leaf)) {
            return stream
                    .filter(onIdEqualTo(leaf))
                    .findFirst();
        }
    }

    @Override
    public @NonNull Stream<CubeSeriesWithData> getAllSeriesWithData(@NonNull CubeId node) throws IOException {
        checkNode(node);
        return getCloseableStream(node)
                .filter(onIdDescendantOf(node));
    }

    @Override
    public @NonNull Optional<IOException> testConnection() {
        return delegate.testConnection();
    }

    @Override
    public @NonNull CubeId getRoot() throws IOException {
        return delegate.getRoot();
    }

    @Override
    public @NonNull String getDisplayName() throws IOException {
        return delegate.getDisplayName();
    }

    @Override
    public @NonNull String getDisplayName(@NonNull CubeId id) throws IOException {
        return delegate.getDisplayName(id);
    }

    @Override
    public @NonNull String getDisplayNodeName(@NonNull CubeId id) throws IOException {
        return delegate.getDisplayNodeName(id);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    private @Nullable List<CubeSeriesWithData> peekList(@NonNull CubeId nodeOrLeaf) throws IOException {
        int cacheLevel = getCacheLevel();
        if (nodeOrLeaf.getLevel() == cacheLevel) {
            return cache.get(nodeOrLeaf);
        } else {
            CubeId ancestor = nodeOrLeaf.getAncestor(cacheLevel);
            if (ancestor != null) {
                return cache.get(ancestor);
            }
        }
        return null;
    }

    private @NonNull Stream<CubeSeriesWithData> getCloseableStream(@NonNull CubeId nodeOrLeaf) throws IOException {
        int cacheLevel = getCacheLevel();
        if (nodeOrLeaf.getLevel() == cacheLevel) {
            return getOrLoad(nodeOrLeaf);
        } else {
            CubeId ancestor = nodeOrLeaf.getAncestor(cacheLevel);
            if (ancestor != null) {
                return getOrLoad(ancestor);
            }
        }
        return load(nodeOrLeaf);
    }

    private @NonNull Stream<CubeSeriesWithData> load(@NonNull CubeId nodeOrLeaf) throws IOException {
        return nodeOrLeaf.isSeries()
                ? delegate.getSeriesWithData(nodeOrLeaf).stream()
                : delegate.getAllSeriesWithData(nodeOrLeaf);
    }

    private @NonNull Stream<CubeSeriesWithData> getOrLoad(@NonNull CubeId nodeOrLeaf) throws IOException {
        List<CubeSeriesWithData> result = cache.get(nodeOrLeaf);
        if (result != null) {
            return result.stream();
        }
        Stream<CubeSeriesWithData> closeableStream = load(nodeOrLeaf);
        IOIterator<CubeSeriesWithData> iterator = IOIterator.checked(closeableStream.iterator());
        return new CachingIterator(nodeOrLeaf, cache, iterator, closeableStream::close).asStream();
    }

    private static Predicate<HasCubeId> onIdEqualTo(CubeId leaf) {
        return ts -> leaf.equals(ts.getId());
    }

    private static Predicate<HasCubeId> onIdDescendantOf(CubeId node) {
        return ts -> node.isAncestorOf(ts.getId());
    }

    @lombok.RequiredArgsConstructor
    private static final class CachingIterator implements IOIterator<CubeSeriesWithData>, Closeable {

        private final CubeId key;
        private final ShortLivedCache<CubeId, List<CubeSeriesWithData>> cache;
        private final IOIterator<CubeSeriesWithData> delegate;
        private final Closeable closeable;

        private final List<CubeSeriesWithData> items = new ArrayList<>();

        @Override
        public boolean hasNextWithIO() throws IOException {
            return delegate.hasNextWithIO();
        }

        @Override
        public CubeSeriesWithData nextWithIO() throws IOException, NoSuchElementException {
            CubeSeriesWithData result = delegate.nextWithIO();
            items.add(result);
            return result;
        }

        @Override
        public @lombok.NonNull Stream<CubeSeriesWithData> asStream() {
            return IOIterator.super.asStream().onClose(IORunnable.unchecked(this::close));
        }

        @Override
        public void close() throws IOException {
            Resource.closeBoth(this::flushToCache, closeable);
        }

        private void flushToCache() throws IOException {
            while (hasNextWithIO()) {
                nextWithIO();
            }
            cache.put(key, items);
        }
    }
}
