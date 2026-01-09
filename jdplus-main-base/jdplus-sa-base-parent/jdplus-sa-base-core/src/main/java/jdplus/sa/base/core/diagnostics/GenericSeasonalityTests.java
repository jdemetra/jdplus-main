/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.sa.base.core.diagnostics;

import java.util.concurrent.atomic.AtomicInteger;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.modelling.DifferencingResult;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.sa.base.core.tests.AutoRegressiveSpectrumTest;
import jdplus.sa.base.core.tests.FTest;
import jdplus.sa.base.core.tests.Friedman;
import jdplus.sa.base.core.tests.KruskalWallis;
import jdplus.sa.base.core.tests.PeriodogramTest;
import jdplus.sa.base.core.tests.Qs;
import jdplus.sa.base.core.tests.SpectralPeaks;
import jdplus.sa.base.core.tests.TukeySpectrumPeaksTest;
import jdplus.toolkit.base.api.arima.SarimaOrders;

/**
 *
 * @author palatej
 */
@lombok.Getter
public class GenericSeasonalityTests {
    
    private final TsData series;
    private final DifferencingResult differencing;

    private final int ncycles;

    @lombok.Builder(builderClassName = "Builder")
    private GenericSeasonalityTests(TsData series, int ndiff, boolean mean, int ncycles) {
        this.series = series;
        this.differencing = DifferencingResult.of(series.getValues(), series.getAnnualFrequency(), ndiff, mean);
        this.ncycles = ncycles;
    }

    @lombok.Getter(lombok.AccessLevel.PRIVATE)
    private volatile StatisticalTest fTest, qsTest, friedmanTest, kruskalWallisTest, periodogramTest;

    @lombok.Getter(lombok.AccessLevel.PRIVATE)
    @SuppressWarnings("VolatileArrayField")
    private volatile SpectralPeaks[] spectralPeaks;

    public int annualFrequency() {
        return series.getAnnualFrequency();
    }

    public StatisticalTest qsTest() {
        StatisticalTest test = qsTest;
        if (test == null) {
            synchronized (this) {
                test = qsTest;
                if (test == null) {
                    DoubleSeq ds = differencing.getDifferenced();
                    if (ncycles > 0) {
                        ds = ds.drop(Math.max(0, ds.length() - annualFrequency() * ncycles), 0);
                    }
                    test = new Qs(ds, annualFrequency())
                            .autoCorrelationsCount(2)
                            .build();
                    qsTest = test;
                }
            }
        }
        return test;
    }

    public StatisticalTest friedmanTest() {
        StatisticalTest test = friedmanTest;
        if (test == null) {
            synchronized (this) {
                test = friedmanTest;
                if (test == null) {
                    DoubleSeq ds = differencing.getDifferenced();
                    if (ncycles > 0) {
                        ds = ds.drop(Math.max(0, ds.length() - annualFrequency() * ncycles), 0);
                    }
                    test = new Friedman(ds, annualFrequency())
                            .build();
                    friedmanTest = test;
                }
            }
        }
        return test;
    }

    public StatisticalTest kruskalWallisTest() {
        StatisticalTest test = kruskalWallisTest;
        if (test == null) {
            synchronized (this) {
                test = kruskalWallisTest;
                if (test == null) {
                    DoubleSeq ds = differencing.getDifferenced();
                    if (ncycles > 0) {
                        ds = ds.drop(Math.max(0, ds.length() - annualFrequency() * ncycles), 0);
                    }
                    test = new KruskalWallis(ds, annualFrequency())
                            .build();
                    kruskalWallisTest = test;
                }
            }
        }
        return test;
    }

    public StatisticalTest periodogramTest() {
        StatisticalTest test = periodogramTest;
        if (test == null) {
            synchronized (this) {
                test = periodogramTest;
                if (test == null) {
                    DoubleSeq ds = differencing.getDifferenced();
                    if (ncycles > 0) {
                        ds = ds.drop(Math.max(0, ds.length() - annualFrequency() * ncycles), 0);
                    }
                    test = new PeriodogramTest(ds, annualFrequency()).buildF();
                    periodogramTest = test;
                }
            }
        }
        return test;
    }

    public StatisticalTest fTest() {
        StatisticalTest test = fTest;
        if (test == null) {
            synchronized (this) {
                test = fTest;
                if (test == null) {
                    DoubleSeq ds = differencing.getDifferenced();
                    if (ncycles > 0) {
                        ds = ds.drop(Math.max(0, ds.length() - annualFrequency() * ncycles-1), 0);
                    }
                    test = new FTest(ds, annualFrequency())
                            .model(SarimaOrders.Prespecified.AR)
                            .build();
                    fTest = test;
                }
            }
        }
        return test;
    }

    public SpectralPeaks[] spectralPeaks() {
        SpectralPeaks[] sp = spectralPeaks;
        if (sp == null) {
            synchronized (this) {
                sp = spectralPeaks;
                if (sp == null) {
                    int ifreq = annualFrequency();
                    DoubleSeq ds = differencing.getDifferenced();
                    if (ncycles > 0) {
                        ds = ds.drop(Math.max(0, ds.length() - annualFrequency() * ncycles), 0);
                    }
                    AutoRegressiveSpectrumTest arPeaks = new AutoRegressiveSpectrumTest();
                    TukeySpectrumPeaksTest tPeaks = new TukeySpectrumPeaksTest();
                    if (!arPeaks.test(ds, ifreq) || !tPeaks.test(ds, ifreq)) {
                        return null;
                    }
                    int[] a = arPeaks.seasonalPeaks(.90, .99);
                    int[] t = tPeaks.seasonalPeaks(.90, .99);
                    sp = new SpectralPeaks[ifreq / 2];
                    for (int i = 0; i < sp.length; ++i) {
                        SpectralPeaks.AR ar = SpectralPeaks.AR.none;
                        SpectralPeaks.Tukey tu = SpectralPeaks.Tukey.none;
                        if (a != null && a.length > i ) {
                            ar = SpectralPeaks.AR.fromInt(a[i]);
                        }
                        if (t != null && t.length > i) {
                            tu = SpectralPeaks.Tukey.fromInt(t[i]);
                        }
                        sp[i] = new SpectralPeaks(ar, tu);
                    }
                    spectralPeaks = sp;
                }
            }
        }
        return sp;
    }
}
