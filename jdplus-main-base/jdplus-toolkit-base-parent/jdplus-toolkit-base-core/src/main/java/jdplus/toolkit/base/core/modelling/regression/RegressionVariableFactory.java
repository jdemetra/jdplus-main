/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.modelling.regression;

import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.timeseries.regression.ITsVariable;
import jdplus.toolkit.base.api.timeseries.TimeSeriesDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.api.timeseries.TimeSeriesInterval;

/**
 *
 * @author palatej
 * @param <X>
 */
public interface RegressionVariableFactory<X extends ITsVariable> {

    default boolean fill(X var, TsPeriod start, FastMatrix buffer) {
        return fill(var, start, buffer, null);
    }

    default <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>> boolean fill(X var, D domain, FastMatrix buffer) {
        return fill(var, domain, buffer, null);
    }

    boolean fill(X var, TsPeriod start, FastMatrix buffer, ProcessingLog log);

    <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>> boolean fill(X var, D domain, FastMatrix buffer, ProcessingLog log);
}
