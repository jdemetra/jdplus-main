/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.base.core.extractors;

import jdplus.toolkit.base.api.dictionaries.RegArimaDictionaries;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.core.regarima.tests.OneStepAheadForecastingTest;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(InformationExtractor.class)
public class OneStepAheadForecastingTestExtractor extends InformationMapping<OneStepAheadForecastingTest> {


    public OneStepAheadForecastingTestExtractor() {
        set(RegArimaDictionaries.FCAST_INSAMPLE_MEAN, StatisticalTest.class, source -> {
            return source.inSampleMeanTest();
        });

        set(RegArimaDictionaries.FCAST_OUTSAMPLE_MEAN, StatisticalTest.class, source -> {
            return source.outOfSampleMeanTest();
        });

        set(RegArimaDictionaries.FCAST_OUTSAMPLE_VARIANCE, StatisticalTest.class, source -> {
            return source.sameVarianceTest();
        });
    }

    @Override
    public Class getSourceClass() {
        return OneStepAheadForecastingTest.class;
    }
}
