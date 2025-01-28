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
package internal.spreadsheet.base.api;

import _test.DataForTest;
import internal.toolkit.base.tsp.util.SimpleMapCache;
import internal.spreadsheet.base.api.grid.SheetGrid;
import jdplus.toolkit.base.api.timeseries.TsCollection;
import jdplus.toolkit.base.tsp.grid.GridReader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class SpreadSheetConnectionTest {

    @Test
    public void testWithCache() throws IOException {
        SheetGrid grid = SheetGrid.of(Path.of("").toFile(), DataForTest.FACTORY, GridReader.DEFAULT);
        SimpleMapCache<String, List<TsCollection>> cache = new SimpleMapCache<>(new HashMap<>());
        try (var conn = new CachedSpreadSheetConnection(grid, cache)) {

            cache.getMap().clear();
            assertThat(conn.getSheetByName("s1")).map(TsCollection::getName).contains("s1");
            assertThat(cache.getMap()).containsKeys("getSheetByName/s1");

            cache.getMap().clear();
            assertThat(conn.getSheetByName("other")).isEmpty();
            assertThat(cache.getMap()).containsKeys("getSheetByName/other");

            cache.getMap().clear();
            assertThat(conn.getSheetNames()).containsExactly("s1", "s2");
            assertThat(cache.getMap()).containsKeys("getSheetNames");

            cache.getMap().clear();
            assertThat(conn.getSheets()).extracting(TsCollection::getName).containsExactly("s1", "s2");
            assertThat(cache.getMap()).containsKeys("getSheets");

            cache.getMap().clear();
            assertThat(conn.getSheets()).extracting(TsCollection::getName).containsExactly("s1", "s2");
            assertThat(conn.getSheetByName("s1")).map(TsCollection::getName).contains("s1");
            assertThat(conn.getSheetNames()).containsExactly("s1", "s2");
            assertThat(cache.getMap()).containsKeys("getSheets");
        }
    }
}
