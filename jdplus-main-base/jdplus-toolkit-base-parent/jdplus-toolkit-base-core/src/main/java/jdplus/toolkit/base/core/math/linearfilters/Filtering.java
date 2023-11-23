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
           double[] p = lf.column(i).drop(0, i).toArray();
            FiniteFilter f = FiniteFilter.of(p, i+1-lb);
            flf[i] = f;
        }
        IFiniteFilter[] frf = new IFiniteFilter[rb];
        int nr=rf.getRowsCount()-rb;
        for (int i = 0; i < rb; ++i) {
            double[] p = rf.column(i).drop(0, i).toArray();
            FiniteFilter f = FiniteFilter.of(p, -nr);
            frf[i] = f;
            flf[i]=f.mirror();                    
        }
        return new Filtering(fcf, flf, frf);
    }

    public static Filtering of(DoubleSeq cf, Matrix rf) {
        int nlags = rf.getColumnsCount();
        if (cf.length() != 2 * nlags + 1) {
            throw new IllegalArgumentException();
        }
        FiniteFilter fcf = new FiniteFilter(Polynomial.of(cf.toArray()), -nlags);
        IFiniteFilter[] flf = new IFiniteFilter[nlags];
        IFiniteFilter[] frf = new IFiniteFilter[nlags];
        for (int i = 0; i < nlags; ++i) {
            double[] p = rf.column(i).drop(0, i).toArray();
            FiniteFilter f = FiniteFilter.of(p, -nlags);
            frf[i] = f;
            flf[i]=f.mirror();                    
        }
        return new Filtering(fcf, flf, frf);
    }

    /**
     *
     * @param lf The columns of the matrix contains the longest to the shortest
     * filter
     * @return
     */
    public static IFiniteFilter[] leftFilters(Matrix lf) {
        int nlags = lf.getColumnsCount();
        IFiniteFilter[] flf = new IFiniteFilter[nlags];
        for (int i = 0; i < nlags; ++i) {
            double[] p = lf.column(i).drop(0, i).toArray();
            FiniteFilter f = FiniteFilter.of(p, i+1-nlags);
            flf[i] = f;
        }
        return flf;
    }

    /**
     *
     * @param rf The columns of the matrix contains the longest to the shortest
     * filter
     * @return
     */
    public static IFiniteFilter[] rightFilters(Matrix rf) {
        int nlags = rf.getColumnsCount();
        IFiniteFilter[] frf = new IFiniteFilter[nlags];
        for (int i = 0; i < nlags; ++i) {
            double[] p = rf.column(i).drop(0, i).toArray();
            FiniteFilter f = FiniteFilter.of(p, -nlags);
            frf[i] = f;
        }
        return frf;
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
