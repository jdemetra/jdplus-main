package jdplus.sa.base.csv;

import jdplus.sa.base.api.SaDocument;
import jdplus.toolkit.base.api.processing.GenericOutput;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tck.demetra.data.Data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CsvOutputTest {

    @TempDir
    Path tempDir;

    @Test
    public void constructorClonesConfiguration() {
        var config = new CsvOutputConfiguration();
        config.setFilePrefix("original");

        var output = new CsvOutput(config);

        config.setFilePrefix("modified");

        assertThat(output.config_.getFilePrefix()).isEqualTo("original");
    }

    @Test
    public void endClearsSummary() throws Exception {
        var config = new CsvOutputConfiguration();
        config.setFolder(tempDir.toFile());
        var output = new CsvOutput(config);

        output.start(null);
        output.end(null);

        assertThat(output.summary_).isNull();
    }

    @Test
    public void endWritesFilesForDocumentsWithData() throws Exception {
        var config = new CsvOutputConfiguration();
        config.setFolder(tempDir.toFile());
        config.setFilePrefix("output");
        config.setSeries(List.of("y"));
        var output = new CsvOutput(config);
        var doc = createDocument("test", "y", 1.0, 2.0);

        output.start(null);
        output.process(doc);
        output.end(null);

        try (Stream<Path> files = Files.list(tempDir)) {
            assertThat(files)
                    .anyMatch(f -> f.getFileName().toString().startsWith("output_"));
        }
    }

    @Test
    public void endDoesNotWriteFilesForDocumentsWithoutData() throws Exception {
        var config = new CsvOutputConfiguration();
        config.setFolder(tempDir.toFile());
        config.setFilePrefix("output");
        config.setSeries(List.of("missing"));
        var output = new CsvOutput(config);
        var doc = createDocument("test", "y", 1.0, 2.0);

        output.start(null);
        output.process(doc);
        output.end(null);

        try (Stream<Path> files = Files.list(tempDir)) {
            assertThat(files).isEmpty();
        }
    }

    @Test
    public void fileNameSanitizesSpecialCharacters() throws Exception {
        var config = new CsvOutputConfiguration();
        config.setFolder(tempDir.toFile());
        config.setFilePrefix("output");
        config.setSeries(List.of("y"));
        var output = new CsvOutput(config);
        var doc = createDocument("test?doc*name.txt", "y", 1.0, 2.0);

        output.start(null);
        output.process(doc);
        output.end(null);

        try (Stream<Path> files = Files.list(tempDir)) {
            Path csvFile = files.findFirst().orElseThrow();
            String fileName = csvFile.getFileName().toString();
            String nameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
            assertThat(nameWithoutExtension)
                    .doesNotContain("?", "*");
        }
    }

    private static SaDocument createDocument(String name, String key, double... values) {
        return new SaDocument(
                name,
                Ts.of(Data.TS_PROD),
                null,
                GenericOutput.builder()
                        .entry(key, TsData.ofInternal(TsPeriod.monthly(2010, 1), values))
                        .build(),
                null,
                null
        );
    }
}





