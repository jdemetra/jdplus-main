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
package internal.toolkit.base.tsp;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.*;


/**
 * @author Philippe Charles
 */
public class UriBuilderTest {

    final String scheme = "http";
    final String host = "www.nbb.be";
    final SortedMap<String, String> query = Collections.unmodifiableSortedMap(new TreeMap<>(
            Map.of(
                    "email", "contact@nbb.be",
                    "*", "[1.2, 2.6]", "file",
                    "C:\\Program Files\\data.xls"
            )));
    final String[] path = {"dq/rd", "demetra+"};
    //
    final String rawScheme = "http";
    final String rawHost = "www.nbb.be";
    final String rawPath = "/dq%2Frd/demetra%2B";
    final String rawQuery = "*=%5B1.2%2C%202.6%5D&email=contact%40nbb.be&file=C%3A%5CProgram%20Files%5Cdata.xls";

    @Test
    public void testBuildString() {
        UriBuilder builder = new UriBuilder(scheme, host).path(path).query(query);
        assertThat(builder.buildString())
                .isEqualTo(rawScheme + "://" + rawHost + rawPath + "?" + rawQuery);
    }

    @Test
    public void testUriVsString() {
        UriBuilder builder = new UriBuilder(scheme, host).path(path).query(query);
        assertThat(builder.build().toString())
                .isEqualTo(builder.buildString());
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testScheme() {
        assertThat(new UriBuilder(scheme, host).path(path).query(query).build().getScheme()).isEqualTo(scheme);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new UriBuilder("", host).build());

        assertThatNullPointerException()
                .isThrownBy(() -> new UriBuilder(null, host).build());
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testHost() {
        assertThat(new UriBuilder(scheme, host).path(path).query(query).build().getHost()).isEqualTo(host);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new UriBuilder(scheme, "").build());

        assertThatNullPointerException()
                .isThrownBy(() -> new UriBuilder(scheme, null).build());
    }

    @Test
    public void testPath() {
        // FIXME: what to do with empty or null ?
//        assertEquals("", new UriBuilder(scheme, host).path("").build().getRawPath());
//        assertEquals("", new UriBuilder(scheme, host).path((String)null).build().getRawPath());
//        assertEquals("", new UriBuilder(scheme, host).path((String[])null).build().getRawPath());

        assertThat(new UriBuilder(scheme, host).build().getRawPath()).isEmpty();
        assertThat(new UriBuilder(scheme, host).path(path).build().getRawPath()).isEqualTo(rawPath);
    }

    @Test
    public void testQuery() {
        URI uri = new UriBuilder(scheme, host).path(path).query(query).build();
        assertThat(uri.getRawQuery()).isEqualTo(rawQuery);
        Map<String, String> tmp = UriBuilder.getQueryMap(uri);
        assertThat(tmp).isNotNull();
        assertThat(tmp.size()).isEqualTo(query.size());
        for (Entry<String, String> o : tmp.entrySet()) {
            assertThat(query.containsKey(o.getKey())).isTrue();
            assertThat(query.get(o.getKey())).isEqualTo(o.getValue());
        }
    }

    @Test
    public void testGetPathArray() {
        assertThat(new UriBuilder(scheme, host).build()).satisfies(o -> {
            assertThat(UriBuilder.getPathArray(o, 0)).isNull();
            assertThat(UriBuilder.getPathArray(o, 1)).isNull();
        });

        assertThat(new UriBuilder(scheme, host).path(path).build()).satisfies(o -> {
            assertThat(UriBuilder.getPathArray(o, 0)).isNull();
            assertThat(UriBuilder.getPathArray(o, 1)).isNull();
            assertThat(UriBuilder.getPathArray(o, 2)).containsExactly(path);
            assertThat(UriBuilder.getPathArray(o, 3)).isNull();
        });
    }
}
