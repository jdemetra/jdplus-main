/*
 * Copyright 2023 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package jdplus.toolkit.base.api.modelling.regular;

import jdplus.toolkit.base.api.data.Parameter;
import nbbrd.design.Development;
import nbbrd.design.LombokWorkaround;
import jdplus.toolkit.base.api.timeseries.regression.IOutlier;
import jdplus.toolkit.base.api.timeseries.regression.InterventionVariable;
import jdplus.toolkit.base.api.timeseries.regression.Ramp;
import jdplus.toolkit.base.api.timeseries.regression.TsContextVariable;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.api.util.Validatable;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, buildMethodName = "buildWithoutValidation")
public final class RegressionSpec implements Validatable<RegressionSpec> {

    public static final double DEF_AICCDIFF = 0;
    public static final boolean DEF_CHECKMU = true;

    private double aicDiff;

    @lombok.NonNull
    MeanSpec mean;

    CalendarSpec calendar;

    @lombok.Singular
    List< Variable<IOutlier>> outliers;
    @lombok.Singular
    List< Variable<Ramp>> ramps;
    @lombok.Singular
    List< Variable<InterventionVariable>> interventionVariables;
    @lombok.Singular
    List< Variable<TsContextVariable>> userDefinedVariables;

    public static final RegressionSpec DEFAULT = RegressionSpec.builder().build();

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .aicDiff(DEF_AICCDIFF)
               .mean(MeanSpec.DEFAULT_UNUSED)
                 .calendar(CalendarSpec.builder().build());
    }

    public boolean isUsed() {
        return mean.isUsed() || calendar.isUsed() || !outliers.isEmpty()
                || !ramps.isEmpty() || !interventionVariables.isEmpty()
                || !userDefinedVariables.isEmpty();
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    @Override
    public RegressionSpec validate() throws IllegalArgumentException {
        return this;
    }

    public static class Builder implements Validatable.Builder<RegressionSpec> {

    }

    public boolean isSpecified() {
        return !(calendar.getEaster().isTest()
                || calendar.getTradingDays().isAutomatic()
                || calendar.getTradingDays().isTest()
                || mean.isTest());
    }

    public boolean hasFixedCoefficients() {
        if (!isUsed()) {
            return false;
        }
        return mean.hasFixedCoefficient() || calendar.hasFixedCoefficients()
                || outliers.stream().anyMatch(var -> !var.isFree())
                || ramps.stream().anyMatch(var -> !var.isFree())
                || interventionVariables.stream().anyMatch(var -> !var.isFree())
                || userDefinedVariables.stream().anyMatch(var -> !var.isFree());
    }
}
