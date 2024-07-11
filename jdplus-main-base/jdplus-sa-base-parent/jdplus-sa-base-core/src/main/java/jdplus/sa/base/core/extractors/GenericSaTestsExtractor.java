/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.base.core.extractors;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.sa.base.api.SaDictionaries;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import java.util.function.IntToDoubleFunction;
import jdplus.toolkit.base.core.regarima.tests.OneStepAheadForecastingTest;
import jdplus.sa.base.core.diagnostics.CombinedSeasonalityTests;
import jdplus.sa.base.core.diagnostics.GenericSaTests;
import jdplus.sa.base.core.diagnostics.ResidualSeasonalityTests;
import jdplus.sa.base.core.diagnostics.ResidualTradingDaysTests;
import jdplus.sa.base.core.tests.SpectralPeaks;
import jdplus.toolkit.base.api.stats.AutoCovariances;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(InformationExtractor.class)
public class GenericSaTestsExtractor extends InformationMapping<GenericSaTests> {

    @Override
    public Class getSourceClass() {
        return GenericSaTests.class;
    }

    public static final String LOG_STAT = "logstat",
            LEVEL_STAT = "levelstat",
            IC_RATIO = "ic-ratio",
            IC_RATIO_HENDERSON = "ic-ratio-henderson",
            MSR_GLOBAL = "msr-global",
            MSR = "msr";

    public GenericSaTestsExtractor() {

        delegate(null, CombinedSeasonalityTests.class, source -> source.combinedSeasonalityTests());
        delegate(null, OneStepAheadForecastingTest.class, source -> source.forecastingTest());

        //////////  Residuals 
        set(SaDictionaries.SEAS_RES_F, StatisticalTest.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnResiduals();
            if (test != null) {
                return test.fTest();
            }
            return null;
        });

        set(SaDictionaries.SEAS_RES_QS, StatisticalTest.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnResiduals();
            if (test != null) {
                return test.qsTest();
            }
            return null;
        });

        set(SaDictionaries.SEAS_RES_KW, StatisticalTest.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnResiduals();
            if (test != null) {
                return test.kruskalWallisTest();
            }
            return null;
        });

        set(SaDictionaries.SEAS_RES_FRIEDMAN, StatisticalTest.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnResiduals();
            if (test != null) {
                return test.friedmanTest();
            }
            return null;
        });

        set(SaDictionaries.SEAS_RES_PERIODOGRAM, StatisticalTest.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnResiduals();
            if (test != null) {
                return test.periodogramTest();
            }
            return null;
        });

        set(SaDictionaries.SEAS_RES_SP, String.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnResiduals();
            if (test != null) {
                SpectralPeaks[] p = test.spectralPeaks();
                if (p != null) {
                    return SpectralPeaks.format(p);
                }
            }
            return null;
        });

        /////////////////// Irregular
        set(SaDictionaries.SEAS_I_F, StatisticalTest.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnIrregular();
            if (test != null) {
                return test.fTest();
            }
            return null;
        });

        set(SaDictionaries.SEAS_I_QS, StatisticalTest.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnIrregular();
            if (test != null) {
                return test.qsTest();
            }
            return null;
        });

        set(SaDictionaries.SEAS_I_KW, StatisticalTest.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnIrregular();
            if (test != null) {
                return test.kruskalWallisTest();
            }
            return null;
        });

        set(SaDictionaries.SEAS_I_PERIODOGRAM, StatisticalTest.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnIrregular();
            if (test != null) {
                return test.periodogramTest();
            }
            return null;
        });

        set(SaDictionaries.SEAS_I_FRIEDMAN, StatisticalTest.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnIrregular();
            if (test != null) {
                return test.friedmanTest();
            }
            return null;
        });

        set(SaDictionaries.SEAS_I_SP, String.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnIrregular();
            if (test != null) {
                SpectralPeaks[] p = test.spectralPeaks();
                if (p != null) {
                    return SpectralPeaks.format(p);
                }
            }
            return null;
        });

        /////////////////////// SA
        set(SaDictionaries.SEAS_SA_F, StatisticalTest.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnSa();
            if (test != null) {
                return test.fTest();
            }
            return null;
        });

        set(SaDictionaries.SEAS_SA_QS, StatisticalTest.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnSa();
            if (test != null) {
                return test.qsTest();
            }
            return null;
        });

        set(SaDictionaries.SEAS_SA_KW, StatisticalTest.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnSa();
            if (test != null) {
                return test.kruskalWallisTest();
            }
            return null;
        });

        set(SaDictionaries.SEAS_SA_FRIEDMAN, StatisticalTest.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnSa();
            if (test != null) {
                return test.friedmanTest();
            }
            return null;
        });

        set(SaDictionaries.SEAS_SA_PERIODOGRAM, StatisticalTest.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnSa();
            if (test != null) {
                return test.periodogramTest();
            }
            return null;
        });

        set(SaDictionaries.SEAS_SA_SP, String.class, source -> {
            ResidualSeasonalityTests test = source.residualSeasonalityTestsOnSa();
            if (test != null) {
                SpectralPeaks[] p = test.spectralPeaks();
                if (p != null) {
                    return SpectralPeaks.format(p);
                }
            }
            return null;
        });
        
        /////////////////////// Linearized

        set(SaDictionaries.SEAS_LIN_F, StatisticalTest.class, source -> {
            ResidualSeasonalityTests test = source.seasonalityTestsOnLinearized();
            if (test != null) {
                return test.fTest();
            }
            return null;
        });

        set(SaDictionaries.SEAS_LIN_QS, StatisticalTest.class, source -> {
            ResidualSeasonalityTests test = source.seasonalityTestsOnLinearized();
            if (test != null) {
                return test.qsTest();
            }
            return null;
        });

        set(SaDictionaries.SEAS_LIN_KW, StatisticalTest.class, source -> {
            ResidualSeasonalityTests test = source.seasonalityTestsOnLinearized();
            if (test != null) {
                return test.kruskalWallisTest();
            }
            return null;
        });

        set(SaDictionaries.SEAS_LIN_FRIEDMAN, StatisticalTest.class, source -> {
            ResidualSeasonalityTests test = source.seasonalityTestsOnLinearized();
            if (test != null) {
                return test.friedmanTest();
            }
            return null;
        });

        set(SaDictionaries.SEAS_LIN_PERIODOGRAM, StatisticalTest.class, source -> {
            ResidualSeasonalityTests test = source.seasonalityTestsOnLinearized();
            if (test != null) {
                return test.periodogramTest();
            }
            return null;
        });

        set(SaDictionaries.SEAS_LIN_SP, String.class, source -> {
            ResidualSeasonalityTests test = source.seasonalityTestsOnLinearized();
            if (test != null) {
                SpectralPeaks[] p = test.spectralPeaks();
                if (p != null) {
                    return SpectralPeaks.format(p);
                }
            }
            return null;
        });
        
        ////////////////////////////////////////////////////////


        set(SaDictionaries.SEAS_SA_AC1, Double.class, source -> {
            DoubleSeq dsa = source.getLsa().delta(1).getValues();
            IntToDoubleFunction ac = AutoCovariances.autoCorrelationFunction(dsa, dsa.average());
            return ac.applyAsDouble(1);
        });

        set(SaDictionaries.TD_SA_ALL, StatisticalTest.class, source -> {
            ResidualTradingDaysTests td = source.residualTradingDaysTests();
            if (td != null) {
                return td.saTest(false);
            }
            return null;
        });

        set(SaDictionaries.TD_SA_LAST, StatisticalTest.class, source -> {
            ResidualTradingDaysTests td = source.residualTradingDaysTests();
            if (td != null) {
                return td.saTest(true);
            }
            return null;
        });

        set(SaDictionaries.TD_I_ALL, StatisticalTest.class, source -> {
            ResidualTradingDaysTests td = source.residualTradingDaysTests();
            if (td != null) {
                return td.irrTest(false);
            }
            return null;
        });

        set(SaDictionaries.TD_I_LAST, StatisticalTest.class, source -> {
            ResidualTradingDaysTests td = source.residualTradingDaysTests();
            if (td != null) {
                return td.irrTest(true);
            }
            return null;
        });

        set(SaDictionaries.TD_RES_ALL, StatisticalTest.class, source -> {
            ResidualTradingDaysTests td = source.residualTradingDaysTests();
            if (td != null) {
                return td.residualsTest(false);
            }
            return null;
        });

        set(SaDictionaries.TD_RES_LAST, StatisticalTest.class, source -> {
            ResidualTradingDaysTests td = source.residualTradingDaysTests();
            if (td != null) {
                return td.residualsTest(true);
            }
            return null;
        });
    }
}
