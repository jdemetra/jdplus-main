/*
 * Copyright 2013-2014 National Bank copyOf Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.toolkit.base.core.ssf.multivariate;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.ssf.UpdateInformation;


/**
 * The multivariate update information contains all update information related
 * to the observations at a given t Information is only related to non missing
 * observations (which means that the size of the buffers) can change following
 * t
 *
 * @author Jean Palate
 */
@lombok.Value
public class MultivariateUpdateInformation {

    @lombok.Builder
    public MultivariateUpdateInformation(DoubleSeq e, FastMatrix R, FastMatrix M, UpdateInformation.Status[] status) {
        this.e = e;
        this.R = R;
        this.M = M;
        this.status = status;
        double[] pu = e.toArray();
        LowerTriangularMatrix.solveLx(R, DataBlock.of(pu));
        this.u = DoubleSeq.of(pu);
        int nused=e.length();
        usedMeasurements=new int[nused];
        for (int i=0, j=0; i<status.length; ++i){
            if (status[i] != UpdateInformation.Status.MISSING){
                usedMeasurements[j++]=i;
            }
        }
    }

    /**
     * e contains the prediction errors (untransformed). e((i)=y(i)-Z(i)a u is
     * the transformed prediction error (u = R^-1)* u
     */
    private DoubleSeq e;
    
    private DoubleSeq u;

    /**
     * R=(ZPZ'+H)^1/2 Cholesky factor of the variance/covariance matrix of the
     * prediction errors (lower triangular). nvars x nvars F is the prediction
     * error covariance
     */
    @lombok.NonNull
    private FastMatrix R;
    

    /**
     * K = P Z' L'^-1 dim x nvars
     */
    @lombok.NonNull
    private FastMatrix M;

    @lombok.NonNull
    private UpdateInformation.Status[] status;
    
    @lombok.NonNull
    private int[] usedMeasurements;

    public static int fillStatus(IMultivariateSsfData data, int pos, UpdateInformation.Status[] s) {
        int nvars = s.length;
        for (int i = 0; i < s.length; ++i) {
            if (data.isMissing(pos, i)) {
                s[i] = UpdateInformation.Status.MISSING;
                --nvars;
            } else {
                if (data.isConstraint(pos, i)) {
                    s[i] = UpdateInformation.Status.CONSTRAINT;
                } else {
                    s[i] = UpdateInformation.Status.OBSERVATION;
                }
            }
        }
        return nvars;
    }
}
