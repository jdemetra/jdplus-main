package jdplus.toolkit.base.api.time;

import lombok.NonNull;

import java.time.DateTimeException;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;

public interface TimeIntervalAccessor {

    @NonNull Temporal start() throws DateTimeException;

    @NonNull Temporal end() throws DateTimeException;

    @NonNull TemporalAmount getDuration() throws DateTimeException;
}
