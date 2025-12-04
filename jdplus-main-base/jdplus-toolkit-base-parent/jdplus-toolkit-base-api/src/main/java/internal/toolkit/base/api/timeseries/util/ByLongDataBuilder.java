/*
 * Copyright 2017 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package internal.toolkit.base.api.timeseries.util;

import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.timeseries.util.ObsCharacteristics;
import jdplus.toolkit.base.api.timeseries.util.ObsGathering;
import jdplus.toolkit.base.api.timeseries.util.TsDataBuilder;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.function.ToLongFunction;

/**
 * @param <DATE>
 * @author Philippe Charles
 */
public final class ByLongDataBuilder<DATE> extends ObsListDataBuilder<DATE, ByLongObsList> {

    @StaticFactoryMethod
    public static ByLongDataBuilder<Date> fromCalendar(ObsGathering gathering, ObsCharacteristics[] characteristics, int initialCapacity, Calendar resource) {
        return of(gathering, characteristics, initialCapacity, new CalendarConverter(resource.getTimeZone().toZoneId()));
    }

    @StaticFactoryMethod
    public static ByLongDataBuilder<LocalDate> fromDate(ObsGathering gathering, ObsCharacteristics[] characteristics, int initialCapacity) {
        return of(gathering, characteristics, initialCapacity, DateConverter.INSTANCE);
    }

    private static <T> ByLongDataBuilder<T> of(
            ObsGathering gathering, ObsCharacteristics[] characteristics, int initialCapacity,
            Converter<T> converter) {

        return new ByLongDataBuilder<>(
                ByLongObsList.of(isOrdered(characteristics), converter.asPeriodIdFactory(), initialCapacity),
                converter::dateToLong,
                gathering
        );
    }

    private final ToLongFunction<DATE> toLong;

    private ByLongDataBuilder(
            ByLongObsList obsList,
            ToLongFunction<DATE> toLong,
            ObsGathering gathering) {
        super(obsList, gathering);
        this.toLong = toLong;
    }

    @Override
    public @NonNull TsDataBuilder<DATE> add(DATE date, Number value) {
        if (date != null) {
            if (value != null) {
                obsList.add(toLong.applyAsLong(date), value.doubleValue());
            } else if (gathering.isIncludeMissingValues()) {
                obsList.add(toLong.applyAsLong(date), Double.NaN);
            }
        }
        return this;
    }

    private sealed interface Converter<DATE> permits CalendarConverter, DateConverter {

        long dateToLong(DATE value);

        LocalDateTime longToLocalDateTime(long l);

        default ByLongObsList.PeriodIdFactory asPeriodIdFactory() {
            return (LocalDateTime epoch, TsUnit unit, long l) -> TsPeriod.idAt(epoch, unit, longToLocalDateTime(l));
        }
    }

    @lombok.AllArgsConstructor
    private static final class CalendarConverter implements Converter<Date> {

        private final ZoneId zoneId;

        @Override
        public long dateToLong(Date value) {
            return value.getTime();
        }

        @Override
        public LocalDateTime longToLocalDateTime(long value) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(value), zoneId);
        }
    }

    private enum DateConverter implements Converter<LocalDate> {
        INSTANCE;

        @Override
        public long dateToLong(LocalDate date) {
            return (date.getYear() * 100L + date.getMonthValue()) * 100 + date.getDayOfMonth();
        }

        @Override
        public LocalDateTime longToLocalDateTime(long value) {
            int dayOfMonth = (int) value % 100;
            value /= 100;
            int month = (int) value % 100;
            value /= 100;
            return LocalDateTime.of((int) value, month, dayOfMonth, 0, 0);
        }
    }
}
