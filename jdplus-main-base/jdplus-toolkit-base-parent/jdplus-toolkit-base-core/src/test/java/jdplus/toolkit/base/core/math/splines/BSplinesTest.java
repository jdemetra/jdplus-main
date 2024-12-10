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

import internal.toolkit.base.core.math.functions.gsl.interpolation.CubicSplines;
import jdplus.toolkit.base.api.data.DoubleSeq;
import tck.demetra.data.WeeklyData;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.analysis.DiscreteKernel;
import jdplus.toolkit.base.core.math.linearfilters.AsymmetricFiltersFactory;
import jdplus.toolkit.base.core.math.linearfilters.FilterUtility;
import jdplus.toolkit.base.core.math.linearfilters.IFiniteFilter;
import jdplus.toolkit.base.core.math.linearfilters.LocalPolynomialFilters;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import jdplus.toolkit.base.core.math.linearsystem.LinearSystemSolver;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.GeneralMatrix;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.decomposition.ElementaryTransformations;
import jdplus.toolkit.base.core.math.polynomials.UnitRoots;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author palatej
 */
public class BSplinesTest {

    public BSplinesTest() {
    }

    @Test
    public void testAugmented() {

        double[] knots = new double[]{0, 0.1, 0.2, 0.5, 0.6, 0.8, 0.9, 1};
        for (int k = 1; k <= 4; ++k) {
            BSplines.BSpline bs = BSplines.augmented(k, knots);
            FastMatrix N = BSplines.splines(bs, DoubleSeq.onMapping(101, i -> i / 100.0));
            assertTrue(N.rowSums().stream().allMatch(z -> Math.abs(z - 1) < 1e-12));
        }
    }

    @Test
    public void testPeriodic() {

        double[] knots = new double[]{0, 0.1, 0.2, 0.5, 0.6, 0.8, 0.9};
        for (int k = 1; k <= 4; ++k) {
            BSplines.BSpline bs = BSplines.periodic(k, knots, 1);
            FastMatrix N = BSplines.splines(bs, DoubleSeq.onMapping(101, i -> i / 100.0));
            assertTrue(N.rowSums().stream().allMatch(z -> Math.abs(z - 1) < 1e-12));
        }
        knots = new double[]{0.05, 0.1, 0.2, 0.5, 0.6, 0.8, 0.9};
        for (int k = 1; k <= 4; ++k) {
            BSplines.BSpline bs = BSplines.periodic(k, knots, 1);
            FastMatrix N = BSplines.splines(bs, DoubleSeq.onMapping(101, i -> i / 100.0));
            assertTrue(N.rowSums().stream().allMatch(z -> Math.abs(z - 1) < 1e-12));
        }
    }

    @Test
    public void testPeriodicInterpolation() {

        double[] knots = new double[]{0, 0.1, 0.12, 0.2, 0.3, 0.31, 0.41, 0.42, 0.5, 0.6, 0.61, 0.62, 0.63, 0.7, 0.75, 0.8, 0.9};
        DoubleSeq x = DoubleSeq.onMapping(101, i -> i / 100.0);
        int dim = knots.length;
        FastMatrix M = null, Q = null;
        long t0 = System.currentTimeMillis();
        for (int s = 0; s < 10000; ++s) {
            BSplines.BSpline bs = BSplines.periodic(4, knots, 1);
            FastMatrix N = BSplines.splines(bs, DoubleSeq.of(knots)).inv();
            M = BSplines.splines(bs, x);
            M=GeneralMatrix.AB(M, N);
 //           LinearSystemSolver.fastSolver().solve(N, M);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
//        System.out.println(M);
        t0 = System.currentTimeMillis();
        for (int s = 0; s < 10000; ++s) {
            double[] xi = new double[dim + 1];
            for (int i = 0; i < knots.length; ++i) {
                xi[i] = knots[i];
            }
            double x0 = xi[0];
            xi[dim] = x0 + 1;

            Q = FastMatrix.make(x.length(), dim);
            for (int i = 0; i < dim; ++i) {
                double[] f = new double[dim + 1];
                if (i == 0) {
                    f[0] = 1;
                    f[dim] = 1;
                } else {
                    f[i] = 1;
                }
                CubicSplines.Spline cs = CubicSplines.periodic(DoubleSeq.of(xi), DoubleSeq.of(f));
                DataBlock column = Q.column(i);
                for (int j = 0; j < Q.getRowsCount(); ++j) {
                    column.set(j, cs.applyAsDouble(x.get(j)));
                }
            }
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        assertTrue(M.minus(Q).isZero(1e-9));
    }

    @Test
    public void testPeriodic2() {

        double[] knots = new double[]{0, 1.0, 2.0, 3.5, 4.0};
        BSplines.BSpline bs = BSplines.periodic(1, knots, 5);
        double[] B0 = new double[bs.getOrder()];
        int pos0 = bs.eval(0, B0);
        double[] B1 = new double[bs.getOrder()];
        int pos1 = bs.eval(1, B1);
    }

    @Test
    public void testPeriodic3() {

        double[] knots = new double[]{0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
        for (int k = 1; k <= 4; ++k) {
            BSplines.BSpline bs = BSplines.periodic(k, knots, 1);
            FastMatrix N = BSplines.splines(bs, DoubleSeq.onMapping(100, i -> i * 0.01));
//            System.out.println(N);
//            System.out.println();
        }
    }

    public static void main(String[] arg) {
//        int q = 100;
//        long l0 = System.currentTimeMillis();
//        double[] knots = new double[q];
//        double c = 365.0 / q;
//        for (int i = 0; i < q; ++i) {
//            knots[i] = i * c;
//        }
//
//        double[] pos = new double[365 + 366 + 365];
//        int j = 0;
//        for (int i = 0; i < 365; ++i) {
//            pos[j++] = i;
//        }
//        c = 365.0 / 366.0;
//        for (int i = 0; i < 366; ++i) {
//            pos[j++] = i * c;
//        }
//        for (int i = 0; i < 365; ++i) {
//            pos[j++] = i;
//        }
//        BSplines.BSpline bs = BSplines.periodic(4, knots, 365);
//        FastMatrix B = BSplines.splines(bs, DoubleSeq.onMapping(pos.length, i -> pos[i]));
//
//        FastMatrix BtB = SymmetricMatrix.XtX(B);
//
//        DoubleSeq coeff = UnitRoots.D(1, 4).coefficients();
//        FastMatrix D = FastMatrix.square(q);
//        for (int i = 0; i < coeff.length(); ++i) {
//            D.subDiagonal(-i).set(coeff.get(i));
//        }
//        for (int i = 1; i < coeff.length(); ++i) {
//            D.subDiagonal(q - i).set(coeff.get(i));
//        }
//        double[] w = new double[q];
//        for (int i = 0; i < q; ++i) {
//            w[i] = 1;
//        }
//
//        Random rnd = new Random(0);
//        double[] y = new double[pos.length];
//        for (int i = 0; i < pos.length; ++i) {
//            y[i] = i%91 + rnd.nextDouble( 10);
//        }
//        DataBlock By = DataBlock.make(B.getColumnsCount());
//        By.addAProduct(1, B.columnsIterator(), DataBlock.of(y));
//
//        double lambda = 1000;
//            double[] z=new double[q];
//            int k=0;
//        for (; k < 20; ++k) {
//
//            FastMatrix C = FastMatrix.square(q);
//            FastMatrix W = FastMatrix.diagonal(DoubleSeq.of(w));
//            FastMatrix DtWD = SymmetricMatrix.XtSX(W, D);
//
//            C.setAY(lambda, DtWD);
//            C.add(BtB);
//
//            DataBlock A = By.deepClone();
//            SymmetricMatrix.solve(C, A);
// 
//            double del=0;
//            // New w
//            for (int i = 0; i < q; ++i) {
//                double da = D.row(i).dot(A);
//                double wcur = 1 / (da * da + 1e-10);
//                del+=(wcur-w[i])*wcur-w[i];
//                w[i]=wcur;
//                z[i]=w[i]*da*da;
//            }
//            if (del<1e-6)break;
//        }
//        System.out.println(k);
//        System.out.println(DoubleSeq.of(z));
//        long l1 = System.currentTimeMillis();
//
//        System.out.println(l1 - l0);

        double[] y = WeeklyData.US_CLAIMS;

        int q = 50;
        int k = 4;
        long l0 = System.currentTimeMillis();
        double[] knots = new double[q];
        double P = 365.25 / 7;
        double c = P / q;
        for (int i = 0; i < q; ++i) {
            knots[i] = i * c;
        }

        int nyears = 5;
        int ny = (int) (nyears * P);

//        double[] pos = new double[365 + 366 + 365];
//        int j = 0;
//        for (int i = 0; i < 365; ++i) {
//            pos[j++] = i;
//        }
//        c = 365.0 / 366.0;
//        for (int i = 0; i < 366; ++i) {
//            pos[j++] = i * c;
//        }
//        for (int i = 0; i < 365; ++i) {
//            pos[j++] = i;
//        }
        BSplines.BSpline bs = BSplines.periodic(k, knots, P);
        DoubleSeq m = DoubleSeq.onMapping(ny, i -> i * c - P * (int) ((i * c) / P));
        FastMatrix B = BSplines.splines(bs, m);
        FastMatrix Bt = B.transpose();
        ElementaryTransformations.fastGivensTriangularize(Bt);

//        FastMatrix BtB = SymmetricMatrix.XtX(B);
        DoubleSeq coeff = UnitRoots.D(1, k).coefficients();
        FastMatrix D = FastMatrix.square(q);
        for (int i = 0; i < coeff.length(); ++i) {
            D.subDiagonal(-i).set(coeff.get(i));
        }
        for (int i = 1; i < coeff.length(); ++i) {
            D.subDiagonal(q - i).set(coeff.get(i));
        }
        double[] w = new double[q];
        int nq = q / 10;
        for (int i = 0; i < q; ++i) {
            if (i % nq != 0) {
                w[i] = 0;
            }
        }
        SymmetricFilter sf = LocalPolynomialFilters.of(26, 1, DiscreteKernel.uniform(26));
        IFiniteFilter[] afilters = AsymmetricFiltersFactory.mmsreFilters(sf, 0, new double[]{1}, null);
        IFiniteFilter[] lfilters = afilters.clone();
        for (int i = 0; i < lfilters.length; ++i) {
            lfilters[i] = lfilters[i].mirror();
        }
        DoubleSeq t = FilterUtility.filter(DoubleSeq.of(y).log(), sf, lfilters, afilters);

        DataBlock Y = DataBlock.make(ny);
        Y.set(i -> Math.log(y[i]) - t.get(i));

//        System.out.println(t);
//        System.out.println(Y);
//
        DataBlock By = DataBlock.make(B.getColumnsCount());
        By.addAProduct(1, B.columnsIterator(), Y);

        double lambda = 0.005;
        double[] z = new double[q];
        for (int i = 0; i < q; ++i) {
            z[i] = 0;
        }
        int j = 0;
        FastMatrix B2 = FastMatrix.make(q, 2 * q);
        for (; j < 10; ++j) {

            B2.extract(0, q, 0, q).copy(Bt.extract(0, q, 0, q));
            for (int l = 0; l < q; ++l) {
                B2.column(q + l).setAY(Math.sqrt(lambda * w[l]), D.row(l));
            }
            ElementaryTransformations.fastGivensTriangularize(B2);
            DataBlock A = By.deepClone();
            LowerTriangularMatrix.solveLx(B2.extract(0, q, 0, q), A);
            LowerTriangularMatrix.solvexL(B2.extract(0, q, 0, q), A);
//            System.out.println(A);
//
//            FastMatrix C = FastMatrix.square(q);
//            FastMatrix W = FastMatrix.diagonal(DoubleSeq.of(w));
//            FastMatrix DtWD = SymmetricMatrix.XtSX(W, D);
//
//            C.setAY(lambda, DtWD);
//            C.add(BtB);
//
//            A = By.deepClone();
//            SymmetricMatrix.solve(C, A);
//
            System.out.println(A);
            double del = 0;
            // New w
            for (int i = 0; i < q; ++i) {
                double da = D.row(i).dot(A);
                double wcur = 1 / (da * da + 1e-10);
                double zcur = z[i];
                if (i % nq != 0) {
                    w[i] = wcur;
                    z[i] = da * da * wcur;
                    del += (z[i] - zcur) * (z[i] - zcur);
                } else {
                    z[i] = 1;
                    w[i] = 0;
                }
            }
            if (Math.sqrt(del / z.length) < 1e-6) {
                break;
            }
            System.out.println(DoubleSeq.of(z));
        }
        System.out.println(DoubleSeq.of(z));
        long l1 = System.currentTimeMillis();

        System.out.println(l1 - l0);
    }
}
