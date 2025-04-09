/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.tramoseats.desktop.plugin.tramoseats.diagnostics.impl;

import jdplus.sa.desktop.plugin.diagnostics.CoherenceDiagnosticsBuddy;
import jdplus.tramoseats.desktop.plugin.tramoseats.diagnostics.TramoSeatsDiagnosticsFactoryBuddy;
import jdplus.sa.base.core.diagnostics.CoherenceDiagnostics;
import jdplus.sa.base.core.diagnostics.CoherenceDiagnosticsConfiguration;
import jdplus.sa.base.core.diagnostics.CoherenceDiagnosticsFactory;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsResults;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(service = TramoSeatsDiagnosticsFactoryBuddy.class, position = 1000)
public final class TramoSeatsCoherenceDiagnosticsBuddy extends CoherenceDiagnosticsBuddy implements TramoSeatsDiagnosticsFactoryBuddy<CoherenceDiagnosticsConfiguration> {

    public TramoSeatsCoherenceDiagnosticsBuddy() {
        setActiveDiagnosticsConfiguration(CoherenceDiagnosticsConfiguration.getDefault());
    }

    @Override
    public CoherenceDiagnosticsFactory<TramoSeatsResults> createFactory() {
        return new CoherenceDiagnosticsFactory<>(getActiveDiagnosticsConfiguration(),
                (TramoSeatsResults r) -> {
                    if (r.getFinals() == null) {
                        return null;
                    } else {
                        return new CoherenceDiagnostics.Input(r.getFinals().getMode(), r);
                    }
                });
    }

}
