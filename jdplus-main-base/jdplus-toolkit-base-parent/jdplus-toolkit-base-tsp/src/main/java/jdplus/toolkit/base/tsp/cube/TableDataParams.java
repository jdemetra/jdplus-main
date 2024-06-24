/*
 * Copyright 2017 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.toolkit.base.tsp.cube;

import jdplus.toolkit.base.api.util.Validatable;
import jdplus.toolkit.base.api.util.Validations;
import jdplus.toolkit.base.tsp.util.ObsFormat;
import lombok.NonNull;
import nbbrd.design.LombokWorkaround;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(buildMethodName = "buildWithoutValidation")
public class TableDataParams implements Validatable<TableDataParams> {

    @lombok.NonNull
    String periodColumn;

    @lombok.NonNull
    String valueColumn;

    @lombok.NonNull
    String versionColumn;

    @lombok.NonNull
    ObsFormat obsFormat;

    @LombokWorkaround
    public static TableDataParams.Builder builder() {
        return new Builder()
                .versionColumn("")
                .obsFormat(ObsFormat.DEFAULT);
    }

    @Override
    public @NonNull TableDataParams validate() throws IllegalArgumentException {
        Validations.notBlank(periodColumn, "periodColumn");
        Validations.notBlank(valueColumn, "valueColumn");
        return this;
    }

    public static final class Builder implements Validatable.Builder<TableDataParams> {
    }
}
