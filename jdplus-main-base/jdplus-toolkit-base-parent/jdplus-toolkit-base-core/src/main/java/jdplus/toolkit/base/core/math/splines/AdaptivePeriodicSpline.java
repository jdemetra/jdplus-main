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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.decomposition.ElementaryTransformations;
import jdplus.toolkit.base.core.math.polynomials.UnitRoots;
import jdplus.toolkit.base.core.stats.Combinatorics;
import jdplus.toolkit.base.core.stats.linearmodel.LeastSquaresResults;
import jdplus.toolkit.base.core.stats.linearmodel.LinearModel;
import jdplus.toolkit.base.core.stats.linearmodel.Ols;

/**
 *
 * @author palatej
 */
public class AdaptivePeriodicSpline {

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
        // Position of fixed knots (can't be removed)
        int[] fixedKnots;

        double precision;
        // Selection threshold 
        double selectionThreshold;
        int maxIter;

        public static Builder builder() {
            return new Builder()
                    .splineOrder(4)
                    .precision(1e-6)
                    .selectionThreshold(.99)
                    .maxIter(20);
        }

        public int getFixedKnotsCount() {
            return fixedKnots == null ? 0 : fixedKnots.length;
        }
    }

    @lombok.Value
    @lombok.Builder(builderClassName = "Builder")
    public static class Step {

        static int selectedKnotsCount(double[] z, Specification spec) {
            int[] fixedKnots = spec.getFixedKnots();
            int n = fixedKnots == null ? 0 : fixedKnots.length;
            int q = z.length;
            for (int i = 0; i < q; ++i) {
                if (z[i] >= spec.getSelectionThreshold()) {
                    ++n;
                }
            }
            return n;
        }

        static int[] selectedKnotsPosition(double[] z, Specification spec) {
            int n = selectedKnotsCount(z, spec);
            int[] selectedKnots = new int[n];
            int j = 0;
            for (int i = 0; i < z.length; ++i) {
                if (z[i] >= spec.getSelectionThreshold()) {
                    selectedKnots[j++] = i;
                }
            }
            int[] fixedKnots = spec.getFixedKnots();
            if (fixedKnots != null) {
                for (int i = 0; i < fixedKnots.length; ++i) {
                    selectedKnots[j++] = fixedKnots[i];
                }
            }
            Arrays.sort(selectedKnots);
            return selectedKnots;
        }

        static double[] selectedKnots(double[] z, Specification spec) {
            int[] sel = selectedKnotsPosition(z, spec);
            if (sel == null) {
                return null;
            }
            double[] dsel = new double[sel.length];
            double[] knots = spec.getKnots();
            for (int i = 0; i < sel.length; ++i) {
                dsel[i] = knots[sel[i]];
            }
            return dsel;
        }

        double lambda;
        /**
         * a=estimated coefficients s=B*a; w=weights of each knots approximate
         * norm0
         */
        double[] a, w, z, s;
        double aic, bic, ebic, ll;

    }

    private final Specification spec;

    private final Step step0;
    private final List<Step> steps = new ArrayList<>();

    private final double sigma2, scaling;
    private final FastMatrix B, B2, D, LB;
    private final DataBlock By;

    private Step nextStep(Step start, double lambda) {
        double[] w = start.getW().clone();
        int q = w.length;
        double[] z = new double[q];
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
        int k = spec.getSplineOrder();
        double[] selectedKnots = Step.selectedKnots(z, spec);
        if (selectedKnots.length < spec.getSplineOrder()) {
            return null;
        }
        BSplines.BSpline bs = BSplines.periodic(k, selectedKnots, spec.getPeriod());
        FastMatrix Bnew = BSplines.splines(bs, spec.getX());
        LinearModel lm = LinearModel.builder()
                .y(spec.getY())
                .addX(Bnew)
                .build();
        LeastSquaresResults rslt = Ols.compute(lm);
        double[] knots = spec.getKnots();
        DataBlock s = DataBlock.make(knots.length);
        FastMatrix Bs = BSplines.splines(bs, DoubleSeq.of(knots));
        s.product(DataBlock.of(rslt.getCoefficients()), Bs.rowsIterator());
        Step.Builder builder = Step.builder()
                .lambda(lambda)
                .a(A.getStorage())
                .w(w)
                .z(z)
                .s(s.getStorage());
        return completeStep(rslt.getErrorSumOfSquares(), spec.getY().length(), selectedKnots.length, builder);
    }

    AdaptivePeriodicSpline(Specification spec) {
        // initialization
        this.spec = spec;
        int k = spec.getSplineOrder();
        double[] knots = spec.getKnots();
        int q = knots.length;
        double P = spec.getPeriod();
        BSplines.BSpline bs = BSplines.periodic(k, knots, P);
        // B is the matrix of the regression variables corresponding to the splines
        B = BSplines.splines(bs, spec.getX());
        FastMatrix Bt = B.transpose();
        ElementaryTransformations.fastGivensTriangularize(Bt);
        // LB*LB' = B'B
        LB = Bt.extract(0, q, 0, q).deepClone();
        DoubleSeq coeff = UnitRoots.D(1, k).coefficients(); //k+1 coefficients
        // Differencing matrix
        D = FastMatrix.square(q);
        for (int i = -1; i < k; ++i) {
            D.subDiagonal(i).set(coeff.get(k - 1 - i));
        }
        D.set(0, q-1, coeff.get(k));
        for (int i = 1; i < k; ++i) {
            D.subDiagonal(i - q).set(coeff.get(i+1));
        }
        double[] w = new double[q];
        double[] z = new double[q];
        for (int i = 0; i < q; ++i) {
            w[i] = 1;
            z[i] = 1;
        }
        int[] fixedKnots = spec.getFixedKnots();
        if (fixedKnots != null) {
            for (int i = 0; i < fixedKnots.length; ++i) {
                w[fixedKnots[i]] = 0;
            }
        }
        // By
        By = DataBlock.make(B.getColumnsCount());
        DataBlock Y = DataBlock.of(spec.y);
        By.addAProduct(1, B.columnsIterator(), Y);

        B2 = FastMatrix.make(q, 2 * q);
        B2.extract(0, q, 0, q).copy(LB);
        DataBlock A = By.deepClone();
        LowerTriangularMatrix.solveLx(LB, A);
        LowerTriangularMatrix.solvexL(LB, A);
        DataBlock e = Y.deepClone();
        e.addAProduct(-1, B.rowsIterator(), A);
        sigma2 = e.ssq() / B.getRowsCount();
        DataBlock s = DataBlock.make(knots.length);
        FastMatrix Bs = BSplines.splines(bs, DoubleSeq.of(knots));
        s.product(A, Bs.rowsIterator());
        Step.Builder builder = Step.builder()
                .lambda(0)
                .a(A.getStorage())
                .s(s.getStorage())
                .w(w)
                .z(z);
        step0 = completeStep(e.ssq(), Y.length(), A.length(), builder);
        scaling = sigma2 / coeff.norm2();
    }

    private Step completeStep(double ssq, int n, int nparams, Step.Builder builder) {
        double ll = -0.5 * ssq / sigma2;
        return builder
                .ll(ll)
                .aic(2 * (-ll + nparams))
                .bic(-2 * ll + Math.log(n) * nparams)
                .ebic(-2 * ll + Math.log(n) * nparams + 2 * Combinatorics.logChoose(B.getColumnsCount(), nparams))
                .build();
    }

    public static AdaptivePeriodicSpline of(Specification spec) {
        return new AdaptivePeriodicSpline(spec);
    }

    public boolean process(double lambda) {
        steps.clear();
        int niter = 0;
        Step current = step0;
        lambda *= scaling;
        steps.add(current);

        for (; niter < spec.getMaxIter(); ++niter) {
            Step next = nextStep(current, lambda);
            if (next == null) {
                break;
            }
            if (Step.selectedKnotsCount(next.getZ(), spec) < spec.getSplineOrder()) {
                break;
            }
            double da = DoubleSeq.of(current.getA()).distance(DoubleSeq.of(next.getA()));
            if (da < spec.getPrecision()) {
                break;
            }
            if (current.getLl() != next.getLl()) {
                steps.add(current);
            }
            current = next;
        }
        return niter < spec.maxIter;
    }

    public Specification getSpecification() {
        return spec;
    }

    public int getiterationCount() {
        return steps.size();
    }

    public Step step(int i) {
        return steps.get(i);
    }

    public List<Step> allSteps() {
        return Collections.unmodifiableList(steps);
    }

    public Step result() {
        return steps.get(steps.size() - 1);
    }

    public int selectedKnotsCount(int pos) {
        double[] z = steps.get(pos).getZ();
        if (z == null) {
            return spec.knots.length;
        }
        return Step.selectedKnotsCount(z, spec);
    }

    public int[] selectedKnotsPosition(int pos) {
        double[] z = steps.get(pos).getZ();
        if (z == null) {
            return null;
        }
        return Step.selectedKnotsPosition(z, spec);
    }

    public double[] selectedKnots(int pos) {
        double[] z = steps.get(pos).getZ();
        if (z == null) {
            return null;
        }
        return Step.selectedKnots(z, spec);
    }

    public FastMatrix A() {
        FastMatrix A = FastMatrix.make(steps.size(), spec.getKnots().length);
        DataBlockIterator rows = A.rowsIterator();
        for (Step step : steps) {
            rows.next().copy(DataBlock.of(step.getA()));
        }
        return A;
    }

    public FastMatrix Z() {
        FastMatrix Z = FastMatrix.make(steps.size(), spec.getKnots().length);
        DataBlockIterator rows = Z.rowsIterator();
        for (Step step : steps) {
            rows.next().copy(DataBlock.of(step.getZ()));
        }
        return Z;
    }

    public FastMatrix W() {
        FastMatrix W = FastMatrix.make(steps.size(), spec.getKnots().length);
        DataBlockIterator rows = W.rowsIterator();
        for (Step step : steps) {
            rows.next().copy(DataBlock.of(step.getW()));
        }
        return W;
    }

    public FastMatrix S() {
        FastMatrix S = FastMatrix.make(steps.size(), spec.getKnots().length);
        DataBlockIterator rows = S.rowsIterator();
        for (Step step : steps) {
            rows.next().copy(DataBlock.of(step.getS()));
        }
        return S;
    }

}
