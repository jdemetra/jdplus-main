/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.modelling.regression;

import jdplus.toolkit.base.api.timeseries.regression.AdditiveOutlier;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.linearfilters.BackFilter;
import jdplus.toolkit.base.core.math.linearfilters.RationalBackFilter;
import jdplus.toolkit.base.api.timeseries.TimeSeriesDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import java.time.LocalDateTime;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.api.timeseries.TimeSeriesInterval;

/**
 *
 * @author palatej
 */
public class AdditiveOutlierFactory implements IOutlierFactory {

    public static final AdditiveOutlierFactory FACTORY = new AdditiveOutlierFactory();

    private AdditiveOutlierFactory() {
    }

    @Override
    public AdditiveOutlier make(LocalDateTime position) {
        return new AdditiveOutlier(position);
    }

    @Override
    public void fill(int outlierPosition, DataBlock buffer) {
        buffer.set(outlierPosition, 1);
    }

    @Override
    public FilterRepresentation getFilterRepresentation() {
        return new FilterRepresentation(new RationalBackFilter(
                BackFilter.ONE, BackFilter.ONE, 0), 0);
    }

    @Override
    public int excludingZoneAtStart() {
        return 0;
    }

    @Override
    public int excludingZoneAtEnd() {
        return 0;
    }

    @Override
    public String getCode() {
        return AdditiveOutlier.CODE;
    }
}

class AOFactory implements RegressionVariableFactory<AdditiveOutlier> {

    static AOFactory FACTORY=new AOFactory();

    private AOFactory(){}

    @Override
    public boolean fill(AdditiveOutlier var, TsPeriod start, FastMatrix buffer) {
        TsPeriod p = start.withDate(var.getPosition());
        int opos = start.until(p);
        if (opos >= 0 && opos < buffer.getRowsCount()) {
            buffer.set(opos, 0, 1);
        }
        return true;
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>>  boolean fill(AdditiveOutlier var, D domain, FastMatrix buffer) {
        long pos = domain.indexOf(var.getPosition());
        if (pos >= 0) {
            buffer.set((int) pos, 0, 1);
        }
        return true;
    }
}
