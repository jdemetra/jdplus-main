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

import jdplus.toolkit.base.api.information.GenericExplorable;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.core.regarima.diagnostics.RegArimaDiagnostics;
import jdplus.toolkit.base.core.regarima.tests.OneStepAheadForecastingTest;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.toolkit.base.core.sarima.SarimaModel;

/**
 *
 * @author palatej
 */
public class GenericSaTests implements GenericExplorable {

    @lombok.Getter
    private final RegSarimaModel regarima;

    @lombok.Getter
    private final boolean mul;

    @lombok.Getter
    private final TsData linearized, residuals;

    @lombok.Getter
    private final TsData y, sa, irr, si;

    @lombok.Getter
    private final TsData lsa, lirr;

    @lombok.Builder(builderClassName = "Builder")
    private GenericSaTests(RegSarimaModel regarima, boolean mul,
            TsData y, TsData res, TsData sa, TsData irr, TsData si, TsData lin, TsData lsa, TsData lirr) {
        this.regarima = regarima;
        this.mul = mul;
        this.linearized = lin;
        this.residuals = res;
        this.y = y;
        this.sa = sa;
        this.irr = irr;
        this.si = si;
        this.lsa = lsa;
        this.lirr = lirr;
    }

    @lombok.Getter(lombok.AccessLevel.PRIVATE)
    private volatile ResidualSeasonalityTests linearizedTests, residualsTests, lsaTests, lirrTests;
    @lombok.Getter(lombok.AccessLevel.PRIVATE)
    private volatile CombinedSeasonalityTests combinedTests;
    @lombok.Getter(lombok.AccessLevel.PRIVATE)
    private volatile ResidualTradingDaysTests tdTests;
    @lombok.Getter(lombok.AccessLevel.PRIVATE)
    private volatile OneStepAheadForecastingTest outOfSampleTest;

    public int annualFrequency() {
        return linearized.getAnnualFrequency();
    }

    private int differencingOrder() {
        if (regarima != null) {
            SarimaModel arima = regarima.arima();
            return arima.getD() + arima.getBd();
        } else {
            return -1;
        }
    }

    private boolean mean() {
        if (regarima != null) {
            return regarima.isMeanCorrection();
        } else {
            return true;
        }
    }

    public ResidualSeasonalityTests residualSeasonalityTestsOnResiduals() {
        if (residuals == null) {
            return null;
        }
        ResidualSeasonalityTests tests = residualsTests;
        if (tests == null) {
            synchronized (this) {
                tests = residualsTests;
                if (tests == null) {
                    tests = ResidualSeasonalityTests.builder()
                            .series(residuals)
                            .ndiff(0)
                            .mean(false)
                            .options(ResidualSeasonalityTestsOptions.getDefault())
                            .build();
                    residualsTests = tests;
                }
            }
        }
        return tests;
    }

    public ResidualSeasonalityTests seasonalityTestsOnLinearized() {
        if (linearized == null) {
            return null;
        }
        ResidualSeasonalityTests tests = linearizedTests;
        if (tests == null) {
            synchronized (this) {
                tests = linearizedTests;
                if (tests == null) {
                    tests = ResidualSeasonalityTests.builder()
                            .series(linearized)
                            .ndiff(differencingOrder())
                            .mean(mean())
                            .options(ResidualSeasonalityTestsOptions.getDefault())
                            .build();
                    linearizedTests = tests;
                }
            }
        }
        return tests;
    }

    public ResidualSeasonalityTests residualSeasonalityTestsOnSa() {
        if (lsa == null) {
            return null;
        }
        ResidualSeasonalityTests tests = lsaTests;
        if (tests == null) {
            synchronized (this) {
                tests = lsaTests;
                if (tests == null) {
                    tests = ResidualSeasonalityTests.builder()
                            .series(lsa)
                            .ndiff(differencingOrder())
                            .mean(mean())
                            .options(ResidualSeasonalityTestsOptions.getDefault())
                            .build();
                    lsaTests = tests;
                }
            }
        }
        return tests;
    }

    public ResidualSeasonalityTests residualSeasonalityTestsOnIrregular() {
        if (lirr == null) {
            return null;
        }
        ResidualSeasonalityTests tests = lirrTests;
        if (tests == null) {
            synchronized (this) {
                tests = lirrTests;
                if (tests == null) {
                    tests = ResidualSeasonalityTests.builder()
                            .series(lirr)
                            .ndiff(0)
                            .mean(false)
                            .options(ResidualSeasonalityTestsOptions.getDefault())
                            .build();
                    lirrTests = tests;
                }
            }
        }
        return tests;
    }

    public CombinedSeasonalityTests combinedSeasonalityTests() {
        CombinedSeasonalityTests tests = combinedTests;
        if (tests == null) {
            synchronized (this) {
                tests = combinedTests;
                if (tests == null) {
                    tests = CombinedSeasonalityTests.builder()
                            .y(y)
                            .sa(sa)
                            .si(si)
                            .irr(irr)
                            .mul(mul)
                            .residuals(residuals)
                            .options(CombinedSeasonalityOptions.getDefault())
                            .build();
                    combinedTests = tests;
                }
            }
        }
        return tests;
    }

    public ResidualTradingDaysTests residualTradingDaysTests() {
        ResidualTradingDaysTests tests = tdTests;
        if (tests == null) {
            synchronized (this) {
                tests = tdTests;
                if (tests == null) {
                    tests = ResidualTradingDaysTests.builder()
                            .sa(lsa)
                            .irr(lirr)
                            .residuals(residuals)
                            .options(ResidualTradingDaysTestsOptions.getDefault())
                            .build();
                    tdTests = tests;
                }
            }
        }
        return tests;
    }

    public OneStepAheadForecastingTest forecastingTest() {
        if (regarima == null) {
            return null;
        }
        OneStepAheadForecastingTest os = outOfSampleTest;
        if (os == null) {
            synchronized (this) {
                os = outOfSampleTest;
                if (os == null) {
                    try {
                        os = RegArimaDiagnostics.oneStepAheadForecastingTest(regarima.regarima(), 0);
                        outOfSampleTest = os;
                    } catch (Exception err) {

                    }
                }
            }
        }
        return os;
    }

}
