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

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.ssf.StateInfo;
import jdplus.toolkit.base.core.ssf.univariate.IFilteringResults;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public interface IAugmentedFilteringResults extends IFilteringResults {

    /**
     *
     * @param pos
     */
    void close(int pos);

    /**
     *
     * @param t
     * @param pe
     */
    void save(int t, AugmentedUpdateInformation pe);

    /**
     *
     * @param t
     * @param state
     * @param info
     */
    void save(int t, AugmentedState state, StateInfo info);

    default FastMatrix B(int pos) {
        return null;
    }

    default DataBlock E(int pos) {
        return null;
    }
}
