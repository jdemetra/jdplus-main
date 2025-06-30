package jdplus.toolkit.base.api.time;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@FunctionalInterface
public interface TimeIntervalQuery<R> {

    @Nullable R queryFrom(@NonNull TimeIntervalAccessor timeInterval);
}
