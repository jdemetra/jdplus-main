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
import jdplus.sa.base.core.tests.ModifiedQs;
import jdplus.sa.base.core.tests.PeriodogramTest;
import jdplus.sa.base.core.tests.Qs;
import jdplus.toolkit.base.core.data.analysis.WindowFunction;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class SeasonalityTests {

    public StatisticalTest qsTest(double[] s, int period, int ny) {
        return qsTest(s, period, ny, 1);
    }

    public double modifiedQsTest(double[] s, int period, int ny) {
        DoubleSeq y = DoubleSeq.of(s).cleanExtremities();
        if (ny != 0) {
            y = y.drop(Math.max(0, y.length() - period * ny), 0);
        }
        return ModifiedQs.test(y, period);
    }

    public StatisticalTest qsTest(double[] s, int period, int ny, int type) {
        DoubleSeq y = DoubleSeq.of(s).cleanExtremities();
        if (ny != 0) {
            y = y.drop(Math.max(0, y.length() - period * ny), 0);
        }
        Qs qs = new Qs(y, period)
                .autoCorrelationsCount(2);
        switch (type) {
            case 1 ->
                qs.usePositiveAutocorrelations();
            case -1 ->
                qs.useNegativeAutocorrelations();
            default ->
                qs.useAllAutocorrelations();
        }
        return qs.build();
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

    public double[] canovaHansenTrigs(double[] s, double[] periods, boolean lag1, String kernel, int truncation, boolean original) {
        DoubleSeq x = DoubleSeq.of(s).cleanExtremities();
        if (truncation < 0) {
            truncation = (int) Math.floor(0.75 * Math.sqrt(x.length()));
        }
        double[] rslt = new double[periods.length];
        for (int i = 0; i < periods.length; ++i) {
            double p = periods[i];
            if (original) {
                rslt[i] = CanovaHansen.test(x)
                        .specific(p, 1)
                        .lag1(lag1)
                        .windowFunction(WindowFunction.valueOf(kernel))
                        .truncationLag(truncation)
                        .build()
                        .testAll();
            } else {
                rslt[i] = CanovaHansen2.of(x)
                        .periodicity(p)
                        .lag1(lag1)
                        .windowFunction(WindowFunction.valueOf(kernel))
                        .truncationLag(truncation)
                        .compute();
            }
        }
        return rslt;
    }

    public double[] canovaHansen(double[] s, int period, String type, boolean lag1, String kernel, int truncation, int start) {
        DoubleSeq x = DoubleSeq.of(s).cleanExtremities();
        if (truncation < 0) {
            truncation = (int) Math.floor(0.75 * Math.sqrt(x.length()));
        }
        CanovaHansen.Builder builder = CanovaHansen.test(x)
                .lag1(lag1)
                .truncationLag(truncation)
                .windowFunction(WindowFunction.valueOf(kernel))
                .startPosition(start);
        CanovaHansen.Variables vars = CanovaHansen.Variables.valueOf(type);
        if (null == vars) {
            return null;
        }
        switch (vars) {
            case Trigonometric -> {
                CanovaHansen ch = builder.trigonometric(period).build();
                boolean even = period % 2 == 0;
                int p2 = period / 2;
                int nq = even ? p2 - 1 : p2;
                double[] q = new double[3 + p2];
                int icur = 0;
                for (int i = 0; i < nq; ++i, ++icur) {
                    q[icur] = ch.test(i * 2, 2);
                }
                if (even) {
                    q[icur++] = ch.test(period - 2);
                }
                q[icur++] = ch.testAll();
                StatisticalTest seasonalityTest = ch.seasonalityTest();
                q[icur++] = seasonalityTest.getValue();
                q[icur] = seasonalityTest.getPvalue();
                return q;
            }
            case Dummy -> {
                CanovaHansen ch = builder.dummies(period).build();
                double[] q = new double[period + 3];
                for (int i = 0; i < period; ++i) {
                    q[i] = ch.test(i);
                }
                q[period] = ch.testAll();
                StatisticalTest seasonalityTest = ch.seasonalityTest();
                q[period + 1] = seasonalityTest.getValue();
                q[period + 2] = seasonalityTest.getPvalue();
                return q;
            }
            default -> {
                CanovaHansen ch = builder.contrasts(period).build();
                double[] q = new double[period + 3];
                for (int i = 0; i < period - 1; ++i) {
                    q[i] = ch.test(i);
                }
                q[period - 1] = ch.testDerived();
                q[period] = ch.testAll();
                StatisticalTest seasonalityTest = ch.seasonalityTest();
                q[period + 1] = seasonalityTest.getValue();
                q[period + 2] = seasonalityTest.getPvalue();
                return q;

            }
        }
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
