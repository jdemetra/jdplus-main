package jdplus.toolkit.base.tsp.grid;

import jdplus.toolkit.base.tsp.util.ObsFormat;
import jdplus.toolkit.base.tsp.util.PropertyHandler;
import lombok.NonNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

@lombok.Builder(toBuilder = true)
public final class GridWriterHandler implements PropertyHandler<GridWriter> {

    @NonNull
    private final PropertyHandler<ObsFormat> format;

    @NonNull
    private final PropertyHandler<GridLayout> layout;

    @NonNull
    private final PropertyHandler<Boolean> ignoreNames;

    @NonNull
    private final PropertyHandler<Boolean> ignoreDates;

    @NonNull
    private final PropertyHandler<String> cornerLabel;

    @NonNull
    private final PropertyHandler<Boolean> reverseChronology;

    @Override
    public @NonNull GridWriter get(@NonNull Function<? super String, ? extends CharSequence> properties) {
        return GridWriter
                .builder()
                .format(format.get(properties))
                .layout(layout.get(properties))
                .ignoreNames(ignoreNames.get(properties))
                .ignoreDates(ignoreDates.get(properties))
                .cornerLabel(cornerLabel.get(properties))
                .reverseChronology(reverseChronology.get(properties))
                .build();
    }

    @Override
    public void set(@NonNull BiConsumer<? super String, ? super String> properties, GridWriter value) {
        if (value != null) {
            format.set(properties, value.getFormat());
            layout.set(properties, value.getLayout());
            ignoreNames.set(properties, value.isIgnoreNames());
            ignoreDates.set(properties, value.isIgnoreDates());
            cornerLabel.set(properties, value.getCornerLabel());
            reverseChronology.set(properties, value.isReverseChronology());
        }
    }
}
