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
package jdplus.sa.base.core.diagnostics;

import jdplus.toolkit.base.api.processing.DiagnosticsConfiguration;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class CombinedSeasonalityDiagnosticsConfiguration implements DiagnosticsConfiguration {

    private static final AtomicReference<CombinedSeasonalityDiagnosticsConfiguration> DEFAULT
            = new AtomicReference<>(builder().build());

    public static void setDefault(CombinedSeasonalityDiagnosticsConfiguration config) {
        DEFAULT.set(config);
    }

    public static CombinedSeasonalityDiagnosticsConfiguration getDefault() {
        return DEFAULT.get();
    }

    public static final boolean ACTIVE = true;
    private boolean active;
    
    private boolean onSa, onI, onLastSa, onLastI, strict;
 

    public static Builder builder() {
        return new Builder()
                .active(ACTIVE)
                .onI(true)
                .onLastI(true)
                .onSa(true)
                .onLastSa(true)
                .strict(true);
    }

    @Override
    public DiagnosticsConfiguration activate(boolean active) {
        if (this.active == active) {
            return this;
        } else {
            return toBuilder().active(active).build();
        }
    }

}
