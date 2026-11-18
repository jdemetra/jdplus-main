/*
 * Copyright 2026 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.toolkit.base.core.ssf.utility;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixStorage;
import jdplus.toolkit.base.core.ssf.composite.CompositeSsf;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;

/**
 * 0/1 approach only for the time being
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class Weights {

    public MatrixStorage smoothingWeights(ISsf ssf, int n) {
        MatrixStorage storage = new MatrixStorage(ssf.getStateDim(), n, n);
        for (int i = 0; i < n; ++i) {
            double[] x = new double[n];
            x[i] = 1;
            DefaultSmoothingResults sr = DkToolkit.smooth(ssf, new SsfData(x), false, false);
            FastMatrix M = storage.matrix(i);
            DataBlockIterator columnsIterator = M.columnsIterator();
            for (int j = 0; j < n; ++j) {
                columnsIterator.next().copy(sr.a(j));
            }
        }
        return storage;
    }

    public void weights(MatrixStorage storage, DoubleSeq Z, FastMatrix M) {
        int n = storage.getMatrixColumnsCount();
        for (int i = 0; i < n; ++i) {}
    }

    public MatrixStorage weights(CompositeSsf ssf, MatrixStorage storage) {
        int n = storage.getMatrixColumnsCount();
        int[] dim = ssf.componentsDimension();
        MatrixStorage R = new MatrixStorage(n, n, dim.length);
        DataBlock Z = DataBlock.make(ssf.getStateDim());

        for (int i = 0; i < n; ++i) {
            FastMatrix curM = storage.matrix(i);
            Z.set(0);
            ssf.loading().Z(i, Z);
            for (int j = 0, k = 0; j < 3; ++j) {
                DataBlock curZ = Z.extract(k, dim[j]);
                //                DataBlock curA = srslts.a(i).extract(k, dim[j]);
                //                cmps.set(i, j, curZ.dot(curA));
                //                FastMatrix curP = srslts.P(i).extract(k, dim[j], k, dim[j]);
                //                ecmps.set(i, j, QuadraticForm.apply(curP, curZ));
                k += dim[j];
            }
        }
        return null;
    }
}
