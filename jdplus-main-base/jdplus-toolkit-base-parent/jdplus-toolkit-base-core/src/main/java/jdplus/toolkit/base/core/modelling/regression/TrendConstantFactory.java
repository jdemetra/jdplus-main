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
import jdplus.toolkit.base.api.timeseries.regression.TrendConstant;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.polynomials.UnitRoots;

/**
 *
 * @author Jean Palate
 */
class TrendConstantFactory implements RegressionVariableFactory<TrendConstant>{

    static TrendConstantFactory FACTORY=new TrendConstantFactory();

    private TrendConstantFactory(){}

    @Override
    public boolean fill(TrendConstant var, TsPeriod start, FastMatrix buffer) {
        double[] D = UnitRoots.D(var.getD()).times(UnitRoots.D(start.getUnit().getAnnualFrequency(), var.getBd())).toArray();
        int d = D.length - 1;
        int n=buffer.getRowsCount();
        DataBlock m = buffer.column(0);
        m.set(d, 1);
        for (int i = d + 1; i < n; ++i) {
            double s = 1;
            for (int j = 1; j <= d; ++j) {
                s -= m.get(i - j) * D[j];
            }
            m.set(i, s);
        }
         return true;
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>>  boolean fill(TrendConstant var, D domain, FastMatrix buffer) {
        return false;
    }
  
}
