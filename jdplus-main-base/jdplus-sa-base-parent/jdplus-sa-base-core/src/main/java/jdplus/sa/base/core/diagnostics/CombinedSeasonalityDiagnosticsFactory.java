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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import jdplus.toolkit.base.api.information.Explorable;
import jdplus.toolkit.base.api.processing.Diagnostics;
import jdplus.sa.base.api.SaDiagnosticsFactory;
import lombok.NonNull;

/**
 *
 * @author Kristof Bayens
 * @param <R>
 */
public class CombinedSeasonalityDiagnosticsFactory<R extends Explorable> implements SaDiagnosticsFactory<CombinedSeasonalityDiagnosticsConfiguration, R> {

    public static final String NAME = "Combined seasonality tests",
            SA = NAME + " on sa", SA_LAST = NAME + " on sa (last years)", IRR = NAME + " on irregular", IRR_LAST = NAME + " on irregular (last years)";
    public static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(SA, SA_LAST, IRR, IRR_LAST));

    private final CombinedSeasonalityDiagnosticsConfiguration config;
    private final Function<R, GenericSaTests> extractor;

    public CombinedSeasonalityDiagnosticsFactory(@NonNull CombinedSeasonalityDiagnosticsConfiguration config,
            @NonNull Function<R, GenericSaTests> extractor) {
        this.config = config;
        this.extractor=extractor;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<String> getTestDictionary() {
        return ALL.stream().map(s -> s + ":2").collect(Collectors.toList());
    }

    @Override
    public Diagnostics of(R rslts) {
        return CombinedSeasonalityDiagnostics.of(config, extractor.apply(rslts));
    }

    @Override
    public CombinedSeasonalityDiagnosticsConfiguration getConfiguration() {
        return config;
    }

    @Override
    public CombinedSeasonalityDiagnosticsFactory with(CombinedSeasonalityDiagnosticsConfiguration newConfig) {
        return new CombinedSeasonalityDiagnosticsFactory(newConfig, extractor);
    }

     @Override
    public Scope getScope() {
        return Scope.Decomposition;
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
