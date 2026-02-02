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
import jdplus.toolkit.base.core.data.LogSign;
import jdplus.toolkit.base.core.stats.likelihood.DeterminantalTerm;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.ssf.likelihood.DiffuseLikelihood;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixException;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate
 */
public class QAugmentation3 implements QAugmentation {

    private FastMatrix S, L;
    private DataBlock s;
    private double q;
    private int n, nd;
    private final DeterminantalTerm det = new DeterminantalTerm();

    @Override
    public void prepare(final int nd, final int nvars) {
        clear();
        this.nd = nd;
        S = FastMatrix.make(nd, nd);
        s = DataBlock.make(nd);
    }

    @Override
    public void clear() {
        n = 0;
        if (S != null) {
            S.set(0);
        }
        L = null;
        det.clear();
        q = 0;
        s = null;
    }

    public int getDegreesofFreedom() {
        return n - nd;
    }

    @Override
    public void update(AugmentedUpdateInformation pe) {
        double v = pe.getVariance();
        if (v == 0) {
            return; // redundant constraint
        }
        ++n;
        double e = pe.get();
        det.add(v);
        S.addXaXt(1 / v, pe.E());
        s.addAY(e / v, pe.E());
        q += e * e / v;
        L = null;
    }

    public FastMatrix S() {
        return S.extract(0, nd, 0, nd);
    }

    public DataBlock s() {
        return s;
    }

    DataBlock b() {
        FastMatrix LS=choleskyS();
        if (LS == null)
            return null;
        DataBlock b = s.deepClone();
        LowerTriangularMatrix.solveLx(LS, b);
        return b;
    }

    @Override
    public DoubleSeq delta() {
        DataBlock b = b();
        LowerTriangularMatrix.solvexL(L, b);
        return b;
    }

    @Override
    public double ssq() {
        DataBlock b = b();
        return q - b.fastNorm2();
    }

    @Override
    public int getDegreesOfFreedom(){
        return n-nd;
    }

    @Override
    public FastMatrix choleskyS() {
       if (L == null) {
            try {
                L = S.deepClone();
                SymmetricMatrix.lcholesky(L);
            } catch (MatrixException err) {
                return null;
            }
        }
        return L;
    }

    @Override
    public DiffuseLikelihood likelihood(boolean scalingfactor) {
        FastMatrix LS=choleskyS();
        if (LS == null)
            return null;
        DataBlock b = b();
        double cc = q - b.ssq();
        LogSign dsl = LogSign.of(LS.diagonal());
        double dcorr = 2 * dsl.getValue();
        return DiffuseLikelihood.builder(n, nd)
                .ssqErr(cc)
                .logDeterminant(det.getLogDeterminant())
                .diffuseCorrection(dcorr)
                .concentratedScalingFactor(scalingfactor)
                .build();
    }

    @Override
    public boolean canCollapse() {
        if (n < nd) {
            return false;
        }
        if (L == null) {
            try {
                L = S.deepClone();
                SymmetricMatrix.lcholesky(L);
                if (!QAugmentation.isWellConditioned(L.diagonal()))
                    return false;
            } catch (MatrixException err) {
                L = null;
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean collapse(AugmentedState state) {
        if (L == null) {
            try {
                L = S.deepClone();
                SymmetricMatrix.lcholesky(L);
                if (!QAugmentation.isNotNull(L.diagonal()))
                    return false;
            } catch (MatrixException err) {
                L = null;
                return false;
            }
        }

        // update the state vector
        FastMatrix A = state.A().deepClone();
        int d = A.getColumnsCount();
        LowerTriangularMatrix.solveXLt(L, A);
        DataBlock w = s.deepClone();
        LowerTriangularMatrix.solveLx(L, w);
        for (int i = 0; i < d; ++i) {
            DataBlock col = A.column(i);
            state.a().addAY(w.get(i), col);
            state.P().addXaXt(1, col);
        }
        state.dropAllConstraints();
        return true;
    }
}
