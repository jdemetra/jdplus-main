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

import jdplus.sa.desktop.plugin.diagnostics.AdvancedResidualSeasonalityDiagnosticsBuddy;
import jdplus.tramoseats.desktop.plugin.tramoseats.diagnostics.TramoSeatsDiagnosticsFactoryBuddy;
import jdplus.sa.base.core.diagnostics.AdvancedResidualSeasonalityDiagnosticsConfiguration;
import jdplus.sa.base.core.diagnostics.AdvancedResidualSeasonalityDiagnosticsFactory;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsResults;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(service = TramoSeatsDiagnosticsFactoryBuddy.class, position = 1200)
public final class TramoSeatsAdvancedResidualSeasonalityDiagnosticsBuddy extends AdvancedResidualSeasonalityDiagnosticsBuddy implements TramoSeatsDiagnosticsFactoryBuddy<AdvancedResidualSeasonalityDiagnosticsConfiguration> {

    public TramoSeatsAdvancedResidualSeasonalityDiagnosticsBuddy() {
        setActiveDiagnosticsConfiguration(AdvancedResidualSeasonalityDiagnosticsConfiguration.getDefault());
    }
    
//    @lombok.experimental.Delegate
//    private final AdvancedResidualSeasonalityDiagnosticsBuddy delegate = new AdvancedResidualSeasonalityDiagnosticsBuddy();
//
    @Override
    public AdvancedResidualSeasonalityDiagnosticsFactory<TramoSeatsResults> createFactory() {
        return new AdvancedResidualSeasonalityDiagnosticsFactory<>(getActiveDiagnosticsConfiguration(),
                (TramoSeatsResults r) -> r.getDiagnostics() == null ? null : r.getDiagnostics().getGenericDiagnostics()
        );
    }

}
