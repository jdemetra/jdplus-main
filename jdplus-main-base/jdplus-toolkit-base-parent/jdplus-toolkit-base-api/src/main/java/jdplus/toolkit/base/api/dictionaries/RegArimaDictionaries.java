/*
 * Copyright 2025 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.toolkit.base.api.dictionaries;

import jdplus.toolkit.base.api.dictionaries.AtomicDictionary.Item;
import jdplus.toolkit.base.api.stats.StatisticalTest;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RegArimaDictionaries {

    public final String LOG = "log",
            ADJUST = "adjust", MEAN = "mean",
            SPAN = "span", ESPAN="espan", PERIOD = "period", ARIMA = "arima",
            REGRESSION = "regression", LIKELIHOOD = "likelihood", RESIDUALS = "residuals",
            MAX = "regression.ml", ADVANCED = "regression.details";
    
    public final Dictionary REGSARIMA = ComplexDictionary.builder()
            .dictionary(new PrefixedDictionary(null, RegressionDictionaries.BASIC))
            .dictionary(new PrefixedDictionary(LIKELIHOOD, LikelihoodDictionaries.LIKELIHOOD))
            .dictionary(new PrefixedDictionary(ARIMA, ArimaDictionaries.SARIMA_ESTIMATION))
            .dictionary(new PrefixedDictionary(REGRESSION, RegressionDictionaries.REGRESSION_DESC))
            .dictionary(new PrefixedDictionary(REGRESSION, RegressionDictionaries.REGRESSION_EST))
            .dictionary(new PrefixedDictionary(RESIDUALS, ResidualsDictionaries.RESIDUALS_DEFAULT))
            .dictionary(new PrefixedDictionary(MAX, UtilityDictionaries.LL_MAX))
            .dictionary(new PrefixedDictionary(ADVANCED, RegressionDictionaries.REGRESSION_UTILITY))
            .dictionary(new PrefixedDictionary(null, RegressionDictionaries.REGRESSION_EFFECTS))
            .build();
    
     public static final String FCAST_INSAMPLE_MEAN = "fcast-insample-mean",
            FCAST_OUTSAMPLE_MEAN = "fcast-outsample-mean",
            FCAST_OUTSAMPLE_VARIANCE = "fcast-outsample-variance";
    
    public final Dictionary REGSARIMA_DIAGNOSTICS = AtomicDictionary.builder()
            .name("regarima diagnostics")
            .item(Item.builder().name(FCAST_INSAMPLE_MEAN).description("in sample forecast mean test").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(FCAST_OUTSAMPLE_MEAN).description("out of sample forecast mean test").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(FCAST_OUTSAMPLE_VARIANCE).description("out of sample forecast variance test").outputClass(StatisticalTest.class).build())
            .build();
}
