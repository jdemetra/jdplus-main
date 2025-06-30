package jdplus.toolkit.base.api.timeseries;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DayObsTest {

    @Test
    public void testRepresentableAsString() {
        assertThat(DayObs.parse("2010-01-01/P1D=3.14"))
                .hasToString("2010-01-01/P1D=3.14")
                .isEqualTo(DayObs.of(LocalDate.of(2010, 1, 1), 3.14))
                .returns(LocalDate.of(2010, 1, 1), DayObs::getDate)
                .returns(3.14, DayObs::getValue);
    }
}