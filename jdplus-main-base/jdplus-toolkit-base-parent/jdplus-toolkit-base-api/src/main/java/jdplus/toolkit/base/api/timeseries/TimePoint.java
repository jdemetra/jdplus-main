/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.toolkit.base.api.timeseries;

import jdplus.toolkit.base.api.time.ISO_8601;
import jdplus.toolkit.base.api.time.TimeIntervalAccessor;
import jdplus.toolkit.base.api.time.TimeIntervalFormatter;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import lombok.NonNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static jdplus.toolkit.base.api.time.TemporalFormatter.EXTENDED_CALENDAR_TIME;

/**
 * @author Jean Palate
 */
@ISO_8601
@RepresentableAsString
@lombok.Value(staticConstructor = "of")
public class TimePoint implements TimeSeriesInterval<Duration> {

    @lombok.NonNull
    LocalDateTime point;

    @Override
    public @NonNull LocalDateTime start() {
        return point;
    }

    @Override
    public @NonNull LocalDateTime end() {
        return point;
    }

    @Override
    public boolean contains(@NonNull LocalDateTime element) {
        return point.equals(element);
    }

    @Override
    public @NonNull Duration getDuration() {
        return Duration.ZERO;
    }

    @Override
    public String toString() {
        return ISO_8601.format(this);
    }

    @StaticFactoryMethod
    public static @NonNull TimePoint parse(@NonNull CharSequence text) throws DateTimeParseException {
        return ISO_8601.parse(text, TimePoint::from);
    }

    @StaticFactoryMethod
    public static @NonNull TimePoint from(@NonNull TimeIntervalAccessor timeInterval) {
        return TimePoint.of(LocalDateTime.from(timeInterval.start()));
    }

    private static final TimeIntervalFormatter.StartEnd ISO_8601 = TimeIntervalFormatter.StartEnd.of(EXTENDED_CALENDAR_TIME, LocalDateTime::from, false);
}
