/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.toolkit.base.api.timeseries;

import jdplus.toolkit.base.api.time.ISO_8601;
import jdplus.toolkit.base.api.time.TimeIntervalAccessor;
import jdplus.toolkit.base.api.time.TimeIntervalFormatter;
import jdplus.toolkit.base.api.util.HasShortStringRepresentation;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeParseException;

import static jdplus.toolkit.base.api.time.TemporalFormatter.EXTENDED_CALENDAR;

/**
 * Period defined by a starting date (included) and an ending date (excluded)
 *
 * @author Jean Palate
 */
@ISO_8601
@RepresentableAsString
@lombok.Value(staticConstructor = "of")
public class CalendarPeriod implements TimeSeriesInterval<Period>, Comparable<CalendarPeriod>, HasShortStringRepresentation {

    @lombok.NonNull
    LocalDate start, end;

    @Override
    public @NonNull LocalDateTime start() {
        return start.atStartOfDay();
    }

    @Override
    public @NonNull LocalDateTime end() {
        return end.atStartOfDay();
    }

    @Override
    public boolean contains(LocalDateTime element) {
        return element.isBefore(end.atStartOfDay()) && (!element.isBefore(start.atStartOfDay()));
    }

    @Override
    public @NonNull Period getDuration() {
        return Period.between(start, end);
    }

    @Override
    public int compareTo(CalendarPeriod t) {
        if (start.equals(t.start) && end.isAfter(t.end)) {
            return 0;
        }
        if (!end.isAfter(t.start)) {
            return -1;
        }
        if (!t.end.isAfter(start)) {
            return 1;
        }
        throw new TsException(TsException.INCOMPATIBLE_PERIOD);
    }

    @Override
    public String toString() {
        return ISO_8601.format(this);
    }

    @Override
    public @NonNull String toShortString() {
        return ISO_8601_CONCISE.format(this);
    }

    @StaticFactoryMethod
    public static @NonNull CalendarPeriod parse(@NonNull CharSequence text) throws DateTimeParseException {
        return ISO_8601.parse(text, CalendarPeriod::from);
    }

    @StaticFactoryMethod
    public static @NonNull CalendarPeriod from(@NonNull TimeIntervalAccessor timeInterval) {
        return of(LocalDate.from(timeInterval.start()), LocalDate.from(timeInterval.end()));
    }

    private static final TimeIntervalFormatter.StartEnd ISO_8601 = TimeIntervalFormatter.StartEnd.of(EXTENDED_CALENDAR, LocalDate::from, false);
    private static final TimeIntervalFormatter.StartEnd ISO_8601_CONCISE = ISO_8601.withConcise(true);
}
