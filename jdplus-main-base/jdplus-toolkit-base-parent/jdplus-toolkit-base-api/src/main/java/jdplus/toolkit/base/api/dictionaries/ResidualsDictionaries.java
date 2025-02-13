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

import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.dictionaries.AtomicDictionary.Item;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class ResidualsDictionaries {

    public final String RES = "res", TSRES = "tsres", SER = "ser", SER_ML="ser_ml", TYPE = "type", N="n", DF="df", DFC="dfc";
    
    public final String MEAN = "mean", SKEW = "skewness", KURT = "kurtosis", DH = "doornikhansen", LB = "lb", LB2 = "lb2",
            SEASLB = "seaslb", BP = "bp", BP2 = "bp2", SEASBP = "seasbp", NRUNS= "nruns", LRUNS = "lruns",
            NUDRUNS = "nudruns", LUDRUNS = "ludruns" ; 
    
    public final Dictionary RESIDUALS = AtomicDictionary.builder()
            .name("residuals")
            .item(Item.builder().name(RES).description("residuals").outputClass(double[].class).build())
            .item(Item.builder().name(TSRES).description("timeseries residuals").outputClass(TsData.class).build())
            .item(Item.builder().name(N).description("number of obs").outputClass(Integer.class).build())
            .item(Item.builder().name(DF).description("degrees of freedom (number of independent residuals)").outputClass(Integer.class).build())
            .item(Item.builder().name(DFC).description("degrees of freedom corrected by the number of hyper-parameters").outputClass(Integer.class).build())
            .item(Item.builder().name(SER).description("standard error of the residuals, using degrres of freedom corrected by the number of hyper-parameters").outputClass(Double.class).build())
            .item(Item.builder().name(SER_ML).description("standard error of the residuals").outputClass(Double.class).build())
            .item(Item.builder().name(TYPE).description("outputClass of residuals").outputClass(String.class).build())
            .build();
    
    public final Dictionary RESIDUALS_TESTS = AtomicDictionary.builder()
            .name("tests on residuals")
            .item(Item.builder().name(MEAN).description("mean test").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SKEW).description("skewness tess ").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(KURT).description("kurtosis test").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(DH).description("doornik-hansen normality test").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(LB).description("ljung-box test").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(BP).description("box-pierce test").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(LB2).description("ljun-box test on squares").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(BP2).description("box-pierce test on squares").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEASLB).description("seasonal ljung-box test").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEASBP).description("seasonal box-pierce test").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(NRUNS).description("test on the number of runs").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(LRUNS).description("test on the length of runs").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(NUDRUNS).description("test on the number of up and down runs").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(LUDRUNS).description("test on the length of up and down runs").outputClass(StatisticalTest.class).build())
            .build();
    
        public final Dictionary RESIDUALS_DEFAULT = ComplexDictionary.builder()
                .dictionary(new PrefixedDictionary(null, RESIDUALS))
                .dictionary(new PrefixedDictionary(null, RESIDUALS_TESTS))
                .build();
                

}
