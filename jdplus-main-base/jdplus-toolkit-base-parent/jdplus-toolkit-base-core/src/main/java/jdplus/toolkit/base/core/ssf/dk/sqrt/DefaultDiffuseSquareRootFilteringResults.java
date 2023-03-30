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

import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.DataBlocksResults;
import jdplus.toolkit.base.core.ssf.StateInfo;
import jdplus.toolkit.base.core.ssf.akf.AugmentedState;
import jdplus.toolkit.base.core.ssf.ISsfInitialization;
import jdplus.toolkit.base.core.ssf.dk.BaseDiffuseFilteringResults;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class DefaultDiffuseSquareRootFilteringResults extends BaseDiffuseFilteringResults implements IDiffuseSquareRootFilteringResults {

    private final DataBlocksResults B;

    private DefaultDiffuseSquareRootFilteringResults(boolean var) {
        super(var);
        B = var ? new DataBlocksResults() : null;
    }

    public static DefaultDiffuseSquareRootFilteringResults full() {
        return new DefaultDiffuseSquareRootFilteringResults(true);
    }

    public static DefaultDiffuseSquareRootFilteringResults light() {
        return new DefaultDiffuseSquareRootFilteringResults(false);
    }

    @Override
    public void prepare(ISsf ssf, final int start, final int end) {
        super.prepare(ssf, start, end);
        ISsfInitialization initialization = ssf.initialization();
        int dim = initialization.getStateDim(), n = initialization.getDiffuseDim();
        if (B != null) {
            B.prepare(dim, n, n);
        }
    }

    @Override
    public void save(final int t, final AugmentedState state, final StateInfo info) {
        if (info != StateInfo.Forecast) {
            return;
        }
        super.save(t, state, info);
        if (B != null) {
            B.save(t, state.B());
        }

    }

    public FastMatrix B(int pos) {
        return B.matrix(pos);
    }

    @Override
    public void clear() {
        super.clear();
        B.clear();
    }

}
