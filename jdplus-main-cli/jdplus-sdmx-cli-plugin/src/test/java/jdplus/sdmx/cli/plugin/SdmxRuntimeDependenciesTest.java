package jdplus.sdmx.cli.plugin;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import jdplus.main.cli.design.GAV;
import nbbrd.io.text.TextParser;
import org.junit.jupiter.api.Test;

public class SdmxRuntimeDependenciesTest {

    @Test
    public void test() throws IOException {
        assertThat(TextParser.onParsingLines(GAV::parseResolvedMavenDependencies)
                        .parseResource(SdmxRuntimeDependenciesTest.class, "/runtime-dependencies.txt", UTF_8))
                .describedAs("Check runtime dependencies")
                .extracting(coordinate -> coordinate.getGroupId() + ":" + coordinate.getArtifactId())
                .containsExactlyInAnyOrder(
                        "eu.europa.ec.joinup.sat:jdplus-sdmx-base-api", "com.github.nbbrd.sdmx-dl:sdmx-dl-standalone");
    }
}
