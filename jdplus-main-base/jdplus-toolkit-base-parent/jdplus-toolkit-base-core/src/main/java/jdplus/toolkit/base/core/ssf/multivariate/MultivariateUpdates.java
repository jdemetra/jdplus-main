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
package jdplus.toolkit.base.core.ssf.multivariate;

import jdplus.toolkit.base.api.data.DoubleSeq;
import java.util.ArrayList;
import java.util.List;
import jdplus.toolkit.base.core.ssf.IPredictionErrorDecomposition;
import jdplus.toolkit.base.core.ssf.State;
import jdplus.toolkit.base.core.ssf.StateInfo;
import jdplus.toolkit.base.core.ssf.UpdateInformation;
import jdplus.toolkit.base.core.stats.likelihood.Likelihood;
import jdplus.toolkit.base.core.stats.likelihood.ResidualsCumulator;

/**
 *
 * @author palatej
 */
public class MultivariateUpdates implements IPredictionErrorDecomposition, IMultivariateFilteringResults{
    
    private final ResidualsCumulator cumulator = new ResidualsCumulator();
    private List<MultivariateUpdateInformation> infos;

    @Override
    public void open(IMultivariateSsf ssf, IMultivariateSsfData data) {
        infos=new ArrayList<>(data.getObsCount());
        cumulator.clear();
    }

    @Override
    public void close() {
    }
    
    public int size(){
        return infos.size();
    }
    
    public MultivariateUpdateInformation get(int idx){
        return infos.get(idx);
    }
    
    @Override
    public void save(int t, MultivariateUpdateInformation pe) {
        if (pe == null) {
            return;
        }
        infos.set(t, pe);
        DoubleSeq diag = pe.getR().diagonal();
        DoubleSeq err = pe.getE();
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
    public void save(int pos, State state, StateInfo info) {
    }

    @Override
    public Likelihood likelihood(boolean scalingfactor) {
        return Likelihood.builder(cumulator.getObsCount())
                .scalingFactor(scalingfactor)
                .ssqErr(cumulator.getSsqErr())
                .logDeterminant(cumulator.getLogDeterminant()).build();
    }
    
}
