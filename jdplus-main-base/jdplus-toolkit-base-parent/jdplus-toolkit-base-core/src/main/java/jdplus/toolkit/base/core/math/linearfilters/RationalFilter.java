/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.toolkit.base.core.math.linearfilters;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixException;
import nbbrd.design.Development;
import jdplus.toolkit.base.api.util.Arrays2;
import jdplus.toolkit.base.api.math.Complex;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import java.util.function.IntToDoubleFunction;
import jdplus.toolkit.base.core.math.linearsystem.LinearSystemSolver;

/**
 * Rational filters are the ratio of two filters. They are defined in different
 * ways: 1. as the ratio of two generic filters: R(F,B) = [V(B,F)]/[W(B, F)]
 * (roots larger than 1 are associated to the backward operator, roots smaller
 * than 1 are associated to the forward operator and roots equal to 1 are split
 * in backward and forward operator, the last ones, if any, must be double
 * roots) 2. as the sum of a rational filter in the backward operator and of a
 * rational filter in the forward operator: R(F,B) = V(B)/W(B) + X(F)/Y(F) 3. as
 * the ratio of the products of backward/forward filters: R(F,B) = [V(B) *
 * W(F)]/[X(B) * Y(F)] 4. as the ratio of a generic filter and the product of a
 * backward and of a forward filter: R(F,B) = [V(B,F)]/[X(B) * Y(F)] Objects of
 * this class store the representation 1 and 2 together
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.Getter
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PUBLIC)
@lombok.Builder(builderClassName = "Builder")
public final class RationalFilter implements IRationalFilter {

    final RationalBackFilter backFilter;
    final RationalForeFilter foreFilter;
    IFiniteFilter numerator, denominator;

    /**
     * Creates the filter defined by N(B)N(F) / D(B)D(F)
     *
     * @param N The polynomial at the numerator
     * @param D The polynomial at the denominator
     * @return
     */
    public static RationalFilter rationalSymmetricFilter(BackFilter N, BackFilter D) {

        Polynomial.SimplifyingTool smp = new Polynomial.SimplifyingTool();
        if (smp.simplify(N.asPolynomial(), D.asPolynomial())) {
            N = new BackFilter(smp.getLeft());
            D = new BackFilter(smp.getRight());
        }

        SymmetricFilter n = SymmetricFilter.convolutionOf(N);
        BackFilter g = n.decompose(D);
        RationalBackFilter rb = new RationalBackFilter(g, D, 0);
        return RationalFilter.builder()
                .backFilter(rb)
                .foreFilter(rb.mirror())
                .numerator(SymmetricFilter.convolutionOf(N))
                .denominator(SymmetricFilter.convolutionOf(D))
                .build();
    }

    /**
     * Creates the rational filter N1(B)N2(F)/(D1(B)D2(F))
     *
     * @param bnum Backward factor of the numerator
     * @param bdenom Backward factor of the denominator
     * @param fnum Forward factor of the numerator
     * @param fdenom Forward factor of the denominator
     * @return
     */
    public static RationalFilter of(final BackFilter bnum, final BackFilter bdenom,
            final ForeFilter fnum, final ForeFilter fdenom) {
        FiniteFilter num = FiniteFilter.multiply(bnum, fnum);
        Decomposition decomp = Decomposition.of(num, bdenom, fdenom);
        return RationalFilter.builder()
                .backFilter(decomp.getBackFilter())
                .foreFilter(decomp.getForeFilter())
                .numerator(num)
                .denominator(FiniteFilter.multiply(bdenom, fdenom))
                .build();
    }

    public static RationalFilter of(final RationalForeFilter rf) {
        return RationalFilter.builder()
                .backFilter(RationalBackFilter.ZERO)
                .foreFilter(rf)
                .numerator(new FiniteFilter(rf.getNumerator()))
                .denominator(new FiniteFilter(rf.getDenominator()))
                .build();

    }

    public static RationalFilter of(final RationalBackFilter rb) {
        return RationalFilter.builder()
                .backFilter(rb)
                .foreFilter(RationalForeFilter.ZERO)
                .numerator(new FiniteFilter(rb.getNumerator()))
                .denominator(new FiniteFilter(rb.getDenominator()))
                .build();

    }

    /**
     * Computes N / DB*DF
     *
     * @param N The numerator
     * @param DB The back filter of the denominator
     * @param DF The forward filter of the denominator
     * @return
     */
    public static RationalFilter of(final IFiniteFilter N, final BackFilter DB,
            final ForeFilter DF) {
        FiniteFilter num = new FiniteFilter(N);
        FiniteFilter denom = FiniteFilter.multiply(new FiniteFilter(DB), DF);
        Decomposition decomp = Decomposition.of(num, DB, DF);
        return RationalFilter.builder()
                .backFilter(decomp.getBackFilter())
                .foreFilter(decomp.getForeFilter())
                .numerator(num)
                .denominator(denom)
                .build();
    }

    @lombok.Getter
    @lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static class Decomposition {

        private final RationalBackFilter backFilter;
        private final RationalForeFilter foreFilter;

        /**
         * Decompose the filter N(B,F)/(Db(B)Df(F)) in Nb(B)/Db(B) + Nf(F)/Df(F)
         *
         * See Maravall, "Use and Misuses of Unobserved Components...", EUI 1993
         * or Bell, Martin, "Computation of Asymmetric Signal Extraction Filters
         * and Mean Squared Error for ARIMA Component Models", Howard
         * University, Research Report Series 2002.
         *
         * @param num
         * @param dbf
         * @param dff
         * @return The decomposition contains backFilter=Nb/Db, foreFilter=
         * Nf/Df
         */
        public static Decomposition of(final IFiniteFilter num, final BackFilter dbf,
                final ForeFilter dff) {

            int nnb0 = -num.getLowerBound();
            int nnf0 = num.getUpperBound();
            double[] nc = num.weightsToArray();
            int nnb = nnb0;
            if (nnb < 0) {
                nnb = 0;
            }
            int nnf = nnf0;
            if (nnf < 0) {
                nnf = 0;
            }

            int ndb = -dbf.getLowerBound();
            int ndf = dff.getUpperBound();

            int h = Math.max(nnf, ndf);
            int k = Math.max(nnb, ndb);
            int ne = h + k + 1;

            // h+1 unknowns for the num in F,
            // k+1 unknowns for the num in B.
            // ne equations
            if (nc.length != ne) {
                double[] ntmp = new double[ne];
                System.arraycopy(nc, 0, ntmp, k - nnb0, nc.length);
                nc = ntmp;
            }

            double[] cnb = new double[k + 1];
            double[] cnf = new double[h + 1];
            cnf[0] = 0; // we suppress 1 unknown

            double[] db = dbf.weightsToArray(), df = dff.weightsToArray();

            FastMatrix m = FastMatrix.square(ne);
            // initialisation of the matrix
            // left/up block [k+1]
            for (int i = 0; i <= ndf; ++i) {
                for (int j = 0; j <= k; ++j) {
                    m.set(i + j, j, df[i]);
                }
            }
            // right/bottom block [h]
            // first used row: -ndb+1+k
            for (int i = -ndb + 1 + k, ii = 0; ii <= ndb; ++i, ++ii) {
                for (int j = 0; j < h; ++j) {
                    m.set(i + j, j + k + 1, db[ii]);
                }
            }

            try {
                LinearSystemSolver.robustSolver().solve(m, DataBlock.of(nc));
            } catch (MatrixException e) {
                throw new LinearFilterException(
                        "Invalid decomposition of rational filter");
            }

            for (int i = 0; i <= k; ++i) {
                cnb[i] = nc[i];
            }
            for (int i = 1; i <= h; ++i) {
                cnf[i] = nc[i + k];
            }

            Arrays2.reverse(cnb);

            return new Decomposition(new RationalBackFilter(BackFilter.ofInternal(cnb), dbf, 0),
                    new RationalForeFilter(ForeFilter.ofInternal(cnf), dff, 0));
        }
    }

    /**
     * Computes the frequency response of the filter at a given frequency
     *
     * @param freq The frequency (in radians)
     * @return The frequency response
     */
    @Override
    public Complex frequencyResponse(final double freq) {
        if (numerator != null && denominator != null) {
            // it's often a symmetric filter. So, we prefer this approach
            Complex nb = numerator.frequencyResponse(freq);
            Complex nf = denominator.frequencyResponse(freq);
            return nb.div(nf);
        } else {
            Complex nb = backFilter.frequencyResponse(freq);
            Complex nf = foreFilter.frequencyResponse(freq);
            return nb.plus(nf);
        }
    }

    /**
     * Gets the denominator of the filter (see representation 1)
     *
     * @return The denominator.
     */
    @Override
    public IFiniteFilter getDenominator() {
        if (denominator == null) {
            BackFilter bfilter = backFilter.getDenominator();
            ForeFilter ffilter = foreFilter.getDenominator();
            if (bfilter.asPolynomial().equals(ffilter.asPolynomial(), 0)) {
                denominator=SymmetricFilter.convolutionOf(bfilter);
            } else {
                FiniteFilter b = new FiniteFilter(bfilter);
                FiniteFilter f = new FiniteFilter(ffilter);
                FiniteFilter d = FiniteFilter.multiply(b, f);

                //d.smooth();
                denominator = d;
            }
        }
        return denominator;
    }

    /**
     *
     * @return
     */
    public int getLBound() {
        return backFilter.getUBound();
    }

    /**
     *
     * @return
     */
    @Override
    public IFiniteFilter getNumerator() {
        if (numerator == null) {
            FiniteFilter nb = new FiniteFilter(backFilter.getNumerator());
            FiniteFilter nf = new FiniteFilter(foreFilter.getNumerator());
            FiniteFilter db = new FiniteFilter(backFilter.getDenominator());
            FiniteFilter df = new FiniteFilter(foreFilter.getDenominator());
            FiniteFilter n = FiniteFilter.add(FiniteFilter.multiply(nb, df),
                    FiniteFilter.multiply(nf, db));
            //n.smooth();
            numerator = n;
        }
        return numerator;
    }

    /**
     *
     * @return
     */
    @Override
    public RationalBackFilter getRationalBackFilter() {
        return backFilter;
    }

    /**
     *
     * @return
     */
    @Override
    public RationalForeFilter getRationalForeFilter() {
        return foreFilter;
    }

    /**
     *
     * @return
     */
    public int getUBound() {
        return foreFilter.getUBound();
    }

    /**
     *
     * @param pos
     * @return
     */
    public double weight(int pos) {
        double d = 0;
        if (pos <= backFilter.getUBound()) {
            d = backFilter.weight(pos);
        }
        if (pos >= foreFilter.getLBound()) {
            d += foreFilter.weight(pos);
        }
        return d;
    }

    public IntToDoubleFunction weights() {
        return pos -> weight(pos);
    }

    @Override
    public boolean hasLowerBound() {
        return backFilter.hasLowerBound();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasUpperBound() {
        return foreFilter.hasUpperBound();
    }

    /**
     *
     * @param n
     * @param m
     */
    public void prepare(final int n, final int m) {
        backFilter.prepare(n);
        foreFilter.prepare(m);
    }
}
