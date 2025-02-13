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

import jdplus.toolkit.base.api.DemetraException;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class BSplines {

    public BSpline augmented(int order, double[] breaks) {
        return BSpline.of(order, breaks);
    }

    public BSpline periodic(int order, double[] breaks, double period) {
        return BSpline.ofPeriodic(order, breaks, period);
    }

    /**
     * Computes the matrix of regression variables corresponding to the provided
     * periodic b-spline
     *
     * @param spline The periodic b-spline
     * @param pos considered positions. In the case of periodic splines
     * (period=P), it should be in [0, P[. Otherwise, it should be between the
     * first and last knots (included). Not checked
     * @return
     */
    public FastMatrix splines(BSpline spline, DoubleSeq pos) {
        int dim = spline.dimension();
        FastMatrix M = FastMatrix.make(pos.length(), dim);
        DoubleSeqCursor cursor = pos.cursor();
        double[] B = new double[spline.getOrder()];
        DataBlockIterator rows = M.rowsIterator();
        while (rows.hasNext()) {
            int left = spline.eval(cursor.getAndNext(), B);
            if (left < 0) {
                left += dim;
            }
            DataBlock row = rows.next();
            for (int i = 0; i < B.length; ++i) {
                row.set((i + left) % dim, B[i]);
            }
        }
        return M;
    }

    public static class BSpline {

        /**
         * Order of the spline
         */
        private final int k;
        /**
         * Number of internal polynomials = number of breaks-1 in the case of
         * ordinary splines or = number of breaks in the case of periodic
         * splines
         */
        private final int n;
        /**
         * Number of knots added to the beginning of the breaks
         */
        private final double[] knots;
        private final double period;

        private final double[] deltar, deltal;

        private BSpline(int k, int n, double[] knots, double period) {
            this.k = k;
            this.n = n;
            this.knots = knots;
            this.deltal = new double[k];
            this.deltar = new double[k];
            this.period = period;
        }

        public boolean isPeriodic() {
            return period != 0;
        }

        public double getPeriod() {
            return period;
        }

        public int getOrder() {
            return k;
        }

        public DoubleSeq knots() {
            return DoubleSeq.of(knots, k-1, n);
        }

        public int dimension() {
            return n;
        }

        public int eval(double x, double[] B) {
            if (x < knots[k - 1]) {
                if (!isPeriodic()) {
                    return -1;
                } else {
                    x += period;
                }
            }
            int end = isPeriodic() ? pfindInterval(x) : findInterval(x);
            if (end < k - 1) {
                return -1;
            }
            pppack_bsplvb(x, end, B);

            return end - k + 1; // first non null index
        }

        void pppack_bsplvb(final double x, int left, double[] biatx) {
            double saved;
            biatx[0] = 1.0;

            for (int j = 0; j < k - 1; ++j) {
                deltar[j] = knots[left + j + 1] - x;
                deltal[j] = x - knots[left - j];
                saved = 0.0;
                for (int i = 0; i <= j; ++i) {
                    double term = biatx[i] / (deltar[i]
                            + deltal[j - i]);
                    biatx[i] = saved + deltar[i] * term;
                    saved = deltal[j - i] * term;
                }

                biatx[j + 1] = saved;
            }
        }

        /**
         * Position of x in the knots (extended breaks). Find knot interval such
         * that knots_i <= x < knots_{i + 1} Its position in the breaks is
         * find(x)-(k-1)
         *
         *
         * @param x
         * @return
         */
        private int pfindInterval(double x) {
            int imax = k + n - 1;
            for (int i = k - 1; i < imax; i++) {
                double ti = knots[i], tip1 = knots[i + 1];
                if (ti <= x && x < tip1) {
                    return i;
                }
            }
            return imax;
        }

        private int findInterval(double x) {
            int imax = k + n - 1;
            for (int i = k - 1; i < imax; i++) {
                double ti = knots[i], tip1 = knots[i + 1];
                if (ti <= x && x < tip1) {
                    return i;
                }
                if (ti < x && x == tip1 && tip1 == knots[k + n - 2]) {
                    return i;
                }
            }
            return imax;
        }

        /* bspline_find_interval() */
        static BSpline of(int order, double[] breaks) {
            checkBreaks(breaks, 0);

            // Fill with k-1 * break[0] at the beginning
            // and k-i * break[l] at the end
            int k = order;
            int km1 = k - 1;
            int l = breaks.length;
            int n = breaks.length + km1;
            double[] knots = new double[n + km1];
            for (int i = 0; i < km1; ++i) {
                knots[i] = breaks[0];
            }
            for (int i = 0, j = km1; i < breaks.length; ++i, ++j) {
                knots[j] = breaks[i];
            }
            for (int i = n; i < knots.length; i++) {
                knots[i] = breaks[l - 1];
            }
            return new BSpline(k, l, knots, 0);
        }

        static BSpline ofPeriodic(int order, double[] breaks, double P) {
            // Fill the beginning/end with the corresponding end/beginning of the breakpoints
            checkBreaks(breaks, P);
            int k = order;
            int km1 = k - 1;
            int n = breaks.length;
            double[] knots = new double[n + 2 * km1 + 1];
            // internal points
            for (int i = 0, j = km1; i < breaks.length; ++i, ++j) {
                knots[j] = breaks[i];
            }
            // beginning
            for (int i = 0, j = n - km1; i < km1; ++i, ++j) {
                knots[i] = breaks[j] - P;
            }
            // end
            for (int i = n + km1, j = 0; i < knots.length; ++i, ++j) {
                knots[i] = breaks[j] + P;
            }
            return new BSpline(k, n, knots, P);

        }

        private static void checkBreaks(double[] breaks, double P) {
            if (P > 0) {
                for (int i = 0; i < breaks.length; ++i) {
                    if (breaks[i] < 0 || breaks[i] >= P) {
                        throw new IllegalArgumentException("Knots should be in [0, P[");
                    }
                }
            }
            for (int i = 0; i < breaks.length - 1; ++i) {
                if (breaks[i + 1] < breaks[i]) {
                    throw new DemetraException("Invalid knots in B-Spline");
                }
            }

        }
    }
}