package jdplus.toolkit.base.api.timeseries;

import jdplus.toolkit.base.api.util.HasShortStringRepresentation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TsObsTest {

    @Test
    public void testRepresentableAsString() {
        assertThat(TsObs.parse("2011-02-01T00:00/P1M=3.14"))
                .hasToString("2011-02-01T00:00:00/P1M=3.14")
                .returns("2011-02/P1M=3.14", HasShortStringRepresentation::toShortString)
                .isEqualTo(TsObs.of(TsPeriod.parse("2011-02-01T00:00/P1M"), 3.14))
                .isEqualTo(TsObs.parse("2011-02/P1M=3.14"))
                .returns(TsPeriod.parse("2011-02-01T00:00/P1M"), TsObs::getPeriod)
                .returns(3.14, TsObs::getValue);
    }
}