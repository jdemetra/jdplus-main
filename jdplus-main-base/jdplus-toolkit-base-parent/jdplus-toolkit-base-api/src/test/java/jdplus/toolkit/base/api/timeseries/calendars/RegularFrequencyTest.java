package jdplus.toolkit.base.api.timeseries.calendars;

import jdplus.toolkit.base.api.timeseries.TsUnit;
import net.bytebuddy.dynamic.loading.PackageDefinitionStrategy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class RegularFrequencyTest {

    @Test
    public void testIntRepresentation() {
        for (RegularFrequency x : RegularFrequency.values()) {
            assertThat(RegularFrequency.parse(x.toInt()))
                    .isEqualTo(x);
        }
        assertThatIllegalArgumentException()
                .isThrownBy(() -> RegularFrequency.parse(42));
    }

    @Test
    public void testTsUnitRepresentation() {
        for (RegularFrequency x : RegularFrequency.values()) {
            assertThat(RegularFrequency.parseTsUnit(x.toTsUnit()))
                    .isEqualTo(x);
        }

        assertThat(RegularFrequency.parseTsUnit(TsUnit.PT1M))
                .isEqualTo(RegularFrequency.Undefined);

        assertThat(RegularFrequency.parseTsUnit(TsUnit.UNDEFINED))
                .isEqualTo(RegularFrequency.Undefined);
    }
}
