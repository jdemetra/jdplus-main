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
import nbbrd.design.BuilderPattern;
import jdplus.toolkit.base.core.stats.linearmodel.LeastSquaresResults;
import jdplus.toolkit.base.core.stats.linearmodel.LinearModel;
import jdplus.toolkit.base.core.stats.linearmodel.Ols;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.api.timeseries.regression.PeriodicDummies;
import jdplus.toolkit.base.core.stats.RobustCovarianceComputer;
import jdplus.toolkit.base.core.modelling.regression.PeriodicDummiesFactory;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class CanovaHansen {

    public static enum Variables {

        Dummy, Trigonometric, UserDefined
    }

    public static Builder test(DoubleSeq s) {
        return new Builder(s);
    }

    @BuilderPattern(CanovaHansen.class)
    public static class Builder {

        private final DoubleSeq s;
        private double period;
        private boolean lag1 = true;
        private Variables type = Variables.Dummy;
        private WindowFunction winFunction = WindowFunction.Bartlett;
        private int truncationLag = 12;
        private int startPosition;
        private int nh;

        private Builder(DoubleSeq s) {
            this.s = s;
        }

        public Builder dummies(int period) {
            this.type = Variables.Dummy;
            this.period = period;
            return this;
        }

        public Builder trigonometric(int period) {
            this.type = Variables.Trigonometric;
            this.period = period;
            return this;
        }

        public Builder specific(double period, int nharmonics) {
            this.type = Variables.UserDefined;
            this.period = period;
            this.nh = nharmonics;
            return this;
        }

        public Builder lag1(boolean lag1) {
            this.lag1 = lag1;
            return this;
        }

        public Builder truncationLag(int truncationLag) {
            this.truncationLag = truncationLag;
            return this;
        }

        public Builder windowFunction(WindowFunction winFunction) {
            this.winFunction = winFunction;
            return this;
        }

        public Builder startPosition(int startPosition) {
            this.startPosition = startPosition;
            return this;
        }

        public CanovaHansen build() {
            FastMatrix x = sx();
            LinearModel lm = buildModel(x);
            return new CanovaHansen(x, lm, winFunction, truncationLag);
        }

        private FastMatrix sx() {
            int len = s.length();
            int pos = startPosition;
            if (lag1) {
                ++pos;
                --len;
            }
            switch (type) {
                case Dummy: {
                    PeriodicDummies vars = new PeriodicDummies((int) period);
                    return PeriodicDummiesFactory.matrix(vars, len, pos);
                }
                case Trigonometric: {
                    TrigonometricSeries vars = TrigonometricSeries.regular((int) period);
                    return vars.matrix(len, pos);
                }
                default:
                    TrigonometricSeries vars = TrigonometricSeries.all(period, nh);
                    return vars.matrix(len, pos);
            }

        }

        private LinearModel buildModel(FastMatrix sx) {

            LinearModel.Builder builder = LinearModel.builder();
            if (lag1) {
                builder.y(s.drop(1, 0))
                        .addX(s.drop(0, 1));
            } else {
                builder.y(s);
            }
            switch (type) {
                case Dummy ->  {
                    builder.addX(sx);
                }
                case Trigonometric ->  {
                    builder.addX(sx)
                            .meanCorrection(true);
                }
                default -> builder.addX(sx)
                            .meanCorrection(true);
            }
            return builder.build();
        }
    }

    /**
     * @return the e
     */
    public DoubleSeq getE() {
        return u;
    }

    private final FastMatrix x, xe, cxe, omega;
    private final DoubleSeq c, u;

    private CanovaHansen(final FastMatrix x, final LinearModel lm, final WindowFunction winFunction, int truncationLag) {
        this.x = x;
        LeastSquaresResults olsResults = Ols.compute(lm);
        c=olsResults.getCoefficients();
        u = lm.calcResiduals(c);
        xe = x.deepClone();
        // multiply the columns of x by e
        xe.applyByColumns(col -> col.apply(u, (a, b) -> a * b));
        omega = RobustCovarianceComputer.covariance(xe, winFunction, truncationLag);
        cxe = xe.deepClone();
        cxe.applyByColumns(col -> col.cumul());
    }

    public double test(int var) {
        return computeStat(omega.extract(var, 1, var, 1), cxe.extract(0, cxe.getRowsCount(), var, 1));
    }

    public double test(int var, int nvars) {
        return computeStat(omega.extract(var, nvars, var, nvars), cxe.extract(0, cxe.getRowsCount(), var, nvars));
    }

    public double testAll() {
        return computeStat(omega, cxe);
    }

    private double computeStat(FastMatrix O, FastMatrix cx) {
        int n = cx.getRowsCount(), nx = cx.getColumnsCount();
        // compute tr( O^-1*xe'*xe)
        // cusum
        FastMatrix FF = FastMatrix.square(nx);
        for (int i = 0; i < n; ++i) {
            FF.addXaXt(1, cx.row(i));
        }
        // LL'^-1 * xe2 = L'^-1* L^-1 xe2 = L'^-1*a <-> a=L^-1 xe2 <->La=xe2
        FastMatrix sig = O.deepClone();
        SymmetricMatrix.lcholesky(sig);
        LowerTriangularMatrix.solveLX(sig, FF);
        // b=L'^-1*a <-> L'b=a 
        LowerTriangularMatrix.solveLtX(sig, FF);
        double tr = FF.diagonal().sum();
        return tr / (n * n);
    }

//    private FastMatrix robustCovarianceOfCoefficients() {
//        FastMatrix Lo = omega.deepClone();
//        SymmetricMatrix.lcholesky(Lo);
//
//        FastMatrix Lx = SymmetricMatrix.XtX(x);
//        SymmetricMatrix.lcholesky(Lx);
//        LowerTriangularMatrix.solveLX(Lx, Lo);
//        LowerTriangularMatrix.solveLtX(Lx, Lo);
//
//        FastMatrix XXt = SymmetricMatrix.XXt(Lo);
//        XXt.mul(xe.getRowsCount());
//        return XXt;
//    }

}
