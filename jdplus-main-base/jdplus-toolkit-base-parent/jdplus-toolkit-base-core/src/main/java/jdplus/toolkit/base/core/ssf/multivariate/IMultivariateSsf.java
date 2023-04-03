/*
 * Copyright 2015 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.toolkit.base.core.ssf.multivariate;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.ISsfState;
import jdplus.toolkit.base.core.ssf.State;


/**
 *
 * @author Jean Palate
 */
public interface IMultivariateSsf extends ISsfState {

    ISsfMeasurements measurements();

    default ISsfLoading loading(int eq) {
        return measurements().loading(eq);
    }

    default ISsfErrors errors() {
        return measurements().errors();
    }

    default int measurementsCount() {
        return measurements().getCount();
    }

    default void xL(int pos, DataBlock x, FastMatrix M, FastMatrix R, int[] used) {
        // XT - [(XT)*M]R'^(-1) * Z
        // q=XT
        dynamics().XT(pos, x);
        DataBlock w = DataBlock.make(M.getColumnsCount());
        // w = qM
        w.product(x, M.columnsIterator());
        // y = wR^-1 <=> yR = w
        LowerTriangularMatrix.solvexL(R, w, State.ZERO);
        for (int i = 0; i < used.length; ++i) {
            loading(used[i]).XpZd(pos, x, -w.get(i));
        }
    }

    default void XL(int pos, FastMatrix X, FastMatrix M, FastMatrix R, int[] used) {
        // Apply XL on each row of X
        DataBlockIterator rows = X.rowsIterator();
        DataBlock w = DataBlock.make(M.getColumnsCount());
        while (rows.hasNext()) {
            DataBlock x = rows.next();
            dynamics().XT(pos, x);
            // w = qM
            w.product(x, M.columnsIterator());
            // y = wR^-1 <=> yR = w
            LowerTriangularMatrix.solvexL(R, w, State.ZERO);
            for (int i = 0; i < used.length; ++i) {
                loading(used[i]).XpZd(pos, x, -w.get(i));
            }
        }
    }

    default void XtL(int pos, FastMatrix X, FastMatrix M, FastMatrix R, int[] used) {
        // Apply XL on each column of M
        DataBlockIterator cols = X.columnsIterator();
        DataBlock w = DataBlock.make(M.getColumnsCount());
        while (cols.hasNext()) {
            DataBlock x = cols.next();
            dynamics().XT(pos, x);
            // w = qM
            w.product(x, M.columnsIterator());
            // y = wR^-1 <=> yR = w
            LowerTriangularMatrix.solvexL(R, w, State.ZERO);
            for (int i = 0; i < used.length; ++i) {
                loading(used[i]).XpZd(pos, x, -w.get(i));
            }
        }
    }
}