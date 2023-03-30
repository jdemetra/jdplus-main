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
package jdplus.toolkit.base.core.math.polynomials;

import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.api.math.Constants;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixException;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.linearsystem.QRLeastSquaresSolution;
import jdplus.toolkit.base.core.math.linearsystem.QRLeastSquaresSolver;

/**
 *
 * @author Jean Palate
 */
public class LeastSquaresDivision {

    public static final double EPS = Math.sqrt(Constants.getEpsilon());
    private double err;
    private double[] coeff;

    public boolean divide(Polynomial num, Polynomial denom) {
        try {
            err = 0;
            DoubleSeq N = num.coefficients();
            DoubleSeq D = denom.coefficients();
            int n = N.length(), d = D.length();
            if (d > n) {
                return false;
            }
            int q = n - d + 1;
            FastMatrix m = FastMatrix.make(n, q);
            DataBlockIterator columns = m.columnsIterator();
            int c = 0;
            while (columns.hasNext()) {
                columns.next().range(c, c + d).copy(D);
                ++c;
            }
            
            QRLeastSquaresSolution ls = QRLeastSquaresSolver.fastLeastSquares(N, m);
            this.coeff=ls.getB().toArray();
            this.err = ls.getSsqErr() / d;
            return true;
        } catch (MatrixException error) {
            return false;
        }
    }

    public Polynomial getQuotient() {
        return Polynomial.of(coeff);
    }

    public double getError() {
        return err;
    }

    public boolean isExact() {
        return err < EPS;
    }
}
