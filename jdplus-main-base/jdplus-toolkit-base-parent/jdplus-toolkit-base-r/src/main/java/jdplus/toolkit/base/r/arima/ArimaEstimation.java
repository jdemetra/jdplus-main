/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.toolkit.base.r.arima;

import jdplus.toolkit.base.api.data.Doubles;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.core.stats.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.core.regsarima.RegSarimaComputer;
import jdplus.toolkit.base.api.util.IntList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.api.information.GenericExplorable;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.core.data.interpolation.AverageInterpolator;

/**
 *
 * @author Jean Palate
 */
@lombok.Getter
@lombok.Setter
public class ArimaEstimation {

    private double[] phi, theta, bphi, btheta;
    private int[] order, seasonalOrder;
    private int period;
    private double[] y;
    private final List<double[]> xreg = new ArrayList<>();
    private boolean mean;

    public void addX(double[] x) {
        xreg.add(x);
    }

    public Results process() {
        SarimaOrders spec = new SarimaOrders(period);
        if (order != null) {
            spec.setP(order[0]);
            spec.setD(order[1]);
            spec.setQ(order[2]);
        }
        if (seasonalOrder != null) {
            spec.setBp(seasonalOrder[0]);
            spec.setBd(seasonalOrder[1]);
            spec.setBq(seasonalOrder[2]);
        }
        SarimaModel.Builder builder = SarimaModel.builder(spec);
        //
        SarimaModel arima = builder.setDefault().build();
        RegSarimaComputer monitor = RegSarimaComputer.builder()
                .useParallelProcessing(true)
                .useMaximumLikelihood(true)
                .useCorrectedDegreesOfFreedom(false) // compatibility with R
                .precision(1e-12)
                .startingPoint(RegSarimaComputer.StartingPoint.Multiple)
                .computeExactFinalDerivatives(true)
                .build();

        IntList missings = new IntList();
        AverageInterpolator.cleanMissings(y, missings);
        RegArimaModel.Builder<SarimaModel> rbuilder = RegArimaModel.<SarimaModel>builder()
                .y(Doubles.of(y))
                .arima(arima)
                .meanCorrection(mean)
                .missing(missings.toArray());

        for (double[] x : xreg) {
            rbuilder.addX(DoubleSeq.of(x));
        }

        RegArimaEstimation<SarimaModel> rslt = monitor.process(rbuilder.build(), null);
        return new Results(rslt.getModel(), rslt.getConcentratedLikelihood(), rslt.statistics(),
                rslt.getMax().asymptoticCovariance(), rslt.getMax().getScore());
    }

    @lombok.Value
    public static class Results implements GenericExplorable {

        RegArimaModel<SarimaModel> regarima;
        ConcentratedLikelihoodWithMissing concentratedLogLikelihood;
        LikelihoodStatistics statistics;
        FastMatrix parametersCovariance;
        DoubleSeq score;

        public SarimaModel getArima() {
            return regarima.arima();
        }

        private static final String ARIMA = "sarima", LL = "likelihood", PCOV = "pcov", SCORE = "score",
                B = "b", UNSCALEDBVAR = "unscaledbvar", MEAN = "mean";
        private static final InformationMapping<Results> MAPPING = new InformationMapping<Results>() {
            @Override
            public Class getSourceClass() {
                return Results.class;
            }
        };

        static {
            MAPPING.delegate(ARIMA, SarimaModel.class, r -> r.getArima());
            MAPPING.delegate(LL, LikelihoodStatistics.class, r -> r.statistics);
            MAPPING.set(PCOV, Matrix.class, source -> source.getParametersCovariance());
            MAPPING.set(SCORE, double[].class, source -> source.getScore().toArray());
            MAPPING.set(B, double[].class, source
                    -> {
                DoubleSeq b = source.getConcentratedLogLikelihood().coefficients();
                return b.toArray();
            });
            MAPPING.set(MEAN, Double.class, source
                    -> {
                if (source.getRegarima().isMean()) {
                    DoubleSeq b = source.getConcentratedLogLikelihood().coefficients();
                    int mpos = source.getRegarima().getMissingValuesCount();
                    return b.get(mpos);
                } else {
                    return 0.0;
                }
            });
            MAPPING.set(UNSCALEDBVAR, Matrix.class, source -> source.getConcentratedLogLikelihood().unscaledCovariance());
        }

        @Override
        public boolean contains(String id) {
            return MAPPING.contains(id);
        }

        @Override
        public Map<String, Class> getDictionary() {
            Map<String, Class> dic = new LinkedHashMap<>();
            MAPPING.fillDictionary(null, dic, true);
            return dic;
        }

        @Override
        public <T> T getData(String id, Class<T> tclass) {
            return MAPPING.getData(this, id, tclass);
        }

        public static final InformationMapping<Results> getMapping() {
            return MAPPING;
        }

    }
}
