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

import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.dictionaries.AtomicDictionary.Item;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class UtilityDictionaries {

    public final String START = "start", END = "end", N = "n", NM = "missing";

    public final Dictionary OBS_SPAN = AtomicDictionary.builder()
            .name("span")
            .item(Item.builder().name(START).description("start").outputClass(TsPeriod.class).build())
            .item(Item.builder().name(END).description("end").outputClass(TsPeriod.class).build())
            .item(Item.builder().name(N).description("number of obs").outputClass(Integer.class).build())
            .item(Item.builder().name(NM).description("number of missing").outputClass(Integer.class).build())
            .build();

    public final String P = "parameters", PCOVAR = "pcovar", PCOVAR_ML = "pcovar-ml", PCORR = "pcorr", SCORE = "pscore";

    public final Dictionary LL_MAX = AtomicDictionary.builder()
            .name("span")
            .item(Item.builder().name(P).description("parameters").outputClass(double[].class).build())
            .item(Item.builder().name(PCOVAR).description("unbiased covariance of the parameters").outputClass(Matrix.class).build())
            .item(Item.builder().name(PCOVAR_ML).description("maximum-likelihood covariance of the parameters").outputClass(Matrix.class).build())
            .item(Item.builder().name(PCORR).description("correlations of the parameters (unbiased)").outputClass(Matrix.class).build())
            .item(Item.builder().name(SCORE).description("scores of the parameters").outputClass(double[].class).build())
            .build();

}
