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

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalQuery;
import java.util.function.Function;

import static jdplus.toolkit.base.api.time.IsoDateTimeFormatter.*;

/**
 * @author Philippe Charles
 */
public abstract sealed class TimeIntervalFormatter
        permits TimeIntervalFormatter.StartEnd, TimeIntervalFormatter.StartDuration, TimeIntervalFormatter.DurationEnd, TimeIntervalFormatter.Duration {

    @NonNull
    public String format(@NonNull TimeInterval<?, ?> timeInterval) {
        StringBuilder result = new StringBuilder(32);
        formatTo(timeInterval, result);
        return result.toString();
    }

    abstract public void formatTo(@NonNull TimeInterval<?, ?> timeInterval, @NonNull Appendable appendable);

    @NonNull
    abstract public <I extends TimeInterval<?, ?>> I parse(@NonNull CharSequence text, @NonNull TimeIntervalQuery<I> query) throws DateTimeParseException;

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
        private final IsoDateTimeFormatter temporalFormatter;

        @lombok.NonNull
        private final TemporalQuery<? extends Temporal> temporalQuery;

        @lombok.With
        private boolean concise;

        @Override
        public void formatTo(@NonNull TimeInterval<?, ?> timeInterval, @NonNull Appendable appendable) {
            String left = temporalFormatter.format(timeInterval.start());
            String right = temporalFormatter.format(timeInterval.end());
            try {
                appendable.append(left);
                appendable.append(INTERVAL_DESIGNATOR);
                appendable.append(concise ? compact(left, right) : right);
            } catch (IOException ex) {
                throw new DateTimeException(ex.getMessage(), ex);
            }
        }

        @Override
        public <I extends TimeInterval<?, ?>> @NonNull I parse(@NonNull CharSequence text, @NonNull TimeIntervalQuery<I> query) {
            int intervalDesignatorIdx = getIntervalDesignatorIndex(text);
            CharSequence left = text.subSequence(0, intervalDesignatorIdx);
            CharSequence right = text.subSequence(intervalDesignatorIdx + 1, text.length());
            return query.queryFrom(new AbstractTimeIntervalAccessor() {
                @Override
                public Temporal start() {
                    return temporalFormatter.parse(left, temporalQuery);
                }

                @Override
                public Temporal end() {
                    return temporalFormatter.parse(concise ? expand(left, right) : right, temporalQuery);
                }
            });
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
        private final IsoDateTimeFormatter temporalFormatter;

        @lombok.NonNull
        private final TemporalQuery<? extends Temporal> temporalQuery;

        @lombok.NonNull
        private final Function<? super CharSequence, ? extends TemporalAmount> duration;

        @Override
        public void formatTo(@NonNull TimeInterval<?, ?> timeInterval, @NonNull Appendable appendable) {
            TemporalAccessor left = timeInterval.start();
            TemporalAmount right = timeInterval.getDuration();
            try {
                temporalFormatter.formatTo(left, appendable);
                appendable.append(INTERVAL_DESIGNATOR);
                formatDurationTo(right, appendable);
            } catch (IOException ex) {
                throw new DateTimeException(ex.getMessage(), ex);
            }
        }

        @Override
        public <I extends TimeInterval<?, ?>> @NonNull I parse(@NonNull CharSequence text, @NonNull TimeIntervalQuery<I> query) {
            int intervalDesignatorIdx = getIntervalDesignatorIndex(text);
            CharSequence left = text.subSequence(0, intervalDesignatorIdx);
            CharSequence right = text.subSequence(intervalDesignatorIdx + 1, text.length());
            return query.queryFrom(new AbstractTimeIntervalAccessor() {
                @Override
                public Temporal start() {
                    return temporalFormatter.parse(left, temporalQuery);
                }

                @Override
                public TemporalAmount getDuration() {
                    return duration.apply(right);
                }
            });
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
        private final IsoDateTimeFormatter temporalFormatter;

        @lombok.NonNull
        private final TemporalQuery<? extends Temporal> temporalQuery;

        @Override
        public void formatTo(@NonNull TimeInterval<?, ?> timeInterval, @NonNull Appendable appendable) {
            TemporalAmount left = timeInterval.getDuration();
            TemporalAccessor right = timeInterval.end();
            try {
                formatDurationTo(left, appendable);
                appendable.append(INTERVAL_DESIGNATOR);
                temporalFormatter.formatTo(right, appendable);
            } catch (IOException ex) {
                throw new DateTimeException(ex.getMessage(), ex);
            }
        }

        @Override
        public <I extends TimeInterval<?, ?>> @NonNull I parse(@NonNull CharSequence text, @NonNull TimeIntervalQuery<I> query) {
            int intervalDesignatorIdx = getIntervalDesignatorIndex(text);
            CharSequence left = text.subSequence(0, intervalDesignatorIdx);
            CharSequence right = text.subSequence(intervalDesignatorIdx + 1, text.length());
            return query.queryFrom(new AbstractTimeIntervalAccessor() {
                @Override
                public Temporal end() {
                    return temporalFormatter.parse(right, temporalQuery);
                }

                @Override
                public TemporalAmount getDuration() {
                    return duration.apply(left);
                }
            });
        }
    }

    @lombok.AllArgsConstructor(staticName = "of")
    public static final class Duration extends TimeIntervalFormatter {

        @lombok.NonNull
        private final Function<? super CharSequence, ? extends TemporalAmount> duration;

        @Override
        public void formatTo(@NonNull TimeInterval<?, ?> timeInterval, @NonNull Appendable appendable) {
            try {
                formatDurationTo(timeInterval.getDuration(), appendable);
            } catch (IOException ex) {
                throw new DateTimeException(ex.getMessage(), ex);
            }
        }

        @Override
        public <I extends TimeInterval<?, ?>> @NonNull I parse(@NonNull CharSequence text, @NonNull TimeIntervalQuery<I> query) {
            return query.queryFrom(new AbstractTimeIntervalAccessor() {
                @Override
                public TemporalAmount getDuration() {
                    return duration.apply(text);
                }
            });
        }
    }

    private static final char INTERVAL_DESIGNATOR = '/';

    private static int getIntervalDesignatorIndex(CharSequence text) throws DateTimeParseException {
        int intervalDesignatorIdx = indexOf(text, INTERVAL_DESIGNATOR);
        if (intervalDesignatorIdx == -1) {
            throw new DateTimeParseException("Cannot find interval designator", text, 0);
        }
        return intervalDesignatorIdx;
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

    private static void formatDurationTo(TemporalAmount duration, Appendable appendable) throws IOException {
        appendable.append(duration.toString());
    }

    private static abstract class AbstractTimeIntervalAccessor implements TimeIntervalAccessor {
        @Override
        public Temporal start() {
            throw new RuntimeException();
        }

        @Override
        public Temporal end() {
            throw new RuntimeException();
        }

        @Override
        public TemporalAmount getDuration() {
            throw new RuntimeException();
        }
    }
}
