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

class CsvMatrixOutputTest {

    @TempDir
    Path tempDir;

    @Test
    public void endWritesSingleFileForAllDocuments() throws Exception {
        var config = new CsvMatrixOutputConfiguration();
        config.setFolder(tempDir.toFile());
        config.setFileName("matrix");
        config.setItems(List.of("key1", "key2"));
        var output = new CsvMatrixOutput(config);
        var doc1 = createDocument("doc1", "key1", "value1");
        var doc2 = createDocument("doc2", "key2", "value2");

        output.start(null);
        output.process(doc1);
        output.process(doc2);
        output.end(null);

        try (Stream<Path> files = Files.list(tempDir)) {
            assertThat(files.count()).isEqualTo(1);
            Path csvFile = Files.list(tempDir).findFirst().orElseThrow();
            assertThat(csvFile.getFileName().toString()).isEqualTo("matrix.csv");
        }
    }

    @Test
    public void endWritesFileEvenWithNoDocuments() throws Exception {
        var config = new CsvMatrixOutputConfiguration();
        config.setFolder(tempDir.toFile());
        config.setFileName("empty");
        config.setItems(List.of("key1"));
        var output = new CsvMatrixOutput(config);

        output.start(null);
        output.end(null);

        try (Stream<Path> files = Files.list(tempDir)) {
            assertThat(files.count()).isEqualTo(1);
        }
    }

    @Test
    public void endClearsInfosList() throws Exception {
        var config = new CsvMatrixOutputConfiguration();
        config.setFolder(tempDir.toFile());
        config.setFileName("matrix");
        var output = new CsvMatrixOutput(config);
        var doc = createDocument("test", "key", "value");

        output.start(null);
        output.process(doc);
        output.end(null);

        assertThat(output.infos).isNull();
    }

    @Test
    public void multipleDocumentsWrittenToSameFile() throws Exception {
        var config = new CsvMatrixOutputConfiguration();
        config.setFolder(tempDir.toFile());
        config.setFileName("combined");
        config.setItems(List.of("data"));
        var output = new CsvMatrixOutput(config);
        var doc1 = createDocument("first", "data", "one");
        var doc2 = createDocument("second", "data", "two");
        var doc3 = createDocument("third", "data", "three");

        output.start(null);
        output.process(doc1);
        output.process(doc2);
        output.process(doc3);
        output.end(null);

        try (Stream<Path> files = Files.list(tempDir)) {
            assertThat(files.count()).isEqualTo(1);
            Path csvFile = Files.list(tempDir).findFirst().orElseThrow();
            String content = Files.readString(csvFile);
            assertThat(content).contains("first", "second", "third");
        }
    }

    private static SaDocument createDocument(String name, String key, String value) {
        return new SaDocument(
                name,
                Ts.of(Data.TS_PROD),
                null,
                GenericOutput.builder()
                        .entry(key, value)
                        .build(),
                null,
                null
        );
    }
}

