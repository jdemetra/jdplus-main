/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.toolkit.base.api.time;

import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import org.jspecify.annotations.Nullable;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalQuery;
import java.util.function.Function;

import static jdplus.toolkit.base.api.time.TemporalFormatter.*;

/**
 * @author Philippe Charles
 */
public abstract sealed class TimeIntervalFormatter
        permits TimeIntervalFormatter.StartEnd, TimeIntervalFormatter.StartDuration, TimeIntervalFormatter.DurationEnd, TimeIntervalFormatter.Duration {

    public @NonNull String format(@NonNull TimeInterval<?, ?> timeInterval) throws DateTimeException {
        return format(timeInterval, null);
    }

    public @NonNull String format(@NonNull TimeInterval<?, ?> timeInterval, @Nullable ChronoUnit precision) throws DateTimeException {
        StringBuilder result = new StringBuilder(32);
        formatTo(timeInterval, result, precision);
        return result.toString();
    }

    public void formatTo(@NonNull TimeInterval<?, ?> timeInterval, @NonNull Appendable appendable) throws DateTimeException {
        formatTo(timeInterval, appendable, null);
    }

    abstract public void formatTo(@NonNull TimeInterval<?, ?> timeInterval, @NonNull Appendable appendable, @Nullable ChronoUnit precision) throws DateTimeException;

    abstract public <I extends TimeInterval<?, ?>> @NonNull I parse(@NonNull CharSequence text, @NonNull TimeIntervalQuery<I> query) throws DateTimeParseException;

    @lombok.AllArgsConstructor(staticName = "of")
    public static final class StartEnd extends TimeIntervalFormatter {

        @Deprecated
        public static final StartEnd ISO_LOCAL_DATE = of(EXTENDED_CALENDAR, LocalDate::from, false);
        @Deprecated
        public static final StartEnd BASIC_ISO_DATE = of(BASIC_CALENDAR, LocalDate::from, false);
        @Deprecated
        public static final StartEnd ISO_ORDINAL_DATE = of(EXTENDED_ORDINAL, LocalDate::from, false);
        @Deprecated
        public static final StartEnd ISO_WEEK_DATE = of(EXTENDED_WEEK, LocalDate::from, false);
        @Deprecated
        public static final StartEnd ISO_LOCAL_DATE_TIME = of(EXTENDED_CALENDAR_TIME, LocalDateTime::from, false);

        @lombok.NonNull
        private final TemporalFormatter startEndFormatter;

        @lombok.NonNull
        private final TemporalQuery<? extends Temporal> startEndQuery;

        @lombok.With
        private boolean concise;

        @Override
        public void formatTo(@NonNull TimeInterval<?, ?> timeInterval, @NonNull Appendable appendable, @Nullable ChronoUnit precision) {
            String left = startEndFormatter.format(timeInterval.start());
            String right = startEndFormatter.format(timeInterval.end());
            appendTo(left, appendable);
            appendTo(INTERVAL_DESIGNATOR, appendable);
            appendTo(concise ? compact(left, right) : right, appendable);
        }

        @Override
        public <I extends TimeInterval<?, ?>> @NonNull I parse(@NonNull CharSequence text, @NonNull TimeIntervalQuery<I> query) {
            int intervalDesignatorIdx = getIntervalDesignatorIndex(text);
            CharSequence left = text.subSequence(0, intervalDesignatorIdx);
            CharSequence right = text.subSequence(intervalDesignatorIdx + 1, text.length());
            I result = query.queryFrom(new StartEndAccessor(left, right));
            if (result == null) {
                throw new DateTimeException("Unable to obtain TimeInterval from TimeIntervalQuery");
            }
            return result;
        }

        @lombok.AllArgsConstructor
        private final class StartEndAccessor implements TimeIntervalAccessor {

            private final CharSequence left;
            private final CharSequence right;

            @Override
            public @NonNull Temporal start() {
                return startEndFormatter.parse(left, startEndQuery);
            }

            @Override
            public @NonNull Temporal end() {
                return startEndFormatter.parse(expand(left, right), startEndQuery);
            }

            @Override
            public @NonNull TemporalAmount getDuration() {
                throw new DateTimeException("Not supported for this TimeIntervalFormatter");
            }
        }
    }

    @lombok.AllArgsConstructor(staticName = "of")
    public static final class StartDuration extends TimeIntervalFormatter {

        @Deprecated
        public static final StartDuration ISO_LOCAL_DATE = of(EXTENDED_CALENDAR, LocalDate::from, Period::parse);
        @Deprecated
        public static final StartDuration BASIC_ISO_DATE = of(BASIC_CALENDAR, LocalDate::from, Period::parse);
        @Deprecated
        public static final StartDuration ISO_ORDINAL_DATE = of(EXTENDED_ORDINAL, LocalDate::from, Period::parse);
        @Deprecated
        public static final StartDuration ISO_WEEK_DATE = of(EXTENDED_WEEK, LocalDate::from, Period::parse);
        @Deprecated
        public static final StartDuration ISO_LOCAL_DATE_TIME = of(EXTENDED_CALENDAR_TIME, LocalDateTime::from, java.time.Duration::parse);

        @lombok.NonNull
        private final TemporalFormatter startFormatter;

        @lombok.NonNull
        private final TemporalQuery<? extends Temporal> startQuery;

        @lombok.NonNull
        private final Function<? super CharSequence, ? extends TemporalAmount> duration;

        @Override
        public void formatTo(@NonNull TimeInterval<?, ?> timeInterval, @NonNull Appendable appendable, @Nullable ChronoUnit precision) {
            Temporal left = timeInterval.start();
            TemporalAmount right = timeInterval.getDuration();
            startFormatter.formatTo(left, appendable, precision);
            appendTo(INTERVAL_DESIGNATOR, appendable);
            formatDurationTo(right, appendable);
        }

        @Override
        public <I extends TimeInterval<?, ?>> @NonNull I parse(@NonNull CharSequence text, @NonNull TimeIntervalQuery<I> query) {
            int intervalDesignatorIdx = getIntervalDesignatorIndex(text);
            CharSequence left = text.subSequence(0, intervalDesignatorIdx);
            CharSequence right = text.subSequence(intervalDesignatorIdx + 1, text.length());
            I result = query.queryFrom(new StartDurationAccessor(left, right));
            if (result == null) {
                throw new DateTimeException("Unable to obtain TimeInterval from TimeIntervalQuery");
            }
            return result;
        }

        @lombok.AllArgsConstructor
        private final class StartDurationAccessor implements TimeIntervalAccessor {

            private final CharSequence left;
            private final CharSequence right;

            @Override
            public @NonNull Temporal start() {
                return startFormatter.parse(left, startQuery);
            }

            @Override
            public @NonNull Temporal end() {
                throw new DateTimeException("Not supported for this TimeIntervalFormatter");
            }

            @Override
            public @NonNull TemporalAmount getDuration() {
                return duration.apply(right);
            }
        }
    }

    @lombok.AllArgsConstructor(staticName = "of")
    public static final class DurationEnd extends TimeIntervalFormatter {

        @Deprecated
        public static final DurationEnd ISO_LOCAL_DATE = of(Period::parse, EXTENDED_CALENDAR, LocalDate::from);
        @Deprecated
        public static final DurationEnd BASIC_ISO_DATE = of(Period::parse, BASIC_CALENDAR, LocalDate::from);
        @Deprecated
        public static final DurationEnd ISO_ORDINAL_DATE = of(Period::parse, EXTENDED_ORDINAL, LocalDate::from);
        @Deprecated
        public static final DurationEnd ISO_WEEK_DATE = of(Period::parse, EXTENDED_WEEK, LocalDate::from);
        @Deprecated
        public static final DurationEnd ISO_LOCAL_DATE_TIME = of(java.time.Duration::parse, EXTENDED_CALENDAR_TIME, LocalDateTime::from);

        @lombok.NonNull
        private final Function<? super CharSequence, TemporalAmount> duration;

        @lombok.NonNull
        private final TemporalFormatter endFormatter;

        @lombok.NonNull
        private final TemporalQuery<? extends Temporal> endQuery;

        @Override
        public void formatTo(@NonNull TimeInterval<?, ?> timeInterval, @NonNull Appendable appendable, @Nullable ChronoUnit precision) {
            TemporalAmount left = timeInterval.getDuration();
            Temporal right = timeInterval.end();
            formatDurationTo(left, appendable);
            appendTo(INTERVAL_DESIGNATOR, appendable);
            endFormatter.formatTo(right, appendable, precision);
        }

        @Override
        public <I extends TimeInterval<?, ?>> @NonNull I parse(@NonNull CharSequence text, @NonNull TimeIntervalQuery<I> query) {
            int intervalDesignatorIdx = getIntervalDesignatorIndex(text);
            CharSequence left = text.subSequence(0, intervalDesignatorIdx);
            CharSequence right = text.subSequence(intervalDesignatorIdx + 1, text.length());
            I result = query.queryFrom(new DurationEndAccessor(right, left));
            if (result == null) {
                throw new DateTimeException("Unable to obtain TimeInterval from TimeIntervalQuery");
            }
            return result;
        }

        @lombok.AllArgsConstructor
        private final class DurationEndAccessor implements TimeIntervalAccessor {

            private final CharSequence right;
            private final CharSequence left;

            @Override
            public @NonNull Temporal start() {
                throw new DateTimeException("Not supported for this TimeIntervalFormatter");
            }

            @Override
            public @NonNull Temporal end() {
                return endFormatter.parse(right, endQuery);
            }

            @Override
            public @NonNull TemporalAmount getDuration() {
                return duration.apply(left);
            }
        }
    }

    @lombok.AllArgsConstructor(staticName = "of")
    public static final class Duration extends TimeIntervalFormatter {

        @lombok.NonNull
        private final Function<? super CharSequence, ? extends TemporalAmount> duration;

        @Override
        public void formatTo(@NonNull TimeInterval<?, ?> timeInterval, @NonNull Appendable appendable, @Nullable ChronoUnit precision) {
            formatDurationTo(timeInterval.getDuration(), appendable);
        }

        @Override
        public <I extends TimeInterval<?, ?>> @NonNull I parse(@NonNull CharSequence text, @NonNull TimeIntervalQuery<I> query) {
            I result = query.queryFrom(new DurationAccessor(text));
            if (result == null) {
                throw new DateTimeException("Unable to obtain TimeInterval from TimeIntervalQuery");
            }
            return result;
        }

        @lombok.AllArgsConstructor
        private final class DurationAccessor implements TimeIntervalAccessor {

            private final CharSequence text;

            @Override
            public @NonNull Temporal start() {
                throw new DateTimeException("Not supported for this TimeIntervalFormatter");
            }

            @Override
            public @NonNull Temporal end() {
                throw new DateTimeException("Not supported for this TimeIntervalFormatter");
            }

            @Override
            public @NonNull TemporalAmount getDuration() {
                return duration.apply(text);
            }
        }
    }

    private static final char INTERVAL_DESIGNATOR = '/';

    private static int getIntervalDesignatorIndex(CharSequence text) throws DateTimeParseException {
        int result = indexOf(text, INTERVAL_DESIGNATOR);
        if (result == -1) {
            throw new DateTimeParseException("Cannot find interval designator", text, 0);
        } else if (result == 0) {
            throw new DateTimeParseException("Cannot find interval left part", text, 0);
        } else if (result == text.length() - 1) {
            throw new DateTimeParseException("Cannot find interval right part", text, result);
        }
        return result;
    }

    @MightBePromoted
    static int indexOf(CharSequence text, char c) {
        if (text instanceof String) {
            return ((String) text).indexOf(c);
        }
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }

    private static CharSequence compact(CharSequence ref, CharSequence value) {
        int anchor = -1;
        for (int i = 0; i < ref.length(); i++) {
            if (!Character.isDigit(ref.charAt(i))) {
                anchor = i;
            }
            if (ref.charAt(i) != value.charAt(i)) {
                return value.subSequence(anchor + 1, value.length());
            }
        }
        return "";
    }

    private static CharSequence expand(CharSequence ref, CharSequence value) {
        int diff = ref.length() - value.length();
        if (diff <= 0) {
            return value;
        }
        return ref.subSequence(0, diff).toString() + value;
    }

    private static void formatDurationTo(TemporalAmount duration, Appendable appendable) {
        appendTo(duration.toString(), appendable);
    }
}
