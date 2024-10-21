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
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.stats.TestType;
import jdplus.toolkit.base.api.timeseries.regression.PeriodicContrasts;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.dstats.F;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.QuadraticForm;
import jdplus.toolkit.base.core.modelling.regression.PeriodicContrastsFactory;
import jdplus.toolkit.base.core.stats.tests.TestsUtility;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class CanovaHansen {

    public static enum Variables {

        Dummy, Contrast, Trigonometric, UserDefined
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

        public Builder contrasts(int period) {
            this.type = Variables.Contrast;
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
            return new CanovaHansen(type, lm, x.getColumnsCount(), winFunction, truncationLag);
        }

        private FastMatrix sx() {
            int len = s.length();
            int pos = startPosition;
            if (lag1) {
                ++pos;
                --len;
            }
            switch (type) {
                case Dummy -> {
                    PeriodicDummies vars = new PeriodicDummies((int) period);
                    return PeriodicDummiesFactory.matrix(vars, len, pos);
                }
                case Contrast -> {
                    PeriodicContrasts vars = new PeriodicContrasts((int) period);
                    return PeriodicContrastsFactory.matrix(vars, len, pos);
                }
                case Trigonometric -> {
                    TrigonometricSeries vars = TrigonometricSeries.regular((int) period);
                    return vars.matrix(len, pos);
                }
                default -> {
                    TrigonometricSeries vars = TrigonometricSeries.all(period, nh);
                    return vars.matrix(len, pos);
                }
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
                case Dummy -> {
                    builder.addX(sx);
                }
                case Trigonometric -> {
                    builder.addX(sx)
                            .meanCorrection(true);
                }
                case Contrast -> {
                    builder.addX(sx)
                            .meanCorrection(true);
                }
                default ->
                    builder.addX(sx)
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

    private final Variables type;
    private final FastMatrix x, xe, cxe, phi;
    private final DoubleSeq c, u;
    private final int nx, ns;

    private CanovaHansen(final Variables type, final LinearModel lm, int ns, final WindowFunction winFunction, int truncationLag) {
        this.ns = ns;
        this.type=type;
        x = lm.variables();
        nx = x.getColumnsCount();
        LeastSquaresResults olsResults = Ols.compute(lm);
        c = olsResults.getCoefficients();
        u = lm.calcResiduals(c);
        // multiply the columns of x by e
        xe = x.deepClone();
        xe.applyByColumns(col -> col.apply(u, (a, b) -> a * b));
        phi = RobustCovarianceComputer.covariance(xe, winFunction, truncationLag);
        cxe = xe.deepClone();
        cxe.applyByColumns(col -> col.cumul());
    }

    public double test(int var) {
        int dx = nx - ns, dvar = var + dx;
        return computeStat(phi.get(dvar, dvar), cxe.column(dvar));
    }

    public double test(int var, int nvars) {
        int dx = nx - ns, dvar = var + dx;
        return computeStat(phi.extract(dvar, nvars, dvar, nvars), cxe.extract(0, cxe.getRowsCount(), dvar, nvars));
    }

    public double testAll() {
        return test(0, ns);
    }

    public double testDerived() {
        if (type != Variables.Contrast)
            return Double.NaN;
        int dx = nx - ns;
        FastMatrix tphi = phi.extract(dx, ns, dx, ns);
        DataBlock tc = DataBlock.of(c.extract(dx, ns));
        double v = QuadraticForm.apply(tphi, tc);
        FastMatrix ce = cxe.extract(0, cxe.getRowsCount(), dx, ns);
        DataBlock E = DataBlock.make(ce.getRowsCount());
        E.product(ce.rowsIterator(), tc);
        return computeStat(v, E);
    }
    
    public StatisticalTest seasonalityTest() {
        int dx = nx - ns;
        FastMatrix rcov = robustCovarianceOfCoefficients().extract(dx, ns, dx, ns);
        SymmetricMatrix.lcholesky(rcov);
        LowerTriangularMatrix.toLower(rcov);
        DataBlock b = DataBlock.of(c.extract(dx, ns));
        LowerTriangularMatrix.solveLx(rcov, b);
        double fval = b.ssq() / ns;
        F f = new F(ns, x.getRowsCount() - c.length());
        return TestsUtility.testOf(fval, f, TestType.Upper);
    }

    private static double computeStat(FastMatrix O, FastMatrix cx) {
        int n = cx.getRowsCount(), ncx = cx.getColumnsCount();
        // compute tr( O^-1*xe'*xe)
        // cusum
        // FF = X'X
        FastMatrix FF = FastMatrix.square(ncx);
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

    private static double computeStat(double O, DataBlock cx) {
        int n = cx.length();
        // compute tr( O^-1*xe'*xe)
        // cusum
        // F = X'X
        double F = cx.ssq();
        // LL'^-1 * xe2 = L'^-1* L^-1 xe2 = L'^-1*a <-> a=L^-1 xe2 <->La=xe2
        double tr = F/O;
        return tr / (n * n);
    }

    private FastMatrix robustCovarianceOfCoefficients() {
        FastMatrix Lo = phi.deepClone();
        SymmetricMatrix.lcholesky(Lo);
        LowerTriangularMatrix.toLower(Lo);

        FastMatrix Lx = SymmetricMatrix.XtX(x);
        SymmetricMatrix.lcholesky(Lx);
        LowerTriangularMatrix.solveLX(Lx, Lo);
        LowerTriangularMatrix.solveLtX(Lx, Lo);

        FastMatrix XXt = SymmetricMatrix.XXt(Lo);
        XXt.mul(xe.getRowsCount());
        return XXt;
    }

}
