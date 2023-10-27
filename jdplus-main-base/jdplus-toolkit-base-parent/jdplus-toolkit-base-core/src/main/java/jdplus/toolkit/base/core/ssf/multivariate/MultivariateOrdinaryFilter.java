/*
 * Copyright 2013-2014 National Bank copyOf Belgium
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
package jdplus.toolkit.base.core.ssf.multivariate;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.ssf.State;
import jdplus.toolkit.base.core.ssf.StateInfo;
import jdplus.toolkit.base.core.ssf.UpdateInformation;

/**
 *
 * @author Jean Palate
 */
public class MultivariateOrdinaryFilter {

    public static interface Initializer {

        int initialize(State state, IMultivariateSsf ssf, IMultivariateSsfData data);
    }

    private final Initializer initializer;
    private State state;
    private MultivariateUpdateInformation updinfo;
    private IMultivariateSsf ssf;
    private ISsfMeasurements measurements;
    private ISsfDynamics dynamics;
    private IMultivariateSsfData data;

    /**
     *
     */
    public MultivariateOrdinaryFilter() {
        initializer = null;
    }

    /**
     *
     * @param initializer
     */
    public MultivariateOrdinaryFilter(final Initializer initializer) {
        this.initializer = initializer;
    }

    /**
     * Computes a(t+1|t), P(t+1|t) from a(t|t), P(t|t) a(t+1|t) = T(t)a(t|t)
     * P(t+1|t) = T(t)P(t|t)T'(t)
     *
     * @param pos
     */
    protected void pred(int pos) {
        dynamics.TX(pos, state.a());
        dynamics.TVT(pos, state.P());
        dynamics.addV(pos, state.P());
    }

    /**
     * Computes: e(t)=y(t) - Z(t)a(t|t-1)) F(t)=Z(t)P(t|t-1)Z'(t)+H(t) F(t) =
     * L(t)L'(t) E(t) = e(t)L'(t)^-1 K(t)= P(t|t-1)Z'(t)L'(t)^-1
     *
     * Not computed for missing values
     *
     * @param pos
     */
    protected void error(int pos) {
        int dim = ssf.getStateDim();
        UpdateInformation.Status[] status = new UpdateInformation.Status[data.getVarsCount()];
        int nv = MultivariateUpdateInformation.fillStatus(data, pos, status);
        if (nv > 0) {        // 
            FastMatrix M = FastMatrix.make(dim, nv);
            FastMatrix R = FastMatrix.square(nv);
            double[] E = new double[nv];
            // step 1:
            // computes ZPZ'; results in R
            // PZ' in M
            MZt(pos, status, state.P(), M);
            // ZPZ'
            ZM(pos, status, M, R);
            addH(pos, status, R);
            SymmetricMatrix.reenforceSymmetry(R);
            SymmetricMatrix.lcholesky(R, State.ZERO);

            // We put in M  PZ'*(ZPZ'+H)^-1/2 = PZ'*(RR')^-1/2 = PZ'(R')^-1
            // M R' = PZ' 
            LowerTriangularMatrix.solveXLt(R, M, State.ZERO);
            for (int i = 0, iv = 0; i < status.length; ++i) {
                if (status[i] != UpdateInformation.Status.MISSING) {
                    double y = data.get(pos, i);
                    E[iv++] = y - measurements.loading(i).ZX(pos, state.a());
                }
            }
            updinfo = MultivariateUpdateInformation.builder()
                    .e(DoubleSeq.of(E))
                    .M(M)
                    .R(R)
                    .status(status)
                    .build();
        } else {
            updinfo = null;
        }
    }

    /**
     * Updates the state vector and its covariance a(t|t) = a(t|t-1) + e(t)
     */
    protected void update() {
        if (updinfo == null) {
            return;
        }
        int n = updinfo.getM().getColumnsCount();
        // P = P - (M)* F^-1 *(M)' --> Symmetric
        // PZ'(LL')^-1 ZP' =PZ'L'^-1*L^-1*ZP'
        // A = a + (M)* F^-1 * v
        FastMatrix P = state.P();
        FastMatrix M = updinfo.getM();
        DoubleSeq U = updinfo.getU();
        for (int i = 0; i < n; ++i) {
            P.addXaXt(-1, M.column(i));//, state_.K.column(i));
            state.a().addAY(U.get(i), M.column(i));
        }
    }

    /**
     *
     * @return
     */
    public State getState() {
        return state;
    }

    private int initialize(IMultivariateSsf ssf, IMultivariateSsfData data) {
        this.data = data;
        this.ssf = ssf;
        measurements = ssf.measurements();
        dynamics = ssf.dynamics();
        updinfo = null;
        if (initializer == null) {
            state = State.of(ssf);
            return state == null ? -1 : 0;
        } else {
            state = new State(ssf.getStateDim());
            return initializer.initialize(state, ssf, data);
        }
    }

    /**
     *
     * @param ssf
     * @param data
     * @param rslts
     * @return
     */
    public boolean process(final IMultivariateSsf ssf, final IMultivariateSsfData data, final IMultivariateFilteringResults rslts) {
        int t = initialize(ssf, data);
        if (t < 0) {
            return false;
        }
        if (rslts != null) {
            rslts.open(ssf, this.data);
        }
        int end = data.getObsCount();
        while (t < end) {
            if (rslts != null) {
                rslts.save(t, state, StateInfo.Forecast);
            }
            error(t);
            if (rslts != null) {
                rslts.save(t, updinfo);
            }
            update();
            if (rslts != null) {
                rslts.save(t, state, StateInfo.Concurrent);
            }
            pred(t++);
        }
        if (rslts != null) {
            rslts.close();
        }
        return true;
    }

    public void compute(IMultivariateSsf ssf, int t, State state, DoubleSeq x, int[] equations) {

        // pe_L contains the Cholesky factor !!!
    }

    /**
     *
     * @param t
     * @param status
     * @param M
     * @param zm
     */
    private void ZM(int t, UpdateInformation.Status[] status, FastMatrix M, FastMatrix zm) {
        DataBlockIterator zrows = zm.rowsIterator();
        for (int i = 0; i < status.length && zrows.hasNext(); ++i) {
            if (status[i] != UpdateInformation.Status.MISSING) {
                measurements.loading(i).ZM(t, M, zrows.next());
            }
        }
    }

    /**
     *
     * @param t
     * @param status
     * @param M
     * @param zm
     */
    private void MZt(int t, UpdateInformation.Status[] status, FastMatrix M, FastMatrix zm) {
        DataBlockIterator zcols = zm.columnsIterator();
        for (int i = 0; i < status.length && zcols.hasNext(); ++i) {
            if (status[i] != UpdateInformation.Status.MISSING) {
                measurements.loading(i).MZt(t, M, zcols.next());
            }
        }
    }

    /**
     * *
     *
     * @param t The current position
     * @param errors The errors
     * @param equations The position of the measurement corresponding to
     * available observations. When we have observations for all equations, set
     * to null.
     * @param P The covariance matrix of the prediction errors
     */
    private void addH(int t, UpdateInformation.Status[] status, FastMatrix F) {
        ISsfErrors errors = measurements.errors();
        if (errors == null) {
            return;
        }
        if (F.getRowsCount() == status.length) {
            errors.addH(t, F);
        } else {
            FastMatrix H = FastMatrix.square(status.length);
            errors.H(t, H);
            DataBlock diag = H.diagonal();
            for (int i = 0, iv = 0; i < status.length; ++i) {
                if (status[i] != UpdateInformation.Status.MISSING) {
                    for (int j = 0, jv = 0; j < i; ++j) {
                        if (status[j] != UpdateInformation.Status.MISSING) {
                            double h = H.get(i, j);
                            if (h != 0) {
                                F.add(iv, jv, h);
                                F.add(jv, iv, h);
                            }
                            ++jv;
                        }
                    }
                    F.add(iv, iv, diag.get(i));
                    ++iv;
                }
            }
        }
    }

}
