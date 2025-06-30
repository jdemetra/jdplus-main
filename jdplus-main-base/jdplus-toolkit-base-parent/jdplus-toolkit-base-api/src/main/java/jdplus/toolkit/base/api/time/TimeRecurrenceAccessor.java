package jdplus.toolkit.base.api.time;

import lombok.NonNull;
import org.checkerframework.checker.index.qual.NonNegative;

import java.time.DateTimeException;

public interface TimeRecurrenceAccessor {

    @NonNull TimeInterval<?, ?> getInterval() throws DateTimeException;

    @NonNegative int length() throws DateTimeException;
}
