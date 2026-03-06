/*
 * Copyright 2015 National Bank copyOf Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
 *  
 * http://ec.europa.eu/idabc/eupl
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
 /*
 */
package jdplus.toolkit.base.core.ssf.univariate;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.ResultsRange;
import jdplus.toolkit.base.core.ssf.State;
import jdplus.toolkit.base.core.ssf.StateInfo;

/**
 *
 * @author Jean Palate
 */
public class OrdinarySmoother {

    public static class Builder {

        private final ISsf ssf;
        private boolean calcVariance = true;

        public Builder(ISsf ssf) {
            this.ssf = ssf;
        }

        public Builder calcVariance(boolean calc) {
            this.calcVariance = calc;
            return this;
        }

        public OrdinarySmoother build() {
            return new OrdinarySmoother(ssf, calcVariance);
        }
    }

    public static Builder builder(ISsf ssf) {
        return new Builder(ssf);
    }

    private final ISsf ssf;
    private final ISsfDynamics dynamics;
    private final ISsfLoading loading;
    private final boolean calcvar;
    private State state;
    private ISmoothingResults srslts;
    private DefaultFilteringResults frslts;

    private double err, errVariance;
    private DataBlock M, K, R;
    private FastMatrix N;
    private boolean missing;
    private int stop;

    public OrdinarySmoother(ISsf ssf, boolean calcvar) {
        this.ssf = ssf;
        this.calcvar = calcvar;
        dynamics = ssf.dynamics();
        loading = ssf.loading();
    }

    public boolean process(ISsfData data) {
        if (ssf.initialization().isDiffuse()) {
            return false;
        }
        OrdinaryFilter filter = new OrdinaryFilter();
        DefaultFilteringResults fresults = DefaultFilteringResults.full();
        if (!filter.process(ssf, data, fresults)) {
            return false;
        }
        return process(0, data.length(), fresults);
    }

    public boolean process(DefaultFilteringResults results) {
        if (ssf.initialization().isDiffuse()) {
            return false;
        }
        ResultsRange range = results.getRange();
        return process(range.getStart(), range.getEnd(), results);
    }

    public boolean process(int start, int end, DefaultFilteringResults results) {
        ISmoothingResults sresults;
        if (calcvar) {
            sresults = DefaultSmoothingResults.full();
        } else {
            sresults = DefaultSmoothingResults.light();
        }

        return process(start, end, results, sresults);
    }

    public boolean process(final int start, final int end, DefaultFilteringResults results, ISmoothingResults sresults) {
        frslts = results;
        srslts = sresults;
        stop = start;
        initSmoother(ssf);
        int t = end;
        while (--t >= stop) {
            loadInfo(t);
            if (iterate(t)) {
                srslts.save(t, state, StateInfo.Smoothed);
            }
        }

        return true;
    }

    public ISmoothingResults getResults() {
        return srslts;
    }

    public DataBlock getFinalR() {
        return R;
    }

    public FastMatrix getFinalN() {
        return N;
    }

    private void initSmoother(ISsf ssf) {
        int dim = ssf.getStateDim();
        state = new State(dim);

        R = DataBlock.make(dim);
        M = DataBlock.make(dim);
        K = DataBlock.make(dim);

        if (calcvar) {
            N = FastMatrix.square(dim);
        }
    }

    private void loadInfo(int pos) {
        err = frslts.error(pos);
        errVariance = frslts.errorVariance(pos);
        M.copy(frslts.M(pos));
        missing = !Double.isFinite(err);
    }

    private boolean iterate(int pos) {
        iterateR(pos);
        if (calcvar) {
            iterateN(pos);
        }
        DataBlock fa = frslts.a(pos);
        FastMatrix fP = frslts.P(pos);
        if (fP == null) {
            return false;
        }
        // a = a + r*P
        DataBlock a = state.a();
        a.copy(fa);
        a.addProduct(R, fP.columnsIterator());
        if (calcvar) {
            // P = P-PNP
            FastMatrix P = state.P();
            P.copy(fP);
            FastMatrix V = SymmetricMatrix.XtSX(N, P);
            P.sub(V);
        }
        return true;
    }
    // 

    /**
     *
     */
    private void iterateN(int pos) {
        if (!missing && errVariance != 0) {
            ssf.XL(pos, N, M, errVariance);
            ssf.LtX(pos, N, M, errVariance);
            loading.VpZdZ(pos, N, 1 / errVariance);
        } else {
            //T'*N(t)*T
            dynamics.MT(pos, N);
            dynamics.TtM(pos, N);
        }
        SymmetricMatrix.reenforceSymmetry(N);
        N.apply(z -> Math.abs(z) < State.ZERO ? 0 : z);
    }

    /**
     *
     */
    private void iterateR(int pos) {
        if (!missing && errVariance != 0) {
            ssf.xL(pos, R, M, errVariance);
            loading.XpZd(pos, R, err/errVariance);
        } else {
            dynamics.XT(pos, R);
        }
        R.apply(z -> Math.abs(z) < State.ZERO ? 0 : z);
    }
}
