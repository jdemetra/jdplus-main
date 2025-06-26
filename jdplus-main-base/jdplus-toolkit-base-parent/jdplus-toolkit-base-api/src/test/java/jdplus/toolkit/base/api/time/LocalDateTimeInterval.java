package jdplus.toolkit.base.api.time;

import jdplus.toolkit.base.api.timeseries.TsUnit;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;

import java.time.LocalDateTime;
import java.time.Period;

@lombok.Value(staticConstructor = "of")
class LocalDateTimeInterval implements TimeInterval<LocalDateTime, TsUnit> {

    @StaticFactoryMethod
    @NonNull
    public static LocalDateTimeInterval from(@NonNull TimeIntervalAccessor timeInterval) {
        return of(LocalDateTime.from(timeInterval.start()), (TsUnit) timeInterval.getDuration());
    }

    @NonNull
    LocalDateTime start;

    @NonNull
    TsUnit duration;

    @Override
    public @NonNull LocalDateTime start() {
        return start;
    }

    @Override
    public @NonNull LocalDateTime end() {
        return start.plus(duration);
    }

    @Override
    public @NonNull TsUnit getDuration() {
        return duration;
    }
}
