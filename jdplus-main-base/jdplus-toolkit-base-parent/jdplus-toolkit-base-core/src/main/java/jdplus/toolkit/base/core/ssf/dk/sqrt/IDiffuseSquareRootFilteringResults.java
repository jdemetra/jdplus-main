/*
 * Copyright 2016 National Bank of Belgium
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
package jdplus.toolkit.base.core.ssf.dk.sqrt;

import jdplus.toolkit.base.core.ssf.StateInfo;
import jdplus.toolkit.base.core.ssf.akf.AugmentedState;
import jdplus.toolkit.base.core.ssf.dk.DiffuseUpdateInformation;
import jdplus.toolkit.base.core.ssf.dk.IBaseDiffuseFilteringResults;

/**
 *
 * @author Jean Palate
 */
public interface IDiffuseSquareRootFilteringResults extends IBaseDiffuseFilteringResults {

    
    /**
     *
     * @param t
     * @param pe
     */
    void save(int t, DiffuseUpdateInformation pe);
    
    /**
     *
     * @param pos
     * @param state
     * @param info
     */
    void save(int pos, AugmentedState state, StateInfo info);

}
