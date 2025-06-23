package jdplus.toolkit.base.api.time;

import lombok.AccessLevel;
import lombok.NonNull;

import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.Locale;

import static jdplus.toolkit.base.api.time.DateType.*;
import static jdplus.toolkit.base.api.time.RepresentationFormat.BASIC;
import static jdplus.toolkit.base.api.time.RepresentationFormat.EXTENDED;

@lombok.Builder(access = AccessLevel.PRIVATE)
public final class IsoDateTimeFormatter {

    public static final IsoDateTimeFormatter BASIC_CALENDAR = IsoDateTimeFormatter.builder().format(BASIC).type(CALENDAR).time(false).build();
    public static final IsoDateTimeFormatter BASIC_CALENDAR_TIME = IsoDateTimeFormatter.builder().format(BASIC).type(CALENDAR).time(true).build();
    public static final IsoDateTimeFormatter BASIC_ORDINAL = IsoDateTimeFormatter.builder().format(BASIC).type(ORDINAL).time(false).build();
    public static final IsoDateTimeFormatter BASIC_WEEK = IsoDateTimeFormatter.builder().format(BASIC).type(WEEK).time(false).build();

    public static final IsoDateTimeFormatter EXTENDED_CALENDAR = IsoDateTimeFormatter.builder().format(EXTENDED).type(CALENDAR).time(false).build();
    public static final IsoDateTimeFormatter EXTENDED_CALENDAR_TIME = IsoDateTimeFormatter.builder().format(EXTENDED).type(CALENDAR).time(true).build();
    public static final IsoDateTimeFormatter EXTENDED_ORDINAL = IsoDateTimeFormatter.builder().format(EXTENDED).type(ORDINAL).time(false).build();
    public static final IsoDateTimeFormatter EXTENDED_WEEK = IsoDateTimeFormatter.builder().format(EXTENDED).type(WEEK).time(false).build();

    @lombok.NonNull
    private final RepresentationFormat format;

    @lombok.NonNull
    private final DateType type;

    private final boolean time;

    public @NonNull String format(@NonNull TemporalAccessor temporal) throws DateTimeException {
        StringBuilder result = new StringBuilder(32);
        formatTo(temporal, result);
        return result.toString();
    }

    public void formatTo(@NonNull TemporalAccessor temporal, @NonNull Appendable appendable) throws DateTimeException {
        getDateTimeFormatter(format, type, time).formatTo(temporal, appendable);
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

    private static final DateTimeFormatter EXTENDED_CALENDAR_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final DateTimeFormatter[][] DATE_FORMATTERS = {
            {BASIC_CALENDAR_FORMATTER, BASIC_ORDINAL_FORMATTER, BASIC_WEEK_FORMATTER},
            {EXTENDED_CALENDAR_FORMATTER, EXTENDED_ORDINAL_FORMATTER, EXTENDED_WEEK_FORMATTER}
    };

    private static final DateTimeFormatter[][] DATE_TIME_FORMATTERS = {
            {BASIC_CALENDAR_TIME_FORMATTER, null, null},
            {EXTENDED_CALENDAR_TIME_FORMATTER, null, null}
    };
}
