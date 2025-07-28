package jdplus.toolkit.base.api.time;

import lombok.NonNull;
import nbbrd.design.NonNegative;

import java.time.DateTimeException;

public interface TimeRecurrenceAccessor {

    @NonNull TimeInterval<?, ?> getInterval() throws DateTimeException;

    @NonNegative int length() throws DateTimeException;
}
