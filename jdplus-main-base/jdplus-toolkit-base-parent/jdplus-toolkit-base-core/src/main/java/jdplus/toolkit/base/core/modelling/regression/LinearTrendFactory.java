/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.modelling.regression;

import jdplus.toolkit.base.api.timeseries.regression.LinearTrend;
import jdplus.toolkit.base.api.timeseries.TimeSeriesDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.api.timeseries.TimeSeriesInterval;

/**
 *
 * @author Jean Palate
 */
class LinearTrendFactory implements RegressionVariableFactory<LinearTrend>{

    static LinearTrendFactory FACTORY=new LinearTrendFactory();

    private LinearTrendFactory(){}

    @Override
    public boolean fill(LinearTrend var, TsPeriod start, FastMatrix buffer) {
        int del=TsPeriod.of(start.getUnit(), var.getStart()).until(start);
        buffer.column(0).set(r->r+del);
        return true;
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>>  boolean fill(LinearTrend var, D domain, FastMatrix buffer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}
