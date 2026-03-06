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
import jdplus.toolkit.base.core.math.linearsystem.QRLeastSquaresSolution;
import jdplus.toolkit.base.core.math.linearsystem.QRLeastSquaresSolver;
import jdplus.toolkit.base.core.stats.likelihood.DeterminantalTerm;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixException;
import jdplus.toolkit.base.core.math.matrices.decomposition.Householder2;
import jdplus.toolkit.base.core.math.matrices.decomposition.QRDecomposition;

/**
 *
 * @author Jean Palate
 */
public class QRAugmentation implements QAugmentation {

    private FastMatrix XL;
    private DataBlock yl;
    private int n, nd;
    private final DeterminantalTerm det = new DeterminantalTerm();
    private QRLeastSquaresSolution ls;

    @Override
    public void prepare(final int nd, final int nvars, int nmax) {
        clear();
        this.nd = nd;
        XL = FastMatrix.make(nmax, nd);
        yl = DataBlock.make(nmax);
    }

    @Override
    public void clear() {
        n = 0;
        if (XL != null) {
            XL.set(0);
        }
        if (yl != null) {
            yl.set(0);
        }
        det.clear();
        ls = null;
    }

    public int getDegreesofFreedom() {
        return n - nd;
    }

    @Override
    public void update(AugmentedUpdateInformation pe) {
        if (pe.isMissing()) {
            return;
        }
        double v = pe.getVariance();
        if (v == 0) {
            return; // redundant constraint
        }
        double e = pe.get();
        det.add(v);
        double f = pe.getStandardDeviation();
        XL.row(n).addAY(1 / f, pe.E());
        yl.set(n++, e / f);
        ls = null;
    }

    @Override
    public int n() {
        return n;
    }

    @Override
    public int nd() {
        return nd;
    }

    @Override
    public double logDeterminant() {
        return det.getLogDeterminant();
    }

    boolean compute() {
        if (ls == null) {
            try {
                Householder2 hous = new Householder2();
                QRDecomposition qr = hous.decompose(XL.extract(0, n, 0, nd).deepClone());
                ls = QRLeastSquaresSolver.leastSquares(qr, yl.range(0, n), 1e-12);
            } catch (MatrixException err) {
                return false;
            }
        }
        return true;
    }

    @Override
    public FastMatrix choleskyS() {
        if (!compute()) {
            return null;
        }
        return ls.rawR().transpose();
    }

    @Override
    public double ssq() {
        if (!compute()) {
            return Double.NaN;
        }
        return ls.getSsqErr();
    }

    @Override
    public DoubleSeq delta() {
        if (!compute()) {
            return null;
        }
        return ls.getB().fn(x->-x);
    }

    @Override
    public boolean canCollapse() {
        if (n < nd) {
            return false;
        }
        if (!compute()) {
            return false;
        }
        return QAugmentation.isWellConditioned(ls.rawRDiagonal().fn(z->Math.abs(z)));
    }

    @Override
    public boolean collapse(AugmentedState state) {
        if (!compute()) {
            return false;
        }

        // update the state vector
        // update the state vector
        FastMatrix L = choleskyS();
        FastMatrix A = state.A();
        FastMatrix B = A.deepClone();
        int d = A.getColumnsCount();
        LowerTriangularMatrix.solveXLt(L, B);
        DoubleSeq w = delta();
        for (int i = 0; i < d; ++i) {
            state.a().addAY(w.get(i), A.column(i));
            state.P().addXaXt(1, B.column(i));
        }
        state.dropAllConstraints();
        return true;
    }
}
