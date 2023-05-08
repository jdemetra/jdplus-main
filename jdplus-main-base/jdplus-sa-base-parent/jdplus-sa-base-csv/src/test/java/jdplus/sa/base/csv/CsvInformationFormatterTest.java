package jdplus.sa.base.csv;


import jdplus.toolkit.base.api.information.Explorable;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.api.math.Complex;
import jdplus.toolkit.base.api.processing.GenericOutput;
import jdplus.toolkit.base.api.util.NamedObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static jdplus.sa.base.csv.CsvInformationFormatter.*;
import static org.assertj.core.api.Assertions.assertThat;

class CsvInformationFormatterTest {

    private static Locale SAVED_LOCALE;
    private static char SAVED_CSV_SEPARATOR;

    @BeforeAll
    public static void beforeAll() {
        SAVED_LOCALE = getLocale();
        setLocale(Locale.ROOT);
        SAVED_CSV_SEPARATOR = getCsvSeparator();
        setCsvSeparator(',');
    }

    @AfterAll
    public static void afterAll() {
        setLocale(SAVED_LOCALE);
        setCsvSeparator(SAVED_CSV_SEPARATOR);
    }

    @Test
    public void testFormattedTypes() {
        assertThat(formattedTypes())
                .isNotEmpty();
    }

    @Test
    public void testFormatEmpty() throws IOException {
        try (var writer = new StringWriter()) {
            format(writer, Collections.emptyList(), Collections.emptyList(), false);
            assertThat(writer.toString())
                    .isEqualToIgnoringNewLines("");
        }

        try (var writer = new StringWriter()) {
            format(writer, Collections.emptyList(), List.of("complex", "string"), false);
            assertThat(writer.toString())
                    .isEqualToIgnoringNewLines("");
        }
    }

    @Test
    public void testFormatFull() throws IOException {
        try (var writer = new StringWriter()) {
            var line1 = new InformationSet();
            line1.set("complex", Complex.cart(1, 1));
            line1.set("string", "hello");
            line1.set("boolean", true);

            var line2 = new InformationSet();
            line2.set("complex", Complex.cart(2, 1));
            line2.set("string", "world");
            line2.set("boolean", false);

            format(writer, List.of(line1, line2), List.of("complex", "string"), false);
            assertThat(writer.toString())
                    .isEqualToIgnoringNewLines(
                            """
                                    complex,,string
                                    1.4142,8,hello
                                    2.2361,13.5516,world
                                    """
                    );
        }
    }

    @Test
    public void testFormatMissing() throws IOException {
        try (var writer = new StringWriter()) {
            var line1 = new InformationSet();
            line1.set("complex", MISSING_COMPLEX);
            line1.set("string", "hello");
            line1.set("boolean", true);

            var line2 = new InformationSet();
            line2.set("complex", Complex.cart(2, 1));
            line1.set("string", MISSING_STRING);
            line2.set("boolean", false);

            format(writer, List.of(line1, line2), List.of("complex", "string"), false);
            assertThat(writer.toString())
                    .isEqualToIgnoringNewLines(
                            """
                                    complex,,string
                                    ,,hello
                                    2.2361,13.5516,
                                    """
                    );
        }
    }

    @Test
    public void testFormatWildCards() throws IOException {
        try (var writer = new StringWriter()) {
            var line1 = new InformationSet();
            line1.set("complex", "fixme:1x1");
            line1.set("string", "hello");
            line1.set("boolean", "fixme:1x3");

            var line2 = new InformationSet();
            line2.set("complex", "fixme:2x1");
            line2.set("string", "world");
            line2.set("boolean", "fixme:2x3");

            format(writer, List.of(line1, line2), List.of("*", "string"), false);
            assertThat(writer.toString())
                    .isEqualToIgnoringNewLines(
                            """
                                    complex,string,boolean,string
                                    fixme:1x1,hello,fixme:1x3,hello
                                    fixme:2x1,world,fixme:2x3,world
                                    """
                    );
        }
    }

    @Test
    public void testFormatResultsEmpty() throws IOException {
        try (var writer = new StringWriter()) {
            formatResults(writer, Collections.emptyList(), Collections.emptyList(), false, false);
            assertThat(writer.toString())
                    .isEqualToIgnoringNewLines("\"\"");
        }

        try (var writer = new StringWriter()) {
            formatResults(writer, Collections.emptyList(), List.of("complex", "string"), false, false);
            assertThat(writer.toString())
                    .isEqualToIgnoringNewLines("\"\"");
        }
    }

    @Test
    public void testFormatResultsFull() throws IOException {
        try (var writer = new StringWriter()) {
            var line1 = new NamedObject<Explorable>(
                    "line1",
                    GenericOutput
                            .builder()
                            .entry("complex", "fixme:1x1")
                            .entry("string", "hello")
                            .entry("boolean", "fixme:1x3")
                            .build()
            );

            var line2 = new NamedObject<Explorable>(
                    "line2",
                    GenericOutput
                            .builder()
                            .entry("complex", "fixme:2x1")
                            .entry("string", "world")
                            .entry("boolean", "fixme:2x3")
                            .build()
            );

            formatResults(writer, List.of(line1, line2), List.of("complex", "string"), false, false);
            assertThat(writer.toString())
                    .isEqualToIgnoringNewLines(
                            """
                                    ,complex,string
                                    line1,fixme:1x1,hello
                                    line2,fixme:2x1,world
                                    """
                    );
        }
    }

    @Test
    public void testFormatResultsMissing() throws IOException {
        try (var writer = new StringWriter()) {
            var line1 = new NamedObject<Explorable>(
                    "line1",
                    GenericOutput
                            .builder()
                            .entry("complex", MISSING_COMPLEX)
                            .entry("string", "hello")
                            .entry("boolean", "fixme:1x3")
                            .build()
            );

            var line2 = new NamedObject<Explorable>(
                    "line2",
                    GenericOutput
                            .builder()
                            .entry("complex", "fixme:2x1")
                            .entry("string", MISSING_STRING)
                            .entry("boolean", "fixme:2x3")
                            .build()
            );

            formatResults(writer, List.of(line1, line2), List.of("complex", "string"), false, false);
            assertThat(writer.toString())
                    .isEqualToIgnoringNewLines(
                            """
                                    ,complex,string
                                    line1,,hello
                                    line2,fixme:2x1,
                                    """
                    );
        }
    }

    @Test
    public void testFormatResultsWildCards() throws IOException {
        try (var writer = new StringWriter()) {
            var line1 = new NamedObject<Explorable>(
                    "line1",
                    GenericOutput
                            .builder()
                            .entry("complex", "fixme:1x1")
                            .entry("string", "hello")
                            .entry("boolean", "fixme:1x3")
                            .build()
            );

            var line2 = new NamedObject<Explorable>(
                    "line2",
                    GenericOutput
                            .builder()
                            .entry("complex", "fixme:2x1")
                            .entry("string", "world")
                            .entry("boolean", "fixme:2x3")
                            .build()
            );

            formatResults(writer, List.of(line1, line2), List.of("*", "string"), false, false);
            assertThat(writer.toString())
                    .isEqualToIgnoringNewLines(
                            """
                                    ,complex,string,boolean,string
                                    line1,fixme:1x1,hello,fixme:1x3,hello
                                    line2,fixme:2x1,world,fixme:2x3,world
                                    """
                    );
        }
    }

    private static final Complex MISSING_COMPLEX = null;
    private static final String MISSING_STRING = null;
}