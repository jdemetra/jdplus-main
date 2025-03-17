/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.core.tramoseats;

import jdplus.toolkit.base.api.arima.SarimaSpec;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.data.ParameterType;
import jdplus.toolkit.base.api.modelling.ComponentInformation;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.SaVariable;
import jdplus.sa.base.api.SeriesDecomposition;
import jdplus.tramoseats.base.api.seats.SeatsModelSpec;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.tramoseats.base.api.tramo.TransformSpec;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsSpec;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.sa.base.core.CholetteProcessor;
import jdplus.sa.base.core.PreliminaryChecks;
import jdplus.sa.base.core.SaBenchmarkingResults;
import jdplus.sa.base.core.modelling.TwoStepsDecomposition;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.tramoseats.base.core.seats.SeatsKernel;
import jdplus.tramoseats.base.core.seats.SeatsResults;
import jdplus.tramoseats.base.core.seats.SeatsToolkit;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import jdplus.tramoseats.base.core.tramo.TramoKernel;

/**
 *
 * @author palatej
 */
@lombok.Value
public class TramoSeatsKernel {

    private static PreliminaryChecks.Tool of(TramoSeatsSpec spec) {

        TransformSpec transform = spec.getTramo().getTransform();
        return (TsData s, ProcessingLog logs) -> {
            TsData sc = s.select(transform.getSpan());
            if (transform.isPreliminaryCheck()) {
                if (!PreliminaryChecks.testSeries(sc, logs)) {
                    return null;
                }
            }
            return sc;
        };
    }

    private PreliminaryChecks.Tool preliminary;
    private TramoKernel tramo;
    private SeatsKernel seats;
    private CholetteProcessor cholette;

    public static TramoSeatsKernel of(TramoSeatsSpec spec, ModellingContext context) {
        PreliminaryChecks.Tool check = of(spec);
        TramoKernel tramo = TramoKernel.of(spec.getTramo(), context);
        SeatsKernel seats = new SeatsKernel(SeatsToolkit.of(spec.getSeats()));
        return new TramoSeatsKernel(check, tramo, seats, CholetteProcessor.of(spec.getBenchmarking()));
    }

    public TramoSeatsResults process(TsData s, ProcessingLog log) {
        if (log == null) {
            log = ProcessingLog.dummy();
        }
        
        try {
            // Step 0. Preliminary checks
            // sc is the series corresponding to the series span, after some verifications
            // null in case of problems
            TsData sc = preliminary.check(s, log);
            if (sc == null) {
                return TramoSeatsResults.builder()
                        .log(log)
                        .build();
            }
            // Step 1. Tramo
            // We reuse the full series because selection is integrated in the preprocessing step
            RegSarimaModel preprocessing = tramo.process(s, log);
            // Step 2. Link between tramo and seats
            SeatsModelSpec smodel = of(preprocessing);
            // Step 3. Seats
            SeatsResults srslts = seats.process(smodel, log);
            // Step 4. Final decomposition
            SeriesDecomposition finals = TwoStepsDecomposition.merge(preprocessing, srslts.getFinalComponents());
            // Step 5. Benchmarking
            SaBenchmarkingResults bench = null;
            if (cholette != null) {
                bench = cholette.process(s, TsData.concatenate(finals.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value),
                        finals.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast)), preprocessing);
            }
            // Step 6. Diagnostics
            TramoSeatsDiagnostics diagnostics = TramoSeatsDiagnostics.of(preprocessing, srslts, finals);

            return TramoSeatsResults.builder()
                    .preprocessing(preprocessing)
                    .decomposition(srslts)
                    .finals(finals)
                    .benchmarking(bench)
                    .diagnostics(diagnostics)
                    .log(log)
                    .build();
        } catch (Exception err) {
            log.error(err);
            return TramoSeatsResults.builder()
                    .log(log)
                    .build();
        }
    }

    private static SeatsModelSpec of(RegSarimaModel model) {
        TsData series = model.interpolatedSeries(false);
        TsData det = model.deterministicEffect(null, v -> !SaVariable.isRegressionEffect(v, ComponentType.Undefined));
        det = model.backTransform(det, true);
        // we remove all the regression effects except the undefined ones (which will be included in the different components)
        if (model.getDescription().isLogTransformation()) {
            series = TsData.divide(series, det);
        } else {
            series = TsData.subtract(series, det);
        }

        SarimaModel arima = model.arima();
        SarimaSpec sarima = SarimaSpec.builder()
                .d(arima.getD())
                .bd(arima.getBd())
                .phi(Parameter.of(arima.getPhi(), ParameterType.Fixed))
                .theta(Parameter.of(arima.getTheta(), ParameterType.Fixed))
                .bphi(Parameter.of(arima.getBphi(), ParameterType.Fixed))
                .btheta(Parameter.of(arima.getBtheta(), ParameterType.Fixed))
                .build();
        LikelihoodStatistics ll = model.getEstimation().getStatistics();
        return SeatsModelSpec.builder()
                .series(series)
                .log(model.getDescription().isLogTransformation())
                .meanCorrection(model.isMeanCorrection())
                .sarimaSpec(sarima)
                .innovationVariance(ll.getSsqErr() / (ll.getEffectiveObservationsCount() - ll.getEstimatedParametersCount()))
                .build();
    }

}
