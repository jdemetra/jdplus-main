/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.stats;

import java.util.function.DoubleUnaryOperator;
import jdplus.toolkit.base.core.math.functions.NumericalIntegration;

/**
 *
 * @author Jean Palate
 */
public interface Kernel {
    double lowerBound();
    double upperBound();
    DoubleUnaryOperator asFunction();
    
    default double moment(final int order){
       return NumericalIntegration.integrate(x->Math.pow(x, order)*asFunction().applyAsDouble(x)
               , lowerBound(), upperBound());

    }
        
}
