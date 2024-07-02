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

import _test.tsproviders.WatchingCubeConnection;
import internal.toolkit.base.tsp.cube.CubeRepository;
import internal.toolkit.base.tsp.util.MapCache;
import internal.toolkit.base.tsp.util.MapCaching;
import internal.toolkit.base.tsp.util.SimpleMapCache;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.tsp.fixme.ResourceWatcher;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyMap;
import static jdplus.toolkit.base.api.timeseries.TsPeriod.monthly;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Philippe Charles
 */
public class BulkCubeConnectionTest {

    private static final CubeId ROOT_ID = CubeId.root("sector", "region");
    private static final CubeId INDUSTRY_ID = ROOT_ID.child("industry");
    private static final CubeId INDUSTRY_BE_ID = INDUSTRY_ID.child("be");
    private static final CubeId INDUSTRY_EU_ID = INDUSTRY_ID.child("eu");
    private static final CubeId OTHER_ID = ROOT_ID.child("other");
    private static final CubeId OTHER_BE_ID = OTHER_ID.child("be");
    private static final CubeId OTHER_EU_ID = OTHER_ID.child("eu");

    private static final CubeSeriesWithData INDUSTRY_BE = new CubeSeriesWithData(INDUSTRY_BE_ID, null, emptyMap(), TsData.of(monthly(2012, 1), DoubleSeq.of(1.2, 2.3)));
    private static final CubeSeriesWithData INDUSTRY_EU = new CubeSeriesWithData(INDUSTRY_EU_ID, null, emptyMap(), TsData.of(monthly(2012, 1), DoubleSeq.of(3.4, 4.5)));
    private static final CubeSeriesWithData OTHER_BE = new CubeSeriesWithData(OTHER_BE_ID, null, emptyMap(), TsData.of(monthly(2012, 1), DoubleSeq.of(5.6, 6.7)));
    private static final CubeSeriesWithData OTHER_EU = new CubeSeriesWithData(OTHER_EU_ID, null, emptyMap(), TsData.of(monthly(2012, 1), DoubleSeq.of(7.8, 8.9)));

    private static final CubeRepository DIM2 = CubeRepository
            .builder()
            .name("sample")
            .root(ROOT_ID)
            .item(INDUSTRY_BE)
            .item(INDUSTRY_EU)
            .item(OTHER_BE)
            .item(OTHER_EU)
            .build();

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testBulkApi() {
        var connection = BulkCubeConnection.of(DIM2.asConnection(), BulkCube.NONE, new MapCaching());
        assertThatThrownBy(() -> connection.getAllSeriesWithData(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> connection.getSeriesWithData(null)).isInstanceOf(NullPointerException.class);
    }

    @lombok.RequiredArgsConstructor
    private static class Context {

        final ResourceWatcher watcher;
        final MapCache<CubeId, List<CubeSeriesWithData>> cache;
        int callCount = 0;

        BulkCubeConnection newConnection(CubeRepository repo, int depth) {
            return new BulkCubeConnection(WatchingCubeConnection.of(repo.asConnection(), watcher), depth, cache);
        }

        void clearCache() {
            cache.getMap().clear();
        }

        Set<CubeId> getKeySet() {
            return cache.getMap().keySet();
        }

        int getCallCount() {
            int result = watcher.getCount() - callCount;
            callCount = watcher.getCount();
            return result;
        }
    }

    @Test
    public void testBulkDepth() throws IOException {
        var x = new Context(new ResourceWatcher(), new SimpleMapCache<>(new HashMap<>()));

        try (var conn = x.newConnection(DIM2, 0)) {
            x.clearCache();
            assertThat(INDUSTRY_BE_ID).satisfies(id -> {
                assertThatIllegalArgumentException()
                        .describedAs("illegal call to Children() on level 2")
                        .isThrownBy(() -> conn.getChildren(id));
                assertThat(x).has(emptyCache()).has(noCallToDelegate());

                assertThatIllegalArgumentException()
                        .describedAs("illegal call to AllSeries() on level 2")
                        .isThrownBy(() -> conn.getAllSeries(id));
                assertThat(x).has(emptyCache()).has(noCallToDelegate());

                assertThatIllegalArgumentException()
                        .describedAs("illegal call to AllSeriesWithData() on level 2")
                        .isThrownBy(() -> conn.getAllSeriesWithData(id));
                assertThat(x).has(emptyCache()).has(noCallToDelegate());

                assertThat(conn.getSeries(id))
                        .describedAs("first call to Series() on level 2")
                        .contains(INDUSTRY_BE.withoutData());
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());

                assertThat(conn.getSeriesWithData(id))
                        .describedAs("first call to SeriesWithData() on level 2")
                        .contains(INDUSTRY_BE);
                assertThat(x).has(cacheContaining(INDUSTRY_BE_ID)).has(oneCallToDelegate());

                assertThat(conn.getSeries(id))
                        .describedAs("second call to Series() on level 2")
                        .contains(INDUSTRY_BE.withoutData());
                assertThat(x).has(cacheContaining(INDUSTRY_BE_ID)).has(noCallToDelegate());

                assertThat(conn.getSeriesWithData(id))
                        .describedAs("second call to SeriesWithData() on level 2")
                        .contains(INDUSTRY_BE);
                assertThat(x).has(cacheContaining(INDUSTRY_BE_ID)).has(noCallToDelegate());
            });

            x.clearCache();
            assertThat(INDUSTRY_ID).satisfies(id -> {
                assertThatIllegalArgumentException()
                        .describedAs("illegal call to Series() on level 1")
                        .isThrownBy(() -> conn.getSeries(id));
                assertThat(x).has(emptyCache()).has(noCallToDelegate());

                assertThatIllegalArgumentException()
                        .describedAs("illegal call to SeriesWithData() on level 1")
                        .isThrownBy(() -> conn.getSeriesWithData(id));
                assertThat(x).has(emptyCache()).has(noCallToDelegate());

                try (var stream = conn.getChildren(id)) {
                    assertThat(stream)
                            .describedAs("first call to Children() on level 1")
                            .containsExactly(INDUSTRY_BE_ID, INDUSTRY_EU_ID);
                }
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());

                try (var stream = conn.getAllSeries(id)) {
                    assertThat(stream)
                            .describedAs("first call to AllSeries() on level 1")
                            .containsExactly(INDUSTRY_BE.withoutData(), INDUSTRY_EU.withoutData());
                }
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());

                try (var stream = conn.getAllSeriesWithData(id)) {
                    assertThat(stream)
                            .describedAs("first call to AllSeriesWithData() on level 1")
                            .containsExactly(INDUSTRY_BE, INDUSTRY_EU);
                }
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());

                try (var stream = conn.getChildren(id)) {
                    assertThat(stream)
                            .describedAs("second call to Children() on level 1")
                            .containsExactly(INDUSTRY_BE_ID, INDUSTRY_EU_ID);
                }
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());

                try (var stream = conn.getAllSeries(id)) {
                    assertThat(stream)
                            .describedAs("second call to AllSeries() on level 1")
                            .containsExactly(INDUSTRY_BE.withoutData(), INDUSTRY_EU.withoutData());
                }
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());

                try (var stream = conn.getAllSeriesWithData(id)) {
                    assertThat(stream)
                            .describedAs("second call to AllSeriesWithData() on level 1")
                            .containsExactly(INDUSTRY_BE, INDUSTRY_EU);
                }
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());
            });

            x.clearCache();
            assertThat(ROOT_ID).satisfies(id -> {
                assertThatIllegalArgumentException()
                        .describedAs("illegal call to Series() on level 0")
                        .isThrownBy(() -> conn.getSeries(id));
                assertThat(x).has(emptyCache()).has(noCallToDelegate());

                assertThatIllegalArgumentException()
                        .describedAs("illegal call to Series() on level 0")
                        .isThrownBy(() -> conn.getSeriesWithData(id));
                assertThat(x).has(emptyCache()).has(noCallToDelegate());

                try (var stream = conn.getChildren(id)) {
                    assertThat(stream)
                            .describedAs("first call to Children() on level 0")
                            .containsExactly(INDUSTRY_ID, OTHER_ID);
                }
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());

                try (var stream = conn.getAllSeries(id)) {
                    assertThat(stream)
                            .describedAs("first call to AllSeries() on level 0")
                            .containsExactly(INDUSTRY_BE.withoutData(), INDUSTRY_EU.withoutData(), OTHER_BE.withoutData(), OTHER_EU.withoutData());
                }
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());

                try (var stream = conn.getAllSeriesWithData(id)) {
                    assertThat(stream)
                            .describedAs("first call to AllSeriesWithData() on level 0")
                            .containsExactly(INDUSTRY_BE, INDUSTRY_EU, OTHER_BE, OTHER_EU);
                }
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());

                try (var stream = conn.getChildren(id)) {
                    assertThat(stream)
                            .describedAs("second call to Children() on level 0")
                            .containsExactly(INDUSTRY_ID, OTHER_ID);
                }
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());

                try (var stream = conn.getAllSeries(id)) {
                    assertThat(stream)
                            .describedAs("second call to AllSeries() on level 0")
                            .containsExactly(INDUSTRY_BE.withoutData(), INDUSTRY_EU.withoutData(), OTHER_BE.withoutData(), OTHER_EU.withoutData());
                }
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());

                try (var stream = conn.getAllSeriesWithData(id)) {
                    assertThat(stream)
                            .describedAs("second call to AllSeriesWithData() on level 0")
                            .containsExactly(INDUSTRY_BE, INDUSTRY_EU, OTHER_BE, OTHER_EU);
                }
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());
            });
        }

        try (var conn = x.newConnection(DIM2, 1)) {
            x.clearCache();
            assertThat(INDUSTRY_BE_ID).satisfies(id -> {
                assertThatIllegalArgumentException()
                        .describedAs("illegal call to Children() on level 2")
                        .isThrownBy(() -> conn.getChildren(id));
                assertThat(x).has(emptyCache()).has(noCallToDelegate());

                assertThatIllegalArgumentException()
                        .describedAs("illegal call to AllSeries() on level 2")
                        .isThrownBy(() -> conn.getAllSeries(id));
                assertThat(x).has(emptyCache()).has(noCallToDelegate());

                assertThatIllegalArgumentException()
                        .describedAs("illegal call to AllSeriesWithData() on level 2")
                        .isThrownBy(() -> conn.getAllSeriesWithData(id));
                assertThat(x).has(emptyCache()).has(noCallToDelegate());

                assertThat(conn.getSeries(id))
                        .describedAs("first call to Series() on level 2")
                        .contains(INDUSTRY_BE.withoutData());
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());

                assertThat(conn.getSeriesWithData(id))
                        .describedAs("first call to SeriesWithData() on level 2")
                        .contains(INDUSTRY_BE);
                assertThat(x).has(cacheContaining(INDUSTRY_ID)).has(oneCallToDelegate());

                assertThat(conn.getSeries(id))
                        .describedAs("second call to Series() on level 2")
                        .contains(INDUSTRY_BE.withoutData());
                assertThat(x).has(cacheContaining(INDUSTRY_ID)).has(noCallToDelegate());

                assertThat(conn.getSeriesWithData(id))
                        .describedAs("second call to SeriesWithData() on level 2")
                        .contains(INDUSTRY_BE);
                assertThat(x).has(cacheContaining(INDUSTRY_ID)).has(noCallToDelegate());
            });

            x.clearCache();
            assertThat(INDUSTRY_ID).satisfies(id -> {
                assertThatIllegalArgumentException()
                        .describedAs("illegal call to Series() on level 1")
                        .isThrownBy(() -> conn.getSeries(id));
                assertThat(x).has(emptyCache()).has(noCallToDelegate());

                assertThatIllegalArgumentException()
                        .describedAs("illegal call to SeriesWithData() on level 1")
                        .isThrownBy(() -> conn.getSeriesWithData(id));
                assertThat(x).has(emptyCache()).has(noCallToDelegate());

                try (var stream = conn.getChildren(id)) {
                    assertThat(stream)
                            .describedAs("first call to Children() on level 1")
                            .containsExactly(INDUSTRY_BE_ID, INDUSTRY_EU_ID);
                }
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());

                try (var stream = conn.getAllSeries(id)) {
                    assertThat(stream)
                            .describedAs("first call to AllSeries() on level 1")
                            .containsExactly(INDUSTRY_BE.withoutData(), INDUSTRY_EU.withoutData());
                }
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());

                try (var stream = conn.getAllSeriesWithData(id)) {
                    assertThat(stream)
                            .describedAs("first call to AllSeriesWithData() on level 1")
                            .containsExactly(INDUSTRY_BE, INDUSTRY_EU);
                }
                assertThat(x).has(cacheContaining(INDUSTRY_ID)).has(oneCallToDelegate());

                try (var stream = conn.getChildren(id)) {
                    assertThat(stream)
                            .describedAs("second call to Children() on level 1")
                            .containsExactly(INDUSTRY_BE_ID, INDUSTRY_EU_ID);
                }
                assertThat(x).has(cacheContaining(INDUSTRY_ID)).has(noCallToDelegate());

                try (var stream = conn.getAllSeries(id)) {
                    assertThat(stream)
                            .describedAs("second call to AllSeries() on level 1")
                            .containsExactly(INDUSTRY_BE.withoutData(), INDUSTRY_EU.withoutData());
                }
                assertThat(x).has(cacheContaining(INDUSTRY_ID)).has(noCallToDelegate());

                try (var stream = conn.getAllSeriesWithData(id)) {
                    assertThat(stream)
                            .describedAs("second call to AllSeriesWithData() on level 1")
                            .containsExactly(INDUSTRY_BE, INDUSTRY_EU);
                }
                assertThat(x).has(cacheContaining(INDUSTRY_ID)).has(noCallToDelegate());
            });

            x.clearCache();
            assertThat(ROOT_ID).satisfies(id -> {
                assertThatIllegalArgumentException()
                        .describedAs("illegal call to Series() on level 0")
                        .isThrownBy(() -> conn.getSeries(id));
                assertThat(x).has(emptyCache()).has(noCallToDelegate());

                assertThatIllegalArgumentException()
                        .describedAs("illegal call to SeriesWithData() on level 0")
                        .isThrownBy(() -> conn.getSeriesWithData(id));
                assertThat(x).has(emptyCache()).has(noCallToDelegate());

                try (var stream = conn.getChildren(id)) {
                    assertThat(stream)
                            .describedAs("first call to Children() on level 0")
                            .containsExactly(INDUSTRY_ID, OTHER_ID);
                }
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());

                try (var stream = conn.getAllSeries(id)) {
                    assertThat(stream)
                            .describedAs("first call to AllSeries() on level 0")
                            .containsExactly(INDUSTRY_BE.withoutData(), INDUSTRY_EU.withoutData(), OTHER_BE.withoutData(), OTHER_EU.withoutData());
                }
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());

                try (var stream = conn.getAllSeriesWithData(id)) {
                    assertThat(stream)
                            .describedAs("first call to AllSeriesWithData() on level 0")
                            .containsExactly(INDUSTRY_BE, INDUSTRY_EU, OTHER_BE, OTHER_EU);
                }
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());

                try (var stream = conn.getChildren(id)) {
                    assertThat(stream)
                            .describedAs("second call to Children() on level 0")
                            .containsExactly(INDUSTRY_ID, OTHER_ID);
                }
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());

                try (var stream = conn.getAllSeries(id)) {
                    assertThat(stream)
                            .describedAs("second call to AllSeries() on level 0")
                            .containsExactly(INDUSTRY_BE.withoutData(), INDUSTRY_EU.withoutData(), OTHER_BE.withoutData(), OTHER_EU.withoutData());
                }
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());

                try (var stream = conn.getAllSeriesWithData(id)) {
                    assertThat(stream)
                            .describedAs("second call to AllSeriesWithData() on level 0")
                            .containsExactly(INDUSTRY_BE, INDUSTRY_EU, OTHER_BE, OTHER_EU);
                }
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());
            });
        }

        try (var conn = x.newConnection(DIM2, 2)) {
            x.clearCache();
            assertThat(INDUSTRY_BE_ID).satisfies(id -> {
                assertThatIllegalArgumentException()
                        .describedAs("illegal call to Children() on level 2")
                        .isThrownBy(() -> conn.getChildren(id));
                assertThat(x).has(emptyCache()).has(noCallToDelegate());

                assertThatIllegalArgumentException()
                        .describedAs("illegal call to AllSeries() on level 2")
                        .isThrownBy(() -> conn.getAllSeries(id));
                assertThat(x).has(emptyCache()).has(noCallToDelegate());

                assertThatIllegalArgumentException()
                        .describedAs("illegal call to AllSeriesWithData() on level 2")
                        .isThrownBy(() -> conn.getAllSeriesWithData(id));
                assertThat(x).has(emptyCache()).has(noCallToDelegate());

                assertThat(conn.getSeries(id))
                        .describedAs("first call to Series() on level 2")
                        .contains(INDUSTRY_BE.withoutData());
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());

                assertThat(conn.getSeriesWithData(id))
                        .describedAs("first call to SeriesWithData() on level 2")
                        .contains(INDUSTRY_BE);
                assertThat(x).has(cacheContaining(ROOT_ID)).has(oneCallToDelegate());

                assertThat(conn.getSeries(id))
                        .describedAs("second call to Series() on level 2")
                        .contains(INDUSTRY_BE.withoutData());
                assertThat(x).has(cacheContaining(ROOT_ID)).has(noCallToDelegate());

                assertThat(conn.getSeriesWithData(id))
                        .describedAs("second call to SeriesWithData() on level 2")
                        .contains(INDUSTRY_BE);
                assertThat(x).has(cacheContaining(ROOT_ID)).has(noCallToDelegate());
            });

            x.clearCache();
            assertThat(INDUSTRY_ID).satisfies(id -> {
                assertThatIllegalArgumentException()
                        .describedAs("illegal call to Series() on level 1")
                        .isThrownBy(() -> conn.getSeries(id));
                assertThat(x).has(emptyCache()).has(noCallToDelegate());

                assertThatIllegalArgumentException()
                        .describedAs("illegal call to SeriesWithData() on level 1")
                        .isThrownBy(() -> conn.getSeriesWithData(id));
                assertThat(x).has(emptyCache()).has(noCallToDelegate());

                try (var stream = conn.getChildren(id)) {
                    assertThat(stream)
                            .describedAs("first call to Children() on level 1")
                            .containsExactly(INDUSTRY_BE_ID, INDUSTRY_EU_ID);
                }
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());

                try (var stream = conn.getAllSeries(id)) {
                    assertThat(stream)
                            .describedAs("first call to AllSeries() on level 1")
                            .containsExactly(INDUSTRY_BE.withoutData(), INDUSTRY_EU.withoutData());
                }
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());

                try (var stream = conn.getAllSeriesWithData(id)) {
                    assertThat(stream)
                            .describedAs("first call to AllSeriesWithData() on level 1")
                            .containsExactly(INDUSTRY_BE, INDUSTRY_EU);
                }
                assertThat(x).has(cacheContaining(ROOT_ID)).has(oneCallToDelegate());

                try (var stream = conn.getChildren(id)) {
                    assertThat(stream)
                            .describedAs("second call to Children() on level 1")
                            .containsExactly(INDUSTRY_BE_ID, INDUSTRY_EU_ID);
                }
                assertThat(x).has(cacheContaining(ROOT_ID)).has(noCallToDelegate());

                try (var stream = conn.getAllSeries(id)) {
                    assertThat(stream)
                            .describedAs("second call to AllSeries() on level 1")
                            .containsExactly(INDUSTRY_BE.withoutData(), INDUSTRY_EU.withoutData());
                }
                assertThat(x).has(cacheContaining(ROOT_ID)).has(noCallToDelegate());

                try (var stream = conn.getAllSeriesWithData(id)) {
                    assertThat(stream)
                            .describedAs("second call to AllSeriesWithData() on level 1")
                            .containsExactly(INDUSTRY_BE, INDUSTRY_EU);
                }
                assertThat(x).has(cacheContaining(ROOT_ID)).has(noCallToDelegate());
            });

            x.clearCache();
            assertThat(ROOT_ID).satisfies(id -> {
                assertThatIllegalArgumentException()
                        .describedAs("illegal call to Series() on level 0")
                        .isThrownBy(() -> conn.getSeries(id));
                assertThat(x).has(emptyCache()).has(noCallToDelegate());

                assertThatIllegalArgumentException()
                        .describedAs("illegal call to SeriesWithData() on level 0")
                        .isThrownBy(() -> conn.getSeriesWithData(id));
                assertThat(x).has(emptyCache()).has(noCallToDelegate());

                try (var stream = conn.getChildren(id)) {
                    assertThat(stream)
                            .describedAs("first call to Children() on level 0")
                            .containsExactly(INDUSTRY_ID, OTHER_ID);
                }
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());

                try (var stream = conn.getAllSeries(id)) {
                    assertThat(stream)
                            .describedAs("first call to AllSeries() on level 0")
                            .containsExactly(INDUSTRY_BE.withoutData(), INDUSTRY_EU.withoutData(), OTHER_BE.withoutData(), OTHER_EU.withoutData());
                }
                assertThat(x).has(emptyCache()).has(oneCallToDelegate());

                try (var stream = conn.getAllSeriesWithData(id)) {
                    assertThat(stream)
                            .describedAs("first call to AllSeriesWithData() on level 0")
                            .containsExactly(INDUSTRY_BE, INDUSTRY_EU, OTHER_BE, OTHER_EU);
                }
                assertThat(x).has(cacheContaining(ROOT_ID)).has(oneCallToDelegate());

                try (var stream = conn.getChildren(id)) {
                    assertThat(stream)
                            .describedAs("second call to Children() on level 0")
                            .containsExactly(INDUSTRY_ID, OTHER_ID);
                }
                assertThat(x).has(cacheContaining(ROOT_ID)).has(noCallToDelegate());

                try (var stream = conn.getAllSeries(id)) {
                    assertThat(stream)
                            .describedAs("second call to AllSeries() on level 0")
                            .containsExactly(INDUSTRY_BE.withoutData(), INDUSTRY_EU.withoutData(), OTHER_BE.withoutData(), OTHER_EU.withoutData());
                }
                assertThat(x).has(cacheContaining(ROOT_ID)).has(noCallToDelegate());

                try (var stream = conn.getAllSeriesWithData(id)) {
                    assertThat(stream)
                            .describedAs("second call to AllSeriesWithData() on level 0")
                            .containsExactly(INDUSTRY_BE, INDUSTRY_EU, OTHER_BE, OTHER_EU);
                }
                assertThat(x).has(cacheContaining(ROOT_ID)).has(noCallToDelegate());
            });
        }

        assertThat(x.watcher.isLeaking()).isFalse();
    }

    private static Condition<Context> emptyCache() {
        return new Condition<>(o -> o.getKeySet().isEmpty(), "cache empty");
    }

    private static Condition<Context> cacheContaining(CubeId id) {
        return new Condition<>(o -> o.getKeySet().size() == 1 && o.getKeySet().contains(id), "cache containing " + id);
    }

    private static Condition<Context> noCallToDelegate() {
        return new Condition<>(o -> o.getCallCount() == 0, "no call to delegate");
    }

    private static Condition<Context> oneCallToDelegate() {
        return new Condition<>(o -> o.getCallCount() == 1, "one call to delegate");
    }
}
