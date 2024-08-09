/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.sa.base.core.tests;

import jdplus.toolkit.base.core.data.analysis.TrigonometricSeries;
import jdplus.toolkit.base.core.data.analysis.WindowFunction;
import jdplus.toolkit.base.core.stats.linearmodel.LeastSquaresResults;
import jdplus.toolkit.base.core.stats.linearmodel.LinearModel;
import jdplus.toolkit.base.core.stats.RobustCovarianceComputer;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.stats.linearmodel.Ols;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class CanovaHansen2 {

    public static CanovaHansen2 of(DoubleSeq s) {
        return new CanovaHansen2(s);
    }

    private final DoubleSeq s;
    private boolean trend = false;
    private double period;
    private WindowFunction winFunction = WindowFunction.Bartlett;
    private int truncationLag = 12;
    private boolean lag1 = true;

    private CanovaHansen2(DoubleSeq s) {
        this.s = s;
    }

    public CanovaHansen2 periodicity(double period) {
        this.period = period;
        return this;
    }

    public CanovaHansen2 lag1(boolean lag1) {
        this.lag1 = lag1;
        return this;
    }

    public CanovaHansen2 trend(boolean trend) {
        this.trend = trend;
        return this;
    }

    public CanovaHansen2 truncationLag(int truncationLag) {
        this.truncationLag = truncationLag;
        return this;
    }

    public CanovaHansen2 windowFunction(WindowFunction winFunction) {
        this.winFunction = winFunction;
        return this;
    }

    public double compute() {
        FastMatrix x = sx();
        LinearModel lm = buildModel(x);
        LeastSquaresResults olsResults = Ols.compute(lm);
        DoubleSeq e = lm.calcResiduals(olsResults.getCoefficients());
        double rvar = RobustCovarianceComputer.covariance(e, winFunction, truncationLag);
        int n = lm.getObservationsCount();
        FastMatrix xe = lag1 ? x.extract(1, n, 0, x.getColumnsCount()) : x;

        // multiply the columns of x by e
        xe.applyByColumns(c -> c.apply(e, (a, b) -> a * b));
        xe.applyByColumns(c -> c.cumul());
        if (xe.getColumnsCount() == 1) {
            return xe.column(0).ssq() / (n * n * rvar);
        } else {
            return 2 * (xe.column(0).ssq() + xe.column(1).ssq()) / (n * n * rvar);
        }
    }

    private FastMatrix sx() {
        int len = s.length();
        TrigonometricSeries vars = TrigonometricSeries.specific(period);
        return vars.matrix(len, 0);
    }

    private LinearModel buildModel(FastMatrix sx) {
        if (!lag1) {
            LinearModel.Builder builder = LinearModel.builder();
            builder.y(s);
            if (trend) {
                builder.addX(DoubleSeq.onMapping(s.length(), i -> i));
            }
            builder.addX(sx)
                    .meanCorrection(true);
            return builder.build();
        } else {
            LinearModel.Builder builder = LinearModel.builder();
            builder.y(s.drop(1, 0))
                    .addX(s.drop(0, 1))
                    .meanCorrection(true);
            if (trend) {
                builder.addX(DoubleSeq.onMapping(s.length(), i -> i));
            }
            return builder.build();

        }
    }

}
