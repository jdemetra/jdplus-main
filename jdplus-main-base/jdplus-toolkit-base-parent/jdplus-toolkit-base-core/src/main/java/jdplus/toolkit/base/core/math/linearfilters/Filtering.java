/*
 * Copyright 2023 National Bank of Belgium.
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

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import jdplus.toolkit.base.api.math.matrices.Matrix;

/**
 *
 * @author PALATEJ
 */
public class Filtering implements IFiltering {

    private final IFiniteFilter cf;
    private final IFiniteFilter[] lf;
    private final IFiniteFilter[] rf;

    public Filtering(IFiniteFilter cf, IFiniteFilter[] lf, IFiniteFilter[] rf) {
        this.cf = cf;
        this.lf = lf.clone();
        this.rf = rf.clone();
    }

    public static Filtering of(DoubleSeq cf, Matrix lf, Matrix rf) {
        int lb = lf.getColumnsCount(), rb = rf.getColumnsCount();
        if (cf.length() != lb + rb + 1) {
            throw new IllegalArgumentException();
        }
        FiniteFilter fcf = new FiniteFilter(Polynomial.of(cf.toArray()), -lb);
        IFiniteFilter[] flf = new IFiniteFilter[lb];
        for (int i = 0; i < lb; ++i) {
            flf[i] = new FiniteFilter(lfilter(lf.column(i)), i - lb + 1);
        }
        IFiniteFilter[] frf = new IFiniteFilter[rb];
        for (int i = 0; i < rb; ++i) {
            Polynomial r = rfilter(rf.column(i));
            frf[i] = new FiniteFilter(r, rb-i-r.degree()-1);
        }
        return new Filtering(fcf, flf, frf);
    }

    public static Filtering of(DoubleSeq cf, Matrix lf) {
        int l = lf.getColumnsCount();
        if (cf.length() != 2 * l + 1) {
            throw new IllegalArgumentException();
        }
        FiniteFilter fcf = new FiniteFilter(Polynomial.of(cf.toArray()), -l);
        IFiniteFilter[] flf = new IFiniteFilter[l];
        IFiniteFilter[] frf = new IFiniteFilter[l];
        for (int i = 0; i < l; ++i) {
            FiniteFilter f = new FiniteFilter(lfilter(lf.column(i)), i - l + 1);
            flf[i] = f;
            frf[i] = f.mirror();
        }
        return new Filtering(fcf, flf, frf);
    }

    private static Polynomial lfilter(DoubleSeq col) {
        int n = col.length(), pos = n - 1;
        while (pos > 0) {
            double cur = col.get(pos);
            if (cur != 0) {
                break;
            }
            --pos;
        }
        return Polynomial.raw(col.extract(0, pos + 1).toArray());
    }

    private static Polynomial rfilter(DoubleSeq col) {
        int n = col.length(), pos = 0;
        while (pos < n) {
            double cur = col.get(pos);
            if (cur != 0) {
                break;
            }
            ++pos;
        }
        return Polynomial.raw(col.range(pos, n).toArray());
    }

    @Override
    public DoubleSeq process(DoubleSeq in) {
        return FilterUtility.filter(in, cf, lf, rf);
    }

    @Override
    public IFiniteFilter centralFilter() {
        return cf;
    }

    @Override
    public IFiniteFilter[] leftEndPointsFilters() {
        return lf.clone();
    }

    @Override
    public IFiniteFilter[] rightEndPointsFilters() {
        return rf.clone();
    }

}
