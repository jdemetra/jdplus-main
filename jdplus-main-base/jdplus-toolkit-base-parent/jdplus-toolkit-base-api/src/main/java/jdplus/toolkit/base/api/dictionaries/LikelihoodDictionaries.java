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

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class LikelihoodDictionaries {

    public final String LL = "ll", LLC = "adjustedll", SSQ = "ssqerr", AIC = "aic", BIC = "bic", AICC = "aicc", BICC = "bicc", BIC2 = "bic2", HQ = "hannanquinn",
            NPARAMS = "nparams", NOBS = "nobs", NEFFECTIVEOBS = "neffectiveobs", DF = "df", NDIFFUSE = "ndiffuse", DCORRECTION="dcorrection", MCORRECTION="mcorrection";

    public final AtomicDictionary LIKELIHOOD = AtomicDictionary.builder()
            .name("likelihood")
            .item(AtomicDictionary.Item.builder().name(LL).description("log-likelihood").outputClass(Double.class).build())
            .item(AtomicDictionary.Item.builder().name(LLC).description("adjusted log-likelihood").outputClass(Double.class).build())
            .item(AtomicDictionary.Item.builder().name(SSQ).description("sum of squares").outputClass(Double.class).build())
            .item(AtomicDictionary.Item.builder().name(AIC).description("aic").outputClass(Double.class).build())
            .item(AtomicDictionary.Item.builder().name(BIC).description("bic").outputClass(Double.class).build())
            .item(AtomicDictionary.Item.builder().name(AICC).description("aicc").outputClass(Double.class).build())
            .item(AtomicDictionary.Item.builder().name(BICC).description("bicc").outputClass(Double.class).build())
            .item(AtomicDictionary.Item.builder().name(BIC2).description("bic corrected for length").outputClass(Double.class).build())
            .item(AtomicDictionary.Item.builder().name(HQ).description("hannan-quinn").outputClass(Double.class).build())
            .item(AtomicDictionary.Item.builder().name(NPARAMS).description("number of parameters").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(NOBS).description("number of observations").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(NEFFECTIVEOBS).description("number of effective observations").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(DF).description("degrees of freedom (=number of effective obs - number of parameters)").outputClass(Integer.class).build())
            .build();

    public final AtomicDictionary DIFFUSELIKELIHOOD = AtomicDictionary.builder()
            .name("diffuse likelihood")
            .item(AtomicDictionary.Item.builder().name(LL).description("log-likelihood").outputClass(Double.class).build())
            .item(AtomicDictionary.Item.builder().name(LLC).description("adjusted log-likelihood").outputClass(Double.class).build())
            .item(AtomicDictionary.Item.builder().name(SSQ).description("sum of squares").outputClass(Double.class).build())
            .item(AtomicDictionary.Item.builder().name(AIC).description("aic").outputClass(Double.class).build())
            .item(AtomicDictionary.Item.builder().name(BIC).description("bic").outputClass(Double.class).build())
            .item(AtomicDictionary.Item.builder().name(AICC).description("aicc").outputClass(Double.class).build())
            .item(AtomicDictionary.Item.builder().name(HQ).description("hannan-quinn").outputClass(Double.class).build())
            .item(AtomicDictionary.Item.builder().name(NPARAMS).description("number of parameters").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(NOBS).description("number of observtions").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(NDIFFUSE).description("number of diffuse effects").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(DCORRECTION).description("correction for the diffuse effects").outputClass(Double.class).build())
            .item(AtomicDictionary.Item.builder().name(DF).description("degrees of freedom (=number of obs - number of diffuse - number of parameters)").outputClass(Integer.class).build())
            .build();

    public final AtomicDictionary MARGINALLIKELIHOOD = AtomicDictionary.builder()
            .name("marginal likelihood")
            .item(AtomicDictionary.Item.builder().name(MCORRECTION).description("marginal correction").outputClass(Double.class).build())
            .build();
}
