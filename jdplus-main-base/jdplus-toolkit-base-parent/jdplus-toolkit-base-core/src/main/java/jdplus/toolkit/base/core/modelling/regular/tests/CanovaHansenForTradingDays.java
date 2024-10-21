/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.modelling.regular.tests;

import jdplus.toolkit.base.core.data.analysis.WindowFunction;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.stats.linearmodel.Ols;
import nbbrd.design.BuilderPattern;
import jdplus.toolkit.base.core.stats.linearmodel.LinearModel;
import jdplus.toolkit.base.core.stats.RobustCovarianceComputer;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.stats.TestType;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.calendars.DayClustering;
import jdplus.toolkit.base.api.timeseries.calendars.GenericTradingDays;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.dstats.F;
import jdplus.toolkit.base.core.math.matrices.QuadraticForm;
import jdplus.toolkit.base.core.modelling.regression.GenericTradingDaysFactory;
import jdplus.toolkit.base.core.stats.linearmodel.LeastSquaresResults;
import jdplus.toolkit.base.core.stats.tests.TestsUtility;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class CanovaHansenForTradingDays {

    public static Builder test(TsData s) {
        return new Builder(s);
    }

    @BuilderPattern(CanovaHansenForTradingDays.class)
    public static class Builder {

        private final TsData s;
        private int[] differencingLags;
        private WindowFunction winFunction = WindowFunction.Bartlett;
        private int truncationLag = 15;

        private Builder(TsData s) {
            this.s = s;
        }

        public Builder differencingLags(int... lags) {
            this.differencingLags = lags;
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

        public CanovaHansenForTradingDays build() {
            FastMatrix x = sx();
            LinearModel lm = buildModel(x);
            return new CanovaHansenForTradingDays(lm, x.getColumnsCount(), winFunction, truncationLag);
        }

        private FastMatrix sx() {

            GenericTradingDays gtd = GenericTradingDays.contrasts(DayClustering.TD7);
            int n = s.length();
            FastMatrix m = FastMatrix.make(n, 6);
            GenericTradingDaysFactory.FACTORY.fill(gtd, s.getStart(), m);
            FastMatrix dm = m;
            if (differencingLags != null) {
                for (int j = 0; j < differencingLags.length; ++j) {
                    int lag = differencingLags[j];
                    if (lag > 0) {
                        FastMatrix mj = dm;
                        int nr = mj.getRowsCount(), nc = mj.getColumnsCount();
                        dm = mj.extract(lag, nr - lag, 0, nc).deepClone();
                        dm.sub(mj.extract(0, nr - lag, 0, nc));
                    }
                }
            }
            return dm;
        }

        private DoubleSeq y() {

            DoubleSeq dy = s.getValues();
            if (differencingLags != null) {
                for (int j = 0; j < differencingLags.length; ++j) {
                    int lag = differencingLags[j];
                    if (lag > 0) {
                        dy = dy.delta(lag);
                    }
                }
            }
            return dy;
        }

        private LinearModel buildModel(FastMatrix sx) {

            return LinearModel.builder()
                    .y(y())
                    .addX(sx)
                    .meanCorrection(true)
                    .build();
        }
    }

    /**
     * @return the e
     */
    public DoubleSeq getE() {
        return u;
    }

    private final FastMatrix x, xe, cxe, phi;
    private final DoubleSeq c, u;
    private final int nx, ntd;

    private CanovaHansenForTradingDays(final LinearModel lm, int ntd, final WindowFunction winFunction, int truncationLag) {
        this.ntd = ntd;
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
        int dx = nx - ntd, dvar = var + dx;
        return computeStat(phi.extract(dvar, 1, dvar, 1), cxe.extract(0, cxe.getRowsCount(), dvar, 1));
    }

    public double test(int var, int nvars) {
        int dx = nx - ntd, dvar = var + dx;
        return computeStat(phi.extract(dvar, nvars, dvar, nvars), cxe.extract(0, cxe.getRowsCount(), dvar, nvars));
    }

    public double testDerived() {
        int dx = nx - ntd;
        FastMatrix tphi = phi.extract(dx, ntd, dx, ntd);
        DataBlock tc = DataBlock.of(c.extract(dx, ntd));
        double v = QuadraticForm.apply(tphi, tc);
        FastMatrix V = FastMatrix.square(1);
        V.set(0, 0, v);
        FastMatrix ce = cxe.extract(0, cxe.getRowsCount(), dx, ntd);
        FastMatrix E = FastMatrix.make(ce.getRowsCount(), 1);
        E.column(0).product(ce.rowsIterator(), tc);
        return computeStat(V, E);
    }

    public double testAll() {
        return test(0, ntd);
    }

    private static double computeStat(FastMatrix O, FastMatrix cx) {
        int n = cx.getRowsCount(), ncx = cx.getColumnsCount();
        // compute tr( O^-1*xe'*xe)
        // cusum
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

    public StatisticalTest tdTest() {
        int dx = nx - ntd;
        FastMatrix rcov = robustCovarianceOfCoefficients().extract(dx, ntd, dx, ntd);
        SymmetricMatrix.lcholesky(rcov);
        LowerTriangularMatrix.toLower(rcov);
        DataBlock b = DataBlock.of(c.extract(dx, ntd));
        LowerTriangularMatrix.solveLx(rcov, b);
        double fval = b.ssq() / ntd;
        F f = new F(ntd, x.getRowsCount() - c.length());
        return TestsUtility.testOf(fval, f, TestType.Upper);
    }

}
