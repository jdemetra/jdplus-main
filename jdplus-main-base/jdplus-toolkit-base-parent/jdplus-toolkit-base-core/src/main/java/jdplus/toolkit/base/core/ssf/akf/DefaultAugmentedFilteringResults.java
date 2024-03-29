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
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.DefaultFilteringResults;
import jdplus.toolkit.base.core.ssf.DataBlockResults;
import jdplus.toolkit.base.core.ssf.MatrixResults;
import jdplus.toolkit.base.core.ssf.StateInfo;
import jdplus.toolkit.base.core.ssf.ISsfInitialization;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class DefaultAugmentedFilteringResults extends DefaultFilteringResults implements IAugmentedFilteringResults {

    private final MatrixResults B;
    private final DataBlockResults E;

    protected DefaultAugmentedFilteringResults(boolean var) {
        super(var);
        B = new MatrixResults();
        E = new DataBlockResults();
    }

    public static DefaultAugmentedFilteringResults full() {
        return new DefaultAugmentedFilteringResults(true);
    }

    public static DefaultAugmentedFilteringResults light() {
        return new DefaultAugmentedFilteringResults(false);
    }

    @Override
    public void prepare(ISsf ssf, final int start, final int end) {
        super.prepare(ssf, start, end);
        ISsfInitialization initialization = ssf.initialization();
        int dim = initialization.getStateDim(), n = initialization.getDiffuseDim();
        B.prepare(dim, n, 0, n);
        E.prepare(dim, 0, n);
    }

    @Override
    public void save(int t, AugmentedUpdateInformation pe) {
        super.save(t, pe);
        E.save(t, pe.E());
    }

    @Override
    public void close(int pos) {
    }

    @Override
    public void save(final int t, final AugmentedState state, final StateInfo info) {
        if (info != StateInfo.Forecast) {
            return;
        }
        super.save(t, state, info);
        B.save(t, state.B());
    }

    @Override
    public FastMatrix B(int pos) {
        return B.matrix(pos);
    }

    @Override
    public DataBlock E(int pos) {
        return E.datablock(pos);
    }

    @Override
    public void clear() {
        super.clear();
        B.clear();
    }
}
