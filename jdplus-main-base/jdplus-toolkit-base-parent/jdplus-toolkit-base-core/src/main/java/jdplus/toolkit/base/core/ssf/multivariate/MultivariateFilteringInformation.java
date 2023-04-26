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

import java.util.ArrayList;
import java.util.List;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.IPredictionErrorDecomposition;
import jdplus.toolkit.base.core.ssf.State;
import jdplus.toolkit.base.core.ssf.StateInfo;
import jdplus.toolkit.base.core.ssf.StateStorage;
import jdplus.toolkit.base.core.stats.likelihood.Likelihood;

/**
 *
 * @author palatej
 */
public class MultivariateFilteringInformation implements IPredictionErrorDecomposition, IMultivariateFilteringResults{
    
    private PredictionErrorsDecomposition predictionErrorsDecomposition;
    private MultivariateUpdateInformation[] infos;
    private StateStorage states;
    
    public DataBlock a(int t){
        return states.a(t);
    }
    
    public FastMatrix P(int t){
        return states.P(t);
    }
    
    @Override
    public void open(IMultivariateSsf ssf, IMultivariateSsfData data) {
        infos=new MultivariateUpdateInformation[data.getObsCount()];
        predictionErrorsDecomposition=new PredictionErrorsDecomposition();
        states= StateStorage.full(StateInfo.Forecast);
        states.prepare(ssf.getStateDim(), 0, data.getObsCount());
    }

    @Override
    public void close() {
    }
    
    public int size(){
        return infos.length;
    }
    
    public MultivariateUpdateInformation get(int idx){
        return infos[idx];
    }
    
    private static final int ALLOC=32;
    
    @Override
    public void save(int t, MultivariateUpdateInformation pe) {
        predictionErrorsDecomposition.save(t, pe);
        if (t >= infos.length){
            MultivariateUpdateInformation[]tmp=new MultivariateUpdateInformation[t+ALLOC];
            System.arraycopy(infos, 0, tmp, 0, infos.length);
            infos=tmp;
        }
        infos[t]=pe;
     }

    @Override
    public void save(int pos, State state, StateInfo info) {
        states.save(pos, state, info);
    }

    @Override
    public Likelihood likelihood(boolean scalingfactor) {
        return predictionErrorsDecomposition.likelihood(scalingfactor);
    }
    
}
