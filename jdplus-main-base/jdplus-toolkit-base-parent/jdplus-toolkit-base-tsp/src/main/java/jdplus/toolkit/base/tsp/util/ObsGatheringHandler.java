package jdplus.toolkit.base.tsp.util;

import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.timeseries.util.ObsGathering;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

@lombok.Builder(toBuilder = true)
public final class ObsGatheringHandler implements PropertyHandler<ObsGathering> {

    @lombok.NonNull
    private final PropertyHandler<TsUnit> unit;

    @lombok.NonNull
    private final PropertyHandler<AggregationType> aggregationType;

    @lombok.NonNull
    private final PropertyHandler<Boolean> allowPartialAggregation;

    @lombok.NonNull
    private final PropertyHandler<Boolean> includeMissingValues;

    @Override
    public @NonNull ObsGathering get(@NonNull Function<? super String, ? extends CharSequence> properties) {
        return ObsGathering
                .builder()
                .unit(unit.get(properties))
                .aggregationType(aggregationType.get(properties))
                .allowPartialAggregation(allowPartialAggregation.get(properties))
                .includeMissingValues(includeMissingValues.get(properties))
                .build();
    }

    @Override
    public void set(@NonNull BiConsumer<? super String, ? super String> properties, @Nullable ObsGathering value) {
        if (value != null) {
            unit.set(properties, value.getUnit());
            aggregationType.set(properties, value.getAggregationType());
            allowPartialAggregation.set(properties, value.isAllowPartialAggregation());
            includeMissingValues.set(properties, value.isIncludeMissingValues());
        }
    }
}
