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

import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.GeneralMatrix;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.QuadraticForm;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.ssf.ISsfInitialization;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.State;
import jdplus.toolkit.base.core.ssf.StateInfo;
import jdplus.toolkit.base.core.ssf.univariate.ISmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.ISsfData;
import jdplus.toolkit.base.core.ssf.univariate.OrdinarySmoother;

/**
 *
 * @author Jean Palate
 */
public class AugmentedSmoother {

    private ISsf ssf;
    private ISsfDynamics dynamics;
    private ISsfLoading loading;
    private ISmoothingResults srslts;
    private DefaultAugmentedFilteringResults frslts;

    // state information
    private State state;
    // contains the "states" of the diffuse effects 
    private FastMatrix A;
    // 1-step ahead errors and their variances
    private double err, errVariance;
    private DataBlock E;
    // smoothing arrays (ordinary, diffuse)
    private DataBlock r, rc;
    private FastMatrix Rd;
    // variances of the smoothing arrays;
    private FastMatrix N;
    // diffuse effects + variances
    private FastMatrix lS, Psi;
    private DataBlock delta;
    // auxiliary
    private DataBlock m;
    private FastMatrix RpNA, B;
    private boolean missing, calcvar = true;

    public boolean process(final ISsf ssf, final ISsfData data, boolean collapsing, ISmoothingResults sresults) {
        DefaultAugmentedFilteringResults fresults = AkfToolkit.filter(ssf, data, true, true);
        return process(ssf, data.length(), fresults, sresults);
    }

    public boolean process(final ISsf ssf, final ISsfData data, ISmoothingResults sresults) {
        return process(ssf, data, true, sresults);
    }

    public boolean process(ISsf ssf, final int endpos, DefaultAugmentedFilteringResults results, ISmoothingResults sresults) {
        frslts = results;
        srslts = sresults;
        initFilter(ssf);
        initSmoother(ssf);
        int t = results.getCollapsingPosition();
        QAugmentation q = results.getAugmentation();
        lS = q.choleskyS();
        delta = DataBlock.of(q.delta());
        Psi = q.Psi();
        // Psi = (lS*lS')^-1
        if (t >= endpos) {
            return processNoCollapsing(endpos);
        } else {
            ordinarySmoothing(ssf, t, endpos);
            if (t > 0) {
                A.copy(frslts.A(t));
                calcSmoothedDiffuseEffects();
                while (--t >= 0) {
                    iterate(t, true);
                    srslts.save(t, state, StateInfo.Smoothed);
                }
            }
        }
        return true;
    }

    public ISmoothingResults getResults() {
        return srslts;
    }

    private void initSmoother(ISsf ssf) {
        this.ssf=ssf;
        ISsfInitialization initialization = ssf.initialization();
        int dim = initialization.getStateDim();
        int nd = initialization.getDiffuseDim();
        state = new State(dim);
        A = FastMatrix.make(dim, nd);
        r = DataBlock.make(dim);
        rc = DataBlock.make(dim);
        m = DataBlock.make(dim);
        E = DataBlock.make(nd);
        Rd = FastMatrix.make(dim, nd);
        // auxiliary matrices
        B = FastMatrix.make(dim, nd);
        RpNA = FastMatrix.make(dim, nd);

        if (calcvar) {
            N = FastMatrix.square(dim);
        } else {
            N = null;
        }
    }

    private void loadInfo(int pos) {
        missing = frslts.isMissing(pos);
        if (!missing) {
            err = frslts.error(pos);
            errVariance = frslts.errorVariance(pos);
            E.copy(frslts.E(pos));
            m.copy(frslts.M(pos));
        }

        state.a().copy(frslts.a(pos));
        A.copy(frslts.A(pos));
        state.P().copy(frslts.P(pos));
    }

    private void iterate(int pos, boolean collapsing) {
        loadInfo(pos);
        iterateR(pos);
        // B =A + PR
        calcB();
        // a + P*r + (A+P*r)*d = a + Ad + P(r+Rd)
        updateA();
        if (calcvar) {
            // P = P-PNP
            iterateN(pos, collapsing);
            updateP(collapsing);
        }
    }

    /**
     * B =A + PR
     */
    private void calcB() {
        B.copy(A);
        GeneralMatrix.addAB(state.P(), Rd, B);
    }

    // R + NA is always 0 if no collapsing
    private void calcRpNA() {
        RpNA.copy(Rd);
        GeneralMatrix.addAB(N, A, RpNA);
    }

    private void updateA() {
        // a(t) + P*r(t-1) + (A(t)+P*r(t-1))*d
        DataBlock a = state.a();
        // normal iteration
        a.addProduct(r, state.P().columnsIterator());
        // diffuse correction
        a.addProduct(B.rowsIterator(), delta);
    }

    private void updateP(boolean collapsing) {
        // B=(A(t)+P*r(t-1)), C= r(t-1)+N(t-1)*A(t)
        // S^-1 = (lS*lS')^-1 = lS'^-1 /lS^-1
        // P(t|y)=P(t)-P(t)N(t-1)P(t)+B*psi*B' - W - W' 
        // W =P * C * lS'^-1*lS^-1 * B't
        FastMatrix P = state.P();
        FastMatrix P0 = collapsing ? P.deepClone() : null;
        // normal iteration
        FastMatrix PNP = SymmetricMatrix.XtSX(N, P);
        P.sub(PNP);
        // diffuse correction
        P.add(SymmetricMatrix.XSXt(Psi, B));
        if (collapsing) {
            calcRpNA();
            // C
            FastMatrix PC = GeneralMatrix.AB(P0, RpNA);
            // PC*L'^-1 = Y <=> PC = X L' 
            LowerTriangularMatrix.solveXLt(lS, PC);
            // L^-1 B' = X <=> B' = LX 
            FastMatrix Bt = B.transpose();
            LowerTriangularMatrix.solveLX(lS, Bt);
            FastMatrix W = GeneralMatrix.AB(PC, Bt);

            P.sub(W);
            P.subTranspose(W);
        }
        P.apply(z -> Math.abs(z) < State.ZERO ? 0 : z);
    }

    private void xL(int pos, DataBlock x) {
        // xL = x(T-KZ) = x(T-Tm/f*Z) = xT - ((xT)*m)/f * Z
        // compute xT
        dynamics.XT(pos, x);
        // compute q=xT*c
        double q = x.dot(m);
        // remove q/f*Z
        loading.XpZd(pos, x, -q / errVariance);
    }

    private void XL(int pos, DataBlockIterator X) {
        while (X.hasNext()) {
            xL(pos, X.next());
        }
    }

    /**
     *
     */
    private void iterateN(int pos, boolean collapsing) {
        if (!missing) {
            // N(t-1) = Z'(t)*Z(t)/f(t) + L'(t)*N(t)*L(t)
            XL(pos, N.rowsIterator());
            XL(pos, N.columnsIterator());
            loading.VpZdZ(pos, N, 1 / errVariance);
        } else {
            dynamics.MT(pos, N);
            dynamics.TtM(pos, N);
        }
    }

    /**
     *
     */
    private void iterateR(int pos) {
        if (!missing && errVariance != 0) {
            ssf.xL(pos, r, m, errVariance);
            ssf.XtL(pos, Rd, m, errVariance);
            loading.XpZd(pos, r, err/errVariance);
            DataBlockIterator rcols = Rd.columnsIterator();
            DoubleSeqCursor ecur = E.cursor();
            while (rcols.hasNext()) {
                loading.XpZd(pos, rcols.next(), ecur.getAndNext()/errVariance);
            }
        } else {
            dynamics.XT(pos, r);
            dynamics.MT(pos, Rd.columnsIterator());
        }
        rc.copy(r);
        rc.addProduct(Rd.rowsIterator(), delta);
        rc.apply(z -> Math.abs(z) < State.ZERO ? 0 : z);
    }

    private void initFilter(ISsf ssf) {
        dynamics = ssf.dynamics();
        loading = ssf.loading();
    }

    public void setCalcVariances(boolean b) {
        calcvar = b;
    }

    public boolean isCalcVariances() {
        return calcvar;
    }

    private void ordinarySmoothing(ISsf ssf, final int startpos, final int endpos) {
        OrdinarySmoother smoother = OrdinarySmoother
                .builder(ssf)
                .calcVariance(calcvar)
                .build();
        smoother.process(startpos, endpos, frslts, srslts);
        // updates r, N
        r.copy(smoother.getFinalR());
        rc.copy(r);
        if (calcvar) {
            N.copy(smoother.getFinalN());
        }
    }

    private void calcSmoothedDiffuseEffects() {
        // We must add to delta the correction factor
        // SA'r
        FastMatrix SA = GeneralMatrix.ABt(Psi, A);
        delta.addProduct(SA.rowsIterator(), r);

        // We must remove SA'NAS from Psi
        Psi.sub(SymmetricMatrix.XSXt(N, SA));
    }

    public DefaultAugmentedFilteringResults getFilteringResults() {
        return frslts;
    }

    private boolean processNoCollapsing(int endpos) {
        int t = endpos;
        while (--t >= 0) {
            iterate(t, false);
            srslts.save(t, state, StateInfo.Smoothed);
        }
        return true;
    }
}
