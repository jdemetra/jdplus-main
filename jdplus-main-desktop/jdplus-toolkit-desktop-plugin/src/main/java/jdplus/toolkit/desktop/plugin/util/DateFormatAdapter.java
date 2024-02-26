package jdplus.toolkit.desktop.plugin.util;

import jdplus.toolkit.base.tsp.util.ObsFormat;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateFormatAdapter extends DateFormat {

    @StaticFactoryMethod
    public static @NonNull DateFormatAdapter of(@NonNull ObsFormat obsFormat) {
        DateFormatAdapter result = new DateFormatAdapter(obsFormat);
        fixClone(result);
        return result;
    }

    private static void fixClone(DateFormatAdapter result) {
        result.setCalendar(Calendar.getInstance(TimeZone.getDefault(), Locale.ROOT));
        result.setNumberFormat(NumberFormat.getInstance(Locale.ROOT));
    }

    private final @NonNull ObsFormat obsFormat;

    @lombok.Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final Formatter<Date> dateFormatter = obsFormat.calendarFormatter();

    @lombok.Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final Parser<Date> dateParser = obsFormat.calendarParser();

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        return toAppendTo.append(getDateFormatter().format(date));
    }

    @Override
    public Date parse(String source, ParsePosition pos) {
        Date result = getDateParser().parse(source);
        if (result != null) {
            pos.setIndex(source.length());
        } else {
            pos.setErrorIndex(0);
        }
        return result;
    }
}
