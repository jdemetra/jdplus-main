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
import nbbrd.io.IOIterator;
import nbbrd.io.Resource;
import nbbrd.io.function.IOFunction;
import nbbrd.io.function.IORunnable;
import org.checkerframework.checker.index.qual.NonNegative;
import lombok.NonNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

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
    public @NonNull Stream<CubeSeriesWithData> getAllSeriesWithData(@NonNull CubeId ref) throws IOException {
        if (!ref.isSeries()) {
            int cacheLevel = getCacheLevel();
            if (ref.getLevel() == cacheLevel) {
                return getOrLoad(cache, ref, delegate::getAllSeriesWithData);
            } else {
                CubeId ancestor = ref.getAncestor(cacheLevel);
                if (ancestor != null) {
                    return getAllSeriesWithData(ancestor).filter(ts -> ref.isAncestorOf(ts.getId()));
                }
            }
        }
        return delegate.getAllSeriesWithData(ref);
    }

    @Override
    public @NonNull Optional<CubeSeriesWithData> getSeriesWithData(@NonNull CubeId ref) throws IOException {
        if (ref.isSeries()) {
            int cacheLevel = getCacheLevel();
            CubeId ancestor = ref.getAncestor(cacheLevel);
            if (ancestor != null) {
                try (Stream<CubeSeriesWithData> stream = getAllSeriesWithData(ancestor)) {
                    return stream.filter(ts -> ref.equals(ts.getId())).findFirst();
                }
            }
        }
        return delegate.getSeriesWithData(ref);
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
    public @NonNull Stream<CubeSeries> getAllSeries(@NonNull CubeId id) throws IOException {
        return delegate.getAllSeries(id);
    }

    @Override
    public @NonNull Optional<CubeSeries> getSeries(@NonNull CubeId id) throws IOException {
        return delegate.getSeries(id);
    }

    @Override
    public @NonNull Stream<CubeId> getChildren(@NonNull CubeId id) throws IOException {
        return delegate.getChildren(id);
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

    @NonNull
    private static Stream<CubeSeriesWithData> getOrLoad(
            @NonNull ShortLivedCache<CubeId, List<CubeSeriesWithData>> cache,
            @NonNull CubeId key,
            @NonNull IOFunction<CubeId, Stream<CubeSeriesWithData>> loader) throws IOException {

        requireNonNull(cache, "cache");
        requireNonNull(key, "key");
        requireNonNull(loader, "loader");

        List<CubeSeriesWithData> result = cache.get(key);
        if (result != null) {
            return result.stream();
        }
        Stream<CubeSeriesWithData> delegate = loader.applyWithIO(key);
        IOIterator<CubeSeriesWithData> iterator = IOIterator.checked(delegate.iterator());
        return new CachingIterator(key, cache, iterator, delegate::close).asStream();
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
