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

import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.core.diagnostics.SpectralDiagnostics;
import jdplus.sa.desktop.plugin.diagnostics.SpectralDiagnosticsBuddy;
import jdplus.tramoseats.desktop.plugin.tramoseats.diagnostics.TramoSeatsDiagnosticsFactoryBuddy;
import jdplus.sa.base.core.diagnostics.SpectralDiagnosticsConfiguration;
import jdplus.sa.base.core.diagnostics.SpectralDiagnosticsFactory;
import jdplus.toolkit.base.api.modelling.ComponentInformation;
import jdplus.tramoseats.base.core.seats.SeatsResults;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsResults;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(service = TramoSeatsDiagnosticsFactoryBuddy.class, position = 1210)
public final class TramoSeatsSpectralDiagnosticsBuddy extends SpectralDiagnosticsBuddy implements TramoSeatsDiagnosticsFactoryBuddy<SpectralDiagnosticsConfiguration> {

    public TramoSeatsSpectralDiagnosticsBuddy(){
        this.setActiveDiagnosticsConfiguration(SpectralDiagnosticsConfiguration.getDefault());
    }
    
    @Override
    public SpectralDiagnosticsFactory<TramoSeatsResults> createFactory() {
         SpectralDiagnosticsFactory<TramoSeatsResults> spectral
                = new SpectralDiagnosticsFactory<>(SpectralDiagnosticsConfiguration.getDefault(),
                        (TramoSeatsResults r) -> {
            SeatsResults sd = r.getDecomposition();
            if (sd == null)
                return null;
            return new SpectralDiagnostics.Input(r.getDecomposition().getFinalComponents().getMode(), 
                    sd.getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.Value),
                    sd.getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
                        });
         return spectral;
   }

}
