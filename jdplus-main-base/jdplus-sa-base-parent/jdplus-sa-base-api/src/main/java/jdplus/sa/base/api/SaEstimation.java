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
package jdplus.sa.base.api;

import jdplus.toolkit.base.api.processing.ProcDiagnostic;
import jdplus.toolkit.base.api.processing.ProcQuality;

import java.util.Collections;
import java.util.List;
import jdplus.toolkit.base.api.information.GenericExplorable;

/**
 *
 * @author PALATEJ
 */
@lombok.Getter
@lombok.experimental.FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@lombok.AllArgsConstructor
@lombok.Builder(toBuilder = true)
public class SaEstimation {

    /**
     * Results of the estimation
     */
    GenericExplorable results;

    @lombok.Singular
    List<ProcDiagnostic> diagnostics;
    
    @lombok.Singular
    List<String> warnings;

    @lombok.With
    ProcQuality quality;

    /**
     * Specification corresponding to the results of the current estimation (fully identified model)
     */
    SaSpecification pointSpec;

    /**
     * Warnings on the current estimation
     *
     * @return
     */
    public List<String> warnings() {
        return Collections.unmodifiableList(warnings);
    }

    SaEstimation flush() {
        if (results == null)
            return this;
        return builder()
                .pointSpec(pointSpec)
                .quality(quality)
                .build();
    }
}
