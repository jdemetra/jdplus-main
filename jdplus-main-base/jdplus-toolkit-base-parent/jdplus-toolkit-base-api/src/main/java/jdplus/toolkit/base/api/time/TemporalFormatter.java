package jdplus.toolkit.base.api.time;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.design.VisibleForTesting;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.SignStyle;
import java.time.temporal.*;
import java.util.Locale;

import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static jdplus.toolkit.base.api.time.DateType.*;
import static jdplus.toolkit.base.api.time.RepresentationFormat.BASIC;
import static jdplus.toolkit.base.api.time.RepresentationFormat.EXTENDED;

@lombok.Builder(access = AccessLevel.PRIVATE)
public final class TemporalFormatter {

    public static final TemporalFormatter BASIC_CALENDAR = TemporalFormatter.builder().format(BASIC).type(CALENDAR).time(false).build();
    public static final TemporalFormatter BASIC_CALENDAR_TIME = TemporalFormatter.builder().format(BASIC).type(CALENDAR).time(true).build();
    public static final TemporalFormatter BASIC_ORDINAL = TemporalFormatter.builder().format(BASIC).type(ORDINAL).time(false).build();
    public static final TemporalFormatter BASIC_WEEK = TemporalFormatter.builder().format(BASIC).type(WEEK).time(false).build();

    public static final TemporalFormatter EXTENDED_CALENDAR = TemporalFormatter.builder().format(EXTENDED).type(CALENDAR).time(false).build();
    public static final TemporalFormatter EXTENDED_CALENDAR_TIME = TemporalFormatter.builder().format(EXTENDED).type(CALENDAR).time(true).build();
    public static final TemporalFormatter EXTENDED_ORDINAL = TemporalFormatter.builder().format(EXTENDED).type(ORDINAL).time(false).build();
    public static final TemporalFormatter EXTENDED_WEEK = TemporalFormatter.builder().format(EXTENDED).type(WEEK).time(false).build();

    @lombok.NonNull
    private final RepresentationFormat format;

    @lombok.NonNull
    private final DateType type;

    private final boolean time;

    public @NonNull String format(@NonNull Temporal temporal) throws DateTimeException {
        return format(temporal, null);
    }

    public @NonNull String format(@NonNull Temporal temporal, @Nullable ChronoUnit precision) throws DateTimeException {
        StringBuilder result = new StringBuilder(32);
        formatTo(temporal, result, precision);
        return result.toString();
    }

    public void formatTo(@NonNull Temporal temporal, @NonNull Appendable appendable) throws DateTimeException {
        formatTo(temporal, appendable, null);
    }

    public void formatTo(@NonNull Temporal temporal, @NonNull Appendable appendable, @Nullable ChronoUnit precision) throws DateTimeException {
        if (precision != null && format.equals(EXTENDED) && type.equals(CALENDAR)) {
            appendTo(getAsShortString(temporal, precision), appendable);
        } else {
            getDateTimeFormatter(format, type, time).formatTo(temporal, appendable);
        }
    }

    public <T> @NonNull T parse(@NonNull CharSequence text, @NonNull TemporalQuery<T> query) throws DateTimeParseException {
        return getDateTimeFormatter(format, type, time).parse(text, query);
    }

    private DateTimeFormatter getDateTimeFormatter(RepresentationFormat format, DateType type, boolean time) {
        DateTimeFormatter result = time
                ? DATE_TIME_FORMATTERS[format.ordinal()][type.ordinal()]
                : DATE_FORMATTERS[format.ordinal()][type.ordinal()];
        if (result == null)
            throw new IllegalArgumentException("Unsupported format: " + format + ", type: " + type + ", time: " + time);
        return result;
    }

    @VisibleForTesting
    static String getAsShortString(Temporal temporal, ChronoUnit precision) {
        if (getNanoOfSeconds(temporal) != 0) {
            return temporal.toString();
        }

        int year = get(temporal, ChronoField.YEAR, 0);
        int month = get(temporal, ChronoField.MONTH_OF_YEAR, 1);
        int day = get(temporal, ChronoField.DAY_OF_MONTH, 1);
        int hour = get(temporal, ChronoField.HOUR_OF_DAY, 0);
        int minute = get(temporal, ChronoField.MINUTE_OF_HOUR, 0);
        int second = get(temporal, ChronoField.SECOND_OF_MINUTE, 0);

        ChronoUnit startUnit = getMinChronoUnit(second, minute, hour, day, month);
        ChronoUnit min = min(startUnit, precision);

        return switch (min) {
            case SECONDS -> year +
                    (month < 10 ? "-0" : "-") + month +
                    (day < 10 ? "-0" : "-") + day +
                    (hour < 10 ? "T0" : "T") + hour +
                    (minute < 10 ? ":0" : ":") + minute +
                    (second < 10 ? ":0" : ":") + second;
            case MINUTES -> year +
                    (month < 10 ? "-0" : "-") + month +
                    (day < 10 ? "-0" : "-") + day +
                    (hour < 10 ? "T0" : "T") + hour +
                    (minute < 10 ? ":0" : ":") + minute;
            case HOURS -> year +
                    (month < 10 ? "-0" : "-") + month +
                    (day < 10 ? "-0" : "-") + day +
                    (hour < 10 ? "T0" : "T") + hour;
            case DAYS -> year +
                    (month < 10 ? "-0" : "-") + month +
                    (day < 10 ? "-0" : "-") + day;
            case MONTHS -> year +
                    (month < 10 ? "-0" : "-") + month;
            case YEARS -> String.valueOf(year);
            default -> temporal.toString();
        };
    }

    private static long getNanoOfSeconds(TemporalAccessor temporal) {
        return temporal.isSupported(ChronoField.NANO_OF_SECOND) ? temporal.getLong(ChronoField.NANO_OF_SECOND) : (long) 0;
    }

    private static int get(TemporalAccessor temporal, TemporalField field, int defaultValue) {
        return temporal.isSupported(field) ? temporal.get(field) : defaultValue;
    }

    @MightBePromoted
    private static <T extends Comparable<T>> T min(T first, T second) {
        return first.compareTo(second) < 0 ? first : second;
    }

    private static ChronoUnit getMinChronoUnit(int second, int minute, int hour, int day, int month) {
        if (second != 0) return ChronoUnit.SECONDS;
        if (minute != 0) return ChronoUnit.MINUTES;
        if (hour != 0) return ChronoUnit.HOURS;
        if (day != 1) return ChronoUnit.DAYS;
        if (month != 1) return ChronoUnit.MONTHS;
        return ChronoUnit.YEARS;
    }

    private static final DateTimeFormatter BASIC_CALENDAR_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private static final DateTimeFormatter EXTENDED_CALENDAR_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private static final DateTimeFormatter BASIC_ORDINAL_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
            .appendValue(ChronoField.DAY_OF_YEAR, 3)
            .optionalStart()
            .appendOffsetId()
            .toFormatter(Locale.ROOT);

    private static final DateTimeFormatter EXTENDED_ORDINAL_FORMATTER = DateTimeFormatter.ISO_ORDINAL_DATE;

    private static final DateTimeFormatter BASIC_WEEK_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendValue(IsoFields.WEEK_BASED_YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
            .appendLiteral("W")
            .appendValue(IsoFields.WEEK_OF_WEEK_BASED_YEAR, 2)
            .appendValue(ChronoField.DAY_OF_WEEK, 1)
            .optionalStart()
            .appendOffsetId()
            .toFormatter(Locale.ROOT);

    private static final DateTimeFormatter EXTENDED_WEEK_FORMATTER = DateTimeFormatter.ISO_WEEK_DATE;

    private static final DateTimeFormatter BASIC_TIME = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .toFormatter(Locale.ROOT);

    private static final DateTimeFormatter BASIC_CALENDAR_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(BASIC_CALENDAR_FORMATTER)
            .appendLiteral('T')
            .append(BASIC_TIME)
            .toFormatter(Locale.ROOT);

    private static final DateTimeFormatter EXTENDED_CALENDAR_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .optionalStart().appendLiteral('-').appendPattern("MM")
            .optionalStart().appendLiteral('-').appendPattern("dd")
            .optionalStart().appendLiteral('T').appendPattern("HH")
            .optionalStart().appendLiteral(':').appendPattern("mm")
            .optionalStart().appendLiteral(':').appendPattern("ss")
            .optionalStart().appendFraction(NANO_OF_SECOND, 0, 9, true)
            .optionalEnd().optionalEnd().optionalEnd().optionalEnd().optionalEnd().optionalEnd()
            .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
            .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
            .toFormatter(Locale.ROOT);

    private static final DateTimeFormatter[][] DATE_FORMATTERS = {
            {BASIC_CALENDAR_FORMATTER, BASIC_ORDINAL_FORMATTER, BASIC_WEEK_FORMATTER},
            {EXTENDED_CALENDAR_FORMATTER, EXTENDED_ORDINAL_FORMATTER, EXTENDED_WEEK_FORMATTER}
    };

    private static final DateTimeFormatter[][] DATE_TIME_FORMATTERS = {
            {BASIC_CALENDAR_TIME_FORMATTER, null, null},
            {EXTENDED_CALENDAR_TIME_FORMATTER, null, null}
    };

    static void appendTo(CharSequence text, Appendable appendable) throws DateTimeException {
        try {
            appendable.append(text);
        } catch (IOException ex) {
            throw new DateTimeException(ex.getMessage(), ex);
        }
    }

    static void appendTo(char c, Appendable appendable) throws DateTimeException {
        try {
            appendable.append(c);
        } catch (IOException ex) {
            throw new DateTimeException(ex.getMessage(), ex);
        }
    }
}
