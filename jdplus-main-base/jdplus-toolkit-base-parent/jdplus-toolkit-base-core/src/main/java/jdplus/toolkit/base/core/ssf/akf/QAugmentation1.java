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
import jdplus.toolkit.base.core.math.matrices.decomposition.ElementaryTransformations;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.ssf.likelihood.DiffuseLikelihood;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class QAugmentation1 implements QAugmentation {

    private FastMatrix S, A;
    private DataBlock s;
    private double q;
    private int n, nd;
    private final DeterminantalTerm det = new DeterminantalTerm();

    @Override
    public void prepare(final int nd, final int nvars) {
        clear();
        this.nd = nd;
        S = FastMatrix.make(nd, nd + nvars);
        s = DataBlock.make(nd);
    }

    @Override
    public void clear() {
        n = 0;
        S = null;
        det.clear();
        q = 0;
        s = null;
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
        DataBlock col = S.column(nd);
        double se = Math.sqrt(v);
        col.setAY(1 / se, pe.E());
        s.addAY(e / v, pe.E());
        q += e * e / v;
        ElementaryTransformations.fastGivensTriangularize(S);
    }

    @Override
    public FastMatrix choleskyS() {
        return S.extract(0, nd, 0, nd);
    }

    public DataBlock s() {
        return s;
    }

    @Override
    public DoubleSeq delta() {
        DataBlock b = s.deepClone();
        LowerTriangularMatrix.solveLx(S.extract(0, nd, 0, nd), b);
        return b;
    }

    @Override
    public double ssq() {
        DoubleSeq b = delta();
        return q - b.fastNorm2();
    }
    
    @Override
    public int getDegreesOfFreedom(){
        return n-nd;
    }

    /**
     * Gets the matrix of the diffuse effects used for collapsing
     *
     * @return
     */
    public FastMatrix B() {
        return A;
    }

    @Override
    public DiffuseLikelihood likelihood(boolean scalingfactor) {
        DoubleSeq b = delta();
        double cc = q - b.ssq();
        LogSign dsl = LogSign.of(S.diagonal());
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
        
        return isWellConditioned();
    }

    @Override
    public boolean collapse(AugmentedState state) {
        if (!QAugmentation.isNotNull(S.diagonal())) {
            return false;
        }

        // update the state vector
        FastMatrix W = choleskyS();
        A = state.A().deepClone();
        int d = A.getColumnsCount();
        LowerTriangularMatrix.solveXLt(W, A);
        DataBlock w = s.deepClone();
        LowerTriangularMatrix.solveLx(W, w);
        for (int i = 0; i < d; ++i) {
            DataBlock col = A.column(i);
            state.a().addAY(w.get(i), col);
            state.P().addXaXt(1, col);
        }
        state.dropAllConstraints();
        return true;
    }
}
