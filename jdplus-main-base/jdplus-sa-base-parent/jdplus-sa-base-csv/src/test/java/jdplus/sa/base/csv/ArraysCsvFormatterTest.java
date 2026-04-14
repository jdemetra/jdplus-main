package jdplus.sa.base.csv;

import nbbrd.design.MightBePromoted;
import nbbrd.io.function.IOConsumer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Locale;

import static jdplus.sa.base.csv.CsvInformationFormatter.getCsvSeparator;
import static jdplus.sa.base.csv.CsvInformationFormatter.getLocale;
import static jdplus.sa.base.csv.CsvInformationFormatter.setCsvSeparator;
import static jdplus.sa.base.csv.CsvInformationFormatter.setLocale;
import static org.assertj.core.api.Assertions.assertThat;

class ArraysCsvFormatterTest {

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
    public void writeReturnsFalseForEmptyCollection() throws IOException {
        var formatter = new ArraysCsvFormatter();

        assertThat(writeToString(w -> formatter.write(List.of(), List.of(), w)))
                .isEmpty();
    }

    @Test
    public void writeReturnsFalseForMismatchedSizes() throws IOException {
        var formatter = new ArraysCsvFormatter();
        var arrays = List.of(DoubleArray.of(new double[]{1.0, 2.0}));
        var names = List.of("name1", "name2");

        boolean result = formatter.write(arrays, names, new StringWriter());

        assertThat(result).isFalse();
    }

    @Test
    public void writesSingleArrayWithOneDimension() throws IOException {
        var formatter = new ArraysCsvFormatter();
        var arrays = List.of(DoubleArray.of(new double[]{1.0, 2.0, 3.0}));
        var names = List.of("array1");

        assertThat(writeToString(w -> formatter.write(arrays, names, w)))
                .containsIgnoringNewLines("array1,1,3,1,2,3");
    }

    @Test
    public void writesSingleArrayWithTwoDimensions() throws IOException {
        var formatter = new ArraysCsvFormatter();
        var array = new DoubleArray(new int[]{2, 3}, new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0});
        var arrays = List.of(array);
        var names = List.of("matrix");

        assertThat(writeToString(w -> formatter.write(arrays, names, w)))
                .containsIgnoringNewLines("matrix,2,2,3,1,2,3,4,5,6");
    }

    @Test
    public void writesMultipleArrays() throws IOException {
        var formatter = new ArraysCsvFormatter();
        var arrays = List.of(
                DoubleArray.of(new double[]{1.0, 2.0}),
                DoubleArray.of(new double[]{3.0, 4.0, 5.0})
        );
        var names = List.of("arr1", "arr2");

        String result = writeToString(w -> formatter.write(arrays, names, w));

        assertThat(result).containsIgnoringNewLines("arr1,1,2,1,2");
        assertThat(result).containsIgnoringNewLines("arr2,1,3,3,4,5");
    }

    @Test
    public void handlesNaNValuesInArray() throws IOException {
        var formatter = new ArraysCsvFormatter();
        var arrays = List.of(DoubleArray.of(new double[]{1.0, Double.NaN, 3.0}));
        var names = List.of("withNaN");

        String result = writeToString(w -> formatter.write(arrays, names, w));

        assertThat(result).containsIgnoringNewLines("withNaN,1,3,1,,3");
    }

    @Test
    public void quotesNamesContainingComma() throws IOException {
        var formatter = new ArraysCsvFormatter();
        var arrays = List.of(DoubleArray.of(new double[]{1.0}));
        var names = List.of("name,with,comma");

        String result = writeToString(w -> formatter.write(arrays, names, w));

        assertThat(result).contains("\"name,with,comma\"");
    }

    @Test
    public void quotesNamesContainingQuotes() throws IOException {
        var formatter = new ArraysCsvFormatter();
        var arrays = List.of(DoubleArray.of(new double[]{1.0}));
        var names = List.of("name\"with\"quotes");

        String result = writeToString(w -> formatter.write(arrays, names, w));

        assertThat(result).contains("\"name\"\"with\"\"quotes\"");
    }

    @Test
    public void usesShortNameByDefault() throws IOException {
        var formatter = new ArraysCsvFormatter();
        formatter.setFullName(false);
        var arrays = List.of(DoubleArray.of(new double[]{1.0}));
        var names = List.of("part1\npart2\npart3");

        String result = writeToString(w -> formatter.write(arrays, names, w));

        assertThat(result).contains("part3");
        assertThat(result).doesNotContain("part1");
        assertThat(result).doesNotContain("part2");
    }

    @Test
    public void usesFullNameWhenSet() throws IOException {
        var formatter = new ArraysCsvFormatter();
        formatter.setFullName(true);
        var arrays = List.of(DoubleArray.of(new double[]{1.0}));
        var names = List.of("part1\npart2\npart3");

        String result = writeToString(w -> formatter.write(arrays, names, w));

        assertThat(result).contains("part1 * part2 * part3");
    }

    @Test
    public void writesEmptyArrayDimensions() throws IOException {
        var formatter = new ArraysCsvFormatter();
        var arrays = List.of(DoubleArray.empty());
        var names = List.of("empty");

        String result = writeToString(w -> formatter.write(arrays, names, w));

        assertThat(result).containsIgnoringNewLines("empty,0");
    }

    @MightBePromoted
    private static String writeToString(IOConsumer<? super Writer> consumer) throws IOException {
        StringWriter writer = new StringWriter();
        consumer.acceptWithIO(writer);
        return writer.toString();
    }
}


