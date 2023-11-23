/*
 * Copyright 2016 National Bank of Belgium
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
package _test.tsproviders;

import jdplus.toolkit.base.tsp.fixme.ResourceWatcher;
import jdplus.toolkit.base.tsp.cube.CubeConnection;
import jdplus.toolkit.base.tsp.cube.CubeId;
import jdplus.toolkit.base.tsp.cube.CubeSeries;
import jdplus.toolkit.base.tsp.cube.CubeSeriesWithData;
import nbbrd.io.function.IORunnable;
import lombok.NonNull;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
public final class XCubeConnection implements CubeConnection {

    private final CubeId root;
    private final ResourceWatcher resourceWatcher;

    public XCubeConnection(CubeId root, ResourceWatcher resourceWatcher) {
        this.root = Objects.requireNonNull(root);
        this.resourceWatcher = Objects.requireNonNull(resourceWatcher);
    }

    @Override
    public @NonNull Optional<IOException> testConnection() {
        return Optional.empty();
    }

    @Override
    public @NonNull CubeId getRoot() {
        return root;
    }

    @Override
    public @NonNull Stream<CubeSeries> getAllSeries(@NonNull CubeId id) throws IOException {
        Objects.requireNonNull(id);
        return Stream.<CubeSeries>empty().onClose(IORunnable.unchecked(resourceWatcher.watchAsCloseable("getAllSeries")::close));
    }

    @Override
    public @NonNull Stream<CubeSeriesWithData> getAllSeriesWithData(@NonNull CubeId id) throws IOException {
        Objects.requireNonNull(id);
        return Stream.<CubeSeriesWithData>empty().onClose(IORunnable.unchecked(resourceWatcher.watchAsCloseable("getAllSeriesWithData")::close));
    }

    @Override
    public @NonNull Optional<CubeSeries> getSeries(@NonNull CubeId id) throws IOException {
        Objects.requireNonNull(id);
        resourceWatcher.watchAsCloseable("getSeries").close();
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<CubeSeriesWithData> getSeriesWithData(@NonNull CubeId id) throws IOException {
        Objects.requireNonNull(id);
        resourceWatcher.watchAsCloseable("getSeriesWithData").close();
        return Optional.empty();
    }

    @Override
    public @NonNull Stream<CubeId> getChildren(@NonNull CubeId id) throws IOException {
        Objects.requireNonNull(id);
        return Stream.<CubeId>empty().onClose(IORunnable.unchecked(resourceWatcher.watchAsCloseable("getChildren")::close));
    }

    @Override
    public @NonNull String getDisplayName() throws IOException {
        return root.toString();
    }

    @Override
    public @NonNull String getDisplayName(@NonNull CubeId id) throws IOException {
        return id.toString();
    }

    @Override
    public @NonNull String getDisplayNodeName(@NonNull CubeId id) throws IOException {
        return id.toString();
    }

    @Override
    public void close() throws IOException {
    }
}
