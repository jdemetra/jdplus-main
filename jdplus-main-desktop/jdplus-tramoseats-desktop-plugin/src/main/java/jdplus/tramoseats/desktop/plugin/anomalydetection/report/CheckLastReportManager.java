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
package jdplus.tramoseats.desktop.plugin.anomalydetection.report;

import jdplus.main.desktop.design.GlobalService;
import jdplus.toolkit.desktop.plugin.util.CollectionSupplier;
import jdplus.toolkit.desktop.plugin.util.FixmeCollectionSupplier;
import jdplus.toolkit.desktop.plugin.util.LazyGlobalService;
import nbbrd.design.MightBeGenerated;

import java.util.ArrayList;
import java.util.List;

import static jdplus.toolkit.desktop.plugin.util.NetBeansServiceBackend.*;

/**
 * @author Mats Maggi
 */
@GlobalService
public final class CheckLastReportManager {

    public static CheckLastReportManager get() {
        return LazyGlobalService.get(CheckLastReportManager.class, CheckLastReportManager::new);
    }

    private final CollectionSupplier<CheckLastReportFactory> factories;

    private CheckLastReportManager() {
        factories = FixmeCollectionSupplier.of(CheckLastReportFactory.class, buildServiceLoader()::get);
    }

    @MightBeGenerated
    private static CheckLastReportFactoryLoader buildServiceLoader() {
        return CheckLastReportFactoryLoader.builder().backend(lookupFactory(), lookupStreamer(), lookupReloader()).build();
    }

    public List<CheckLastReportFactory> getFactories() {
        return new ArrayList<>(factories.get());
    }
}
