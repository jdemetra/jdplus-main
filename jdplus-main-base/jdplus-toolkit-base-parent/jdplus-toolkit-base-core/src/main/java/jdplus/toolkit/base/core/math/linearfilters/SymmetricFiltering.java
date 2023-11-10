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
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;

/**
 *
 * @author PALATEJ
 */
public class SymmetricFiltering implements ISymmetricFiltering {

    private final SymmetricFilter cf;
    private final IFiniteFilter[] ff;

    public SymmetricFiltering(SymmetricFilter cf, IFiniteFilter[] endPoints) {
        this.cf = cf;
        this.ff = endPoints.clone();
    }

    @Override
    public SymmetricFilter centralFilter() {
        return cf;
    }

    @Override
    public IFiniteFilter[] endPointsFilters() {
        return ff;
    }

    public static SymmetricFiltering of(DoubleSeq cf, Matrix rf) {
        int l = rf.getColumnsCount();
        if (cf.length() != 2 * l + 1) {
            throw new IllegalArgumentException();
        }
        SymmetricFilter fcf = SymmetricFilter.of(cf);
        IFiniteFilter[] frf = Filtering.rightFilters(rf);
        return new SymmetricFiltering(fcf, frf);
    }

}
