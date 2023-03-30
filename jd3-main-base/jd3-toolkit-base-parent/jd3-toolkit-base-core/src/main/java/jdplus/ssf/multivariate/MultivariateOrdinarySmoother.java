/*
 * Copyright 2023 National Bank of Belgium
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
package jdplus.ssf.multivariate;

import demetra.math.Constants;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.QuadraticForm;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.ResultsRange;
import jdplus.ssf.State;
import jdplus.ssf.StateInfo;
import jdplus.ssf.StateStorage;
import jdplus.ssf.univariate.ISsf;

/**
 *
 * @author Jean Palate
 */
public class MultivariateOrdinarySmoother {

    public static class Builder {

        private final IMultivariateSsf ssf;
        private boolean calcVariance = true;

        public Builder(IMultivariateSsf ssf) {
            this.ssf = ssf;
        }

        public Builder calcVariance(boolean calc) {
            this.calcVariance = calc;
            if (!calc) {
            }
            return this;
        }
        

        public MultivariateOrdinarySmoother build() {
            return new MultivariateOrdinarySmoother(ssf, calcVariance);
        }
    }

    public static Builder builder(IMultivariateSsf ssf) {
        return new Builder(ssf);
    }
    
    private final IMultivariateSsf ssf;
    private final ISsfDynamics dynamics;
    private final ISsfMeasurements measurements;
    private final boolean calcVariance;
    private int stop;

    private State state;
    private MultivariateFilteringResults frslts;
    private IMultivariateSmoothingResults srslts;
    private DataBlock r, u;

    private FastMatrix m_V;
    private IMultivariateSsfData m_data;
    private int m_pos, m_n, m_v;
    private FastMatrix m_K;
    private FastMatrix R;
    private FastMatrix N, m_Z, m_Zl;
    // 
    private DataBlock m_tmp, m_vtmp;
    private boolean[] m_missing;

    /**
     *
     * @param ssf
     * @param calcvar
     * @param stop
     */
    private MultivariateOrdinarySmoother(IMultivariateSsf ssf, boolean calcvar) {
        this.ssf = ssf;
        this.calcVariance = calcvar;
        dynamics = ssf.dynamics();
        measurements = ssf.measurements();
     }

    /**
     *
     * @return
     */
    public boolean isCalcVariance() {
        return calcVariance;
    }

    /**
     *
     * @return
     */
    public int getStopPosition() {
        return stop;
    }
    
        public boolean process(IMultivariateSsfData data) {
        if (ssf.initialization().isDiffuse()) {
            return false;
        }
        MultivariateOrdinaryFilter filter = new MultivariateOrdinaryFilter();
        MultivariateFilteringResults fresults = MultivariateFilteringResults.full();
        if (!filter.process(ssf, data, fresults)) {
            return false;
        }
        return process(0, data.getObsCount(), fresults);
    }

    public boolean process(MultivariateFilteringResults results) {
        if (ssf.initialization().isDiffuse()) {
            return false;
        }
        ResultsRange range = results.getRange();
        return process(range.getStart(), range.getEnd(), results);
    }

    public boolean process(int start, int end, MultivariateFilteringResults results) {
        int n=measurements.getCount();
        IMultivariateSmoothingResults sresults;
        if (calcVariance) {
            sresults = MultivariateSmoothingResults.full(n);
        } else {
            sresults = MultivariateSmoothingResults.light(n);
        }

        return process(start, end, results, sresults);
    }

    public boolean process(final int start, final int end, MultivariateFilteringResults results, IMultivariateSmoothingResults sresults) {
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

    private void initSmoother(IMultivariateSsf ssf) {
        int dim = ssf.getStateDim();
        state = new State(dim);

        r = DataBlock.make(dim);
 
        if (calcVariance) {
            N = FastMatrix.square(dim);
        }
    }

    private void loadInfo(int pos) {
        u = frslts.U(pos);
        R = frslts.R(pos);
        frslts.M(pos);
        missing = !Double.isFinite(err);
    }

    private boolean iterate(int pos) {
        iterateSmoothation(pos);
        iterateR(pos);
        if (calcvar) {
            iterateN(pos);
        }
        srslts.saveSmoothation(pos, u, uVariance);
        srslts.saveR(pos, R, N);
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

    private void iterateSmoothation(int pos) {
        if (missing) {
            u = Double.NaN;
            uVariance = Double.NaN;
            return;
        }
        // u = v(t)/f(t)-K'(t)*R(t)
        DataBlock k = M.deepClone();
        dynamics.TX(pos, k);
        k.div(errVariance);
        if (errVariance != 0) {
            u = err / errVariance - R.dot(k);
            // apply the same to the colums of Rd
            if (calcvar) {
                // uvar = 1/f(t)+ K'NK
                // = 1/f + 1/f*M'T'*N*T*M/f
                uVariance = 1 / errVariance + QuadraticForm.apply(N, k);
                if (uVariance < Constants.getEpsilon()) {
                    uVariance = 0;
                }
//                if (uVariance == 0) {
//                    if (Math.abs(u) < State.ZERO) {
//                        u = 0;
//                    } else {
//                        throw new SsfException(SsfException.INCONSISTENT);
//                    }
//                }
            }
        } else {
            u = 0;
            uVariance = 0;
        }
    }

    /**
     *
     */
    private void iterateN(int pos) {
        if (!missing && errVariance != 0) {
            // N(t-1) = Z'(t)*Z(t)/f(t) + L'(t)*N(t)*L(t)
            // L = T-KZ
            // N(t-1) = Z'(t)*Z(t)/f(t) + (T'(t)-Z'K')*N(t)*(T(t)-KZ)
            // Z'(t)*Z(t)(1/f(t)+K'N(t)K) + T'NT - Z'K'N(t) - NK'Z'
            ssf.XL(pos, N, M, errVariance);
            ssf.XtL(pos, N, M, errVariance);
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
        // R(t-1)=u(t)Z(t)+R(t)T'(t)
        dynamics.XT(pos, R);
        if (!missing && u != 0) {
            // RT
            loading.XpZd(pos, R, u);
        }
        R.apply(z -> Math.abs(z) < State.ZERO ? 0 : z);
    }


    /**
     *
     */
    private void initSmoother() {
        m_pos = m_data.getObsCount() - 1;
        m_n = ssf.getStateDim();
        m_v = ssf.measurementsCount();
        r = DataBlock.make(m_n);
        m_tmp = DataBlock.make(m_n);
        m_vtmp = DataBlock.make(m_v);
        m_missing = new boolean[m_v];
        m_R = FastMatrix.square(m_v);
        u = DataBlock.make(m_v);
        m_a = DataBlock.make(m_n);
        m_Z = FastMatrix.make(m_v, m_n);
        m_Zl = FastMatrix.make(m_v, m_n);
        if (calcVariance) {
            m_V = FastMatrix.square(m_n);
            N = FastMatrix.square(m_n);
        }
        srslts.prepare(m_data.getObsCount(), m_n, m_v);
    }

    /**
     *
     */
    protected void iterateSmoother() {
        if (m_pos >= stop) {
            iterateR();
            m_tmp.product(r, m_P.columnsIterator());
            m_a.add(m_tmp);
            if (calcVariance) {
                iterateN();
                SymmetricMatrix.XtSX(N, m_P, m_V);
                m_V.chs();
                m_V.add(m_P);
            }
        }
        // a = a + r*P
    }

    private void iterateR() {
        // R(t-1)=(v(t)/f(t)-R(t)*M(t))*Z(t)+R(t)*T(t)
        // R(t-1)=v(t)/f(t)*Z(t) + R(t)*L(t)
//        if (!m_bMissing && m_ff != 0) {
//            Utilities.XL(m_ssf, m_pos, m_Rf, m_Kf);
//            m_ssf.XpZd(m_pos, m_Rf, m_v / m_ff);
//        } else {
//            m_c = 0;
//            m_ssf.XT(m_pos, m_Rf);
//        }
    
        xL(r);
        LowerTriangularMatrix.solvexL(m_R, u, State.ZERO);
        for (int i = 0; i < m_v; ++i) {
            if (!m_data.isMissing(m_pos, i)) {
                measurements.loading(i).XpZd(m_pos, r, u.get(i));
            }
        }
    }

    private void iterateN() {
//            // N(t-1) = Z'(t)*R^-1*Z(t) + L'(t)*N(t)*L(t)
        LtXL(N);
        DataBlockIterator zrows = m_Z.rowsIterator(), zlrows = m_Zl.rowsIterator();
        int col = 0;
        while (zrows.hasNext() && zlrows.hasNext()) {
            DataBlock zrow = zrows.next(), zlrow = zlrows.next();
            if (!m_data.isMissing(m_pos, col)) {
                zlrow.copy(zrow);
            } else {
                zlrow.set(0);
            }
            ++col;
        }
        // R^-1*Z = Zl <-> R*Zl = Z
        LowerTriangularMatrix.solveLX(m_R, m_Zl, State.ZERO);
        for (int i = 0; i < m_v; ++i) {
            if (!m_missing[i]) {
                N.addXaXt(1, m_Zl.row(i));
            }
        }
    }

    private void loadInfo() {
        m_R.copy(frslts.R(m_pos)); // Cholesky factor
        u.copy(frslts.U(m_pos)); // e*R'^-1
        m_K = frslts.M(m_pos); // PZ'R'--1
        m_P = frslts.P(m_pos);
        if (m_a.length() != 0) {
            m_a.copy(frslts.A(m_pos));
        }
        for (int i = 0; i < m_v; ++i) {
            m_missing[i] = m_data.isMissing(m_pos, i);
        }
    }

    private void loadModelInfo() {
        measurements.Z(m_pos, m_Z);
    }

    /**
     *
     * @param ssf
     * @param data
     * @param frslts
     * @param rslts
     * @return
     */
    public boolean process(final IMultivariateSsf ssf, final IMultivariateSsfData data,
            final MultivariateFilteringResults frslts, final StateStorage rslts) {
        clear();
        this.ssf = ssf;
        dynamics = ssf.dynamics();
        measurements = ssf.measurements();
        m_data = data;
        this.frslts = frslts;
        srslts = rslts;
        initSmoother();
        if (this.ssf.isTimeInvariant()) {
            loadModelInfo();
        }
        while (m_pos >= stop) {
            if (!this.ssf.isTimeInvariant()) {
                loadModelInfo();
            }
            loadInfo();
            iterateSmoother();
            srslts.save(m_pos, m_a, m_V);
            --m_pos;
        }

        return true;
    }

    /**
     *
     * @param ssf
     * @param data
     * @param rslts
     * @return
     */
    public boolean process(final IMultivariateSsf ssf, final IMultivariateSsfData data, final StateStorage rslts) {
        this.ssf = ssf;
        dynamics = ssf.dynamics();
        measurements = ssf.measurements();
        m_data = data;
        MultivariateFilteringResults frslts = new MultivariateFilteringResults();
        frslts.setSavingM(true);
        frslts.setSavingP(true);
        MultivariateOrdinaryFilter filter = new MultivariateOrdinaryFilter();
        if (!filter.process(ssf, data, frslts)) {
            return false;
        }
        return process(ssf, data, frslts, rslts);
    }

    /**
     * Compute x*(T-KZ)=xT-(xK * Z)=xT-(xQ*R^-1 * Z)
     *
     * @param x
     */
    private void xL(DataBlock x) {
        // xQ
        m_vtmp.set(0);
        for (int i = 0; i < m_v; ++i) {
            if (!m_missing[i]) {
                m_vtmp.set(i, x.dot(m_K.column(i)));
            }
        }
        // xQ*R^-1=Y <=> xQ=YR
        LowerTriangularMatrix.solvexL(m_R, m_vtmp, State.ZERO);
        dynamics.XT(m_pos, x);
        for (int i = 0; i < m_v; ++i) {
            if (!m_missing[i]) {
                measurements.loading(i).XpZd(m_pos, x, -m_vtmp.get(i));
            }
        }
    }

    private void XL(DataBlockIterator X) {
        while (X.hasNext()) {
            xL(X.next());
        }
    }

    private void LtXL(FastMatrix X) {
        XL(X.columnsIterator());
        XL(X.rowsIterator());
    }

    /**
     *
     * @return
     */
    public MultivariateFilteringResults getFilteringResults() {
        return frslts;
    }
}
