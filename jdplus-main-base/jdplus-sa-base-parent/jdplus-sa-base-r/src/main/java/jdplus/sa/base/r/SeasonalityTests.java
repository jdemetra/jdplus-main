/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.sa.base.r;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.sa.base.core.tests.CanovaHansen;
import jdplus.sa.base.core.tests.CanovaHansen2;
import jdplus.sa.base.core.tests.PeriodicLjungBox;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.sa.base.protobuf.SaProtosUtility;
import jdplus.sa.base.core.tests.CombinedSeasonality;
import jdplus.sa.base.core.tests.FTest;
import jdplus.sa.base.core.tests.Friedman;
import jdplus.sa.base.core.tests.KruskalWallis;
import jdplus.sa.base.core.tests.PeriodogramTest;
import jdplus.sa.base.core.tests.Qs;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class SeasonalityTests {

    public StatisticalTest qsTest(double[] s, int period, int ny) {
        DoubleSeq y = DoubleSeq.of(s).cleanExtremities();
        if (ny != 0) {
            y = y.drop(Math.max(0, y.length() - period * ny), 0);
        }
        return new Qs(y, period)
                .autoCorrelationsCount(2)
                .build();
    }

    public StatisticalTest kruskalWallisTest(double[] s, int period, int ny) {
        DoubleSeq y = DoubleSeq.of(s).cleanExtremities();
        if (ny != 0) {
            y = y.drop(Math.max(0, y.length() - period * ny), 0);
        }
        return new KruskalWallis(y, period)
                .build();
    }

    public StatisticalTest friedmanTest(double[] s, int period, int ny) {
        DoubleSeq y = DoubleSeq.of(s).cleanExtremities();
       if (ny != 0) {
            y = y.drop(Math.max(0, y.length() - period * ny), 0);
        }
        return new Friedman(y, period)
                .build();
    }

    public StatisticalTest periodogramTest(double[] s, int period, int ny) {
        DoubleSeq y = DoubleSeq.of(s).cleanExtremities();
       if (ny != 0) {
            y = y.drop(Math.max(0, y.length() - period * ny), 0);
        }
        return new PeriodogramTest(y, period).buildF();
    }

    public StatisticalTest periodicQsTest(double[] s, double[] periods) {
        DoubleSeq y = DoubleSeq.of(s).cleanExtremities();
        return new PeriodicLjungBox(y, 0)
                .lags(periods[0], 2)
                .usePositiveAutocorrelations()
                .build();
    }

    public double[] canovaHansenTest(double[] s, double start, double end, int n, boolean original) {
        DoubleSeq x = DoubleSeq.of(s).cleanExtremities();
        double[] rslt = new double[n];
        double step=(end-start)/(n-1);
        for (int i = 0; i < n; ++i) {
            double p=start+i*step;
            if (original) {
                rslt[i] = CanovaHansen.test(x)
                        .specific(p, 1)
                        .build()
                        .testAll();
            } else {
                rslt[i] = CanovaHansen2.of(x)
                        .periodicity(p)
                        .compute();
            }
        }
        return rslt;
    }

    public StatisticalTest fTest(double[] s, int freq, String model, int ny) {
        DoubleSeq y = DoubleSeq.of(s).cleanExtremities();
        SarimaOrders.Prespecified M = SarimaOrders.Prespecified.valueOf(model);
        try {
            return new FTest(y, freq)
                    .model(M)
                    .ncycles(ny)
                    .build();
        } catch (Exception err) {
            return null;
        }
    }

    public CombinedSeasonality combinedTest(double[] s, int period, int startperiod, boolean mul) {
        DoubleSeq y = DoubleSeq.of(s).cleanExtremities();
        return new CombinedSeasonality(y, period, startperiod, mul ? 1 : 0);
    }

    public byte[] toBuffer(CombinedSeasonality cs) {
        return SaProtosUtility.convert(cs).toByteArray();
    }
}
