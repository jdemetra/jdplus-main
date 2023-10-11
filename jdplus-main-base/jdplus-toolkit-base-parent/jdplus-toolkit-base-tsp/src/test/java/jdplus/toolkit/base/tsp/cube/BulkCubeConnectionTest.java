/*
 * Copyright 2017 National Bank of Belgium
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

import internal.toolkit.base.tsp.util.SimpleMapCache;
import _test.tsproviders.XCubeConnection;
import internal.toolkit.base.tsp.util.MapCaching;
import jdplus.toolkit.base.tsp.fixme.ResourceWatcher;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static jdplus.toolkit.base.tsp.cube.CubeIdTest.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Philippe Charles
 */
public class BulkCubeConnectionTest {

    private static CubeConnection newSample() {
        return new XCubeConnection(DIM2_LEV0, new ResourceWatcher());
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testBulkApi() {
        var connection = BulkCubeConnection.of(newSample(), BulkCube.NONE, new MapCaching());
        assertThatThrownBy(() -> connection.getAllSeriesWithData(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> connection.getSeriesWithData(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testBulkDepth() throws IOException {
        var cache = newMapCache();

        try (var connection = new BulkCubeConnection(newSample(), 0, clear(cache))) {
            connection.getSeriesWithData(DIM2_LEV2);
            assertThat(cache.getMap()).isEmpty();
        }

        try (var connection = new BulkCubeConnection(newSample(), 0, clear(cache))) {
            connection.getAllSeriesWithData(DIM2_LEV1).close();
            assertThat(cache.getMap()).isEmpty();
        }

        try (var connection = new BulkCubeConnection(newSample(), 0, clear(cache))) {
            connection.getAllSeriesWithData(DIM2_LEV0).close();
            assertThat(cache.getMap()).isEmpty();
        }

        try (var connection = new BulkCubeConnection(newSample(), 1, clear(cache))) {
            connection.getSeriesWithData(DIM2_LEV2);
            assertThat(cache.getMap()).isNotEmpty();
        }

        try (var connection = new BulkCubeConnection(newSample(), 1, clear(cache))) {
            connection.getAllSeriesWithData(DIM2_LEV1).close();
            assertThat(cache.getMap()).isNotEmpty();
        }

        try (var connection = new BulkCubeConnection(newSample(), 1, clear(cache))) {
            connection.getAllSeriesWithData(DIM2_LEV0).close();
            assertThat(cache.getMap()).isEmpty();
        }

        try (var connection = new BulkCubeConnection(newSample(), 2, clear(cache))) {
            connection.getSeriesWithData(DIM2_LEV2);
            assertThat(cache.getMap()).isNotEmpty();
        }

        try (var connection = new BulkCubeConnection(newSample(), 2, clear(cache))) {
            connection.getAllSeriesWithData(DIM2_LEV1).close();
            assertThat(cache.getMap()).isNotEmpty();
        }

        try (var connection = new BulkCubeConnection(newSample(), 2, clear(cache))) {
            connection.getAllSeriesWithData(DIM2_LEV0).close();
            assertThat(cache.getMap()).isNotEmpty();
        }
    }

    @Test
    public void testResourceLeak() throws IOException {
        ResourceWatcher watcher = new ResourceWatcher();
        var cache = newMapCache();
        var connection = new BulkCubeConnection(new XCubeConnection(DIM2_LEV0, watcher), 1, cache);
        connection.getSeriesWithData(DIM2_LEV2);
        assertThat(cache.getMap()).isNotEmpty();
        assertThat(watcher.isLeaking()).isFalse();
    }

    private static SimpleMapCache<CubeId, List<CubeSeriesWithData>> newMapCache() {
        return new SimpleMapCache<>(new HashMap<>());
    }

    private static <K, V> SimpleMapCache<K, V> clear(SimpleMapCache<K, V> cache) {
        cache.getMap().clear();
        return cache;
    }
}
