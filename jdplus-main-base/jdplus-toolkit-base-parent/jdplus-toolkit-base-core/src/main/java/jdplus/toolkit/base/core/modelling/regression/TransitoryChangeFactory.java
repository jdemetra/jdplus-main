/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.modelling.regression;

import jdplus.toolkit.base.api.timeseries.regression.TransitoryChange;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.linearfilters.BackFilter;
import jdplus.toolkit.base.core.math.linearfilters.RationalBackFilter;
import jdplus.toolkit.base.api.timeseries.TimeSeriesDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import java.time.LocalDateTime;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.api.timeseries.TimeSeriesInterval;
import jdplus.toolkit.base.api.timeseries.TsException;

/**
 *
 * @author palatej
 */
public class TransitoryChangeFactory implements IOutlierFactory {

    private final double coefficient;
    static double ZERO = 1e-15;

    public static double rate(int period, double monthlyRate) {
        if (period == 12)
            return monthlyRate;
        if (12 % period != 0) {
            throw new TsException(TsException.INVALID_FREQ);
        }
        return Math.pow(monthlyRate, 12 / period);
    }

    public TransitoryChangeFactory(double coefficient) {
        this.coefficient = coefficient;
    }

    @Override
    public TransitoryChange make(LocalDateTime position) {
        return new TransitoryChange(position, coefficient);
    }

    @Override
    public void fill(int outlierPosition, DataBlock buffer) {
        double cur = 1;
        int n = buffer.length();
        for (int pos = outlierPosition; pos < n; ++pos) {
            buffer.set(pos, cur);
            cur *= coefficient;
            if (Math.abs(cur) < ZERO) {
                return;
            }
        }
    }

    @Override
    public FilterRepresentation getFilterRepresentation() {
        return new FilterRepresentation(new RationalBackFilter(
                BackFilter.ONE, BackFilter.ofInternal(1, -coefficient), 0), 0);
    }

    @Override
    public int excludingZoneAtStart() {
        return 0;
    }

    @Override
    public int excludingZoneAtEnd() {
        return 1;
    }

    @Override
    public String getCode() {
        return TransitoryChange.CODE;
    }
}

class TCFactory implements RegressionVariableFactory<TransitoryChange> {

    static double ZERO = 1e-15;

    static TCFactory FACTORY = new TCFactory();

    private TCFactory() {
    }

    @Override
    public boolean fill(TransitoryChange var, TsPeriod start, FastMatrix m) {
        TsPeriod p = start.withDate(var.getPosition());
        fill(var, start.until(p), m.column(0));
        return true;
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>> boolean fill(TransitoryChange var, D domain, FastMatrix m) {
        fill(var, domain.indexOf(var.getPosition()), m.column(0));
        return true;
    }

    public void fill(TransitoryChange var, int xpos, DataBlock buffer) {
        double cur = 1;
        int n = buffer.length();
        for (int pos = xpos; pos < n; ++pos) {
            if (pos >= 0) {
                buffer.set(pos, cur);
            }
            cur *= var.getRate();
            if (Math.abs(cur) < ZERO) {
                return;
            }
        }
    }
}
