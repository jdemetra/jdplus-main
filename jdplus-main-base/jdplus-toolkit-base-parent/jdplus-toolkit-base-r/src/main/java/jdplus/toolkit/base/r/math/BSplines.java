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
package jdplus.toolkit.base.r.math;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.math.matrices.Matrix;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class BSplines {
    
    public Matrix periodic(int order, double period, double[] breaks, double[] pos){
        jdplus.toolkit.base.core.math.splines.BSplines.BSpline bspline= jdplus.toolkit.base.core.math.splines.BSplines.periodic(order, breaks, period);
        return jdplus.toolkit.base.core.math.splines.BSplines.splines(bspline, DoubleSeq.of(pos));
    }
    
    public Matrix of(int order, double[] breaks, double[] pos){
        jdplus.toolkit.base.core.math.splines.BSplines.BSpline bspline= jdplus.toolkit.base.core.math.splines.BSplines.augmented(order, breaks);
        return jdplus.toolkit.base.core.math.splines.BSplines.splines(bspline, DoubleSeq.of(pos));
    }
}
