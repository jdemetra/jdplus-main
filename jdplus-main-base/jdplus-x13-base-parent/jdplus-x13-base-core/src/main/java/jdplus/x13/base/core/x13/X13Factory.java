/*
 * Copyright 2025 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.x13.base.core.x13;

import jdplus.toolkit.base.api.modelling.TransformationType;
import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;
import jdplus.x13.base.api.regarima.RegArimaSpec;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.sa.base.api.EstimationPolicyType;
import jdplus.sa.base.api.SaDiagnosticsFactory;
import jdplus.sa.base.api.SaManager;
import jdplus.sa.base.api.SaProcessor;
import jdplus.sa.base.api.SaSpecification;
import jdplus.x13.base.core.x11.X11Results;
import jdplus.x13.base.api.x11.X11Spec;
import jdplus.x13.base.api.x13.X13Spec;
import nbbrd.service.ServiceProvider;
import jdplus.sa.base.api.SaProcessingFactory;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import jdplus.x13.base.api.x13.X13Dictionaries;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import jdplus.sa.base.api.ComponentType;
import jdplus.toolkit.base.core.regarima.diagnostics.OutOfSampleDiagnosticsConfiguration;
import jdplus.toolkit.base.core.regarima.diagnostics.OutliersDiagnosticsConfiguration;
import jdplus.toolkit.base.core.regarima.diagnostics.ResidualsDiagnosticsConfiguration;
import jdplus.sa.base.core.diagnostics.AdvancedResidualSeasonalityDiagnosticsConfiguration;
import jdplus.sa.base.core.diagnostics.AdvancedResidualSeasonalityDiagnosticsFactory;
import jdplus.sa.base.core.diagnostics.CoherenceDiagnostics;
import jdplus.sa.base.core.diagnostics.CoherenceDiagnosticsConfiguration;
import jdplus.sa.base.core.diagnostics.CoherenceDiagnosticsFactory;
import jdplus.sa.base.core.diagnostics.CombinedSeasonalityDiagnosticsConfiguration;
import jdplus.sa.base.core.diagnostics.CombinedSeasonalityDiagnosticsFactory;
import jdplus.sa.base.core.diagnostics.ResidualTradingDaysDiagnostics;
import jdplus.sa.base.core.diagnostics.ResidualTradingDaysDiagnosticsConfiguration;
import jdplus.sa.base.core.diagnostics.ResidualTradingDaysDiagnosticsFactory;
import jdplus.sa.base.core.diagnostics.SaOutOfSampleDiagnosticsFactory;
import jdplus.sa.base.core.diagnostics.SaOutliersDiagnosticsFactory;
import jdplus.sa.base.core.diagnostics.SaResidualsDiagnosticsFactory;
import jdplus.sa.base.core.diagnostics.SpectralDiagnostics;
import jdplus.sa.base.core.diagnostics.SpectralDiagnosticsConfiguration;
import jdplus.sa.base.core.diagnostics.SpectralDiagnosticsFactory;
import jdplus.toolkit.base.api.timeseries.regression.ITradingDaysVariable;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.x13.base.core.x13.diagnostics.MDiagnosticsConfiguration;
import jdplus.x13.base.core.x13.diagnostics.MDiagnosticsFactory;
import jdplus.x13.base.core.x13.regarima.RegArimaFactory;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(SaProcessingFactory.class)
public class X13Factory implements SaProcessingFactory<X13Spec, X13Results> {

    public static X13Factory getInstance() {
        return (X13Factory) SaManager.processors().stream().filter(x -> x instanceof X13Factory).findAny().orElse(new X13Factory());
    }

    private final List<SaDiagnosticsFactory<?, X13Results>> diagnostics = new CopyOnWriteArrayList<>();

    public static List<SaDiagnosticsFactory<?, X13Results>> defaultDiagnostics() {
        CoherenceDiagnosticsFactory<X13Results> coherence
                = new CoherenceDiagnosticsFactory<>(CoherenceDiagnosticsConfiguration.getDefault(),
                        (X13Results r) -> {
                            return r.getDecomposition() == null ? null : new CoherenceDiagnostics.Input(r.getDecomposition().getMode(), r);
                        }
                );

        SaOutOfSampleDiagnosticsFactory<X13Results> outofsample
                = new SaOutOfSampleDiagnosticsFactory<>(OutOfSampleDiagnosticsConfiguration.getDefault(),
                        r -> r.getDiagnostics() == null ? null : r.getDiagnostics().getGenericDiagnostics().forecastingTest());
        SaResidualsDiagnosticsFactory<X13Results> residuals
                = new SaResidualsDiagnosticsFactory<>(ResidualsDiagnosticsConfiguration.getDefault(),
                        r -> r.getPreprocessing());
        SaOutliersDiagnosticsFactory<X13Results> outliers
                = new SaOutliersDiagnosticsFactory<>(OutliersDiagnosticsConfiguration.getDefault(),
                        r -> r.getPreprocessing());
        SpectralDiagnosticsFactory<X13Results> spectral
                = new SpectralDiagnosticsFactory<>(SpectralDiagnosticsConfiguration.getDefault(),
                        (X13Results r) -> {
                            X11Results x11 = r.getDecomposition();
                            if (x11 == null) {
                                return null;
                            }
                            return new SpectralDiagnostics.Input(x11.getMode(),
                                    x11.getB1(),
                                    x11.getD11());
                        });
        MDiagnosticsFactory mstats = new MDiagnosticsFactory(MDiagnosticsConfiguration.getDefault());
        AdvancedResidualSeasonalityDiagnosticsFactory<X13Results> advancedResidualSeasonality
                = new AdvancedResidualSeasonalityDiagnosticsFactory<>(AdvancedResidualSeasonalityDiagnosticsConfiguration.getDefault(),
                        (X13Results r) -> r.getDiagnostics() == null ? null : r.getDiagnostics().getGenericDiagnostics()
                );
        CombinedSeasonalityDiagnosticsFactory<X13Results> combinedSeasonality
                = new CombinedSeasonalityDiagnosticsFactory<>(CombinedSeasonalityDiagnosticsConfiguration.getDefault(),
                        (X13Results r) -> r.getDiagnostics() == null ? null : r.getDiagnostics().getGenericDiagnostics()
                );

        ResidualTradingDaysDiagnosticsFactory<X13Results> residualTradingDays
                = new ResidualTradingDaysDiagnosticsFactory<>(ResidualTradingDaysDiagnosticsConfiguration.getDefault(),
                        (X13Results r) -> {
                            RegSarimaModel preprocessing = r.getPreprocessing();
                            boolean td = false;
                            if (preprocessing != null) {
                                td = Arrays.stream(preprocessing.getDescription().getVariables()).anyMatch(v -> v.getCore() instanceof ITradingDaysVariable);
                            }
                            return r.getDiagnostics() == null ? null : new ResidualTradingDaysDiagnostics.Input(r.getDiagnostics().getGenericDiagnostics().residualTradingDaysTests(), td);
                        }
                );

        List<SaDiagnosticsFactory<?, X13Results>> all = new ArrayList<>();
        all.add(coherence);
        all.add(residuals);
        all.add(outofsample);
        all.add(outliers);
        all.add(spectral);
        all.add(mstats);
        all.add(combinedSeasonality);
        all.add(advancedResidualSeasonality);
        all.add(residualTradingDays);
        return all;
    }

    public X13Factory() {
        diagnostics.addAll(defaultDiagnostics());
    }

    @Override
    public AlgorithmDescriptor descriptor() {
        return X13Spec.DESCRIPTOR_V3;
    }

    @Override
    public X13Spec generateSpec(X13Spec spec, X13Results estimation) {
        if (!estimation.isValid()) {
            return null;
        }
        X11Spec nxspec = update(spec.getX11(), estimation.getDecomposition());
        X13Spec.Builder builder = spec.toBuilder().x11(nxspec);
        RegArimaSpec nrspec;
        if (spec.getRegArima().getBasic().isPreprocessing()) {
            nrspec = RegArimaFactory.getInstance().generateSpec(spec.getRegArima(), estimation.getPreprocessing().getDescription());
        } else {
            nrspec = spec.getRegArima();
        }
        builder.regArima(nrspec);

        return builder.build();
    }

    @Override
    public X13Spec refreshSpec(X13Spec currentSpec, X13Spec domainSpec, EstimationPolicyType policy, TsDomain frozen) {
        if (policy == EstimationPolicyType.Complete) {
            return domainSpec;
        }
        if (policy == EstimationPolicyType.None || !currentSpec.getRegArima().getBasic().isPreprocessing()) {
            return currentSpec;
        }
        RegArimaSpec nrspec = RegArimaFactory.getInstance().refreshSpec(currentSpec.getRegArima(), domainSpec.getRegArima(), policy, frozen);
        X11Spec x11 = currentSpec.getX11();
        if (nrspec.getTransform().getFunction() == TransformationType.Auto) {
            x11 = x11.toBuilder()
                    .mode(DecompositionMode.Undefined)
                    .build();
        }
        return currentSpec.toBuilder()
                .regArima(nrspec)
                .x11(x11)
                .build();
    }

    private X11Spec update(X11Spec x11, X11Results rslts) {
        // Nothing to do (for the time being)
        return x11;
    }

    @Override
    public boolean canHandle(SaSpecification spec) {
        return spec instanceof X13Spec;
    }

    @Override
    public SaProcessor processor(X13Spec spec) {
        return (s, cxt, log) -> X13Kernel.of(spec, cxt).process(s, log);
    }

    @Override
    public X13Spec decode(SaSpecification spec) {
        if (spec instanceof X13Spec) {
            return (X13Spec) spec;
        } else {
            return null;
        }
    }

    @Override
    public List<SaDiagnosticsFactory<?, X13Results>> diagnosticFactories() {
        return Collections.unmodifiableList(diagnostics);
    }

    public void addDiagnostics(SaDiagnosticsFactory<?, X13Results> diag) {
        diagnostics.add(diag);
    }

    public void replaceDiagnostics(SaDiagnosticsFactory<?, X13Results> olddiag, SaDiagnosticsFactory<?, X13Results> newdiag) {
        int idx = diagnostics.indexOf(olddiag);
        if (idx < 0) {
            diagnostics.add(newdiag);
        } else {
            diagnostics.set(idx, newdiag);
        }
    }

    @Override
    public void resetDiagnosticFactories(List<SaDiagnosticsFactory<?, X13Results>> factories) {
        diagnostics.clear();
        diagnostics.addAll(factories);
    }

    @Override
    public Dictionary outputDictionary() {
        return X13Dictionaries.X13DICTIONARY;
    }

}
