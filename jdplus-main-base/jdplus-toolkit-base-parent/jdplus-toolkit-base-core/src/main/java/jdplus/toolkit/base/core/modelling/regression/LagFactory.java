/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.toolkit.base.core.modelling.regression;

import jdplus.toolkit.base.api.timeseries.TimeSeriesDomain;
import jdplus.toolkit.base.api.timeseries.TimeSeriesInterval;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.regression.TsLag;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author palatej
 */
public class LagFactory implements ModifierFactory<TsLag> {

    static LagFactory FACTORY = new LagFactory();

    private LagFactory() {
    }

    @Override
    public TsDomain needFor(TsLag lags, TsDomain domain) {
        TsPeriod start = domain.getStartPeriod().plus(-lags.getLag());
        return TsDomain.of(start, domain.getLength());
    }

    @Override
    public TimeSeriesDomain needForGeneric(TsLag lags, TimeSeriesDomain domain) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean fill(TsLag modifier, TsPeriod start, FastMatrix input, FastMatrix output) {
        for (int j = 0; j < input.getColumnsCount(); ++j) {
            output.column(j).copy(input.column(j));
        }
        return true;
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>> boolean fill(TsLag var, D domain, FastMatrix input, FastMatrix output) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
