package jdplus.toolkit.desktop.plugin.util;

import jdplus.toolkit.base.tsp.util.ObsFormat;
import org.assertj.core.util.DateUtil;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.ParsePosition;

import static org.assertj.core.api.Assertions.*;

public class DateFormatAdapterTest {

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testFactories() {
        assertThatNullPointerException()
                .isThrownBy(() -> DateFormatAdapter.of(null));
    }

    @Test
    public void testFormat() {
        DateFormatAdapter root = DateFormatAdapter.of(ObsFormat.DEFAULT);
        assertThat(root.format(DateUtil.parse("2003-04-23")))
                .isEqualTo("2003-04-23T00:00:00");
    }

    @Test
    public void testParseWithPosition() {
        DateFormatAdapter root = DateFormatAdapter.of(ObsFormat.DEFAULT);
        assertThat(root.parse("abc", new ParsePosition(0))).isNull();
        assertThat(root.parse("2003-04-23", new ParsePosition(0))).isNull();
        assertThat(root.parse("2003-04-23T00:00:00", new ParsePosition(0))).isEqualTo("2003-04-23T00:00:00");
    }

    @Test
    public void testParse() throws ParseException {
        DateFormatAdapter root = DateFormatAdapter.of(ObsFormat.DEFAULT);
        assertThatExceptionOfType(ParseException.class).isThrownBy(() -> root.parse("abc"));
        assertThatExceptionOfType(ParseException.class).isThrownBy(() -> root.parse("2003-04-23"));
        assertThat(root.parse("2003-04-23T00:00:00")).isEqualTo("2003-04-23T00:00:00");
    }

    @Test
    public void testClone() {
        DateFormatAdapter root = DateFormatAdapter.of(ObsFormat.DEFAULT);
        assertThatCode(root::clone).doesNotThrowAnyException();
    }
}