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
package jdplus.toolkit.base.core.math.splines;

import java.util.Arrays;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.decomposition.ElementaryTransformations;
import jdplus.toolkit.base.core.math.polynomials.UnitRoots;
import jdplus.toolkit.base.core.stats.linearmodel.LeastSquaresResults;
import jdplus.toolkit.base.core.stats.linearmodel.LinearModel;
import jdplus.toolkit.base.core.stats.linearmodel.Ols;

/**
 *
 * @author palatej
 */
public class AdaptativePeriodicSplines {

    @lombok.Value
    @lombok.Builder(builderClassName = "Builder")
    public static class Specification {

        // Points
        DoubleSeq x, y;
        // Order of the B-Splines
        int splineOrder;
        // Period of the splines
        double period;
        // Knots on which the splines are built
        double[] knots;
        // Fixed knots (can't be removed)
        int[] fixedKnots;

        double precision;
        // Selection threshold 
        double selectionThreshold;
        int maxIter, minKnots;

        public static Builder builder() {
            return new Builder()
                    .splineOrder(4)
                    .precision(1e-6)
                    .selectionThreshold(.99)
                    .maxIter(20)
                    .minKnots(0);
        }
    }

    private final Specification spec;

    private int niter;
    private int[] selectedKnots;
    private DoubleSeq a, res;
    private double aic, bic;
    private final double[] w, z;
    private final double sigma2;
    private final FastMatrix B, B2, D, LB;
    private final DataBlock By;

    AdaptativePeriodicSplines(Specification spec) {
        this.spec = spec;
        int k = spec.getSplineOrder();
        double[] knots = spec.getKnots();
        double P = spec.getPeriod();
        BSplines.BSpline bs = BSplines.periodic(k, knots, P);
        // B is the matrix of the regression variables corresponding to the splines
        B = BSplines.splines(bs, spec.getX());
        FastMatrix Bt = B.transpose();
        ElementaryTransformations.fastGivensTriangularize(Bt);
        int q = knots.length;
        DoubleSeq coeff = UnitRoots.D(1, k).coefficients();
        // Differencing matrix
        D = FastMatrix.square(q);
        for (int i = 0; i < coeff.length(); ++i) {
            D.subDiagonal(-i).set(coeff.get(i));
        }
        for (int i = 1; i < coeff.length(); ++i) {
            D.subDiagonal(q - i).set(coeff.get(i));
        }
        w = new double[q];
        for (int i = 0; i < q; ++i) {
            w[i] = 1;
        }
        int[] fixedKnots = spec.getFixedKnots();
        if (fixedKnots != null) {
            for (int i = 0; i < fixedKnots.length; ++i) {
                w[fixedKnots[i]] = 0;
            }
        }
        By = DataBlock.make(B.getColumnsCount());
        DataBlock Y = DataBlock.of(spec.y);
        By.addAProduct(1, B.columnsIterator(), Y);

        z = new double[q];
        for (int i = 0; i < q; ++i) {
            z[i] = 0;
        }
        niter = 0;
        a = DoubleSeq.onMapping(q, i -> 0);
        LB = Bt.extract(0, q, 0, q).deepClone();
        B2 = FastMatrix.make(q, 2 * q);
        B2.extract(0, q, 0, q).copy(LB);
        DataBlock A = By.deepClone();
        LowerTriangularMatrix.solveLx(LB, A);
        LowerTriangularMatrix.solvexL(LB, A);
        DataBlock e = Y.deepClone();
        e.addAProduct(-1, B.rowsIterator(), A);
        sigma2 = e.ssq() / B.getRowsCount();
    }

    public static AdaptativePeriodicSplines of(Specification spec) {
        return new AdaptativePeriodicSplines(spec);
    }

    public boolean process(double lambda) {
        int q = w.length;
        int[] fixedKnots = spec.getFixedKnots();
        int nfixed = fixedKnots == null ? 0 : fixedKnots.length;
        for (; niter < spec.getMaxIter(); ++niter) {
            FastMatrix LBp = B2.extract(0, q, 0, q);
            LBp.copy(LB);
            for (int l = 0; l < q; ++l) {
                B2.column(q + l).setAY(Math.sqrt(lambda * w[l]), D.row(l));
            }
            ElementaryTransformations.fastGivensTriangularize(B2);
            DataBlock A = By.deepClone();

            LowerTriangularMatrix.solveLx(LBp, A);
            LowerTriangularMatrix.solvexL(LBp, A);
            // New w
            for (int i = 0; i < q; ++i) {
                if (w[i] != 0) {
                    double da = D.row(i).dot(A);
                    double wcur = 1 / (da * da + 1e-10);
                    w[i] = wcur;
                    // z[i] = 0 (if da = 0) or 1 (if da >> 1.e5)
                    z[i] = da * da * wcur;
                }
            }
            double da = A.distance(a);
            a = A;
            boolean stop = da < spec.getPrecision();
            if (stop) {
                break;
            }
        }
        int n = 0;
        for (int i = 0; i < q; ++i) {
            if (z[i] >= spec.getSelectionThreshold()) {
                ++n;
            }
        }
        selectedKnots = new int[n + nfixed];
        for (int i = 0, j = 0; i < q; ++i) {
            if (z[i] >= spec.getSelectionThreshold()) {
                selectedKnots[j++] = i;
            }
        }
        for (int i = 0; i < nfixed; ++i) {
            selectedKnots[n + i] = fixedKnots[i];
        }
        n+=nfixed;
        Arrays.sort(selectedKnots);
        double[] ksel = new double[n];
        double[] knots = spec.getKnots();
        double P = spec.getPeriod();
        for (int i = 0; i < ksel.length; ++i) {
            ksel[i] = knots[selectedKnots[i]];
        }
        int k = spec.getSplineOrder();
        BSplines.BSpline bs = BSplines.periodic(k, ksel, P);
        FastMatrix Bnew = BSplines.splines(bs, spec.getX());
        LinearModel lm = LinearModel.builder()
                .y(spec.getY())
                .addX(Bnew)
                .build();
        LeastSquaresResults rslt = Ols.compute(lm);
        double ll = -0.5*rslt.getErrorSumOfSquares()/sigma2;
        
        aic = -2 * (ll - n);
        bic = -2 * ll + Math.log(B.getRowsCount()) * n;

//            if (n+nfixed < spec.minKnots || n == 0) {
//                break;
//            }
//            double ll = -0.5 * e.ssq() / sigma2;
//            // e=y-Xb
//            DataBlock e = DataBlock.of(spec.y);
//            e.addAProduct(-1, B.rowsIterator(), A);
//            res = e;
//           aic = -2 * (ll - n);
//            bic = -2 * ll + Math.log(B.getRowsCount()) * n;
        return niter < spec.maxIter;
    }

    public int[] selectedKnots() {
        return selectedKnots;
    }

    public DoubleSeq z() {
        return DoubleSeq.of(z);
    }

    public DoubleSeq a() {
        return a;
    }

    public FastMatrix B() {
        return B;
    }

    public DoubleSeq residuals() {
        return res;
    }

    public double aic() {
        return aic;
    }

    public double bic() {
        return bic;
    }

    public Specification getSpecification() {
        return spec;
    }

}
