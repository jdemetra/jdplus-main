/*
 * Copyright 2013 National Bank copyOf Belgium
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
package jdplus.toolkit.base.core.ssf.univariate;

import jdplus.toolkit.base.core.ssf.UpdateInformation;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.stats.likelihood.Likelihood;
import jdplus.toolkit.base.core.stats.likelihood.ResidualsCumulator;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.ssf.IPredictionErrorDecomposition;
import jdplus.toolkit.base.core.ssf.State;
import jdplus.toolkit.base.core.ssf.StateInfo;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class PredictionErrorDecomposition implements
        IPredictionErrorDecomposition, IFilteringResults {

    protected final ResidualsCumulator cumulator = new ResidualsCumulator();
    protected DataBlock res;
    protected final boolean bres;

    /**
     *
     * @param bres
     */
    public PredictionErrorDecomposition(final boolean bres) {
        this.bres = bres;
    }

    /**
     *
     * @return
     */
    public boolean hasResiduals() {
        return bres;
    }

    public DoubleSeq errors(boolean normalized, boolean clean) {
        if (!bres || !normalized) {
            return null;
        }
        if (!clean || res.allMatch(x -> Double.isFinite(x))) {
            return res;
        } else {
            return DataBlock.select(res, x -> Double.isFinite(x));
        }
    }

    public void prepare(final ISsf ssf, final int n) {
        clear();
        if (bres) {
            res = DataBlock.make(n);
            res.set(() -> Double.NaN);
        }
    }

    /**
     *
     * @param pos
     * @return
     */
    public double residual(int pos) {
        return bres ? res.get(pos) : Double.NaN;
    }

    @Override
    public void save(final int t, final State state, final StateInfo info) {
    }

    @Override
    public void save(final int t, final UpdateInformation pe) {
        double e = pe.get();
        if (pe.getStatus() == UpdateInformation.Status.OBSERVATION) {
            cumulator.add(e, pe.getVariance());
        }
        if (bres) {
            double sd = pe.getStandardDeviation();
            if (e == 0) {
                res.set(t, 0);
            } else if (sd == 0) {
                res.set(t, Double.NaN);
            } else {
                res.set(t, e / sd);
            }
        }
    }

    @Override
    public void clear() {
        cumulator.clear();
        res = null;
    }

    @Override
    public Likelihood likelihood(boolean scalingfactor) {
        return Likelihood.builder(cumulator.getObsCount())
                .ssqErr(cumulator.getSsqErr())
                .logDeterminant(cumulator.getLogDeterminant())
                .residuals(res)
                .scalingFactor(scalingfactor)
                .build();
    }

}
