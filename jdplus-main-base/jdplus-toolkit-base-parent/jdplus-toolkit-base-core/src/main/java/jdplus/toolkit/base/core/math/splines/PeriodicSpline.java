/*
 * Copyright 2024 JDemetra+
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
package jdplus.toolkit.base.core.math.splines;

import java.util.function.DoubleUnaryOperator;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class PeriodicSpline  implements DoubleUnaryOperator {
    
    private final BSplines.BSpline spline;
    private final double[] a;
    private final double period;
    
    public static PeriodicSpline of(int order, DoubleSeq breaks, DoubleSeq values, double period){
        return new PeriodicSpline(BSplines.periodic(order, breaks.toArray(), period), values, period);
    }
    
    private PeriodicSpline(final BSplines.BSpline spline, DoubleSeq a, double period){
        this.spline=spline;
        this.a=a.toArray();
        this.period=period;        
    }

    @Override
    public double applyAsDouble(double operand) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
