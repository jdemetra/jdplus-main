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
package jdplus.ssf.multivariate;

import demetra.data.DoubleSeq;
import demetra.math.Constants;
import jdplus.data.DataBlock;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.QuadraticForm;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.State;
import jdplus.ssf.StateInfo;
import jdplus.ssf.StateStorage;
import jdplus.ssf.UpdateInformation;

/**
 *
 * @author Jean Palate
 */
public class MultivariateOrdinarySmoother {

//    public static class Builder {
//
//        private final IMultivariateSsf ssf;
//        private boolean rescaleVariance = false;
//        private boolean calcVariance = true;
//
//        public Builder(IMultivariateSsf ssf) {
//            this.ssf = ssf;
//        }
//
//        public Builder calcVariance(boolean calc) {
//            this.calcVariance = calc;
//            if (!calc) {
//                rescaleVariance = false;
//            }
//            return this;
//        }
//
//        public MultivariateOrdinarySmoother build() {
//            return new MultivariateOrdinarySmoother(ssf, calcVariance);
//        }
//    }
//
//    public static Builder builder(IMultivariateSsf ssf) {
//        return new Builder(ssf);
//    }
//
//    private final IMultivariateSsf ssf;
//    private final ISsfDynamics dynamics;
//    private final ISsfMeasurements measurements;
//    private final boolean calcvar;
//    private State state;
//    private StateStorage srslts;
//    private MultivariateUpdates frslts;
//    private DoubleSeq err;
//    private UpdateInformation.Status[] status;
//    private DataBlock u, uVariance;
//    private DataBlock r;
//    private FastMatrix K, R;
//    private FastMatrix N;
//    private int stop;
//
//    public MultivariateOrdinarySmoother(IMultivariateSsf ssf, boolean calcvar) {
//        this.ssf = ssf;
//        this.calcvar = calcvar;
//        dynamics = ssf.dynamics();
//        measurements = ssf.measurements();
//    }
//
//    public boolean process(IMultivariateSsfData data) {
//        if (ssf.initialization().isDiffuse()) {
//            return false;
//        }
//        MultivariateOrdinaryFilter filter = new MultivariateOrdinaryFilter();
//        MultivariateUpdates fresults = new MultivariateUpdates();
//        if (!filter.process(ssf, data, fresults)) {
//            return false;
//        }
//        return process(0, data.getObsCount(), fresults);
//    }
//
//    public boolean process(MultivariateUpdates results) {
//        if (ssf.initialization().isDiffuse()) {
//            return false;
//        }
//        return process(0, results.size(), results);
//    }
//
//    public boolean process(int start, int end, MultivariateUpdates results) {
//        StateStorage sresults;
//        if (calcvar) {
//            sresults = StateStorage.full(StateInfo.Smoothed);
//        } else {
//            sresults = StateStorage.light(StateInfo.Smoothed);
//        }
//
//        return process(start, end, results, sresults);
//    }
//
//    public boolean process(final int start, final int end, MultivariateUpdates results, StateStorage sresults) {
//        frslts = results;
//        srslts = sresults;
//        stop = start;
//        initSmoother(ssf);
//        int t = end;
//        while (--t >= stop) {
//            loadInfo(t);
//            if (iterate(t)) {
//                srslts.save(t, state, StateInfo.Smoothed);
//            }
//        }
//
//        return true;
//    }
//
//    public StateStorage getResults() {
//        return srslts;
//    }
//
//    public DataBlock getFinalR() {
//        return r;
//    }
//
//    public FastMatrix getFinalN() {
//        return N;
//    }
//
//    public DoubleSeq getFinalSmoothation() {
//        return u;
//    }
//
//    public DoubleSeq getFinalSmoothationVariance() {
//        return uVariance;
//    }
//
//    private void initSmoother(IMultivariateSsf ssf) {
//        int dim = ssf.getStateDim();
//        state = new State(dim);
//
//        r = DataBlock.make(dim);
//
//        if (calcvar) {
//            N = FastMatrix.square(dim);
//        }
//    }
//
//    private void loadInfo(int pos) {
//        MultivariateUpdateInformation info = frslts.get(pos);
//        err = info.getU();
//        K = info.getK();
//        R = info.getR();
//        status = info.getStatus();
//    }
//
//    private boolean iterate(int pos) {
//        iterateSmoothation(pos);
//        iterateR(pos);
//        if (calcvar) {
//            iterateN(pos);
//        }
//        DataBlock fa = frslts.a(pos);
//        FastMatrix fP = frslts.P(pos);
//        if (fP == null) {
//            return false;
//        }
//        // a = a + r*P
//        DataBlock a = state.a();
//        a.copy(fa);
//        a.addProduct(r, fP.columnsIterator());
//        if (calcvar) {
//            // P = P-PNP
//            FastMatrix P = state.P();
//            P.copy(fP);
//            FastMatrix V = SymmetricMatrix.XtSX(N, P);
//            P.sub(V);
//        }
//        return true;
//    }
//    // 
//
//    private void iterateSmoothation(int pos) {
//        if (missing) {
//            u = Double.NaN;
//            uVariance = Double.NaN;
//            return;
//        }
//        // u = v(t)/f(t)-K'(t)*R(t)
//        DataBlock k = M.deepClone();
//        dynamics.TX(pos, k);
//        k.div(errVariance);
//        if (errVariance != 0) {
//            u = err / errVariance - r.dot(k);
//            // apply the same to the colums of Rd
//            if (calcvar) {
//                // uvar = 1/f(t)+ K'NK
//                // = 1/f + 1/f*M'T'*N*T*M/f
//                uVariance = 1 / errVariance + QuadraticForm.apply(N, k);
//                if (uVariance < Constants.getEpsilon()) {
//                    uVariance = 0;
//                }
////                if (uVariance == 0) {
////                    if (Math.abs(u) < State.ZERO) {
////                        u = 0;
////                    } else {
////                        throw new SsfException(SsfException.INCONSISTENT);
////                    }
////                }
//            }
//        } else {
//            u = 0;
//            uVariance = 0;
//        }
//    }
//
//    /**
//     *
//     */
//    private void iterateN(int pos) {
//        if (!missing && errVariance != 0) {
//            // N(t-1) = Z'(t)*Z(t)/f(t) + L'(t)*N(t)*L(t)
//            // L = T-KZ
//            // N(t-1) = Z'(t)*Z(t)/f(t) + (T'(t)-Z'K')*N(t)*(T(t)-KZ)
//            // Z'(t)*Z(t)(1/f(t)+K'N(t)K) + T'NT - Z'K'N(t) - NK'Z'
//            ssf.XL(pos, N, M, errVariance);
//            ssf.XtL(pos, N, M, errVariance);
//            loading.VpZdZ(pos, N, 1 / errVariance);
//        } else {
//            //T'*N(t)*T
//            dynamics.MT(pos, N);
//            dynamics.TtM(pos, N);
//        }
//        SymmetricMatrix.reenforceSymmetry(N);
//        N.apply(z -> Math.abs(z) < State.ZERO ? 0 : z);
//    }
//
//    /**
//     *
//     */
//    private void iterateR(int pos) {
//        // R(t-1)=u(t)Z(t)+R(t)T'(t)
//        dynamics.XT(pos, r);
//        if (!missing && u != 0) {
//            // RT
//            loading.XpZd(pos, r, u);
//        }
//        r.apply(z -> Math.abs(z) < State.ZERO ? 0 : z);
//    }

}
