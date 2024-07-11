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

import jdplus.toolkit.base.tsp.cube.CubeConnection;
import jdplus.toolkit.base.tsp.cube.CubeId;
import jdplus.toolkit.base.tsp.cube.CubeSeries;
import jdplus.toolkit.base.tsp.cube.CubeSeriesWithData;
import jdplus.toolkit.base.tsp.fixme.ResourceWatcher;
import lombok.NonNull;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import static nbbrd.io.function.IORunnable.unchecked;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "of")
public final class WatchingCubeConnection implements CubeConnection {

    private final @NonNull CubeConnection delegate;
    private final @NonNull ResourceWatcher resourceWatcher;

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
        return delegate.getAllSeries(id)
                .onClose(unchecked(resourceWatcher.watchAsCloseable("getAllSeries")::close));
    }

    @Override
    public @NonNull Stream<CubeSeriesWithData> getAllSeriesWithData(@NonNull CubeId id) throws IOException {
        return delegate.getAllSeriesWithData(id)
                .onClose(unchecked(resourceWatcher.watchAsCloseable("getAllSeriesWithData")::close));
    }

    @Override
    public @NonNull Optional<CubeSeries> getSeries(@NonNull CubeId id) throws IOException {
        resourceWatcher.watchAsCloseable("getSeries").close();
        return delegate.getSeries(id);
    }

    @Override
    public @NonNull Optional<CubeSeriesWithData> getSeriesWithData(@NonNull CubeId id) throws IOException {
        resourceWatcher.watchAsCloseable("getSeriesWithData").close();
        return delegate.getSeriesWithData(id);
    }

    @Override
    public @NonNull Stream<CubeId> getChildren(@NonNull CubeId id) throws IOException {
        return delegate.getChildren(id)
                .onClose(unchecked(resourceWatcher.watchAsCloseable("getChildren")::close));
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
}
