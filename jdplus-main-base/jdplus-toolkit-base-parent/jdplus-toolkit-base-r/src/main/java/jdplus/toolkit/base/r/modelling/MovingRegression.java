/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.r.modelling;

import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.core.stats.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.regarima.estimation.ConcentratedLikelihoodComputer;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.core.regsarima.RegSarimaComputer;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TimeSelector;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.timeseries.calendars.DayClustering;
import jdplus.toolkit.base.api.timeseries.calendars.GenericTradingDays;
import jdplus.toolkit.base.api.timeseries.regression.GenericTradingDaysVariable;
import jdplus.toolkit.base.core.modelling.regression.Regression;
import jdplus.toolkit.base.api.timeseries.TsData;
import static jdplus.toolkit.base.core.timeseries.simplets.TsDataToolkit.fitToDomain;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.api.information.GenericExplorable;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class MovingRegression {

    @lombok.Data
    static class Airline {

        FastMatrix td;
        double regVariance;
        double theta, btheta;
    }

    @lombok.Value
    @lombok.Builder
    public static class Results implements GenericExplorable {

        TsDomain domain;
        FastMatrix variables;
        FastMatrix coefficients;
        SarimaModel arima;

        private static final String ARIMA = "arima", LL = "likelihood", COEFF = "coefficients", TD = "td", TDEFFECT = "tdeffect";
        private static final InformationMapping<Results> MAPPING = new InformationMapping<Results>() {
            @Override
            public Class getSourceClass() {
                return Results.class;
            }
        };

        static {
//            MAPPING.delegate(ARIMA, SarimaExtractor.getMapping(), r -> ApiUtility.toApi(r.getArima(), null));
            MAPPING.set(COEFF, FastMatrix.class, r -> r.getCoefficients());
            MAPPING.set(TD, FastMatrix.class, r -> r.getVariables());
            MAPPING.set(TDEFFECT, TsData.class, r
                    -> {
                DataBlock tmp = DataBlock.make(r.getDomain().length());
                DataBlock prod = DataBlock.make(r.getDomain().length());
                for (int i = 0; i < r.variables.getColumnsCount(); ++i) {
                    prod.set(r.getCoefficients().column(i), r.getVariables().column(i), (a, b) -> a * b);
                    tmp.add(prod);
                }
                return TsData.of(r.getDomain().getStartPeriod(), tmp);
            });
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

    public Results regarima(TsData s, String td, int nyears) {
        int period = s.getTsUnit().ratioOf(TsUnit.P1Y);
        SarimaOrders spec = SarimaOrders.airline(period);

        SarimaModel arima = SarimaModel.builder(spec)
                .setDefault()
                .build();

        DayClustering dc = days(td);
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
        FastMatrix x = Regression.matrix(s.getDomain(), new GenericTradingDaysVariable(gtd));

        RegSarimaComputer monitor = RegSarimaComputer.builder()
                .useParallelProcessing(true)
                .useMaximumLikelihood(true)
                .useCorrectedDegreesOfFreedom(false) // compatibility with R
                .precision(1e-12)
                .build();
        RegArimaModel.Builder<SarimaModel> rbuilder = RegArimaModel.<SarimaModel>builder()
                .y(s.getValues())
                .arima(arima);
        x.columns().forEach(xx -> rbuilder.addX(xx));

        RegArimaEstimation<SarimaModel> rslt = monitor.process(rbuilder.build(), null);
        arima = rslt.getModel().arima();

        List<double[]> coef = new ArrayList<>();
        TimeSelector sel = TimeSelector.first(nyears * period);
        TsDomain dom = s.getDomain().select(sel);
        while (dom.end().isBefore(s.getDomain().end())) {
            FastMatrix mtd = generate(dom, dc);
            TsData yc = fitToDomain(s, dom);
            RegArimaModel.Builder<SarimaModel> builder = RegArimaModel.<SarimaModel>builder()
                    .y(yc.getValues())
                    .arima(arima);
            mtd.columns().forEach(xx -> builder.addX(xx));
            ConcentratedLikelihoodWithMissing cll = ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(builder.build());
            coef.add(cll.coefficients().toArray());
            dom = dom.move(period);
        }
//        double[] xi = new double[coef.size()];
//        double[] fxi = new double[xi.length];
//        xi[0] = (period * nyears) / 2;
//        for (int i = 1; i < xi.length; ++i) {
//            xi[i] = xi[i - 1] + period;
//        }
        FastMatrix cl = FastMatrix.make(s.length(), x.getColumnsCount());
        cl.set(Double.NaN);
        int j = 0;
        for (; j < (period * nyears) / 2; ++j) {
            cl.row(j).copyFrom(coef.get(0), 0);
        }
        for (int i = 1; i < coef.size(); ++i) {
            double[] a = coef.get(i - 1), b = coef.get(i);
            int l = j;
            for (int k = 0; k < period; ++k, ++j) {
                for (int c = 0; c < a.length; ++c) {
                    double del = (b[c] - a[c]) / period;
                    cl.set(j, c, a[c] + del * (j - l));
                }
            }
        }

        double[] last = coef.get(coef.size() - 1);
        for (; j < cl.getRowsCount(); ++j) {
            cl.row(j).copyFrom(last, 0);
        }
        return Results.builder()
                .domain(s.getDomain())
                .arima(arima)
                .coefficients(cl)
                .variables(x)
                .build();
    }

    public DayClustering days(String td) {
        DayClustering dc;
        dc = switch (td) {
            case "TD2" -> DayClustering.TD2;
            case "TD3" -> DayClustering.TD3;
            case "TD3c" -> DayClustering.TD3c;
            case "TD4" -> DayClustering.TD4;
            default -> DayClustering.TD7;
        };
        return dc;
    }

    private FastMatrix generate(TsDomain domain, DayClustering dc) {
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
        return Regression.matrix(domain, new GenericTradingDaysVariable(gtd));
    }

    private FastMatrix generateVar(DayClustering dc, String var) {
        int groupsCount = dc.getGroupsCount();
        FastMatrix full = FastMatrix.square(7);
        if (!var.equalsIgnoreCase("Contrasts")) {
            full.set(-1.0 / 7.0);
        }
        full.diagonal().add(1);
        FastMatrix Q = FastMatrix.make(groupsCount - 1, 7);
        int[] gdef = dc.getGroupsDefinition();
        for (int i = 1; i < groupsCount; ++i) {
            for (int j = 0; j < 7; ++j) {
                if (gdef[j] == i) {
                    Q.set(i - 1, j, 1);
                }
            }
        }
        return SymmetricMatrix.XSXt(full, Q);
    }

}
