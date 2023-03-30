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

package jdplus.toolkit.base.core.math.functions.ssq;

import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.math.functions.IFunctionDerivatives;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface ISsqFunctionDerivatives extends IFunctionDerivatives {

    /**
     * 
     * @param idx
     * @return
     */
    DoubleSeq dEdX(int idx);
    
    /**
     * Returns the Jacobian of the function at the current point.
     * If the function has m parameters (p) and the residuals (E) n values, 
     * the matrix J is n x m and J(i, j) = dE(i)/dp(j)
     * @param matrix
     */
    void jacobian(FastMatrix matrix);
    
}
