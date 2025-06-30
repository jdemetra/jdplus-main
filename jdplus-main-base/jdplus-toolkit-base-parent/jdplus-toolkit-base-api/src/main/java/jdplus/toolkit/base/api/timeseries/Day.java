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
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeParseException;

import static jdplus.toolkit.base.api.time.TemporalFormatter.EXTENDED_CALENDAR;

/**
 * @author Jean Palate
 */
@ISO_8601
@RepresentableAsString
@lombok.Value(staticConstructor = "of")
public class Day implements TimeSeriesInterval<Period> {

    @lombok.NonNull
    LocalDate day;

    @Override
    public @NonNull LocalDateTime start() {
        return day.atStartOfDay();
    }

    @Override
    public @NonNull LocalDateTime end() {
        return day.plusDays(1).atStartOfDay();
    }

    @Override
    public boolean contains(LocalDateTime element) {
        return element.toLocalDate().equals(day);
    }

    @Override
    public @NonNull Period getDuration() {
        return Period.ofDays(1);
    }

    @Override
    public String toString() {
        return ISO_8601.format(this);
    }

    @StaticFactoryMethod
    public static @NonNull Day parse(@NonNull CharSequence text) throws DateTimeParseException {
        return ISO_8601.parse(text, Day::from);
    }

    @StaticFactoryMethod
    public static @NonNull Day from(@NonNull TimeIntervalAccessor timeInterval) {
        return of(LocalDate.from(timeInterval.start()));
    }

    private static final TimeIntervalFormatter.StartDuration ISO_8601 = TimeIntervalFormatter.StartDuration.of(EXTENDED_CALENDAR, LocalDate::from, Period::parse);
}
