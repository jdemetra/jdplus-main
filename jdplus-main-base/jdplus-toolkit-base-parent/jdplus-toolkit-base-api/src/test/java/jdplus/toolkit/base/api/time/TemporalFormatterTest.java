package jdplus.toolkit.base.api.time;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.temporal.ChronoUnit.*;
import static jdplus.toolkit.base.api.time.TemporalFormatter.*;
import static org.assertj.core.api.Assertions.assertThat;

class TemporalFormatterTest {

    private static final LocalDate _2010_01_01 = LocalDate.of(2010, 1, 1);
    private static final LocalDate _2010_02_01 = LocalDate.of(2010, 2, 1);
    private static final LocalDate _2010_02_15 = LocalDate.of(2010, 2, 15);
    private static final LocalDateTime _2010_02_15T01_02_03 = LocalDateTime.of(2010, 2, 15, 1, 2, 3);

    @Test
    public void testFormat() {
        assertThat(BASIC_CALENDAR.format(_2010_02_15)).isEqualTo("20100215");
        assertThat(EXTENDED_CALENDAR.format(_2010_02_15)).isEqualTo("2010-02-15");

        assertThat(BASIC_ORDINAL.format(_2010_02_15)).isEqualTo("2010046");
        assertThat(EXTENDED_ORDINAL.format(_2010_02_15)).isEqualTo("2010-046");

        assertThat(BASIC_WEEK.format(_2010_02_15)).isEqualTo("2010W071");
        assertThat(EXTENDED_WEEK.format(_2010_02_15)).isEqualTo("2010-W07-1");

        assertThat(BASIC_CALENDAR_TIME.format(_2010_02_15T01_02_03)).isEqualTo("20100215T010203");
        assertThat(EXTENDED_CALENDAR_TIME.format(_2010_02_15T01_02_03)).isEqualTo("2010-02-15T01:02:03");
    }

    @Test
    public void testFormatWithReducedPrecision() {
        assertThat(EXTENDED_CALENDAR.format(_2010_01_01, MONTHS)).isEqualTo("2010-01");
        assertThat(EXTENDED_CALENDAR.format(_2010_02_01, MONTHS)).isEqualTo("2010-02");
        assertThat(EXTENDED_CALENDAR.format(_2010_02_15, MONTHS)).isEqualTo("2010-02-15");
    }

    @Test
    public void testParse() {
        assertThat(BASIC_CALENDAR.parse("20100215", LocalDate::from)).isEqualTo(_2010_02_15);
        assertThat(EXTENDED_CALENDAR.parse("2010-02-15", LocalDate::from)).isEqualTo(_2010_02_15);

        assertThat(BASIC_ORDINAL.parse("2010046", LocalDate::from)).isEqualTo(_2010_02_15);
        assertThat(EXTENDED_ORDINAL.parse("2010-046", LocalDate::from)).isEqualTo(_2010_02_15);

        assertThat(BASIC_WEEK.parse("2010W071", LocalDate::from)).isEqualTo(_2010_02_15);
        assertThat(EXTENDED_WEEK.parse("2010-W07-1", LocalDate::from)).isEqualTo(_2010_02_15);

        assertThat(BASIC_CALENDAR_TIME.parse("20100215T010203", LocalDateTime::from)).isEqualTo(_2010_02_15T01_02_03);
        assertThat(EXTENDED_CALENDAR_TIME.parse("2010-02-15T01:02:03", LocalDateTime::from)).isEqualTo(_2010_02_15T01_02_03);
    }

    @Test
    public void testGetAsShortString() {
        assertThat(getAsShortString(_2010_01_01, SECONDS)).isEqualTo("2010-01-01T00:00:00");
        assertThat(getAsShortString(_2010_01_01, MINUTES)).isEqualTo("2010-01-01T00:00");
        assertThat(getAsShortString(_2010_01_01, HOURS)).isEqualTo("2010-01-01T00");
        assertThat(getAsShortString(_2010_01_01, DAYS)).isEqualTo("2010-01-01");
        assertThat(getAsShortString(_2010_01_01, MONTHS)).isEqualTo("2010-01");
        assertThat(getAsShortString(_2010_01_01, YEARS)).isEqualTo("2010");

        assertThat(getAsShortString(_2010_02_01, SECONDS)).isEqualTo("2010-02-01T00:00:00");
        assertThat(getAsShortString(_2010_02_01, MINUTES)).isEqualTo("2010-02-01T00:00");
        assertThat(getAsShortString(_2010_02_01, HOURS)).isEqualTo("2010-02-01T00");
        assertThat(getAsShortString(_2010_02_01, DAYS)).isEqualTo("2010-02-01");
        assertThat(getAsShortString(_2010_02_01, MONTHS)).isEqualTo("2010-02");
        assertThat(getAsShortString(_2010_02_01, YEARS)).isEqualTo("2010-02");

        assertThat(getAsShortString(_2010_02_15, SECONDS)).isEqualTo("2010-02-15T00:00:00");
        assertThat(getAsShortString(_2010_02_15, MINUTES)).isEqualTo("2010-02-15T00:00");
        assertThat(getAsShortString(_2010_02_15, HOURS)).isEqualTo("2010-02-15T00");
        assertThat(getAsShortString(_2010_02_15, DAYS)).isEqualTo("2010-02-15");
        assertThat(getAsShortString(_2010_02_15, MONTHS)).isEqualTo("2010-02-15");
        assertThat(getAsShortString(_2010_02_15, YEARS)).isEqualTo("2010-02-15");

        assertThat(getAsShortString(_2010_02_15T01_02_03, SECONDS)).isEqualTo("2010-02-15T01:02:03");
        assertThat(getAsShortString(_2010_02_15T01_02_03, MINUTES)).isEqualTo("2010-02-15T01:02:03");
        assertThat(getAsShortString(_2010_02_15T01_02_03, HOURS)).isEqualTo("2010-02-15T01:02:03");
        assertThat(getAsShortString(_2010_02_15T01_02_03, DAYS)).isEqualTo("2010-02-15T01:02:03");
        assertThat(getAsShortString(_2010_02_15T01_02_03, MONTHS)).isEqualTo("2010-02-15T01:02:03");
        assertThat(getAsShortString(_2010_02_15T01_02_03, YEARS)).isEqualTo("2010-02-15T01:02:03");
    }
}