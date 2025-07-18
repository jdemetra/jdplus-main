/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.core.tramoseats;

import jdplus.toolkit.base.api.arima.SarimaSpec;
import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;
import jdplus.sa.base.api.EstimationPolicyType;
import jdplus.sa.base.api.SaDiagnosticsFactory;
import jdplus.sa.base.api.SaManager;
import jdplus.sa.base.api.SaProcessor;
import jdplus.sa.base.api.SaSpecification;
import jdplus.tramoseats.base.api.seats.DecompositionSpec;
import jdplus.tramoseats.base.api.tramo.TramoSpec;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsSpec;
import nbbrd.service.ServiceProvider;
import jdplus.sa.base.api.SaProcessingFactory;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsDictionaries;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.SeriesDecomposition;
import jdplus.toolkit.base.core.modelling.GeneralLinearModel;
import jdplus.sa.base.core.diagnostics.AdvancedResidualSeasonalityDiagnosticsConfiguration;
import jdplus.sa.base.core.diagnostics.AdvancedResidualSeasonalityDiagnosticsFactory;
import jdplus.sa.base.core.diagnostics.CoherenceDiagnostics;
import jdplus.sa.base.core.diagnostics.CoherenceDiagnosticsConfiguration;
import jdplus.sa.base.core.diagnostics.CoherenceDiagnosticsFactory;
import jdplus.sa.base.core.diagnostics.CombinedSeasonalityDiagnosticsConfiguration;
import jdplus.sa.base.core.diagnostics.CombinedSeasonalityDiagnosticsFactory;
import jdplus.sa.base.core.diagnostics.ResidualTradingDaysDiagnostics;
import jdplus.toolkit.base.core.regarima.diagnostics.OutOfSampleDiagnosticsConfiguration;
import jdplus.toolkit.base.core.regarima.diagnostics.OutliersDiagnosticsConfiguration;
import jdplus.toolkit.base.core.regarima.diagnostics.ResidualsDiagnosticsConfiguration;
import jdplus.sa.base.core.diagnostics.ResidualTradingDaysDiagnosticsConfiguration;
import jdplus.sa.base.core.diagnostics.ResidualTradingDaysDiagnosticsFactory;
import jdplus.sa.base.core.diagnostics.SaOutOfSampleDiagnosticsFactory;
import jdplus.sa.base.core.diagnostics.SaOutliersDiagnosticsFactory;
import jdplus.sa.base.core.diagnostics.SaResidualsDiagnosticsFactory;
import jdplus.sa.base.core.diagnostics.SpectralDiagnostics;
import jdplus.sa.base.core.diagnostics.SpectralDiagnosticsConfiguration;
import jdplus.sa.base.core.diagnostics.SpectralDiagnosticsFactory;
import jdplus.toolkit.base.api.modelling.ComponentInformation;
import jdplus.toolkit.base.api.timeseries.regression.ITradingDaysVariable;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.tramoseats.base.core.seats.SeatsResults;
import jdplus.tramoseats.base.core.seats.diagnostics.SeatsDiagnosticsConfiguration;
import jdplus.tramoseats.base.core.seats.diagnostics.SeatsDiagnosticsFactory;
import jdplus.tramoseats.base.core.tramo.TramoFactory;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(SaProcessingFactory.class)
public final class TramoSeatsFactory implements SaProcessingFactory<TramoSeatsSpec, TramoSeatsResults> {

    public static TramoSeatsFactory getInstance() {
        return (TramoSeatsFactory) SaManager.processors().stream().filter(x -> x instanceof TramoSeatsFactory).findAny().orElse(new TramoSeatsFactory());
    }

    private final List<SaDiagnosticsFactory<?, TramoSeatsResults>> diagnostics = new CopyOnWriteArrayList<>();

    public TramoSeatsFactory() {
        diagnostics.addAll(defaultDiagnostics());
    }

    public static List<SaDiagnosticsFactory<?, TramoSeatsResults>> defaultDiagnostics() {
        CoherenceDiagnosticsFactory<TramoSeatsResults> coherence
                = new CoherenceDiagnosticsFactory<>(CoherenceDiagnosticsConfiguration.getDefault(),
                        (TramoSeatsResults r) -> {
                            return r.getFinals() == null ? null : new CoherenceDiagnostics.Input(r.getFinals().getMode(), r);
                        }
                );
        SaOutOfSampleDiagnosticsFactory<TramoSeatsResults> outofsample
                = new SaOutOfSampleDiagnosticsFactory<>(OutOfSampleDiagnosticsConfiguration.getDefault(),
                        r -> r.getDiagnostics() == null ? null : r.getDiagnostics().getGenericDiagnostics().forecastingTest());
        SaResidualsDiagnosticsFactory<TramoSeatsResults> residuals
                = new SaResidualsDiagnosticsFactory<>(ResidualsDiagnosticsConfiguration.getDefault(),
                        r -> r.getPreprocessing());
        SaOutliersDiagnosticsFactory<TramoSeatsResults> outliers
                = new SaOutliersDiagnosticsFactory<>(OutliersDiagnosticsConfiguration.getDefault(),
                        r -> r.getPreprocessing());
        SpectralDiagnosticsFactory<TramoSeatsResults> spectral
                = new SpectralDiagnosticsFactory<>(SpectralDiagnosticsConfiguration.getDefault(),
                        (TramoSeatsResults r) -> {
                            SeatsResults sd = r.getDecomposition();
                            if (sd == null) {
                                return null;
                            }
                            return new SpectralDiagnostics.Input(r.getDecomposition().getFinalComponents().getMode(),
                                    sd.getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.Value),
                                    sd.getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
                        });
        SeatsDiagnosticsFactory<TramoSeatsResults> seats
                = new SeatsDiagnosticsFactory<>(SeatsDiagnosticsConfiguration.getDefault(),
                        r -> r.getDiagnostics() == null ? null : r.getDiagnostics().getSpecificDiagnostics());

        AdvancedResidualSeasonalityDiagnosticsFactory<TramoSeatsResults> advancedResidualSeasonality
                = new AdvancedResidualSeasonalityDiagnosticsFactory<>(AdvancedResidualSeasonalityDiagnosticsConfiguration.getDefault(),
                        (TramoSeatsResults r) -> r.getDiagnostics() == null ? null : r.getDiagnostics().getGenericDiagnostics()
                );
        CombinedSeasonalityDiagnosticsFactory<TramoSeatsResults> combinedSeasonality
                = new CombinedSeasonalityDiagnosticsFactory<>(CombinedSeasonalityDiagnosticsConfiguration.getDefault(),
                        (TramoSeatsResults r) -> r.getDiagnostics() == null ? null : r.getDiagnostics().getGenericDiagnostics()
                );

        ResidualTradingDaysDiagnosticsFactory<TramoSeatsResults> residualTradingDays
                = new ResidualTradingDaysDiagnosticsFactory<>(ResidualTradingDaysDiagnosticsConfiguration.getDefault(),
                        (TramoSeatsResults r) -> {
                            RegSarimaModel preprocessing = r.getPreprocessing();
                            boolean td = false;
                            if (preprocessing != null) {
                                td = Arrays.stream(preprocessing.getDescription().getVariables()).anyMatch(v -> v.getCore() instanceof ITradingDaysVariable);
                            }
                            return r.getDiagnostics() == null ? null : new ResidualTradingDaysDiagnostics.Input(r.getDiagnostics().getGenericDiagnostics().residualTradingDaysTests(), td);
                        }
                );

        List<SaDiagnosticsFactory<?, TramoSeatsResults>> all = new ArrayList<>();

        all.add(coherence);
        all.add(residuals);
        all.add(outofsample);
        all.add(outliers);
        all.add(spectral);
        all.add(seats);
        all.add(combinedSeasonality);
        all.add(advancedResidualSeasonality);
        all.add(residualTradingDays);
        return all;
    }

    @Override
    public AlgorithmDescriptor descriptor() {
        return TramoSeatsSpec.DESCRIPTOR_V3;
    }

    @Override
    public TramoSeatsSpec generateSpec(TramoSeatsSpec spec, TramoSeatsResults estimation) {
        if (! estimation.isValid()) {
            return null;
        }
        return generateSpec(spec, estimation.getPreprocessing().getDescription());
    }

    public TramoSeatsSpec generateSpec(TramoSeatsSpec spec, GeneralLinearModel.Description<SarimaSpec> desc) {

        TramoSpec ntspec = TramoFactory.getInstance().generateSpec(spec.getTramo(), desc);
        DecompositionSpec nsspec = update(spec.getSeats());

        return spec.toBuilder()
                .tramo(ntspec)
                .seats(nsspec)
                .build();
    }

    @Override
    public TramoSeatsSpec refreshSpec(TramoSeatsSpec currentSpec, TramoSeatsSpec domainSpec, EstimationPolicyType policy, TsDomain domain) {
        // NOT COMPLETE
        if (policy == EstimationPolicyType.None) {
            return currentSpec;
        }
        if (policy == EstimationPolicyType.Complete) {
            return domainSpec;
        }
        TramoSpec ntspec = TramoFactory.getInstance().refreshSpec(currentSpec.getTramo(), domainSpec.getTramo(), policy, domain);
        return currentSpec.toBuilder()
                .tramo(ntspec)
                .build();
    }

    private DecompositionSpec update(DecompositionSpec seats) {
        // Nothing to do (for the time being)
        return seats;
    }

    @Override
    public SaProcessor processor(TramoSeatsSpec spec) {
        return (s, cxt, log) -> TramoSeatsKernel.of(spec, cxt).process(s, log);
    }

    @Override
    public TramoSeatsSpec decode(SaSpecification spec) {
        if (spec instanceof TramoSeatsSpec) {
            return (TramoSeatsSpec) spec;
        } else {
            return null;
        }
    }

    @Override
    public boolean canHandle(SaSpecification spec) {
        return spec instanceof TramoSeatsSpec;
    }

    @Override
    public List<SaDiagnosticsFactory<?, TramoSeatsResults>> diagnosticFactories() {
        return Collections.unmodifiableList(diagnostics);
    }

    public void addDiagnostics(SaDiagnosticsFactory<?, TramoSeatsResults> diag) {
        diagnostics.add(diag);
    }

    public void replaceDiagnostics(SaDiagnosticsFactory<?, TramoSeatsResults> olddiag, SaDiagnosticsFactory<?, TramoSeatsResults> newdiag) {
        int idx = diagnostics.indexOf(olddiag);
        if (idx < 0) {
            diagnostics.add(newdiag);
        } else {
            diagnostics.set(idx, newdiag);
        }
    }

    @Override
    public void resetDiagnosticFactories(List<SaDiagnosticsFactory<?, TramoSeatsResults>> factories) {
        diagnostics.clear();
        diagnostics.addAll(factories);
    }

    @Override
    public Dictionary outputDictionary() {
        return TramoSeatsDictionaries.TRAMOSEATSDICTIONARY;
    }

}
