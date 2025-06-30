package jdplus.toolkit.base.api.time;

import lombok.NonNull;

import java.time.LocalDate;
import java.time.Period;

@lombok.Value(staticConstructor = "of")
class LocalDateInterval implements TimeInterval<LocalDate, Period> {

    @lombok.NonNull
    LocalDate start;

    @lombok.NonNull
    Period duration;

    @Override
    public @NonNull LocalDate start() {
        return start;
    }

    @Override
    public @NonNull LocalDate end() {
        return start.plus(duration);
    }

    @Override
    public @NonNull Period getDuration() {
        return duration;
    }
}
