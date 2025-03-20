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

import jdplus.toolkit.base.api.timeseries.regression.RegressionItem;
import jdplus.toolkit.base.api.dictionaries.AtomicDictionary.Item;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class ArimaDictionaries {

    public final String AR = "ar", // Stationary auto-regressive polynomial
            DELTA = "delta", // Differencing polynomial
            MA = "ma", // Moving average polynomial
            VAR = "var";// Innovation variance

    public final Dictionary ARIMA = AtomicDictionary.builder()
            .name("arima")
            .item(Item.builder().name(AR).description("stationary autoregressive polynomial").outputClass(double[].class).build())
            .item(Item.builder().name(DELTA).description("non-stationary autoregressive polynomial").outputClass(double[].class).build())
            .item(Item.builder().name(MA).description("moving-average polynomial").outputClass(double[].class).build())
            .item(Item.builder().name(VAR).description("innovation variance").outputClass(Double.class).build())
            .build();

    public final static String P = "p", D = "d", Q = "q",
            BP = "bp", BD = "bd", BQ = "bq",
            PARAMETERS = "parameters", PARAMETERS2 = "parameters2",
            PHI = "phi", THETA = "theta", BPHI = "bphi", BTHETA = "btheta",
           PERIOD = "period";

    public final Dictionary SARIMA = AtomicDictionary.builder()
            .name("sarima")
            .item(Item.builder().name(PERIOD).description("period of the seasonal part").outputClass(Integer.class).build())
            .item(Item.builder().name(P).description("regular autoregressive order").outputClass(Integer.class).build())
            .item(Item.builder().name(D).description("regular differencing order").outputClass(Integer.class).build())
            .item(Item.builder().name(Q).description("regular moving-average order").outputClass(Integer.class).build())
            .item(Item.builder().name(BP).description("seasonal autoregressive order").outputClass(Integer.class).build())
            .item(Item.builder().name(BD).description("seasonal differencing order").outputClass(Integer.class).build())
            .item(Item.builder().name(BQ).description("seasonal moving-average order").outputClass(Integer.class).build())
            .item(Item.builder().name(PHI).description("regular autoregressive parameters").outputClass(double[].class).build())
            .item(Item.builder().name(THETA).description("regular moving-average parameters").outputClass(double[].class).build())
            .item(Item.builder().name(BPHI).description("seasonal autoregressive parameters").outputClass(double[].class).build())
            .item(Item.builder().name(BTHETA).description("seasonal moving-average parameters").outputClass(double[].class).build())
            .item(Item.builder().name(THETA).description("regular moving-average parameter").outputClass(Double.class).type(Dictionary.EntryType.Array).build())
            .item(Item.builder().name(PHI).description("seasonal autoregressive parameter").outputClass(Double.class).type(Dictionary.EntryType.Array).build())
            .item(Item.builder().name(BTHETA).description("seasonal moving-average parameter").outputClass(Double.class).type(Dictionary.EntryType.Array).build())
            .item(Item.builder().name(BPHI).description("seasonal autoregressive parameter").outputClass(Double.class).type(Dictionary.EntryType.Array).build())
            .item(Item.builder().name(PARAMETERS).description("phi, bphi, theta, btheta").outputClass(double[].class).build())
            .item(Item.builder().name(PARAMETERS2).description("-phi, -bphi, theta, btheta").outputClass(double[].class).build())
            .build();

    public final Dictionary SARIMA_ESTIMATION = AtomicDictionary.builder()
            .name("sarima")
            .item(Item.builder().name(P).description("regular autoregressive order").outputClass(Integer.class).build())
            .item(Item.builder().name(D).description("regular differencing order").outputClass(Integer.class).build())
            .item(Item.builder().name(Q).description("regular moving-average order").outputClass(Integer.class).build())
            .item(Item.builder().name(BP).description("seasonal autoregressive order").outputClass(Integer.class).build())
            .item(Item.builder().name(BD).description("seasonal differencing order").outputClass(Integer.class).build())
            .item(Item.builder().name(BQ).description("seasonal moving-average order").outputClass(Integer.class).build())
            .item(Item.builder().name(THETA).description("regular moving-average parameter").outputClass(RegressionItem.class).type(Dictionary.EntryType.Array).build())
            .item(Item.builder().name(PHI).description("seasonal autoregressive parameter").outputClass(RegressionItem.class).type(Dictionary.EntryType.Array).build())
            .item(Item.builder().name(BTHETA).description("seasonal moving-average parameter").outputClass(RegressionItem.class).type(Dictionary.EntryType.Array).build())
            .item(Item.builder().name(BPHI).description("seasonal autoregressive parameter").outputClass(RegressionItem.class).type(Dictionary.EntryType.Array).build())
            .build();

    public final  String COMPONENT = "component", COMPONENTC = "complement", MODEL = "model", 
            SUM = "sum", // Reduced model
            SIZE = "size";  // Number of components
    
    private final Dictionary UCARIMA_DETAILS=AtomicDictionary.builder()
                            .item(Item.builder()
                                    .name(SIZE).description("number of components").outputClass(Integer.class).build())
                            .build();

    public final Dictionary UCARIMA = ComplexDictionary.builder()
            .dictionary(new PrefixedDictionary(null, UCARIMA_DETAILS))
            .dictionary(new PrefixedDictionary(MODEL, ARIMA))
            .dictionary(new PrefixedDictionary(COMPONENT, ARIMA, Dictionary.EntryType.Array))
            .dictionary(new PrefixedDictionary(COMPONENTC, ARIMA, Dictionary.EntryType.Array))
            .dictionary(new PrefixedDictionary(SUM, ARIMA))
            .build();

}
