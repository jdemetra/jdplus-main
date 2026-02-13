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
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.ssf.ISsfInitialization;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.SsfException;
import jdplus.toolkit.base.core.ssf.State;
import jdplus.toolkit.base.core.ssf.StateInfo;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.ISsfData;
import jdplus.toolkit.base.core.ssf.univariate.ISsfError;

/**
 *
 * @author Jean Palate
 */
public class AugmentedFilter {

    private AugmentedState state;
    private AugmentedUpdateInformation pe;
    private ISsf ssf;
    private ISsfLoading loading;
    private ISsfError error;
    private ISsfDynamics dynamics;
    private ISsfData data;
//    private boolean missing;
    private final boolean collapsing;
    private int collapsingPos = -1;
    //private double scale;

    /**
     *
     */
    public AugmentedFilter() {
        collapsing = false;
    }

    AugmentedFilter(final boolean collapsing) {
        this.collapsing = collapsing;
    }

    private boolean error(int t) {
        if (data.isMissing(t)) {
            pe.setMissing();
            return false;
        } else {
            // computes (ZP)' in C'. Missing values are set to 0 
            // Z~m x r, P~r x r, C~r x f
            DataBlock C = pe.M();
            loading.ZM(t, state.P(), C);
            // f = ZPZ'+ h = ZC + h
            double f = loading.ZX(t, C);
            if (error != null) {
                f += error.at(t);
            }
            if (f < State.ZERO) {
                f = 0;
            }
            pe.setVariance(f);

            double y = data.get(t);
            pe.set(y - loading.ZX(t, state.a()), data.isConstraint(t));

            loading.ZM(t, state.A(), pe.E());
            pe.E().chs();
            return true;
        }
    }

    private void update() {
        double v = pe.getVariance(), e = pe.get();
        if (v == 0) {
            if (Math.abs(e) < State.ZERO) {
                return;
            } else {
                throw new SsfException(SsfException.INCONSISTENT);
            }
        }
        // P = P - M * v^-1 * M' --> Symmetric
        // a = a + M * v^-1 * e
        // A = A - M * v^-1 * tilde(E) = = A + M * v^-1 * E
        state.a().addAY(e / v, pe.M());
        DataBlockIterator acols = state.A().columnsIterator();
        DoubleSeqCursor cell = pe.E().cursor();
        while (acols.hasNext()) {
            acols.next().addAY(cell.getAndNext() / v, pe.M());
        }
        update(state.P(), v, pe.M());
    }

    /**
     *
     * @return
     */
    public AugmentedState getState() {
        return state;
    }

    int getCollapsingPosition() {
        return collapsingPos;
    }

    private boolean initState() {
        state = AugmentedState.of(ssf);
        if (state == null) {
            return false;
        }
        ISsfInitialization initialization = ssf.initialization();
        pe = new AugmentedUpdateInformation(initialization.getStateDim(), initialization.getDiffuseDim());
        return true;
    }

    /**
     *
     * @param ssf
     * @param data
     * @param rslts
     * @return
     */
    public boolean process(final ISsf ssf, final ISsfData data, final IAugmentedFilteringResults rslts) {
        this.ssf = ssf;
        loading = ssf.loading();
        error = ssf.measurementError();
        dynamics = ssf.dynamics();
        this.data = data;
        if (!initState()) {
            return false;
        }
        int t = 0, end = data.length();
        if (!collapsing) {
            collapsingPos = end;
        }
        while (t < end) {
            if (rslts != null) {
                rslts.save(t, state, StateInfo.Forecast);
            }
            if (collapse(t, rslts)) {
                break;
            }
            if (error(t)) {
                if (rslts != null) {
                    rslts.save(t, pe);
                }
                update();
            }
            if (rslts != null) {
                rslts.save(t, state, StateInfo.Concurrent);
            }
            state.next(t++, dynamics);
        }
        return true;
    }

    // P -= c*r
    private void update(FastMatrix P, double v, DataBlock C) {
        P.addXaXt(-1 / v, C);
    }

    private boolean collapse(int t, IAugmentedFilteringResults decomp) {
        if (!collapsing) {
            return false;
        }
        if (!decomp.canCollapse()) {
            return false;
        }
        // update the state vector
        if (!decomp.collapse(t, state)) {
            return false;
        }
        collapsingPos = t;
        return true;
    }

}
