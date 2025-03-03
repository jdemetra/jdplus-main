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

import java.util.Arrays;
import jdplus.sa.base.core.diagnostics.ResidualTradingDaysDiagnostics;
import jdplus.sa.desktop.plugin.diagnostics.ResidualTradingDaysDiagnosticsBuddy;
import jdplus.x13.desktop.plugin.x13.diagnostics.X13DiagnosticsFactoryBuddy;
import jdplus.sa.base.core.diagnostics.ResidualTradingDaysDiagnosticsConfiguration;
import jdplus.sa.base.core.diagnostics.ResidualTradingDaysDiagnosticsFactory;
import jdplus.toolkit.base.api.timeseries.regression.ITradingDaysVariable;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.x13.base.core.x13.X13Results;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(service = X13DiagnosticsFactoryBuddy.class, position = 1230)
public final class X13ResidualTradingDaysDiagnosticsBuddy extends ResidualTradingDaysDiagnosticsBuddy implements X13DiagnosticsFactoryBuddy<ResidualTradingDaysDiagnosticsConfiguration> {

    public X13ResidualTradingDaysDiagnosticsBuddy() {
        this.setActiveDiagnosticsConfiguration(ResidualTradingDaysDiagnosticsConfiguration.getDefault());
    }

    @Override
    public ResidualTradingDaysDiagnosticsFactory<X13Results> createFactory() {
        return new ResidualTradingDaysDiagnosticsFactory<>(this.getActiveDiagnosticsConfiguration(),
                (X13Results r) -> {
                    RegSarimaModel preprocessing = r.getPreprocessing();
                    boolean td = false;
                    if (preprocessing != null) {
                        td = Arrays.stream(preprocessing.getDescription().getVariables()).anyMatch(v -> v.getCore() instanceof ITradingDaysVariable);
                    } 
                    return new ResidualTradingDaysDiagnostics.Input(r.getDiagnostics() == null ? null : r.getDiagnostics().getGenericDiagnostics().residualTradingDaysTests(), td);
                });
    }
}
