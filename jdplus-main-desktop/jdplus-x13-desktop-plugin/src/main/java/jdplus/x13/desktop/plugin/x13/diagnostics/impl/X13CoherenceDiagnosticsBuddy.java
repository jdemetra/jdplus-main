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

import jdplus.sa.desktop.plugin.diagnostics.CoherenceDiagnosticsBuddy;
import jdplus.x13.desktop.plugin.x13.diagnostics.X13DiagnosticsFactoryBuddy;
import jdplus.sa.base.core.diagnostics.CoherenceDiagnostics;
import jdplus.sa.base.core.diagnostics.CoherenceDiagnosticsConfiguration;
import jdplus.sa.base.core.diagnostics.CoherenceDiagnosticsFactory;
import jdplus.x13.base.core.x13.X13Results;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(service = X13DiagnosticsFactoryBuddy.class, position = 1000)
public final class X13CoherenceDiagnosticsBuddy extends CoherenceDiagnosticsBuddy implements X13DiagnosticsFactoryBuddy<CoherenceDiagnosticsConfiguration> {

    public X13CoherenceDiagnosticsBuddy() {
        this.setActiveDiagnosticsConfiguration(CoherenceDiagnosticsConfiguration.getDefault());
    }

    @Override
    public CoherenceDiagnosticsFactory<X13Results> createFactory() {
        return new CoherenceDiagnosticsFactory<>(this.getActiveDiagnosticsConfiguration(),
                (X13Results r) -> {
                    return r.getDecomposition() == null ? null : new CoherenceDiagnostics.Input(r.getDecomposition().getMode(), r);
                });
    }

}
