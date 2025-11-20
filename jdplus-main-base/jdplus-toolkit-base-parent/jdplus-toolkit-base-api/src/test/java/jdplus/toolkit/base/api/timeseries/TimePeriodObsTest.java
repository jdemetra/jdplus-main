package jdplus.toolkit.base.api.timeseries;

import jdplus.toolkit.base.api.util.HasShortStringRepresentation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TimePeriodObsTest {

    @Test
    public void testRepresentableAsString() {
        assertThat(TimePeriodObs.parse("2010-02-17T11:03:00/2010-03-17T11:03:00=3.14"))
                .hasToString("2010-02-17T11:03:00/2010-03-17T11:03:00=3.14")
                .returns("2010-02-17T11:03:00/03-17T11:03:00=3.14", HasShortStringRepresentation::toShortString)
                .isEqualTo(TimePeriodObs.of(TimePeriod.parse("2010-02-17T11:03:00/2010-03-17T11:03:00"), 3.14))
                .returns(TimePeriod.parse("2010-02-17T11:03:00/2010-03-17T11:03:00"), TimePeriodObs::getPeriod)
                .returns(3.14, TimePeriodObs::getValue);
    }
}