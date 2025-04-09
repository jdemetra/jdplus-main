/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.base.core;

import jdplus.sa.base.api.SaDictionaries;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.sa.base.api.SaException;
import jdplus.toolkit.base.api.timeseries.TsData;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class PreliminaryChecks {


    @FunctionalInterface
    public static interface Tool {

        TsData check(TsData original, ProcessingLog log);
    }

    public final static int MAX_REPEAT_COUNT = 80, MAX_MISSING_COUNT = 33;

    public static void testSeries(final TsData y) {
        if (y == null) {
            throw new SaException(SaDictionaries.NO_DATA);
        }
        int nz = y.length();
        int period = y.getAnnualFrequency();
        if (nz < Math.max(8, 3 * period)) {
            throw new SaException(SaDictionaries.NOT_ENOUGH_DATA);
        }
        DoubleSeq values = y.getValues();
        int nrepeat = repeatCount(values);
        if (nrepeat > MAX_REPEAT_COUNT * nz / 100) {
            throw new SaException(SaDictionaries.TOO_MANY_IDENTICAL);
        }
        int nm = values.count(z -> !Double.isFinite(z));
        if (nm > MAX_MISSING_COUNT * nz / 100) {
            throw new SaException(SaDictionaries.TOO_MANY_MISSING);
        }
    }

    public static boolean testSeries(final TsData y, ProcessingLog log) {
        log.push(SaDictionaries.PRELIMINARY_CHECKS);
        try {
            testSeries(y);
            return true;
        } catch (SaException err) {
            log.error(err.getMessage());
            return false;
        }finally{
            log.pop();
        }
    }

    public int repeatCount(DoubleSeq values) {
        int i = 0;
        int n = values.length();
        while ((i < n) && !Double.isFinite(values.get(i))) {
            ++i;
        }
        if (i == n) {
            return 0;
        }
        int c = 0;
        double prev = values.get(i++);
        for (; i < n; ++i) {
            double cur = values.get(i);
            if (Double.isFinite(cur)) {
                if (cur == prev) {
                    ++c;
                } else {
                    prev = cur;
                }
            }
        }
        return c;
    }

}
