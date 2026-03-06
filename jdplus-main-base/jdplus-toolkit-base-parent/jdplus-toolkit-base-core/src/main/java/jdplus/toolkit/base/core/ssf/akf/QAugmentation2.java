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
package jdplus.toolkit.base.core.ssf.akf;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.stats.likelihood.DeterminantalTerm;
import jdplus.toolkit.base.core.math.matrices.decomposition.ElementaryTransformations;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class QAugmentation2 implements QAugmentation{

    // Q is related to the cholesky factor of the usual "Q matrix" of De Jong.
    // Q(dj) = |S   s|
    //         |s'     q|
    // Q = |A 0|
    //     |b c|
    // so that we have:
    // q = b * b' + c * c
    // S = A * A' 
    // s = A * b'
    // s' * S^-1 * s = b * A' * S^-1 * A * b' = b * b'
    // q - s' * S^-1 * s = c * c
    // S^-1 * s = (AA')^-1 * A * b' = A'^-1 * b'
    private FastMatrix Q, A;
    private int n, nd;
    private final DeterminantalTerm det = new DeterminantalTerm();

    @Override
    public void prepare(final int nd, final int nvars, int nmax) {
        clear();
        this.nd = nd;
        Q = FastMatrix.make(nd + 1, nd + 1 + nvars);
    }

    @Override
    public void clear() {
        n = 0;
        Q = null;
        det.clear();
    }
    
    @Override
    public int n(){
        return n;
    }

    @Override
    public int nd(){
        return nd;
    }

    @Override
    public double logDeterminant(){
        return det.getLogDeterminant();
    }
    
    @Override
    public void update(AugmentedUpdateInformation pe) {
        if (pe.isMissing())
            return;
        double v = pe.getVariance();
        if (v == 0)
            return; // redundant constraint
        ++n;
        double e = pe.get();
        det.add(v);
        DataBlock col = Q.column(nd + 1);
        double se = Math.sqrt(v);
        col.range(0, nd).setAY(1 / se, pe.E());
        col.set(nd, e / se);
        ElementaryTransformations.fastGivensTriangularize(Q);
    }

    @Override
    public DoubleSeq delta() {
        DataBlock b=b().deepClone();
        LowerTriangularMatrix.solvexL(choleskyS(), b);
        b.chs();
        return b;
    }    

    DataBlock b() {
        return Q.row(nd).range(0, nd);
    }

    double c() {
        return Q.get(nd, nd);
    }
    
    @Override
    public double ssq(){
        double cc = c();
        return cc*cc;
    }
    
    @Override
    public FastMatrix choleskyS(){
        return Q.extract(0, nd, 0, nd);
    }
    
    @Override
    public boolean canCollapse() {
        if (n < nd) {
            return false;
        }
        return isWellConditioned();
    }
    
    @Override
    public boolean collapse(AugmentedState state) {
        if (!QAugmentation.isNotNull(Q.diagonal().drop(0, 1))) {
            return false;
        }
        // update the state vector
        A =state.A().deepClone();
        int d = A.getColumnsCount();
        // aC'=A' <-> Ca'=A <-> C=A*a'^-1
        LowerTriangularMatrix.solveXLt(choleskyS(), A);
        for (int i = 0; i < d; ++i) {
            DataBlock col = A.column(i);
            state.a().addAY(-Q.get(d, i), col);
            state.P().addXaXt(1, col);
        }
        state.dropAllConstraints();
        return true;
    }
 
}
