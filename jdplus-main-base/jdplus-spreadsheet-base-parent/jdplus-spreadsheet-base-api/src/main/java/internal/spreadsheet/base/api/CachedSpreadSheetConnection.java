/*
 * Copyright 2020 National Bank of Belgium
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

import jdplus.toolkit.base.api.timeseries.TsCollection;
import jdplus.toolkit.base.tsp.util.ShortLivedCache;
import jdplus.toolkit.base.tsp.util.ShortLivedCaching;
import lombok.AccessLevel;
import nbbrd.design.StaticFactoryMethod;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class CachedSpreadSheetConnection implements SpreadSheetConnection {

    @StaticFactoryMethod
    public static @NonNull CachedSpreadSheetConnection of(@NonNull SpreadSheetConnection delegate, @NonNull File target, @NonNull ShortLivedCaching cacheFactory) {
        return new CachedSpreadSheetConnection(delegate, cacheFactory.ofFile(target));
    }

    @lombok.NonNull
    private final SpreadSheetConnection delegate;

    @lombok.NonNull
    private final ShortLivedCache<String, List<TsCollection>> cache;

    @Override
    public @lombok.NonNull Optional<TsCollection> getSheetByName(@lombok.NonNull String name) throws IOException {
        List<TsCollection> all = peekAll();
        if (all != null) {
            return all.stream()
                    .filter(collection -> collection.getName().equals(name))
                    .findFirst();
        }

        String key = "getSheetByName/" + name;
        List<TsCollection> cachedValue = cache.get(key);
        if (cachedValue == null) {
            Optional<TsCollection> result = delegate.getSheetByName(name);
            cache.put(key, result.stream().toList());
            return result;
        } else {
            return cachedValue.stream().findFirst();
        }
    }

    @Override
    public @lombok.NonNull List<String> getSheetNames() throws IOException {
        List<TsCollection> all = peekAll();
        if (all != null) {
            return all.stream()
                    .map(TsCollection::getName)
                    .toList();
        }

        String key = "getSheetNames";
        List<TsCollection> cachedValue = cache.get(key);
        if (cachedValue == null) {
            List<String> result = delegate.getSheetNames();
            cache.put(key, result.stream().map(TsCollection::ofName).toList());
            return result;
        } else {
            return cachedValue.stream().map(TsCollection::getName).toList();
        }
    }

    @Override
    public @lombok.NonNull List<TsCollection> getSheets() throws IOException {
        String key = "getSheets";
        List<TsCollection> cachedValue = cache.get(key);
        if (cachedValue == null) {
            cachedValue = delegate.getSheets();
            cache.put(key, cachedValue);
        }
        return cachedValue;
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    private List<TsCollection> peekAll() {
        return cache.get("getSheets");
    }
}
