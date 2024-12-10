/*
 * Copyright 2024 JDemetra+.
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
package jdplus.toolkit.base.r.math;

import java.util.function.DoubleUnaryOperator;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.splines.CubicSpline;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class CubicSplines {

    public double[] monotonic(double[] x, double[] y, double[] pos) {
        DoubleUnaryOperator fn = CubicSpline.monotonic(x, y);
        for (int i = 0; i < pos.length; ++i) {
            pos[i] = fn.applyAsDouble(pos[i]);
        }
        return pos;
    }

    public double[] natural(double[] x, double[] y, double[] pos) {
        DoubleUnaryOperator fn = CubicSpline.of(x, y);
        for (int i = 0; i < pos.length; ++i) {
            pos[i] = fn.applyAsDouble(pos[i]);
        }
        return pos;
    }

    public double[] periodic(double[] x, double[] y, double[] pos) {
        DoubleUnaryOperator fn = CubicSpline.periodic(x, y);
         for (int i = 0; i < pos.length; ++i) {
            pos[i] = fn.applyAsDouble(pos[i]);
        }
        return pos;
    }
    
    public Matrix periodicCardinalSplines(double[] x, double[] pos) {
                
        int dim = x.length;
        int n=pos.length;
        FastMatrix splines=FastMatrix.make(n, dim-1);
        DataBlockIterator cols = splines.columnsIterator();
        int i=0;
        while (cols.hasNext()){
            double[] f = new double[dim];
            if (i == 0) {
                f[0] = 1;
                f[dim-1] = 1;
            } else {
                f[i] = 1;
            }
            DoubleUnaryOperator fn = CubicSpline.periodic(x, f);
            cols.next().set(j->fn.applyAsDouble(pos[j]));
            ++i;
        }
        return splines;
    }
}
