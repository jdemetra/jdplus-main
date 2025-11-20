/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.toolkit.base.api.timeseries;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author PALATEJ
 */
public final class DynamicTsDataSupplier extends TsDataSupplier {

    private final TsMoniker moniker;
    private final AtomicReference<TsData> cache;

    public DynamicTsDataSupplier(@NonNull TsMoniker moniker) {
        this.moniker = moniker;
        this.cache = new AtomicReference<>(null);
    }

    public DynamicTsDataSupplier(@NonNull TsMoniker moniker, @Nullable TsData current) {
        this.moniker = moniker;
        this.cache = new AtomicReference<>(current);
    }

    public @NonNull TsMoniker getMoniker() {
        return moniker;
    }

    @Override
    public @NonNull TsData get() {
        TsData result = cache.get();
        if (result == null) {
            result = load();
            cache.set(result);
        }
        return result;
    }

    private @NonNull TsData load() {
        // from the moniker.
        return TsFactory.getDefault().makeTs(moniker, TsInformationType.Data).getData();
    }

    public void refresh() {
        TsData newData = load();
        if (!newData.isEmpty() || cache.get() == null) {
            cache.set(newData);
        }
    }
}
