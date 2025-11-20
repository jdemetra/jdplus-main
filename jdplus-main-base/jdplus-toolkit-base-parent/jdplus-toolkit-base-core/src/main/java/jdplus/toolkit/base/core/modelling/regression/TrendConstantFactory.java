/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.modelling.regression;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import jdplus.toolkit.base.api.processing.ProcessingLog;
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
class TrendConstantFactory implements RegressionVariableFactory<TrendConstant> {

    @lombok.Value
    private static class Key {

        int d;
        int bd;
        int period;
    }

    @lombok.Value
    private static class Data {

        int start; // position of the reference period
        double[] data;
    }

    private static final Map<Key, Data> CACHE = new HashMap<>();

    private static Data compute(Key key, int n) {
        double[] D = UnitRoots.D(key.getD()).times(UnitRoots.D(key.getPeriod(), key.getBd())).toArray();
        int d = D.length - 1;
        if (n < d) {
            n = d;
        }
        double[] m = new double[n];
        for (int i = d; i < n; ++i) {
            double s = 1;
            for (int j = 1; j <= d; ++j) {
                s -= m[i - j] * D[j];
            }
            m[i] = s;
        }
        return new Data(0, m);
    }

    private static Data fextend(Key key, Data cur, int n) {
        double[] dcur = cur.getData();
        if (n <= dcur.length - cur.start) {// enough data
            return cur;
        }
        double[] D = UnitRoots.D(key.getD()).times(UnitRoots.D(key.getPeriod(), key.getBd())).toArray();
        int d = D.length - 1;
        double[] m = new double[cur.start + n];
        for (int i = 0; i < dcur.length; ++i) {
            m[i] = dcur[i];
        }
        for (int i = dcur.length; i < m.length; ++i) {
            double s = 1;
            for (int j = 1; j <= d; ++j) {
                s -= m[i - j] * D[j];
            }
            m[i] = s;
        }
        return new Data(cur.start, m);
    }

    private static Data bextend(Key key, Data cur, int n) {
        double[] dcur = cur.getData();
        int del = n - cur.start;
        if (del < 0) { // enough data
            return cur;
        }
        double[] D = UnitRoots.D(key.getD()).times(UnitRoots.D(key.getPeriod(), key.getBd())).mirror().toArray();
        int d = D.length - 1;
        double[] m = new double[del + dcur.length];
        for (int i = del, j = 0; j < dcur.length; ++i, ++j) {
            m[i] = dcur[j];
        }
        double a = D[0];
        for (int i = del - 1; i >= 0; --i) {
            double s = 1;
            for (int j = 1; j <= d; ++j) {
                s -= m[i + j] * D[j];
            }
            m[i] = s / a;
        }
        return new Data(n, m);
    }

    private static Data dataFor(Key key, int start, int n) {

        return CACHE.compute(key, (k, v) -> {
            if (v == null) {
                v = compute(k, n);
            } else {
                v = fextend(k, v, n);
            }
            if (start < 0) {
                v = bextend(k, v, -start);
            }
            return v;
        });
    }

    static TrendConstantFactory FACTORY = new TrendConstantFactory();

    private TrendConstantFactory() {
    }

    @Override
    public boolean fill(TrendConstant var, TsPeriod start, FastMatrix buffer, ProcessingLog log) {
        int n = buffer.getRowsCount();
        int beg = 0;
        if (var.getReference() != null) {
            TsPeriod ref = start.withDate(var.getReference());
            beg = ref.until(start);
        }
        synchronized (CACHE) {
            Data data = dataFor(new Key(var.getD(), var.getBd(), start.annualFrequency()), beg, n + beg);
            buffer.column(0).copyFrom(data.data, data.start + beg);
        }
        return true;
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>> boolean fill(TrendConstant var, D domain, FastMatrix buffer, ProcessingLog log) {
        return false;
    }

}
