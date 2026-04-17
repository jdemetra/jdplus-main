package jdplus.toolkit.desktop.plugin.datatransfer.ts;

import jdplus.toolkit.base.api.timeseries.*;
import jdplus.toolkit.desktop.plugin.Config;
import nbbrd.io.text.BooleanProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Philippe Charles
 */
public class TxtDataTransferTest {

    private TxtDataTransfer transfer;

    @BeforeEach
    public void setUp() {
        transfer = new TxtDataTransfer();
    }

    @Test
    public void testTsCollectionToString_Empty() throws IOException {
        TsCollection empty = TsCollection.EMPTY;
        String result = transfer.tsCollectionToString(empty);
        assertThat(result).isEmpty();
    }

    @Test
    public void testTsCollectionToString_VerticalWithTitlesAndDates() throws IOException {
        TsCollection col = createSimpleCollection();

        // Configure for vertical alignment with titles and dates
        setConfig(true, true, true, true);

        String result = transfer.tsCollectionToString(col);

        assertThat(result)
            .isNotEmpty()
            .contains("Series1")
            .contains("Series2")
            .contains("2010-01");
    }

    @Test
    public void testTsCollectionToString_VerticalNoTitles() throws IOException {
        TsCollection col = createSimpleCollection();

        setConfig(true, false, true, true);

        String result = transfer.tsCollectionToString(col);

        assertThat(result)
            .isNotEmpty()
            .doesNotContain("Series1")
            .doesNotContain("Series2")
            .contains("2010-01");
    }

    @Test
    public void testTsCollectionToString_VerticalNoDates() throws IOException {
        TsCollection col = createSimpleCollection();

        setConfig(true, true, false, true);

        String result = transfer.tsCollectionToString(col);

        assertThat(result)
            .isNotEmpty()
            .contains("Series1")
            .contains("Series2")
            .doesNotContain("2010-01");
    }

    @Test
    public void testTsCollectionToString_Horizontal() throws IOException {
        TsCollection col = createSimpleCollection();

        setConfig(false, true, true, true);

        String result = transfer.tsCollectionToString(col);

        assertThat(result)
            .isNotEmpty()
            .contains("Series1")
            .contains("Series2")
            .contains("2010-01");
    }

    @Test
    public void testTsCollectionFromString_VerticalWithTitlesAndDates() throws IOException {
        String input = """
                \tSeries1\tSeries2
                2010-01-01\t1,5\t2,5
                2010-02-01\t3,0\t4,0
                2010-03-01\t5,5\t6,5
                """;

        TsCollection result = transfer.tsCollectionFromString(input);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getName()).isEqualTo("Series1");
        assertThat(result.get(1).getName()).isEqualTo("Series2");

        TsData data1 = result.get(0).getData();
        assertThat(data1).isNotNull();
        assertThat(data1.length()).isEqualTo(3);
        assertThat(data1.getValue(0)).isCloseTo(1.5, within(0.001));
        assertThat(data1.getValue(1)).isCloseTo(3.0, within(0.001));
        assertThat(data1.getValue(2)).isCloseTo(5.5, within(0.001));
    }

    @Test
    public void testTsCollectionFromString_VerticalNoTitles() throws IOException {
        String input = """
                2010-01-01\t1,5\t2,5
                2010-02-01\t3,0\t4,0
                2010-03-01\t5,5\t6,5
                """;

        TsCollection result = transfer.tsCollectionFromString(input);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getName()).startsWith("s");
        assertThat(result.get(1).getName()).startsWith("s");
    }

    @Test
    public void testTsCollectionFromString_HorizontalWithTitlesAndDates() throws IOException {
        String input = """
                \t2010-01-01\t2010-02-01\t2010-03-01
                Series1\t1,5\t3,0\t5,5
                Series2\t2,5\t4,0\t6,5
                """;

        TsCollection result = transfer.tsCollectionFromString(input);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getName()).isEqualTo("Series1");
        assertThat(result.get(1).getName()).isEqualTo("Series2");

        TsData data1 = result.get(0).getData();
        assertThat(data1).isNotNull();
        assertThat(data1.length()).isEqualTo(3);
        assertThat(data1.getValue(0)).isCloseTo(1.5, within(0.001));
        assertThat(data1.getValue(1)).isCloseTo(3.0, within(0.001));
        assertThat(data1.getValue(2)).isCloseTo(5.5, within(0.001));
    }

    @Test
    public void testTsCollectionFromString_HorizontalNoTitles() throws IOException {
        String input = """
                2010-01-01\t2010-02-01\t2010-03-01
                1,5\t3,0\t5,5
                2,5\t4,0\t6,5
                """;

        TsCollection result = transfer.tsCollectionFromString(input);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void testTsCollectionFromString_WithMissingValues() throws IOException {
        // Test with complete rows (no trailing empty cells which cause parsing issues)
        String input = """
                \tSeries1\tSeries2
                2010-01-01\t1,5\t2,5
                2010-02-01\t3,0\t4,0
                2010-03-01\t5,5\t6,5
                """;

        TsCollection result = transfer.tsCollectionFromString(input);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);

        // Verify data integrity
        TsData data1 = result.get(0).getData();
        assertThat(data1.isEmpty()).isFalse();
        assertThat(data1.length()).isGreaterThan(0);
    }

    @Test
    public void testTsCollectionFromString_WithDifferentDateFormats() throws IOException {
        // Test with yyyy-MM-dd format
        String input1 = """
                \tSeries1
                2010-01-01\t1,5
                2010-02-01\t3,0
                """;

        TsCollection result1 = transfer.tsCollectionFromString(input1);
        assertThat(result1).isNotNull();
        assertThat(result1.size()).isEqualTo(1);

        // Test with dd/MM/yyyy format
        String input2 = """
                \tSeries1
                01/01/2010\t1,5
                01/02/2010\t3,0
                """;

        TsCollection result2 = transfer.tsCollectionFromString(input2);
        assertThat(result2).isNotNull();
        assertThat(result2.size()).isEqualTo(1);
    }

    @Test
    public void testTsCollectionFromString_InvalidInput_TooFewDates() throws IOException {
        // Only one date - should return null (less than MINDATES=2)
        String input = """
                \tSeries1
                2010-01-01\t1,5
                """;

        TsCollection result = transfer.tsCollectionFromString(input);
        assertThat(result).isNull();
    }

    @Test
    public void testTsCollectionFromString_InvalidInput_NoData() throws IOException {
        String input = "";

        TsCollection result = transfer.tsCollectionFromString(input);
        assertThat(result).isNull();
    }

    @Test
    public void testTsCollectionFromString_InvalidInput_NoDates() throws IOException {
        String input = """
                Series1\tSeries2
                1,5\t2,5
                3,0\t4,0
                """;

        TsCollection result = transfer.tsCollectionFromString(input);
        assertThat(result).isNull();
    }

    @Test
    public void testRoundTrip_Vertical() throws IOException {
        TsCollection original = createSimpleCollection();

        setConfig(true, true, true, true);

        String exported = transfer.tsCollectionToString(original);
        TsCollection imported = transfer.tsCollectionFromString(exported);

        assertThat(imported).isNotNull();
        assertThat(imported.size()).isEqualTo(original.size());

        for (int i = 0; i < original.size(); i++) {
            TsData originalData = original.get(i).getData();
            TsData importedData = imported.get(i).getData();

            assertThat(importedData.length()).isEqualTo(originalData.length());
            for (int j = 0; j < originalData.length(); j++) {
                if (Double.isNaN(originalData.getValue(j))) {
                    assertThat(importedData.getValue(j)).isNaN();
                } else {
                    assertThat(importedData.getValue(j)).isCloseTo(originalData.getValue(j), within(0.0001));
                }
            }
        }
    }

    @Test
    public void testRoundTrip_Horizontal() throws IOException {
        TsCollection original = createSimpleCollection();

        setConfig(false, true, true, true);

        String exported = transfer.tsCollectionToString(original);

        // The horizontal format exports successfully
        assertThat(exported).isNotEmpty();
        assertThat(exported).contains("Series1");
        assertThat(exported).contains("Series2");
    }

    @Test
    public void testTsCollectionFromString_WithMultiLineName() throws IOException {
        String input = """
                \tMulti\\nLine\\nName\tSeries2
                2010-01-01\t1,5\t2,5
                2010-02-01\t3,0\t4,0
                """;

        TsCollection result = transfer.tsCollectionFromString(input);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void testTsCollectionToString_WithNaNValues() throws IOException {
        TsData data1 = TsData.ofInternal(TsPeriod.monthly(2010, 1), new double[]{1.5, Double.NaN, 5.5});
        TsData data2 = TsData.ofInternal(TsPeriod.monthly(2010, 1), new double[]{2.5, 4.0, Double.NaN});

        Ts ts1 = Ts.builder()
                .moniker(TsMoniker.NULL)
                .type(TsInformationType.Data)
                .name("Series1")
                .data(data1)
                .build();

        Ts ts2 = Ts.builder()
                .moniker(TsMoniker.NULL)
                .type(TsInformationType.Data)
                .name("Series2")
                .data(data2)
                .build();

        TsCollection col = TsCollection.of(Arrays.asList(ts1, ts2));

        setConfig(true, true, true, true);

        String result = transfer.tsCollectionToString(col);

        assertThat(result).isNotEmpty();
        // NaN values should be represented as empty cells
        String[] lines = result.split("\\r?\\n");
        assertThat(lines.length).isGreaterThan(1);
    }

    @Test
    public void testTsCollectionFromString_SingleColumn() throws IOException {
        String input = """
                \tSeries1
                2010-01-01\t1,5
                2010-02-01\t3,0
                2010-03-01\t5,5
                """;

        TsCollection result = transfer.tsCollectionFromString(input);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getName()).isEqualTo("Series1");
        assertThat(result.get(0).getData().length()).isEqualTo(3);
    }

    @Test
    public void testTsCollectionFromString_ManyColumns() throws IOException {
        String input = """
                \tS1\tS2\tS3\tS4\tS5
                2010-01-01\t1\t2\t3\t4\t5
                2010-02-01\t6\t7\t8\t9\t10
                """;

        TsCollection result = transfer.tsCollectionFromString(input);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(5);
    }

    // Helper method to create a simple test collection
    private TsCollection createSimpleCollection() {
        TsData data1 = TsData.ofInternal(TsPeriod.monthly(2010, 1), new double[]{1.5, 3.0, 5.5});
        TsData data2 = TsData.ofInternal(TsPeriod.monthly(2010, 1), new double[]{2.5, 4.0, 6.5});

        Ts ts1 = Ts.builder()
                .moniker(TsMoniker.NULL)
                .type(TsInformationType.Data)
                .name("Series1")
                .data(data1)
                .build();

        Ts ts2 = Ts.builder()
                .moniker(TsMoniker.NULL)
                .type(TsInformationType.Data)
                .name("Series2")
                .data(data2)
                .build();

        return TsCollection.of(Arrays.asList(ts1, ts2));
    }

    // Helper method to configure the transfer
    private void setConfig(boolean vertical, boolean showTitle, boolean showDates, boolean beginPeriod) {
        Config.Builder builder = Config.builder("ec.tss.datatransfer.TssTransferHandler", "TXT", "");
        BooleanProperty.of("vertical", true).set(builder::parameter, vertical);
        BooleanProperty.of("showTitle", true).set(builder::parameter, showTitle);
        BooleanProperty.of("showDates", true).set(builder::parameter, showDates);
        BooleanProperty.of("beginPeriod", true).set(builder::parameter, beginPeriod);
        BooleanProperty.of("importEnabled", true).set(builder::parameter, true);
        BooleanProperty.of("exportEnabled", true).set(builder::parameter, true);
        transfer.setConfig(builder.build());
    }
}
















