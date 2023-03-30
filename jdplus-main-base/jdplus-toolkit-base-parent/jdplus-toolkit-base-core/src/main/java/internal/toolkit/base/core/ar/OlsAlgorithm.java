/*
 * Copyright 2017 National Bank of Belgium
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
package internal.toolkit.base.core.ar;

import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.api.design.AlgorithmImplementation;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import nbbrd.service.ServiceProvider;
import jdplus.toolkit.base.core.ar.AutoRegressiveEstimation;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.linearsystem.QRLeastSquaresSolution;
import jdplus.toolkit.base.core.math.linearsystem.QRLeastSquaresSolver;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(AutoRegressiveEstimation.class)
@AlgorithmImplementation(algorithm=AutoRegressiveEstimation.class)
public class OlsAlgorithm implements AutoRegressiveEstimation {

    private double[] y, a;

    @Override
    public boolean estimate(DoubleSeq Y, int nar) {
        y=Y.toArray();
        int n = y.length;
        
        FastMatrix M = FastMatrix.make(n-nar, nar);
        DataBlockIterator cols = M.columnsIterator();
        for (int i = 0; i < nar; ++i) {
            cols.next().copy(Y.drop(nar-i-1, n));
        }
        
        DoubleSeq yc = Y.drop(nar, 0);
        QRLeastSquaresSolution rslt = QRLeastSquaresSolver.fastLeastSquares(yc, M);
        if (rslt == null) {
            return false;
        }
        a = rslt.getB().toArray();
        return true;
    }

    @Override
    public DoubleSeq coefficients() {
        return DoubleSeq.of(a);
    }

    @Override
    public DoubleSeq data() {
        return DoubleSeq.of(y);
    }
}
