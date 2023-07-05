/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.math.linearfilters.advanced;

import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author Jean Palate
 */
public enum SpectralDensity {
    Undefined,
    WhiteNoise,
    RandomWalk;
    
    public DoubleUnaryOperator asFunction(){
        return switch (this) {
            case RandomWalk -> x->1/(2-2*Math.cos(x));
            default -> x->1;
        };
    }
}
