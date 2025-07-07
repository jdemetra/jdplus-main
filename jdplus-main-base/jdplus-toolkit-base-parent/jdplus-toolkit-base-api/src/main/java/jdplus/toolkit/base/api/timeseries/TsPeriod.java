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
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import static jdplus.toolkit.base.api.time.TemporalFormatter.EXTENDED_CALENDAR_TIME;

/**
 * @author Philippe Charles
 */
@ISO_8601
@RepresentableAsString
@lombok.Value
@lombok.Builder(toBuilder = true)
public class TsPeriod implements TimeSeriesInterval<TsUnit>, Comparable<TsPeriod>, HasShortStringRepresentation {

    @lombok.NonNull
    LocalDateTime epoch;

    @lombok.NonNull
    TsUnit unit;

    long id;

    @Override
    public @NonNull LocalDateTime start() {
        return dateAt(epoch, unit, id);
    }

    @Override
    public @NonNull LocalDateTime end() {
        return dateAt(epoch, unit, id + 1);
    }

    @Override
    public boolean contains(@NonNull LocalDateTime date) {
        return idAt(epoch, unit, date) == id;
    }

    @Override
    public @NonNull TsUnit getDuration() {
        return unit;
    }

    @Override
    public int compareTo(@NonNull TsPeriod period) {
        checkCompatibility(period);
        return Long.compare(id, getRebasedId(period));
    }

    /**
     * Year of the start of this period
     *
     * @return
     */
    public int year() {
        return start().getYear();
    }

    /**
     * 0-based position of this period in the year
     *
     * @return
     */
    public int annualPosition() {
        TsPeriod p = withUnit(TsUnit.P1Y);
        return TsDomain.splitOf(p, unit, true).indexOf(this);
    }

    public int annualFrequency() {
        return unit.getAnnualFrequency();
    }

    public boolean isAfter(TsPeriod period) {
        checkCompatibility(period);
        return id > getRebasedId(period);
    }

    public boolean isBefore(TsPeriod period) {
        checkCompatibility(period);
        return id < getRebasedId(period);
    }

    public TsPeriod next() {
        return plus(1);
    }

    public TsPeriod previous() {
        return plus(-1);
    }

    public TsPeriod plus(long count) {
        if (count == 0) {
            return this;
        }
        return new TsPeriod(epoch, unit, id + count);
    }

    public TsPeriod withEpoch(LocalDateTime epoch) {
        if (epoch.equals(this.epoch)) {
            return this;
        }
        return make(epoch.equals(DEFAULT_EPOCH) ? DEFAULT_EPOCH : epoch, unit, start());
    }

    public TsPeriod withUnit(TsUnit newUnit) {
        if (unit.equals(newUnit)) {
            return this;
        }
        return make(epoch, newUnit, start());
    }

    public TsPeriod withDate(LocalDateTime date) {
        return make(epoch, unit, date);
    }

    public TsPeriod withId(long id) {
        if (this.id == id) {
            return this;
        }
        return new TsPeriod(epoch, unit, id);
    }

    /**
     * Distance between this period and the given period
     *
     * @param end The given period
     * @return The result is 0 when the two periods are equal, positive if the
     * given period is after this period or negative otherwise.
     */
    public int until(TsPeriod end) {
        checkCompatibility(end);
        return (int) (getRebasedId(end) - id);
    }

    //    /**
//     * 
//     * @param low
//     * @return 
//     */
//    public int getPosition(TsUnit low) {
//        return getPosition(epoch, this.unit, id, low);
//    }
//
    @Override
    public String toString() {
        return ISO_8601.format(this);
    }

    @Override
    public @NonNull String toShortString() {
        return ISO_8601.format(this, unit.getPrecision());
    }

    /**
     * Gets the start part of this time interval as a short string using
     * the <a href="https://en.wikipedia.org/wiki/ISO_8601#Reduced_precision">ISO_8601 reduced precision</a> mechanism.
     *
     * @return a non-null string
     */
    public @NonNull String getStartAsShortString() {
        // TODO: add access to TemporalFormatter in StartDuration ?
        return EXTENDED_CALENDAR_TIME.format(start(), unit.getPrecision());
    }

    public long idAt(LocalDateTime date) {
        return idAt(epoch, unit, date);
    }

    public LocalDateTime dateAt(long id) {
        return dateAt(epoch, unit, id);
    }

    public boolean hasDefaultEpoch() {
        return epoch.equals(DEFAULT_EPOCH);
    }

    private boolean hasSameEpoch(@NonNull TsPeriod period) {
        return epoch.equals(period.epoch);
    }

    long getRebasedId(TsPeriod period) {
        return hasSameEpoch(period)
                ? period.id
                : idAt(period.start());
    }

    void checkCompatibility(TsPeriod period) throws IllegalArgumentException {
        if (unit != period.unit && !unit.equals(period.unit)) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
    }

    public static final LocalDateTime DEFAULT_EPOCH = LocalDate.ofEpochDay(0).atStartOfDay();

    @StaticFactoryMethod
    public static @NonNull TsPeriod of(@NonNull TsUnit unit, @NonNull LocalDateTime date) {
        return make(DEFAULT_EPOCH, unit, date);
    }

    @StaticFactoryMethod
    public static @NonNull TsPeriod of(@NonNull TsUnit unit, @NonNull LocalDate date) {
        return make(DEFAULT_EPOCH, unit, date);
    }

    @StaticFactoryMethod
    public static @NonNull TsPeriod of(@NonNull TsUnit unit, long id) {
        return make(DEFAULT_EPOCH, unit, id);
    }

    @StaticFactoryMethod
    public static @NonNull TsPeriod yearly(int year) {
        return make(DEFAULT_EPOCH, TsUnit.P1Y, LocalDate.of(year, 1, 1));
    }

    /**
     * Creates a quarterly period
     *
     * @param year    Year of the period
     * @param quarter Quarter of the period (in 1-4)
     * @return
     */
    @StaticFactoryMethod
    public static @NonNull TsPeriod quarterly(int year, int quarter) {
        return make(DEFAULT_EPOCH, TsUnit.P3M, LocalDate.of(year, ((quarter - 1) * 3) + 1, 1));
    }

    /**
     * Creates a monthly period
     *
     * @param year  Year of the period
     * @param month Month of the period (in 1-12)
     * @return
     */
    @StaticFactoryMethod
    public static @NonNull TsPeriod monthly(int year, int month) {
        return make(DEFAULT_EPOCH, TsUnit.P1M, LocalDate.of(year, month, 1));
    }

    /**
     * Creates a period of one day
     *
     * @param year       Year of the day
     * @param month      Month of the day (in 1-12)
     * @param dayOfMonth Day of month of the day (1-31)
     * @return
     */
    @StaticFactoryMethod
    public static @NonNull TsPeriod daily(int year, int month, int dayOfMonth) {
        return make(DEFAULT_EPOCH, TsUnit.P1D, LocalDate.of(year, month, dayOfMonth));
    }

    /**
     * Creates a period of seven days
     *
     * @param year       Year of the first day
     * @param month      Month of the first day (in 1-12)
     * @param dayOfMonth Day of month of the first day (1-31)
     * @return
     */
    @StaticFactoryMethod
    public static @NonNull TsPeriod weekly(int year, int month, int dayOfMonth) {
        LocalDate start = LocalDate.of(year, month, dayOfMonth);
        int dw_start = start.getDayOfWeek().getValue();
        int dw_epoch = DEFAULT_EPOCH.getDayOfWeek().getValue();
        return make(DEFAULT_EPOCH.plusDays(dw_start - dw_epoch), TsUnit.P7D, start);
    }

    @StaticFactoryMethod
    public static @NonNull TsPeriod hourly(int year, int month, int dayOfMonth, int hour) {
        return make(DEFAULT_EPOCH, TsUnit.PT1H, LocalDateTime.of(year, month, dayOfMonth, hour, 0));
    }

    @StaticFactoryMethod
    public static @NonNull TsPeriod minutely(int year, int month, int dayOfMonth, int hour, int minute) {
        return make(DEFAULT_EPOCH, TsUnit.PT1M, LocalDateTime.of(year, month, dayOfMonth, hour, minute));
    }

    @StaticFactoryMethod
    public static @NonNull TsPeriod parse(@NonNull CharSequence text) throws DateTimeParseException {
        return ISO_8601.parse(text, TsPeriod::from);
    }

    @StaticFactoryMethod
    public static @NonNull TsPeriod from(@NonNull TimeIntervalAccessor timeInterval) {
        return TsPeriod.of((TsUnit) timeInterval.getDuration(), LocalDateTime.from(timeInterval.start()));
    }

    static final TimeIntervalFormatter ISO_8601
            = TimeIntervalFormatter.StartDuration.of(EXTENDED_CALENDAR_TIME, LocalDateTime::from, TsUnit::parse);

    private static TsPeriod make(LocalDateTime epoch, TsUnit unit, LocalDate date) {
        return new TsPeriod(epoch, unit, idAt(epoch, unit, date.atStartOfDay()));
    }

    static TsPeriod make(LocalDateTime epoch, TsUnit unit, LocalDateTime date) {
        return new TsPeriod(epoch, unit, idAt(epoch, unit, date));
    }

    private static TsPeriod make(LocalDateTime epoch, TsUnit unit, long id) {
        return new TsPeriod(epoch, unit, id);
    }

    public static long idAt(LocalDateTime epoch, TsUnit unit, LocalDateTime date) {
        if (date.compareTo(epoch) >= 0) {
            return (unit.getChronoUnit().between(epoch, date)) / unit.getAmount();
        } else {
            long result = (unit.getChronoUnit().between(epoch, date)) / unit.getAmount();
            return dateAt(epoch, unit, result).compareTo(date) <= 0 ? result : result - 1;
        }
    }

    public static LocalDateTime dateAt(LocalDateTime epoch, TsUnit unit, long id) {
        return epoch.plus(unit.getAmount() * id, unit.getChronoUnit());
    }

    //    private static int getPosition(LocalDateTime epoch, TsUnit high, long id, TsUnit low) {
//        long id0 = id;
//        long id1 = idAt(epoch, low, dateAt(epoch, high, id0));
//        long id2 = idAt(epoch, high, dateAt(epoch, low, id1));
//        return (int) (id0 - id2);
//    }
//

    /**
     * @deprecated see {@link #getStartAsShortString()}
     */
    @Deprecated
    public String display() {
        if (unit.getChronoUnit().getDuration().compareTo(ChronoUnit.DAYS.getDuration()) < 0) {
            return start().toString();
        } else {
            int freq = annualFrequency();
            if (freq < 1) {
                return start().toLocalDate().toString();
            } else if (freq == 1) {
                return Integer.toString(year());
            } else {
                int pos = this.annualPosition() + 1;
                if (freq < 12) {
                    pos *= 12 / freq;
                }
                int year = this.year();
                StringBuilder buffer = new StringBuilder(32);
                buffer.append(pos).append('-').append(year);
                return buffer.toString();
            }
        }
    }

    @ISO_8601
    public static final class Builder implements TimeSeriesInterval<TsUnit>, HasShortStringRepresentation {

        private LocalDateTime epoch = DEFAULT_EPOCH;
        private TsUnit unit = TsUnit.P1M;
        private long id;

        private void refreshId(LocalDateTime oldref, TsUnit oldUnit, LocalDateTime newref, TsUnit newUnit) {
            this.id = TsPeriod.idAt(newref, newUnit, dateAt(oldref, oldUnit, id));
        }

        public Builder epoch(LocalDateTime epoch) {
            refreshId(this.epoch, this.unit, this.epoch = epoch, this.unit);
            return this;
        }

        public Builder unit(TsUnit unit) {
            refreshId(this.epoch, this.unit, this.epoch, this.unit = unit);
            return this;
        }

        public Builder date(LocalDate date) {
            return date(date.atStartOfDay());
        }

        public Builder date(LocalDateTime date) {
            this.id = TsPeriod.idAt(epoch, unit, date);
            return this;
        }

        public Builder plus(int count) {
            this.id += count;
            return this;
        }

        //        public int getPosition(TsUnit low) {
//            return TsPeriod.getPosition(epoch, this.unit, id, low);
//        }
//
        @Override
        public @NonNull LocalDateTime start() {
            return TsPeriod.dateAt(epoch, unit, id);
        }

        @Override
        public @NonNull LocalDateTime end() {
            return TsPeriod.dateAt(epoch, unit, id + 1);
        }

        @Override
        public @NonNull TsUnit getDuration() {
            return unit;
        }

        @Override
        public boolean contains(@NonNull LocalDateTime date) {
            return TsPeriod.idAt(epoch, unit, date) == id;
        }

        @Override
        public String toString() {
            return ISO_8601.format(this);
        }

        @Override
        public @NonNull String toShortString() {
            return ISO_8601.format(this, unit.getPrecision());
        }
    }
}
