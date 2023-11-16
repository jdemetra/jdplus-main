package jdplus.sa.base.csv;

import jdplus.toolkit.base.api.timeseries.TsData;
import nbbrd.design.MightBePromoted;
import nbbrd.io.function.IOConsumer;
import org.junit.jupiter.api.Test;
import tck.demetra.data.DataGenerator;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TsCollectionCsvFormatterTest {

    @Test
    public void testWriteVTable() throws IOException {
        TsCollectionCsvFormatter x = new TsCollectionCsvFormatter();
        x.setPresentation(CsvLayout.VTable);

        assertThat(writeToString(w -> x.write(List.of(), List.of(), w)))
                .isEmpty();

        assertThat(writeToString(w -> x.write(List.of(TsData.empty("no data")), List.of("S1"), w)))
                .isEmpty();

        assertThat(writeToString(w -> x.write(DataGenerator.BY_INDEX.listOfTsData("R0/2010-01-01T00:00:00/P1M", "R3/2010-01-01T00:00:00/P1M", "R1/2010-01-01T00:00:00/P3M"), List.of("S1", "S2", "S3"), w)))
                .isEqualToNormalizingNewlines(
                        """
                                ,S1,S2,S3
                                2010-01-01,,1.1,
                                2010-02-01,,2.2,
                                2010-03-01,,3.3,1.1
                                """
                );
    }

    @Test
    public void testWriteHTable() throws IOException {
        TsCollectionCsvFormatter x = new TsCollectionCsvFormatter();
        x.setPresentation(CsvLayout.HTable);

        assertThat(writeToString(w -> x.write(List.of(), List.of(), w)))
                .isEmpty();

        assertThat(writeToString(w -> x.write(List.of(TsData.empty("no data")), List.of("S1"), w)))
                .isEmpty();

        assertThat(writeToString(w -> x.write(DataGenerator.BY_INDEX.listOfTsData("R0/2010-01-01T00:00:00/P1M", "R3/2010-01-01T00:00:00/P1M", "R1/2010-01-01T00:00:00/P3M"), List.of("S1", "S2", "S3"), w)))
                .isEqualToNormalizingNewlines(
                        """
                                ,2010-01-01,2010-02-01,2010-03-01
                                S1,,,
                                S2,1.1,2.2,3.3
                                S3,,,1.1
                                """
                );
    }

    @Test
    public void testWriteList() throws IOException {
        TsCollectionCsvFormatter x = new TsCollectionCsvFormatter();
        x.setPresentation(CsvLayout.List);

        assertThat(writeToString(w -> x.write(List.of(), List.of(), w)))
                .isEmpty();

        assertThat(writeToString(w -> x.write(List.of(TsData.empty("no data")), List.of("S1"), w)))
                .isEqualToIgnoringNewLines("S1,1,1970,1,0");

        assertThat(writeToString(w -> x.write(DataGenerator.BY_INDEX.listOfTsData("R0/2010-01-01T00:00:00/P1M", "R3/2010-01-01T00:00:00/P1M", "R1/2010-01-01T00:00:00/P3M"), List.of("S1", "S2", "S3"), w)))
                .isEqualToNormalizingNewlines(
                        """
                                S1,12,2010,1,0
                                S2,12,2010,1,3,1.1,2.2,3.3
                                S3,4,2010,1,1,1.1
                                """
                );
    }

    @MightBePromoted
    private static String writeToString(IOConsumer<? super Writer> consumer) throws IOException {
        StringWriter writer = new StringWriter();
        consumer.acceptWithIO(writer);
        return writer.toString();
    }
}
