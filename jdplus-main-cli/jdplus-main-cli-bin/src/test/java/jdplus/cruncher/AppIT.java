package jdplus.cruncher;

import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.io.sys.OS;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static nbbrd.io.function.IOFunction.unchecked;
import static nbbrd.io.sys.ProcessReader.readToString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

public class AppIT {

    @Test
    public void testApp() throws URISyntaxException {
        Path targetFolder = getTargetFolder(AppIT.class).orElseThrow(() -> new IllegalStateException("No target folder found"));
        Path bin = targetFolder.resolve("appassembler").resolve("bin");
        assertThat(resolveScript(bin))
                .existsNoFollowLinks().isRegularFile()
                .extracting(unchecked(AppIT::getVersion), STRING)
                .contains("jwsacruncher")
                .doesNotContainIgnoringCase("warning");
    }

    private static @NonNull Path resolveScript(Path bin) {
        return OS.NAME.equals(OS.Name.WINDOWS)
                ? bin.resolve("jwsacruncher.bat")
                : bin.resolve("jwsacruncher");
    }

    @MightBePromoted
    private static String getVersion(Path program) throws IOException {
        return readToString(UTF_8, new ProcessBuilder(program.toString(), "--version").redirectErrorStream(true).start());
    }

    @MightBePromoted
    private static Optional<Path> getTargetFolder(Class<?> anchor) throws URISyntaxException {
        Path result = Path.of(requireNonNull(anchor.getResource(anchor.getSimpleName() + ".class")).toURI());
        while (result != null && !result.getFileName().toString().equals("target")) {
            result = result.getParent();
        }
        return Optional.ofNullable(result);
    }
}
