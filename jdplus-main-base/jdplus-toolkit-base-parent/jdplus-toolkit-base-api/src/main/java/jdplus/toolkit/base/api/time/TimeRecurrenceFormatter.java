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
import org.jspecify.annotations.Nullable;

import java.time.DateTimeException;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import static jdplus.toolkit.base.api.time.TemporalFormatter.appendTo;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(staticName = "of")
public final class TimeRecurrenceFormatter {

    @lombok.NonNull
    private final TimeIntervalFormatter intervalFormatter;

    @lombok.NonNull
    private final TimeIntervalQuery<? extends TimeInterval<?, ?>> intervalQuery;

    public @NonNull String format(@NonNull TimeRecurrence<?> timeRecurrence) throws DateTimeException {
        return format(timeRecurrence, null);
    }

    public @NonNull String format(@NonNull TimeRecurrence<?> timeRecurrence, @Nullable ChronoUnit precision) throws DateTimeException {
        StringBuilder result = new StringBuilder(32);
        formatTo(timeRecurrence, result, precision);
        return result.toString();
    }

    public void formatTo(@NonNull TimeRecurrence<?> timeRecurrence, @NonNull Appendable appendable) throws DateTimeException {
        formatTo(timeRecurrence, appendable, null);
    }

    public void formatTo(@NonNull TimeRecurrence<?> timeRecurrence, @NonNull Appendable appendable, @Nullable ChronoUnit precision) throws DateTimeException {
        appendTo(RECURRENCE_CHAR, appendable);
        appendTo(String.valueOf(timeRecurrence.length()), appendable);
        appendTo(RECURRENCE_SEPARATOR, appendable);
        intervalFormatter.formatTo(timeRecurrence.getInterval(), appendable, precision);
    }

    public <R extends TimeRecurrence<?>> @NonNull R parse(@NonNull CharSequence text, @NonNull TimeRecurrenceQuery<R> query) throws DateTimeParseException {
        if (text.charAt(0) != RECURRENCE_CHAR) {
            throw new DateTimeParseException("Cannot found recurrence character", text, 0);
        }
        int index = getRecurrenceSeparatorIndex(text);
        CharSequence left = text.subSequence(1, index);
        CharSequence right = text.subSequence(index + 1, text.length());
        R result = query.queryFrom(
                new TimeRecurrenceAccessor() {
                    @Override
                    public @NonNull TimeInterval<?, ?> getInterval() {
                        return intervalFormatter.parse(right, intervalQuery);
                    }

                    @Override
                    public int length() {
                        return parseLength(left);
                    }
                });
        if (result == null) {
            throw new DateTimeException("Unable to obtain TimeRecurrence from TimeRecurrenceQuery");
        }
        return result;
    }

    private static int getRecurrenceSeparatorIndex(CharSequence text) throws DateTimeParseException {
        int intervalDesignatorIdx = TimeIntervalFormatter.indexOf(text, RECURRENCE_SEPARATOR);
        if (intervalDesignatorIdx == -1) {
            throw new DateTimeParseException("Cannot find recurrence separator", text, 0);
        }
        return intervalDesignatorIdx;
    }

    private static int parseLength(CharSequence text) throws DateTimeParseException {
        try {
            return Integer.parseInt(text.toString());
        } catch (NumberFormatException ex) {
            throw new DateTimeParseException("Cannot parse length", text, 0, ex);
        }
    }

    private static final char RECURRENCE_CHAR = 'R';
    private static final char RECURRENCE_SEPARATOR = '/';
}
