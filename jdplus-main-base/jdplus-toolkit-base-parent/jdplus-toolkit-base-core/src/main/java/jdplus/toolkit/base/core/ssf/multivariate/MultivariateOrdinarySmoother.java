/*
 * Copyright 2013-2014 National Bank of Belgium
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

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.ssf.State;
import jdplus.toolkit.base.core.ssf.StateInfo;
import jdplus.toolkit.base.core.ssf.StateStorage;
import jdplus.toolkit.base.core.ssf.UpdateInformation;

/**
 *
 * @author Jean Palate
 */
public class MultivariateOrdinarySmoother {

    public static class Builder {

        private final IMultivariateSsf ssf;
        private boolean rescaleVariance = false;
        private boolean calcVariance = true;
        private boolean calcSmoothationsVariance = true;

        public Builder(IMultivariateSsf ssf) {
            this.ssf = ssf;
        }

        public Builder calcVariance(boolean calc) {
            this.calcVariance = calc;
            if (!calc) {
                rescaleVariance = false;
            }
            return this;
        }

        public Builder calcSmoothationsVariance(boolean calc) {
            this.calcSmoothationsVariance = calc;
            return this;
        }
        
        public MultivariateOrdinarySmoother build() {
            return new MultivariateOrdinarySmoother(ssf, calcVariance, calcSmoothationsVariance);
        }
    }

    public static Builder builder(IMultivariateSsf ssf) {
        return new Builder(ssf);
    }

    private final IMultivariateSsf ssf;
    private final ISsfDynamics dynamics;
    private final ISsfMeasurements measurements;
    private final boolean calcvar, calcsvar;
    private State state;
    private StateStorage srslts;
    private MultivariateFilteringInformation frslts;
    private DoubleSeq err;
    private UpdateInformation.Status[] status;
    private int[] used;
    private DataBlock u, s;
    private DataBlock r;
    private FastMatrix M, K, R;
    private FastMatrix N, sV;
    private int stop;

    public MultivariateOrdinarySmoother(IMultivariateSsf ssf, boolean calcvar, boolean calcsvar) {
        this.ssf = ssf;
        this.calcvar = calcvar;
        this.calcsvar=calcsvar;
        dynamics = ssf.dynamics();
        measurements = ssf.measurements();
    }

    public MultivariateOrdinarySmoother(IMultivariateSsf ssf, boolean calcvar) {
        this.ssf = ssf;
        this.calcvar = calcvar;
        this.calcsvar=calcvar;
        dynamics = ssf.dynamics();
        measurements = ssf.measurements();
    }

    public boolean process(IMultivariateSsfData data) {
        if (ssf.initialization().isDiffuse()) {
            return false;
        }
        MultivariateOrdinaryFilter filter = new MultivariateOrdinaryFilter();
        MultivariateFilteringInformation fresults = new MultivariateFilteringInformation();
        if (!filter.process(ssf, data, fresults)) {
            return false;
        }
        return process(0, data.getObsCount(), fresults);
    }

    public boolean process(MultivariateFilteringInformation results) {
        if (ssf.initialization().isDiffuse()) {
            return false;
        }
        return process(0, results.size(), results);
    }

    public boolean process(int start, int end, MultivariateFilteringInformation results) {
        StateStorage sresults;
        if (calcvar) {
            sresults = StateStorage.full(StateInfo.Smoothed);
        } else {
            sresults = StateStorage.light(StateInfo.Smoothed);
        }
        sresults.prepare(ssf.getStateDim(), start, end);

        return process(start, end, results, sresults);
    }

    public boolean process(final int start, final int end, MultivariateFilteringInformation results, StateStorage sresults) {
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

    public StateStorage getSmoothingResults() {
        return srslts;
    }

    public MultivariateFilteringInformation getFilteringResults() {
        return frslts;
    }

    public DataBlock getFinalR() {
        return r;
    }

    public FastMatrix getFinalN() {
        return N;
    }

    public DoubleSeq getFinalSmoothation() {
        return s;
    }

    public FastMatrix getFinalSmoothationVariance() {
        return sV;
    }

    private void initSmoother(IMultivariateSsf ssf) {
        int dim = ssf.getStateDim();
        state = new State(dim);

        r = DataBlock.make(dim);

        if (calcvar) {
            N = FastMatrix.square(dim);
        }
    }

    private void loadInfo(int pos) {
        MultivariateUpdateInformation info = frslts.get(pos);
        if (info != null) {
            err = info.getU();
            M = info.getM();
            K = M.deepClone();
            dynamics.TM(pos, K);
            u = DataBlock.of(info.getU());
            R = info.getR();
            status = info.getStatus();
            used = info.getUsedMeasurements();
        } else {
            err = DoubleSeq.empty();
            M = FastMatrix.EMPTY;
            K = FastMatrix.EMPTY;
            u = DataBlock.EMPTY;
            R = FastMatrix.EMPTY;
            status = null;
            used = new int[0];
        }
    }

    private boolean iterate(int pos) {
        iterateSmoothation(pos);
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
        a.addProduct(r, fP.columnsIterator());
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

    private void iterateSmoothation(int pos) {
        if (u.isEmpty()) {
            return;
        }
        // s = (u-r(t)K(t))*R(t)^-1 <=> sR=(u-rK)
        s = u.deepClone();
        s.addAProduct(-1, r, K.columnsIterator());
        LowerTriangularMatrix.solvexL(R, s, State.ZERO);
        if (calcsvar) {
            // var(s) = R'^(-1)(I+K'NK)R^(-1)
            // <=> R' var(s)R = (I+K'NK)
            sV = SymmetricMatrix.XtSX(N, K);
            sV.diagonal().add(1);
            LowerTriangularMatrix.solveXL(R, sV, State.ZERO);
            LowerTriangularMatrix.solveLtX(R, sV, State.ZERO);
            SymmetricMatrix.reenforceSymmetry(sV);
            sV.apply(z -> Math.abs(z) < State.ZERO ? 0 : z);
        }
    }

    /**
     *
     */
    private void iterateN(int pos) {
        if (!R.isEmpty()) {
            // A = xl(xl(N)') (put in N)
            // N(t-1) = Z R'^-1R^-1 Z' + A
            ssf.XL(pos, N, M, R, used);
            ssf.XtL(pos, N, M, R, used);
            // we reuse M to store Z
            for (int i = 0; i < used.length; ++i) {
                measurements.loading(used[i]).Z(pos, M.column(i));
            }
            // ZR'-1 =W <=> Z = WR'
            LowerTriangularMatrix.solveXLt(R, M);
            for (int i = 0; i < used.length; ++i) {
                N.addXaXt(1, M.column(i));
            }
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
        // R(t-1)=s(t)Z(t)+R(t)T'(t)
        dynamics.XT(pos, r);
        if (status != null) {
            for (int i = 0, j = 0; i < status.length; ++i) {
                if (status[i] != UpdateInformation.Status.MISSING) {
                    double cu = s.get(j++);
                    measurements.loading(i).XpZd(pos, r, cu);
                }
            }
        }
        r.apply(z -> Math.abs(z) < State.ZERO ? 0 : z);
    }

}
