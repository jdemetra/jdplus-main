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
package jdplus.toolkit.base.core.modelling.regular.tests;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.stats.ProbabilityType;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.calendars.DayClustering;
import jdplus.toolkit.base.api.timeseries.calendars.GenericTradingDays;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.dstats.Chi2;
import jdplus.toolkit.base.core.math.functions.IParametricMapping;
import jdplus.toolkit.base.core.math.functions.ParamValidation;
import jdplus.toolkit.base.core.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.modelling.regression.GenericTradingDaysFactory;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.sarima.estimation.SarimaMapping;
import jdplus.toolkit.base.core.ssf.arima.SsfArima;
import jdplus.toolkit.base.core.ssf.basic.RegSsf;
import jdplus.toolkit.base.core.ssf.dk.SsfFunction;
import jdplus.toolkit.base.core.ssf.dk.SsfFunctionPoint;
import jdplus.toolkit.base.core.ssf.univariate.ISsfData;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;

/**
 *
 * @author palatej
 */
public class TimeVaryingEstimator {

    private FastMatrix tdvar, td;
    private TsData s;
    private SarimaModel arima0, arima;
    private double ll, ll0;
    private DoubleSeq p, p0;

    /**
     * *
     *
     * @param spec
     */
    public TimeVaryingEstimator() {
    }

    public StatisticalTest process(TsData s, DayClustering dc, boolean onContrasts) {
        this.s = s;
        this.tdvar = generateVar(dc, onContrasts);
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
        int n = s.length();
        td = FastMatrix.make(n, gtd.getCount());
        GenericTradingDaysFactory.FACTORY.fill(gtd, s.getStart(), td);
        try {
            if (!compute()) {
                return null;
            }
            Chi2 chi2 = new Chi2(1);
            double val = 2 * (ll - ll0);
            if (val < 0) {
                val = 0;
            }
            return new StatisticalTest(val, chi2.getProbability(val, ProbabilityType.Upper), chi2.getDescription());
        } catch (Exception err) {
            return null;
        } finally {
            cleanUp();
        }
    }

    public static FastMatrix generateVar(DayClustering dc, boolean onContrast) {
        // q(i)=b(i)*D(i), var(b(i))= 1/(D(i)*D(i))->var(q(i)) =1
        // m=avg(q(i)) = sum(q(i))/7, var(m) = ngroups/49
        // p(i) = b(i)-m
        // cov(p(i), p(j)) = ngroups/49 - (1/D(i) + 1/D(j))/7
        // var(pi) = ngroups/49 + 1/(D(i)*D(i)) - 2/(D(i)*7)
        int groupsCount = dc.getGroupsCount() - 1;
        FastMatrix M = FastMatrix.square(groupsCount);
        int[] D = new int[groupsCount];
        for (int i = 0; i < groupsCount; ++i) {
            D[i] = dc.getGroupCount(i + 1);
        }
        M.diagonal().set(i -> 1.0 / (D[i] * D[i]));

        if (!onContrast) {
            double vm = (1 + groupsCount) / 49.0;
            M.add(vm);
            for (int i = 0; i < groupsCount; ++i) {
                M.add(i, i, -2.0 / (D[i] * 7.0));
                for (int j = 0; j < i; ++j) {
                    double c = (1.0 / D[i] + 1.0 / D[j]) / 7.0;
                    M.add(i, j, -c);
                    M.add(j, i, -c);
                }
            }
        }
        return M;
    }

    private boolean compute() {
        try {
            ISsfData data = new SsfData(s.getValues());
            // step 0 fixed model
            int period = s.getAnnualFrequency();
            TDvarData tdVar0 = new TDvarData(SarimaModel.builder(SarimaOrders.airline(period)).setDefault(0, -.6).build(), td, null);
            TDvarMapping mapping0 = new TDvarMapping(tdVar0);
            p0 = mapping0.getDefaultParameters();
            // Create the function
            SsfFunction<TDvarData, Ssf> fn0 = SsfFunction.<TDvarData, Ssf>builder(data, mapping0, q -> q.toSsf())
                    .useSqrtInitialization(true)
                    .useScalingFactor(true)
                    .useLog(true)
                     .build();
            LevenbergMarquardtMinimizer min = LevenbergMarquardtMinimizer.builder()
                    .functionPrecision(1e-9)
                    .build();
            min.minimize(fn0.evaluate(p0));
            SsfFunctionPoint<TDvarData, Ssf> rfn0 = (SsfFunctionPoint<TDvarData, Ssf>) min.getResult();
            arima0 = rfn0.getCore().getArima();
            p0 = rfn0.getParameters();

            TDvarData tdVar1 = new TDvarData(arima0, td, tdvar);
            TDvarMapping mapping1 = new TDvarMapping(tdVar1);
            // Create the function
            SsfFunction<TDvarData, Ssf> fn1 = SsfFunction.<TDvarData, Ssf>builder(data, mapping1, q -> q.toSsf())
                    .useSqrtInitialization(true)
                    .useScalingFactor(true)
                    .useLog(true)
                    .build();
            double[] np=new double[3];
            p0.copyTo(np, 0);
            np[2]=0.001;
            min.minimize(fn1.evaluate(DoubleSeq.of(np)));
            SsfFunctionPoint<TDvarData, Ssf> rfn1 = (SsfFunctionPoint<TDvarData, Ssf>) min.getResult();
            arima = rfn1.getCore().getArima();
            p = rfn1.getParameters();
            ll = rfn1.getLikelihood().logLikelihood();
            ll0 = rfn0.getLikelihood().logLikelihood();
            return true;
        } catch (Exception err) {
            return false;
        }
    }

    private void cleanUp() {
    }

    @lombok.Value
    private static class TDvarData {

        @lombok.NonNull
        private SarimaModel arima;
        @lombok.NonNull
        private FastMatrix td; // regression variable
        private FastMatrix nvar; // unscaled covariance matrix for var coefficients

        boolean hasVar() {
            return nvar != null;
        }

        Ssf toSsf() {
            Ssf ssf = SsfArima.ssf(arima);
            if (nvar != null) {
                return RegSsf.timeVaryingSsf(ssf, td, nvar);
            } else {
                return RegSsf.ssf(ssf, td);
            }

        }
    }

    private static class TDvarMapping implements IParametricMapping<TDvarData> {

        private static final SarimaMapping AIRLINEMAPPING;

        static {
            SarimaOrders spec = SarimaOrders.airline(12);
            AIRLINEMAPPING = SarimaMapping.of(spec);
        }

        private final TDvarData data;

        TDvarMapping(TDvarData data) {
            this.data = data;
        }

        @Override
        public boolean checkBoundaries(DoubleSeq inparams) {
            return AIRLINEMAPPING.checkBoundaries(inparams.extract(0, 2));
        }

        @Override
        public double epsilon(DoubleSeq inparams, int idx) {
            if (idx < 2) {
                return AIRLINEMAPPING.epsilon(inparams, idx);
            }
            return Math.max(inparams.get(2) * .001, 1e-9);
        }

        @Override
        public int getDim() {
            return data.getNvar() == null ? 2 : 3;
        }

        @Override
        public double lbound(int idx) {
            if (idx < 2) {
                return AIRLINEMAPPING.lbound(idx);
            } else {
                return 0;
            }
        }

        @Override
        public double ubound(int idx) {
            if (idx < 2) {
                return AIRLINEMAPPING.ubound(idx);
            } else {
                return 10;
            }
        }

        @Override
        public ParamValidation validate(DataBlock ioparams) {
            return AIRLINEMAPPING.validate(ioparams.extract(0, 2));
        }

        @Override
        public String getDescription(int idx) {
            if (idx < 2) {
                return AIRLINEMAPPING.getDescription(idx);
            } else {
                return "noise stdev";
            }
        }

        @Override
        public TDvarData map(DoubleSeq p) {
            SarimaModel arima = data.getArima().toBuilder()
                    .theta(1, p.get(0))
                    .btheta(1, p.get(1))
                    .build();
            FastMatrix v = null;
            if (data.hasVar()) {
                double nv = p.get(2);
                v = data.getNvar().deepClone();
                v.mul(nv * nv);
            }
            return new TDvarData(arima, data.getTd(), v);
        }

        @Override
        public DoubleSeq getDefaultParameters() {
            double[] p = new double[getDim()];
            p[0] = -.6;
            p[1] = -.6;
            if (p.length > 2) {
                p[2] = .1;
            }
            return DoubleSeq.of(p);
        }
    }

}
