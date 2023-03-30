/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.protobuf;


/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class DecompositionProto {

    public void fill(jdplus.tramoseats.base.api.seats.DecompositionSpec spec, DecompositionSpec.Builder builder) {
        builder.setXlBoundary(spec.getXlBoundary())
                .setTrendBoundary((spec.getTrendBoundary()))
                .setSeastolerance(spec.getSeasTolerance())
                .setSeasBoundary(spec.getSeasBoundary())
                .setSeasBoundaryAtPi(spec.getSeasBoundaryAtPi())
                .setNfcasts(spec.getForecastCount())
                .setNbcasts(spec.getBackcastCount())
                .setApproximation(convert(spec.getApproximationMode()))
                .setAlgorithm(convert(spec.getMethod()))
                .setBiasCorrection(spec.getBiasCorrection() != jdplus.tramoseats.base.api.seats.DecompositionSpec.BiasCorrection.None);
    }

    public DecompositionSpec convert(jdplus.tramoseats.base.api.seats.DecompositionSpec spec) {
        DecompositionSpec.Builder builder = DecompositionSpec.newBuilder();
        fill(spec, builder);
        return builder.build();
    }

    public jdplus.tramoseats.base.api.seats.DecompositionSpec convert(DecompositionSpec spec) {
        return jdplus.tramoseats.base.api.seats.DecompositionSpec.builder()
                .xlBoundary(spec.getXlBoundary())
                .trendBoundary(spec.getTrendBoundary())
                .seasTolerance(spec.getSeastolerance())
                .seasBoundary(spec.getSeasBoundary())
                .seasBoundaryAtPi(spec.getSeasBoundaryAtPi())
                .forecastCount(spec.getNfcasts())
                .backcastCount(spec.getNbcasts())
                .biasCorrection(spec.getBiasCorrection() ? jdplus.tramoseats.base.api.seats.DecompositionSpec.BiasCorrection.Legacy : jdplus.tramoseats.base.api.seats.DecompositionSpec.BiasCorrection.None)
                .approximationMode(convert(spec.getApproximation()))
                .method(convert(spec.getAlgorithm()))
                .build();
    }

    SeatsApproximation convert(jdplus.tramoseats.base.api.seats.DecompositionSpec.ModelApproximationMode app) {
        switch (app) {
            case Legacy:
                return SeatsApproximation.SEATS_APP_LEGACY;
            case Noisy:
                return SeatsApproximation.SEATS_APP_NOISY;
            default:
                return SeatsApproximation.SEATS_APP_NONE;
        }
    }

    SeatsAlgorithm convert(jdplus.tramoseats.base.api.seats.DecompositionSpec.ComponentsEstimationMethod method) {
        switch (method) {
            case KalmanSmoother:
                return SeatsAlgorithm.SEATS_ALG_KALMANSMOOTHER;
            default:
                return SeatsAlgorithm.SEATS_ALG_BURMAN;
        }
    }

    jdplus.tramoseats.base.api.seats.DecompositionSpec.ModelApproximationMode convert(SeatsApproximation app) {
        switch (app) {
            case SEATS_APP_LEGACY:
                return jdplus.tramoseats.base.api.seats.DecompositionSpec.ModelApproximationMode.Legacy;
            case SEATS_APP_NOISY:
                return jdplus.tramoseats.base.api.seats.DecompositionSpec.ModelApproximationMode.Noisy;
            default:
                return jdplus.tramoseats.base.api.seats.DecompositionSpec.ModelApproximationMode.None;
        }
    }

    jdplus.tramoseats.base.api.seats.DecompositionSpec.ComponentsEstimationMethod convert(SeatsAlgorithm method) {
        switch (method) {
            case SEATS_ALG_KALMANSMOOTHER:
                return jdplus.tramoseats.base.api.seats.DecompositionSpec.ComponentsEstimationMethod.KalmanSmoother;
            default:
                return jdplus.tramoseats.base.api.seats.DecompositionSpec.ComponentsEstimationMethod.Burman;
        }
    }
}
