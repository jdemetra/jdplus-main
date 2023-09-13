package jdplus.spreadsheet.desktop.plugin;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.helpers.ArrayBook;
import ec.util.spreadsheet.helpers.ArraySheet;
import internal.spreadsheet.base.api.SpreadsheetManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class SpreadsheetStandaloneTest {

    @Test
    public void testStandaloneFactories(@TempDir Path temp) {
        String[] extensions = {".htm", ".xlsx", ".xml", ".ods"};

        ArrayBook expected = ArraySheet.copyOf("name1", new Object[][]{
                {"A1", "B1"},
                {"A2", "B2"},
        }).toBook();

        SpreadsheetManager manager = SpreadsheetManager.ofServiceLoader();

        for (Path file : Stream.of(extensions).map(ext -> temp.resolve("hello" + ext)).toList()) {
            Optional<Book.Factory> writer = manager.getWriter(file.toFile());
            assertThat(writer).isNotEmpty();
            assertThatCode(() -> writer.orElseThrow().store(file, expected))
                    .doesNotThrowAnyException();

            Optional<Book.Factory> reader = manager.getReader(file.toFile());
            assertThat(reader).isNotEmpty();
            AtomicReference<ArrayBook> found = new AtomicReference<>();
            assertThatCode(() -> {
                try (Book result = reader.orElseThrow().load(file)) {
                    found.set(ArrayBook.copyOf(result));
                }
            }).doesNotThrowAnyException();
            assertThat(found).hasValue(expected);
        }
    }
}
