package internal.ui.components;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class StackTracePrinterTest {

    @Test
    public void testHtmlBuilder(@TempDir Path tmp) {
        StackTracePrinter<StringBuilder> x = StackTracePrinter.htmlBuilder();

        StringBuilder sb = new StringBuilder();

        sb.setLength(0);
        x.printMessage(sb, "This is a test message.");
        assertThat(sb.toString()).contains("<span class='message'>This is a test message.</span>");

        sb.setLength(0);
        x.printMessage(sb, tmp.toString());
        assertThat(sb.toString()).contains("<a class='message' href='" + tmp.toFile().toURI() + "'>" + tmp + "</a>");

        assertThatCode(() -> {
            sb.setLength(0);
            x.printMessage(sb, tmp + File.separator + ":nonexistent.txt");
        }).doesNotThrowAnyException();
    }
}
