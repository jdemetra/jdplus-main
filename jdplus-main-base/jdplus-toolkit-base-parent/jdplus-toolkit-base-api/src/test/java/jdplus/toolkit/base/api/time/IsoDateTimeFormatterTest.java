package jdplus.toolkit.base.api.time;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static jdplus.toolkit.base.api.time.IsoDateTimeFormatter.*;
import static org.assertj.core.api.Assertions.assertThat;

class IsoDateTimeFormatterTest {

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
}