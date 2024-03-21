/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved
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
package jdplus.toolkit.base.tsp;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.Collections.emptySortedMap;
import static java.util.Collections.unmodifiableSortedMap;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class DataSetTest {

    final DataSource id = DataSourceTest.newSample();
    final String k1 = "domain", v1 = "NB01";
    final SortedMap<String, String> content = unmodifiableSortedMap(new TreeMap<>(Map.of(k1, v1)));
    final SortedMap<String, String> emptyContent = emptySortedMap();

    DataSet newSample() {
        return new DataSet(id, DataSet.Kind.DUMMY, content);
    }

    @Test
    public void testConstructor() {
        assertThat(newSample()).satisfies(o -> {
            assertThat(o.getDataSource()).isEqualTo(id);
            assertThat(o.getKind()).isEqualTo(DataSet.Kind.DUMMY);
            assertThat(o.getParameters()).containsAllEntriesOf(content);
            content.forEach((k, v) -> assertThat(o.getParameter(k)).isEqualTo(v));
        });
    }

    @Test
    public void testEquals() {
        assertThat(newSample())
                .isEqualTo(newSample())
                .isNotEqualTo(new DataSet(id, DataSet.Kind.SERIES, content))
                .isNotEqualTo(new DataSet(id, DataSet.Kind.DUMMY, emptyContent));
    }

    @Test
    public void testHashCode() {
        assertThat(newSample().hashCode())
                .isEqualTo(newSample().hashCode())
                .isNotEqualTo(new DataSet(id, DataSet.Kind.DUMMY, emptyContent).hashCode());
    }

    @Test
    public void testGet() {
        assertThat(newSample()).satisfies(o -> {
            assertThat(o.getParameter(k1)).isEqualTo(v1);
            assertThat(o.getParameter("hello")).isNull();
        });
    }

    @Test
    public void testGetParams() {
        assertThat(newSample().getParameters()).containsAllEntriesOf(content);
    }

    @Test
    public void testRepresentableAsString() {
        DataSource dataSource = DataSource
                .builder("+ ABC", "12 3+")
                .parameter("k 1+", "v 1+")
                .build();

        DataSet dataSet = DataSet
                .builder(dataSource, DataSet.Kind.COLLECTION)
                .parameter("k 2+", "v 2+")
                .build();

        String legacy = "demetra://tsprovider/%2B+ABC/12+3%2B/COLLECTION?k+1%2B=v+1%2B#k+2%2B=v+2%2B";
        String expected = "demetra://tsprovider/%2B%20ABC/12%203%2B/COLLECTION?k%201%2B=v%201%2B#k%202%2B=v%202%2B";

        assertThat(dataSet)
                .hasToString(expected)
                .isEqualTo(DataSet.parse(legacy))
                .isEqualTo(DataSet.parse(expected));
    }

    @Test
    public void testRepresentableAsURI() {
        DataSource dataSource = DataSource
                .builder("+ ABC", "12 3+")
                .parameter("k 1+", "v 1+")
                .build();

        DataSet dataSet = DataSet
                .builder(dataSource, DataSet.Kind.COLLECTION)
                .parameter("k 2+", "v 2+")
                .build();

        URI legacy = URI.create("demetra://tsprovider/%2B+ABC/12+3%2B/COLLECTION?k+1%2B=v+1%2B#k+2%2B=v+2%2B");
        URI expected = URI.create("demetra://tsprovider/%2B%20ABC/12%203%2B/COLLECTION?k%201%2B=v%201%2B#k%202%2B=v%202%2B");

        assertThat(dataSet)
                .returns(expected, DataSet::toURI)
                .isEqualTo(DataSet.parseURI(legacy))
                .isEqualTo(DataSet.parseURI(expected));
    }
}
