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
package jdplus.sa.base.information.highfreq;

import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.api.modelling.TransformationType;
import jdplus.toolkit.base.api.modelling.highfreq.TransformSpec;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TransformSpecMapping {

    final String FN = "function",
            AICDIFF = "aicdiff"
            ;

    public InformationSet write(TransformSpec spec, boolean verbose) {
        if (!verbose && spec.isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (verbose || spec.getFunction() != TransformationType.None) {
            info.add(FN, spec.getFunction().name());
        }
        return info;
    }

    public TransformSpec read(InformationSet info) {
        if (info == null) {
            return TransformSpec.DEFAULT;
        }
        TransformSpec.Builder builder = TransformSpec.builder();
        String fn = info.get(FN, String.class);
        if (fn != null) {
            builder = builder.function(TransformationType.valueOf(fn));
        }
        Double aic = info.get(AICDIFF, Double.class);
        if (aic != null) {
            builder.aicDiff(aic);
        }
        return builder.build();
    }

}
