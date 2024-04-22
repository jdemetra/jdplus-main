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

import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.core.modelling.regular.tests.TradingDaysTest;

/**
 *
 * @author palatej
 */
@lombok.Getter
public class ResidualTradingDaysTests {

    private final TsData sa, irr, residuals;

    private final ResidualTradingDaysTestsOptions options;

    @lombok.Builder(builderClassName = "Builder")
    private ResidualTradingDaysTests(TsData sa, TsData irr, TsData residuals, ResidualTradingDaysTestsOptions options) {
        this.sa = sa;
        this.irr = irr;
        this.residuals = residuals;
        this.options = options;
    }

    @lombok.Getter(lombok.AccessLevel.PRIVATE)
    private volatile StatisticalTest saTest, irrTest, residualsTest, saLastTest, irrLastTest, residualsLastTest;

    public int annualFrequency() {
        return sa.getAnnualFrequency();
    }

    public StatisticalTest saTest(boolean last) {
        if (last && options.getFlast() > 0) {
            StatisticalTest test = saLastTest;
            if (test == null) {
                synchronized (this) {
                    test = saLastTest;
                    if (test == null) {
                        test = td2(sa, options.getFlast(), false);
                        saLastTest = test;
                    }
                }
            }
            return test;
        } else {
            StatisticalTest test = saTest;
            if (test == null) {
                synchronized (this) {
                    test = saTest;
                    if (test == null) {
                        test = td2(sa, 0, false);
                        saTest = test;
                    }
                }
            }
            return test;
        }
    }

    public StatisticalTest irrTest(boolean last) {
        if (last && options.getFlast() > 0) {
            StatisticalTest test = irrLastTest;
            if (test == null) {
                synchronized (this) {
                    test = irrLastTest;
                    if (test == null) {
                        test = td2(irr, options.getFlast(), false);
                        irrLastTest = test;
                    }
                }
            }
            return test;
        } else {
            StatisticalTest test = irrTest;
            if (test == null) {
                synchronized (this) {
                    test = irrTest;
                    if (test == null) {
                        test = td2(irr, 0, false);
                        irrTest = test;
                    }
                }
            }
            return test;
        }
    }

    public StatisticalTest residualsTest(boolean last) {
        if (residuals == null) {
            return null;
        }
        if (last && options.getFlast() > 0) {
            StatisticalTest test = residualsLastTest;
            if (test == null) {
                synchronized (this) {
                    test = residualsLastTest;
                    if (test == null) {
                        test = td(residuals, options.getFlast(), 0);
                        residualsLastTest = test;
                    }
                }
            }
            return test;
        } else {
            StatisticalTest test = residualsTest;
            if (test == null) {
                synchronized (this) {
                    test = residualsTest;
                    if (test == null) {
                        test = td(residuals, 0, 0);
                        residualsTest = test;
                    }
                }
            }
            return test;
        }
    }

    private StatisticalTest td(TsData s, int ny, int lag) {
        int ifreq = annualFrequency();
        if (ny > 0) {
            s = s.drop(Math.max(0, s.length() - ifreq * ny), 0);
        }
        return TradingDaysTest.olsTest(s, lag);
    }

    private StatisticalTest td2(TsData s, int ny, boolean seas) {
        int ifreq = annualFrequency();
        if (ny > 0) {
            s = s.drop(Math.max(0, s.length() - ifreq * ny), 0);
        }
        return TradingDaysTest.maTest(s, seas);
    }

}
