/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.modelling.regression;

import jdplus.toolkit.base.api.timeseries.TimeSeriesDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.api.timeseries.TimeSeriesInterval;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.regression.ModifiedTsVariable;

/**
 *
 * @author palatej
 * @param <X>
 */
public interface ModifierFactory <X extends ModifiedTsVariable.Modifier> {
    
    /**
     * Gets the domain necessary for computing the data corresponding to the given domain.
     * @param modifier
     * @param domain
     * @return The returned domain could have a different span and/or a different periodicity
     */
    TsDomain needFor(X modifier, TsDomain domain);
    TimeSeriesDomain needForGeneric(X modifier, TimeSeriesDomain domain);
    
    /**
     * Computes the output corresponding to the given input (starting at the given period)
     * @param modifier
     * @param start
     * @param input
     * @param output
     * @return 
     */
    boolean fill(X modifier, TsPeriod start, FastMatrix input, FastMatrix output);
    <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>> boolean fill(X var, D domain, FastMatrix input, FastMatrix output);
}
