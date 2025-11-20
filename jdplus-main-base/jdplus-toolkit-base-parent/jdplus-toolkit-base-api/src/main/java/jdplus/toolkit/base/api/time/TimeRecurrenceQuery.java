package jdplus.toolkit.base.api.time;

import lombok.NonNull;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface TimeRecurrenceQuery<R> {

    @Nullable R queryFrom(@NonNull TimeRecurrenceAccessor timeRecurrence);
}
