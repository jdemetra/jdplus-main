package jdplus.sa.base.csv;

import jdplus.sa.base.api.SaDocument;
import jdplus.toolkit.base.api.processing.GenericOutput;
import jdplus.toolkit.base.api.timeseries.Ts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tck.demetra.data.Data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CsvArrayOutputTest {

    @TempDir
    Path tempDir;

    @Test
    public void endDoesNotWriteFilesForDocumentsWithoutData() throws Exception {
        var config = new CsvArrayOutputConfiguration();
        config.setFolder(tempDir.toFile());
        config.setFilePrefix("output");
        config.setArrays(List.of("missing"));
        var output = new CsvArrayOutput(config);
        var doc = createDocument("test", "array1", new double[]{1.0, 2.0});

        output.start(null);
        output.process(doc);
        output.end(null);

        try (Stream<Path> files = Files.list(tempDir)) {
            assertThat(files).isEmpty();
        }
    }

    @Test
    public void endWritesFilesForDocumentsWithMatchingArrays() throws Exception {
        var config = new CsvArrayOutputConfiguration();
        config.setFolder(tempDir.toFile());
        config.setFilePrefix("output");
        config.setArrays(List.of("values"));
        var output = new CsvArrayOutput(config);
        var doc = createDocument("test", "values", new double[]{1.0, 2.0, 3.0});

        output.start(null);
        output.process(doc);
        output.end(null);

        try (Stream<Path> files = Files.list(tempDir)) {
            assertThat(files)
                    .anyMatch(f -> f.getFileName().toString().startsWith("output_"));
        }
    }

    @Test
    public void fileNameSanitizesSpecialCharacters() throws Exception {
        var config = new CsvArrayOutputConfiguration();
        config.setFolder(tempDir.toFile());
        config.setFilePrefix("output");
        config.setArrays(List.of("values"));
        var output = new CsvArrayOutput(config);
        var doc = createDocument("test?array*name.txt", "values", new double[]{1.0, 2.0});

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

    private static SaDocument createDocument(String name, String key, double[] values) {
        return new SaDocument(
                name,
                Ts.of(Data.TS_PROD),
                null,
                GenericOutput.builder()
                        .entry(key, values)
                        .build(),
                null,
                null
        );
    }
}



