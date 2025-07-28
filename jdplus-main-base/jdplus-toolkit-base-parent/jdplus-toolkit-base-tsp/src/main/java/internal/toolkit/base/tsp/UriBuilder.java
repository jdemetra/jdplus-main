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

import jdplus.toolkit.base.tsp.fixme.Strings;
import lombok.NonNull;
import nbbrd.design.BuilderPattern;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * http://en.wikipedia.org/wiki/URI_scheme
 * http://msdn.microsoft.com/en-us/library/aa767914%28v=vs.85%29.aspx
 *
 * @author Philippe Charles
 */
@BuilderPattern(URI.class)
public final class UriBuilder {

    // PROPERTIES
    private final String scheme;
    private final String host;
    private String[] path;
    private SortedMap<String, String> query;
    private SortedMap<String, String> fragment;

    public UriBuilder(@NonNull String scheme, @NonNull String host) {
        if (scheme.isEmpty()) {
            throw new IllegalArgumentException("scheme can't be empty");
        }
        if (host.isEmpty()) {
            throw new IllegalArgumentException("host can't be empty");
        }
        this.scheme = scheme;
        this.host = host;
        reset();
    }

    @NonNull
    public UriBuilder reset() {
        this.path = null;
        this.query = null;
        this.fragment = null;
        return this;
    }

    @NonNull
    public UriBuilder path(@Nullable String... path) {
        this.path = path;
        return this;
    }

    @NonNull
    public UriBuilder query(@Nullable SortedMap<String, String> query) {
        this.query = query;
        return this;
    }

    @NonNull
    public UriBuilder fragment(@Nullable SortedMap<String, String> fragment) {
        this.fragment = fragment;
        return this;
    }

    @NonNull
    public String buildString() {
        StringBuilder result = new StringBuilder();
        result.append(scheme);
        result.append("://");
        result.append(host);
        if (path != null) {
            appendPathElements(result.append('/'), path);
        }
        if (query != null) {
            appendKeyValuePairs(result.append('?'), query);
        }
        if (fragment != null) {
            appendKeyValuePairs(result.append('#'), fragment);
        }
        return result.toString();
    }

    public URI build() {
        return URI.create(buildString());
    }

    private static String encode(String o) {
        return URLEncoder.encode(o, UTF_8).replace("+", "%20");
    }

    private static String decode(String o) {
        return URLDecoder.decode(o, UTF_8);
    }

    private static void appendKeyValuePair(@NonNull StringBuilder sb, @NonNull Entry<String, String> o) {
        sb.append(encode(o.getKey()));
        sb.append('=');
        sb.append(encode(o.getValue()));
    }

    private static void appendKeyValuePairs(@NonNull StringBuilder sb, @NonNull Map<String, String> keyValues) {
        if (!keyValues.isEmpty()) {
            Iterator<Entry<String, String>> iterator = keyValues.entrySet().iterator();
            appendKeyValuePair(sb, iterator.next());
            while (iterator.hasNext()) {
                appendKeyValuePair(sb.append('&'), iterator.next());
            }
        }
    }

    private static void appendPathElements(@NonNull StringBuilder sb, @NonNull String[] array) {
        if (array.length > 0) {
            int i = 0;
            sb.append(encode(array[i]));
            while (++i < array.length) {
                sb.append('/').append(encode(array[i]));
            }
        }
    }

    @Nullable
    public static String[] getPathArray(@NonNull URI uri, int expectedSize) {
        String path = uri.getRawPath();
        return path != null && !path.isEmpty() ? splitToArray(path.subSequence(1, path.length()), expectedSize) : null;
    }

    @Nullable
    public static Map<String, String> getQueryMap(@NonNull URI uri) {
        String query = uri.getRawQuery();
        return query != null ? splitMap(query) : null;
    }

    @Nullable
    public static Map<String, String> getFragmentMap(@NonNull URI uri) {
        String fragment = uri.getRawFragment();
        return fragment != null ? splitMap(fragment) : null;
    }

    @Nullable
    private static String[] splitToArray(@NonNull CharSequence input, int expectedSize) {
        Iterator<String> items = Strings.splitToIterator('/', input);
        if (expectedSize == 0 || !items.hasNext()) {
            return null;
        }
        String[] result = new String[expectedSize];
        int index = 0;
        do {
            result[index++] = decode(items.next());
        } while (index < expectedSize && items.hasNext());
        return !items.hasNext() && index == expectedSize ? result : null;
    }

    @Nullable
    private static Map<String, String> splitMap(@NonNull CharSequence input) {
        if (input.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        return splitMapTo(input, result::put) ? result : null;
    }

    private static boolean splitMapTo(@NonNull CharSequence input, @NonNull BiConsumer<String, String> consumer) {
        Iterator<String> iterable = Strings.splitToIterator('&', input);
        while (iterable.hasNext()) {
            String entry = iterable.next();
            Iterator<String> entryFields = Strings.splitToIterator('=', entry);
            if (!entryFields.hasNext()) {
                return false;
            }
            String key = entryFields.next();
            if (!entryFields.hasNext()) {
                return false;
            }
            String value = entryFields.next();
            if (entryFields.hasNext()) {
                return false;
            }
            consumer.accept(decode(key), decode(value));
        }
        return true;
    }
}
