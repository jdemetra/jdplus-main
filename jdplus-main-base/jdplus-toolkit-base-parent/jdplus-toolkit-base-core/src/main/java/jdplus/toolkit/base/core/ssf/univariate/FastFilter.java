/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.ssf.univariate;

import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.core.ssf.State;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class FastFilter {

    public static interface VarianceFilterProvider {

        int size();

        boolean isMissing(int pos);

        double errorVariance(int pos);

        DataBlock M(int pos);

        public static VarianceFilterProvider of(final DefaultFilteringResults fr) {
            return new VarianceFilterProvider() {
                @Override
                public int size() {
                    return fr.size();
                }

                @Override
                public boolean isMissing(int pos) {
                    return fr.isMissing(pos);
                }

                @Override
                public double errorVariance(int pos) {
                    return fr.errorVariance(pos);
                }

                @Override
                public DataBlock M(int pos) {
                    return fr.M(pos);
                }

            };
        }
    }
    private final VarianceFilterProvider vf;
    private final ISsf ssf;
    private final ISsfLoading loading;
    private final ISsfDynamics dynamics;
    private FastMatrix states;
    // temporaries
    private DataBlock tmp;
    private DataBlockIterator scols;

    ;
    // temporaries// temporaries


    public FastFilter(ISsf ssf, DefaultFilteringResults frslts) {
        this.ssf = ssf;
        this.vf = VarianceFilterProvider.of(frslts);
        loading = ssf.measurement().loading();
        dynamics = ssf.dynamics();
    }

    public boolean filter(FastMatrix x) {
        int n = vf.size();
        int m = x.getRowsCount();
        if (n < m) {
            return false;
        }
        int dim = ssf.getStateDim();
        states = FastMatrix.make(dim, x.getColumnsCount());
        prepareTmp();
        DataBlockIterator rows = x.rowsIterator();
        int pos = 0;
        while (pos < m && rows.hasNext()) {
            iterate(pos++, rows.next());
        }
        return true;
    }

    private void prepareTmp() {
        int nvars = states.getColumnsCount();
        tmp = DataBlock.make(nvars);
        scols = states.columnsIterator();
    }

    private void iterate(int i, DataBlock row) {
        // retrieve the current information
        boolean missing = vf.isMissing(i);
        double f = vf.errorVariance(i);
        loading.ZM(i, states, tmp);
        row.sub(tmp);
        // update the states
        if (f > 0) {
            if (!missing) {
                DataBlock C = vf.M(i);
                // process by column
                scols.reset();
                DoubleSeqCursor r = row.cursor();
                while (scols.hasNext()) {
                    scols.next().addAY(r.getAndNext() / f, C);
                }
            }
            row.mul(1 / Math.sqrt(f));
        } else {
            row.apply(q -> Math.abs(q) > State.ZERO ? Double.NaN : 0);
        }
        dynamics.TM(i, states);
        //  
    }

    public void apply(DoubleSeq in, DataBlock out) {
        int dim = ssf.getStateDim(), n = in.length();
        DataBlock state = DataBlock.make(dim);
        int pos = 0, opos = 0;
        do {
            boolean missing = vf.isMissing(pos);
            if (!missing) {
                double f = vf.errorVariance(pos);
                double e = in.get(pos) - loading.ZX(pos, state);
                if (f > 0) {
                    out.set(opos++, e / Math.sqrt(f));
                    // update the state
                    DataBlock C = vf.M(pos);
                    // process by column
                    state.addAY(e / f, C);
                } else {
                    out.set(opos++, Math.abs(e) > State.ZERO ? Double.NaN : 0);
                }
            }
            dynamics.TX(pos++, state);
        } while (pos < n);
    }

    public int getOutputLength(int inputLength) {
        int n = 0;
        int imax = inputLength;
        int end = vf.size();
        if (imax > end) {
            return -1;
        }
        for (int i = 0; i < imax; ++i) {
            double v = vf.errorVariance(i);
            if (!vf.isMissing(i) && v != 0) {
                ++n;
            }
        }
        return n;
    }
}
