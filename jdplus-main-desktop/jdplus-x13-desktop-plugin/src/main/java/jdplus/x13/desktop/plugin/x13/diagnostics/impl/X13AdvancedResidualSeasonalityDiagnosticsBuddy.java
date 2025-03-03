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
package jdplus.x13.desktop.plugin.x13.diagnostics.impl;

import jdplus.sa.desktop.plugin.diagnostics.AdvancedResidualSeasonalityDiagnosticsBuddy;
import jdplus.x13.desktop.plugin.x13.diagnostics.X13DiagnosticsFactoryBuddy;
import jdplus.sa.base.core.diagnostics.AdvancedResidualSeasonalityDiagnosticsConfiguration;
import jdplus.sa.base.core.diagnostics.AdvancedResidualSeasonalityDiagnosticsFactory;
import jdplus.x13.base.core.x13.X13Results;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(service = X13DiagnosticsFactoryBuddy.class, position = 1200)
public final class X13AdvancedResidualSeasonalityDiagnosticsBuddy extends AdvancedResidualSeasonalityDiagnosticsBuddy
        implements X13DiagnosticsFactoryBuddy<AdvancedResidualSeasonalityDiagnosticsConfiguration> {

    public X13AdvancedResidualSeasonalityDiagnosticsBuddy() {
        this.setActiveDiagnosticsConfiguration(AdvancedResidualSeasonalityDiagnosticsConfiguration.getDefault());
    }

    @Override
    public AdvancedResidualSeasonalityDiagnosticsFactory<X13Results> createFactory() {
        return new AdvancedResidualSeasonalityDiagnosticsFactory<>(this.getActiveDiagnosticsConfiguration(),
                (X13Results r) -> r.getDiagnostics() == null ? null : r.getDiagnostics().getGenericDiagnostics()
        );
    }

}
