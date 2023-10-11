/*
 * Copyright 2023 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.toolkit.base.core.math.linearfilters;

import jdplus.toolkit.base.api.data.DoubleSeq;
import java.util.function.IntFunction;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class CrossValidation {
    public double[] doCrossValidation(DoubleSeq data, int low, int high, IntFunction<SymmetricFilter> factory){
        int start=high, end=data.length()-high;
        double[] rslt=new double[high-low];
        double[] pdata = data.toArray();
        for (int i=0; i<rslt.length; ++i){
            int h=low+i;
            SymmetricFilter sf = factory.apply(i+low);
            double w0=sf.weights().applyAsDouble(0);
            double d=0;
            for (int j=start; j<end; ++j){
                double del=(pdata[j]-sf.apply(pdata, j, 1));
                d+=del*del;
            }
            rslt[i]=Math.sqrt(d)/(1-w0)/(end-start);
        }        
        return rslt;
    }
}
