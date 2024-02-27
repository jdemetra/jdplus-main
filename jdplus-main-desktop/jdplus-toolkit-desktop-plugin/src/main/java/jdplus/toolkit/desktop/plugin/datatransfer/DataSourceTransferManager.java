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
package jdplus.toolkit.desktop.plugin.datatransfer;

import internal.uihelpers.FixmeCollectionSupplier;
import jdplus.main.desktop.design.GlobalService;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.desktop.plugin.util.CollectionSupplier;
import jdplus.toolkit.desktop.plugin.util.LazyGlobalService;
import lombok.NonNull;

import java.awt.datatransfer.Transferable;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A support class that deals with DataSource in Transferable.
 *
 * @author Philippe Charles
 */
@GlobalService
public final class DataSourceTransferManager {

    @NonNull
    public static DataSourceTransferManager get() {
        return LazyGlobalService.get(DataSourceTransferManager.class, DataSourceTransferManager::new);
    }

    private DataSourceTransferManager() {
    }

    private final CollectionSupplier<DataSourceTransferSpi> providers = FixmeCollectionSupplier.of(DataSourceTransferSpi.class, DataSourceTransferSpiLoader::load);

    @NonNull
    public Stream<? extends DataSourceTransferSpi> all() {
        return providers.stream();
    }

    public boolean canHandle(@NonNull Transferable t) {
        return all().anyMatch(o -> o.canHandle(t));
    }

    public boolean canHandle(@NonNull Transferable t, @NonNull String providerName) {
        return all().anyMatch(o -> o.canHandle(t, providerName));
    }

    @NonNull
    public Optional<DataSource> getDataSource(@NonNull Transferable t) {
        return all()
                .filter(o -> o.canHandle(t))
                .map(o -> o.getDataSource(t))
                .filter(Optional::isPresent)
                .map(Optional::orElseThrow)
                .findFirst();
    }

    @NonNull
    public Optional<DataSource> getDataSource(@NonNull Transferable t, @NonNull String providerName) {
        return all()
                .filter(o -> o.canHandle(t, providerName))
                .map(o -> o.getDataSource(t, providerName))
                .filter(Optional::isPresent)
                .map(Optional::orElseThrow)
                .findFirst();
    }
}
