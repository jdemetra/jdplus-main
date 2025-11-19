package jdplus.toolkit.desktop.plugin.core.components;

import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.desktop.plugin.core.components.InternalComponents.TsDomainDateFormat;
import org.assertj.core.util.DateUtil;
import org.junit.jupiter.api.Test;

import java.text.ParsePosition;

import static org.assertj.core.api.Assertions.assertThat;

public class InternalComponentsTest {

    @Test
    public void testTsDomainDateFormat() {
        assertThat(new TsDomainDateFormat(() -> TsDomain.parse("R30/2011-02/P1M")))
                .isEqualTo(new TsDomainDateFormat(() -> TsDomain.parse("R30/2011-02/P1M")))
                .returns("2011-01", a1 -> a1.format(DateUtil.parse("2011-01-31")))
                .returns("2011-02", a1 -> a1.format(DateUtil.parse("2011-02-01")))
                .returns(null, a1 -> a1.parse("invalid-date", new ParsePosition(0)));
    }
}
