package jdplus.toolkit.base.api.timeseries;

import jdplus.toolkit.base.api.util.HasShortStringRepresentation;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CalendarPeriodObsTest {

    @Test
    public void testRepresentableAsString() {
        assertThat(CalendarPeriodObs.parse("2010-02-17/2010-03-17=3.14"))
                .hasToString("2010-02-17/2010-03-17=3.14")
                .returns("2010-02-17/03-17=3.14", HasShortStringRepresentation::toShortString)
                .isEqualTo(CalendarPeriodObs.of(CalendarPeriod.parse("2010-02-17/2010-03-17"), 3.14))
                .returns(LocalDate.of(2010,2,17), CalendarPeriodObs::getStart)
                .returns(LocalDate.of(2010,3,17), CalendarPeriodObs::getEnd)
                .returns(3.14, CalendarPeriodObs::getValue);
    }
}