/*
 * Copyright 2013 National Bank of Belgium
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
import jdplus.toolkit.base.core.ssf.IPredictionErrorDecomposition;
import jdplus.toolkit.base.core.ssf.State;
import jdplus.toolkit.base.core.ssf.StateInfo;
import jdplus.toolkit.base.core.ssf.UpdateInformation;
import jdplus.toolkit.base.core.stats.likelihood.Likelihood;
import jdplus.toolkit.base.core.stats.likelihood.ResidualsCumulator;
import nbbrd.design.Development;


/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class PredictionErrorsDecomposition implements
        IPredictionErrorDecomposition, IMultivariateFilteringResults {

    private final ResidualsCumulator cumulator = new ResidualsCumulator();

    /**
     *
     * @param bres
     */
    public PredictionErrorsDecomposition() {
    }

    /**
     *
     */
    @Override
    public void close() {
    }

    /**
     *
     * @param ssf
     * @param data
     */
    @Override
    public void open(final IMultivariateSsf ssf, final IMultivariateSsfData data) {
        cumulator.clear();
    }

//    /**
//     *
//     * @param ssf
//     * @param data
//     */
//    @Override
//    public void prepare(final IMultivariateSsf ssf, final IMultivariateSsfData data) {
//        clear();
//    }
    @Override
    public void save(final int t, final State state, final StateInfo info) {
    }

    @Override
    public void save(final int t, final MultivariateUpdateInformation pe) {
        if (pe == null) {
            return;
        }
        DoubleSeq diag = pe.getR().diagonal();
        DoubleSeq err = pe.getU();
        UpdateInformation.Status[] status = pe.getStatus();
        for (int i = 0, iv = 0; i < status.length; ++i) {
            if (status[i] != UpdateInformation.Status.MISSING) {
                if (status[i] == UpdateInformation.Status.OBSERVATION) {
                    double r = diag.get(iv);
                    if (r != 0) {
                        cumulator.addStd(err.get(iv), r);
                    }
                }
                ++iv;
            }
        }
    }

    @Override
    public Likelihood likelihood(boolean scalingfactor) {
        return Likelihood.builder(cumulator.getObsCount())
                .scalingFactor(scalingfactor)
                .ssqErr(cumulator.getSsqErr())
                .logDeterminant(cumulator.getLogDeterminant()).build();
    }

}
