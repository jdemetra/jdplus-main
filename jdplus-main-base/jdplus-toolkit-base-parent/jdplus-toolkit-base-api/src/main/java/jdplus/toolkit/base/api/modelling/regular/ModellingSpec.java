/*
 * Copyright 2023 National Bank of Belgium
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
package jdplus.toolkit.base.api.modelling.regular;

import jdplus.toolkit.base.api.arima.SarimaSpec;
import jdplus.toolkit.base.api.modelling.TransformationType;
import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;
import jdplus.toolkit.base.api.processing.ProcSpecification;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.api.util.Validatable;
import nbbrd.design.Development;
import nbbrd.design.LombokWorkaround;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, buildMethodName = "buildWithoutValidation")
public final class ModellingSpec implements Validatable<ModellingSpec>, ProcSpecification {

    public static final String METHOD = "demetra.airline";
    public static final String FAMILY = "Modelling";

    public static final String VERSION_V3 = "3.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, METHOD, VERSION_V3);

    public static final ModellingSpec DEFAULT = ModellingSpec.builder().build(), DISABLED=builder().enabled(false).build();

    @lombok.NonNull
    private SeriesSpec series;
    
    private boolean enabled;

    @lombok.NonNull
    private TransformSpec transform;

    @lombok.NonNull
    private EstimateSpec estimate;

    @lombok.NonNull
    private OutlierSpec outliers;

    @lombok.NonNull
    private RegressionSpec regression;

    @lombok.NonNull
    private SarimaSpec arima;

    @LombokWorkaround
    public static Builder builder() {
        SarimaSpec sarima = SarimaSpec.airline();
        return new Builder()
                .enabled(true)
                .series(SeriesSpec.DEFAULT)
                .transform(TransformSpec.DEFAULT)
                .estimate(EstimateSpec.DEFAULT)
                .outliers(OutlierSpec.DEFAULT_ENABLED)
                .regression(RegressionSpec.DEFAULT)
                .arima(sarima);
    }

    @Override
    public ModellingSpec validate() throws IllegalArgumentException {
        outliers.validate();
        regression.validate();
        return this;
    }

    public boolean checkFrequency(int freq) {
        // Legacy specs have frequency set to -1 !!
        if (freq <= 0)
            return true;
        // Could be improved...
        return regression.getInterventionVariables().isEmpty() && regression.getOutliers().isEmpty()
                && regression.getRamps().isEmpty() && regression.getRamps().isEmpty();
    }

    public ModellingSpec setFrequency(int nfreq) {
        int frequency=series.getFrequency();
        if (nfreq == frequency) {
            // Nothing to do
            return this;
        }
        SeriesSpec nseries = series.toBuilder().frequency(nfreq).build();
        if (frequency <= 0) {
            // Nothing to check
            return toBuilder().series(nseries).buildWithoutValidation();
        }
        // Remove pre-specified variables (keeping them would mean a lot of checks/conversions/hypotheses...)
        // We should perhaps change the calendar effects
        return toBuilder().series(nseries)
                .regression(regression.toBuilder()
                .clearOutliers()
                .clearInterventionVariables()
                .clearRamps()
                .clearUserDefinedVariables()
                .build())
                .buildWithoutValidation();
    }

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return DESCRIPTOR;
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    public static class Builder implements Validatable.Builder<ModellingSpec> {

    }

    //<editor-fold defaultstate="collapsed" desc="Default Specifications">
    public static final ModellingSpec FULL;

    static {
        
        TransformSpec tr = TransformSpec.builder()
                .function(TransformationType.Auto)
                .build();

        EasterSpec e = EasterSpec.builder()
                .type(EasterSpec.Type.EASTER)
                .test(true)
                .build();

        TradingDaysSpec td = TradingDaysSpec.automatic(LengthOfPeriodType.LeapYear, TradingDaysSpec.AutoMethod.BIC, TradingDaysSpec.DEF_PTD, true);

        CalendarSpec cal = CalendarSpec.builder()
                .easter(e)
                .tradingDays(td)
                .build();
        RegressionSpec reg = RegressionSpec.builder()
                .calendar(cal)
                .build();

        OutlierSpec o = OutlierSpec.builder()
                .ao(true)
                .ls(true).build();

        FULL = ModellingSpec.builder()
                .series(SeriesSpec.DEFAULT)
                .enabled(true)
                .transform(tr)
                .estimate(EstimateSpec.DEFAULT)
                .outliers(o)
                .regression(reg)
                .arima(SarimaSpec.airline())
                .build();
    }

    //</editor-fold>
    @Override
    public String display() {
        return SMETHOD;
    }

    private static final String SMETHOD = "Demetra-Airline";

}
