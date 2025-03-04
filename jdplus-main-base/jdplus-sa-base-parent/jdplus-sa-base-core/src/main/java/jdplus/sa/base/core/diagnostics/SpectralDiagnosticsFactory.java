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

import jdplus.toolkit.base.api.information.Explorable;
import jdplus.toolkit.base.api.processing.Diagnostics;
import jdplus.sa.base.api.SaDiagnosticsFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import lombok.NonNull;

/**
 *
 * @param <R>
 */
public class SpectralDiagnosticsFactory<R extends Explorable> implements SaDiagnosticsFactory<SpectralDiagnosticsConfiguration, R> {

    public static final String SEAS = "spectral seas peaks", TD = "spectral td peaks";
    public static final String NAME = "Visual spectral analysis";
    public static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(SEAS, TD));
    //public static final SpectralDiagnosticsFactory Default = new SpectralDiagnosticsFactory();
    private final SpectralDiagnosticsConfiguration config;

    private final Function<R, SpectralDiagnostics.Input> extractor;

    public SpectralDiagnosticsFactory(@NonNull SpectralDiagnosticsConfiguration config,
            @NonNull final Function<R, SpectralDiagnostics.Input> extractor) {
        this.config = config;
        this.extractor = extractor;
    }

    @Override
    public SpectralDiagnosticsConfiguration getConfiguration() {
        return config;
    }

    @Override
    public SpectralDiagnosticsFactory<R> with(SpectralDiagnosticsConfiguration config) {
        return new SpectralDiagnosticsFactory(config, extractor);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<String> getTestDictionary() {
        return ALL;
    }

    @Override
    public Diagnostics of(R rslts) {
        return SpectralDiagnostics.of(config, extractor.apply(rslts));
    }

    @Override
    public Scope getScope() {
        return Scope.Preliminary;
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
