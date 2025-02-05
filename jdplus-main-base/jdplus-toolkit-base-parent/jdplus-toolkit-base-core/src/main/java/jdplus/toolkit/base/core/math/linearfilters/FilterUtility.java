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
import jdplus.toolkit.base.core.math.ComplexComputer;
import nbbrd.design.Development;
import jdplus.toolkit.base.api.math.Complex;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import java.util.function.IntToDoubleFunction;

import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.experimental.UtilityClass
public class FilterUtility {

    final double EPS = 1e-9;

    /**
     * Checks that the absolute values of all the inverse of the given roots are
     * lower than the given limit.
     *
     * @param roots The roots
     * @param rmin The limit (positive number)
     * @return
     */
    public boolean checkRoots(final Complex[] roots, final double rmin) {
        if (roots == null || rmin < 0) {
            return true;
        }
        for (int i = 0; i < roots.length; ++i) {
            double n = (roots[i].abs());
            if (1 / n >= rmin) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks that the norm of the inverse of the roots of a given polynomial
     * are higher than rmin
     *
     * @param c The coefficients of the polynomial (excluding the constant,
     * which is considered as 1). The polynomial is 1+c(0)x+...
     * @param rmin The limit of the inverse of the roots
     * @return
     */
    public boolean checkRoots(final DoubleSeq c, final double rmin) {
        int nc = 1+c.lastIndexOf(x->x!=0); // if all are zero, nc=0
        switch (nc) {
            case 0 -> {
                return true;
            }
            case 1 -> {
                double cabs = Math.abs(c.get(0));
                return cabs < rmin;
            }
            case 2 -> {
                double a = c.get(0),
                        b = c.get(1);
                double ro = a * a - 4 * b;
                if (ro > 0) { // Roots are (-a+-sqrt(ro))/(2b)
                    double sro = Math.sqrt(ro);
                    double x0 = (-a + sro) / (2 * b), x1 = (-a - sro) / (2 * b);
                    return 1 / Math.abs(x0) < rmin && 1 / Math.abs(x1) < rmin;
                } else // Roots are (-a+-isqrt(-ro))/(2b). Abs(roots) = (1/2b)*sqrt((a*a - a*a+4*b))=1/sqr(b)
                // b is necessary positive
                {
                    return Math.sqrt(b) < rmin;
                }
            }
            default -> {
                double[] ctmp = new double[nc + 1];
                ctmp[0] = 1;
                c.copyTo(ctmp, 1);
                Polynomial p = Polynomial.ofInternal(ctmp);
                return checkRoots(p.roots(), rmin);
            }
        }
    }

    /**
     * Checks that the polynomial corresponding to the given coefficients has
     * all its roots outside the unit circle. Same as checkRoots(c, 1), but more
     * efficient.
     *
     * @param c The coefficients of the polynomial. The polynomial is
     * 1+c(0)x+...
     * @return
     *
     */
    public boolean checkStability(final DoubleSeq c) {
        int nc = c.length();
        if (nc == 0) {
            return true;
        }
        if (nc == 1) {
            return Math.abs(c.get(0)) < 1;
        }
        double[] coeff = c.toArray();
        double[] pat = new double[nc];
        double[] pu = new double[nc];
        for (int i = coeff.length - 1; i >= 0; --i) {
            pat[i] = coeff[i];
            if (Math.abs(pat[i]) >= 1) {
                return false;
            }
            for (int j = 0; j < i; ++j) {
                pu[j] = coeff[i - j - 1];
            }
            double den = 1 - pat[i] * pat[i];
            for (int j = 0; j < i / 2; ++j) {
                coeff[j] = (coeff[j] - pat[i] * pu[j]) / den;
                coeff[i - j - 1] = (coeff[i - j - 1] - pat[i] * pu[i - j - 1])
                        / den;
            }
            if (i % 2 != 0) {
                coeff[i / 2] = pu[i / 2] / (1 + pat[i]);
            }
        }
        return true;
    }

    /**
     * Checks that the given polynomial has all its roots outside the unit
     * circle.
     *
     * @param p
     * @return
     */
    public boolean checkStability(final Polynomial p) {
        double c = p.get(0);
        if (c == 0) {
            return false;
        }
        double[] q = p.coefficients().toArray();

        if (c != 1) {
            for (int i = 1; i < q.length; ++i) {
                q[i] /= c;
            }
        }
        return checkStability(DoubleSeq.of(q, 1, q.length - 1));
    }

    /**
     * /**
     * Stabilize a given polynomial (all its roots will be >= 1/rmin)
     *
     * @param c The coefficients of the polynomial (excluding the constant,
     * which is considered as 1). The polynomial is 1+c(0)x+...
     * @param rmin The limit of the inverse of the roots (all abs of the roots
     * will be >= 1/rmin)
     * @return true if some coefficients where changed, false otherwise
     */
    public boolean stabilize(DoubleSeq.Mutable c, double rmin) {
        int nc = c.length();
        if (nc == 0) {
            return false;
        }
        if (nc == 1) {
            double c0 = c.get(0);
            double rabs = Math.abs(c0);
            if (rabs < rmin) {
                return false;
            }
            if (rabs > 1 / rmin) {
                c.set(0, 1 / c0);
            } else {
                // in [rmin, 1/rmin]
                // we put it nearly on the boundary
                c.set(0, c0 > 0 ? rmin - EPS : -rmin + EPS);
            }
            return true;
        }

        double[] ctmp = new double[nc + 1];
        ctmp[0] = 1;
        c.copyTo(ctmp, 1);
        Polynomial p = Polynomial.of(ctmp);
        Polynomial sp = stabilize(p, rmin);
        if (p != sp) {
            for (int i = 0; i < nc; ++i) {
                c.set(i, sp.get(1 + i));
            }
            return true;
        }

        return false;
    }

    /**
     * Stabilize a polynomial
     *
     * @param p
     * @param rmin
     * @return A new polynomial is returned if the initial polynomial was not
     * stable (= some roots were lower than 1/rmin)
     */
    public Polynomial stabilize(Polynomial p, double rmin) {
        if (p == null) {
            return null;
        }

        Complex[] roots = p.roots();
        boolean changed = false;
        for (int i = 0; i < roots.length; ++i) {
            Complex root = roots[i];
            double n = roots[i].abs();
            if (n < 1 / rmin) {
                if (n < 1) {
                    root = root.inv();
                }
                if (n > rmin) {
                    root = root.div(rmin - EPS);
                }
                roots[i] = root;
                changed = true;
            }
        }
        if (!changed) {
            return p;
        }
        return Polynomial.fromComplexRoots(roots);
    }

    /**
     *
     * @param data
     * @return
     */
    public double[] compact(final double[] data) {
        int cur = data.length - 1;
        while (cur >= 0 && data[cur] == 0) {
            --cur;
        }
        if (cur < 0) {
            return null;
        }
        if (cur == data.length - 1) {
            return data;
        }
        double[] cdata = new double[cur + 1];
        for (int i = 0; i <= cur; ++i) {
            cdata[i] = data[i];
        }
        return cdata;
    }

    /**
     * Computes the frequency response
     * (f(w)=sum(w(t)e(iwt))=sum(w(t)(cos(wt)+i*sin(wt)))
     *
     * @param c
     * @param lb Lower bound (included)
     * @param ub Upper bound (included)
     * @param w Frequency
     * @return
     */
    public Complex frequencyResponse(final IntToDoubleFunction c, final int lb, final int ub,
            final double w) {

        double cos = Math.cos(w);
        int idx = lb;
        // sum (w(j)e(-i jw)), j: lb->ub
        Complex c0 = lb == 0 ? Complex.ONE : Complex.cart(Math.cos(w * idx), Math.sin(w * idx));
        ComplexComputer rslt = new ComplexComputer(c0)
                .mul(c.applyAsDouble(idx++));

        // computed by the iteration procedure:
        // e(-i(n+1)w)+e(-i(n-1)w)=e(-inw)*2cos w.
        // e(-i(n+1)w)=e(-inw)*2cos w -e(-i(n-1)w)
        if (idx <= ub) {
            Complex c1 = Complex.cart(Math.cos(w * idx), Math.sin(w * idx));
            rslt.addAC(c.applyAsDouble(idx++), c1);
            while (idx <= ub) {
                Complex eiw = new ComplexComputer(c1)
                        .mul(2 * cos)
                        .sub(c0)
                        .result();
                rslt.addAC(c.applyAsDouble(idx++), eiw);
                c0 = c1;
                c1 = eiw;
            }
        }

        return rslt.result();
    }

    /**
     *
     * @param data
     * @return
     */
    public double[] smooth(final double[] data) {
        return smooth(data, EPS, true);
    }

    /**
     *
     * @param data
     * @param epsilon
     * @param bcompact
     * @return
     */
    public double[] smooth(final double[] data, final double epsilon,
            final boolean bcompact) {
        for (int i = 0; i < data.length; ++i) {
            if (Math.abs(data[i]) < epsilon) {
                data[i] = 0;
            }
        }
        if (bcompact) {
            return compact(data);
        } else {
            return data;
        }
    }

    /**
     * Applies the given symmetric filter on a sequence of input. The end-points
     * are handled using given asymmetric filters or set to NaN.
     *
     * @param input The input
     * @param filter The symmetric filter
     * @param afilters The asymmetric filters used for computing the end points.
     * Same treatment on both sides. If the symmetric filters are missing
     * (=null), the end-points are set to NaN. The first asymmetric filter is
     * used for computing out[h-1] and out[n-h], the second for out[h-2],
     * out[n-h+1]...
     * @return The filtered data
     */
    public DoubleSeq filter(DoubleSeq input, final SymmetricFilter filter, final IFiniteFilter[] afilters) {
        double[] x = new double[input.length()];
        int h = filter.getUpperBound();
        DataBlock out = DataBlock.of(x, h, x.length - h);
        filter.apply(input, out);

        // apply the endpoints filters
        if (afilters != null) {
            for (int i = 0, j = h - 1, k = x.length - h, len = 2 * h; i < h; ++i, --len, --j, ++k) {
                x[j] = afilters[i].apply(input.extract(0, len).reverse());
                x[k] = afilters[i].apply(input.extract(x.length - len, len));
            }
        } else {
            for (int i = 0; i < h; ++i) {
                x[i] = Double.NaN;
                x[x.length - i - 1] = Double.NaN;
            }
        }
        return DoubleSeq.of(x);
    }

    public void inPlaceFilter(DoubleSeq input, DataBlock output, final SymmetricFilter filter, final IFiniteFilter[] afilters) {
        int h = filter.getUpperBound(), ilen = input.length();
        DataBlock out = output.drop(h, h);
        filter.apply(input, out);

        // apply the endpoints filters
        if (afilters != null) {
            for (int i = 0, j = h - 1, k = ilen - h, len = 2 * h; i < h; ++i, --len, --j, ++k) {
                output.set(j, afilters[i].apply(input.extract(0, len).reverse()));
                output.set(k, afilters[i].apply(input.extract(ilen - len, len)));
            }
        } else {
            for (int i = 0; i < h; ++i) {
                output.set(i, Double.NaN);
                output.set(ilen - i - 1, Double.NaN);
            }
        }
    }

    /**
     * Applies the given central filter on a sequence of input. The end-points
     * are handled using given asymmetric filters or set to NaN.
     *
     * @param input The input
     * @param filter The central filter
     * @param leftFilters The asymmetric filters used for computing the first
     * points.
     * @param rightFilters The asymmetric filters used for computing the first
     * points. Same treatment on both sides. If the symmetric filters are
     * missing (=null), the end-points are set to NaN. The first left asymmetric
     * filter is used for computing out[l-1], the second for out[l-2]... The
     * first right asymmetric filter is used for computing out[n-u], the second
     * for out[n-u+1]...
     * @return The filtered data
     */
    public DoubleSeq filter(DoubleSeq input, final IFiniteFilter filter, final IFiniteFilter[] leftFilters, final IFiniteFilter[] rightFilters) {
        double[] x = new double[input.length()];
        int l = -filter.getLowerBound(), u = filter.getUpperBound();
        DataBlock out = DataBlock.of(x, l, x.length - u);
        filter.apply(input, out);

        // apply the endpoints filters
        if (leftFilters != null) {
            for (int i = 0, j = l - 1; i < l; ++i, --j) {
                IFiniteFilter cur = leftFilters[i];
                x[j] = cur.apply(input.extract(j + cur.getLowerBound(), cur.length()));
            }
        } else {
            for (int i = 0; i < l; ++i) {
                x[i] = Double.NaN;
            }
        }
        if (rightFilters != null) {
            for (int i = 0, j = x.length - u; i < u; ++i, ++j) {
                IFiniteFilter cur = rightFilters[i];
                x[j] = cur.apply(input.extract(j + cur.getLowerBound(), cur.length()));
            }
        } else {
            for (int i = 0; i < l; ++i) {
                x[x.length - i - 1] = Double.NaN;
            }
        }
        return DoubleSeq.of(x);
    }

    public void inPlaceFilter(DoubleSeq input, DataBlock output, final IFiniteFilter filter, final IFiniteFilter[] leftFilters, final IFiniteFilter[] rightFilters) {
        int l = -filter.getLowerBound(), u = filter.getUpperBound(), ilen = input.length();
        DataBlock out = output.drop(l, u);
        filter.apply(input, out);

        // apply the endpoints filters
        if (leftFilters != null) {
            for (int i = 0, j = l - 1; i < l; ++i, --j) {
                IFiniteFilter cur = leftFilters[i];
                output.set(j, cur.apply(input.extract(j + cur.getLowerBound(), cur.length())));
            }
        } else {
            for (int i = 0; i < l; ++i) {
                output.set(i, Double.NaN);
            }
        }
        if (rightFilters != null) {
            for (int i = 0, j = ilen - u; i < u; ++i, ++j) {
                IFiniteFilter cur = rightFilters[i];
                output.set(j, cur.apply(input.extract(j + cur.getLowerBound(), cur.length())));
            }
        } else {
            for (int i = 0; i < l; ++i) {
                output.set(ilen - i - 1, Double.NaN);
            }
        }
    }

}
