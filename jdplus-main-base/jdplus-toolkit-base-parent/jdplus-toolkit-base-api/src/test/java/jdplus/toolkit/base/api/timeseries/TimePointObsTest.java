package jdplus.toolkit.base.api.timeseries;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TimePointObsTest {

    @Test
    public void testRepresentableAsString() {
        assertThat(TimePointObs.parse("2010-02-17T11:03:00/2010-02-17T11:03:00=3.14"))
                .hasToString("2010-02-17T11:03:00/2010-02-17T11:03:00=3.14")
                .isEqualTo(TimePointObs.of(TimePoint.parse("2010-02-17T11:03:00/2010-02-17T11:03:00"), 3.14))
                .returns(TimePoint.parse("2010-02-17T11:03:00/2010-02-17T11:03:00"), TimePointObs::getPeriod)
                .returns(3.14, TimePointObs::getValue);
    }
}