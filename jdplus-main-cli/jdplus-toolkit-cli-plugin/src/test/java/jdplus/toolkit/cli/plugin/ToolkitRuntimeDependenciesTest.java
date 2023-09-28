package jdplus.toolkit.cli.plugin;

import jdplus.main.cli.design.GAV;
import nbbrd.design.MightBePromoted;
import nbbrd.io.text.TextParser;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ListAssert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.regex.Pattern.compile;
import static org.assertj.core.api.Assertions.assertThat;

public class ToolkitRuntimeDependenciesTest {

    @Test
    public void test() throws IOException {
        assertThat(getRuntimeDependencies(ToolkitRuntimeDependenciesTest.class))
                .describedAs("Check runtime dependencies")
                .satisfies(ToolkitRuntimeDependenciesTest::checkToolkit)
                .satisfies(ToolkitRuntimeDependenciesTest::checkJavaIoUtil)
                .hasSize(20);
    }

    private static void checkToolkit(List<? extends GAV> coordinates) {
        assertThatGroupId(coordinates, "eu.europa.ec.joinup.sat")
                .has(sameVersion())
                .extracting(GAV::getArtifactId)
                .are(matchingPattern(compile("^jdplus-toolkit-base-\\w+$")))
                .hasSize(6);
        assertThatGroupId(coordinates, "com.github.ben-manes.caffeine")
                .extracting(GAV::getArtifactId)
                .containsExactlyInAnyOrder("caffeine");
    }

    private static void checkJavaIoUtil(List<? extends GAV> coordinates) {
        assertThatGroupId(coordinates, "com.github.nbbrd.java-io-util")
                .has(sameVersion())
                .extracting(GAV::getArtifactId)
                .are(matchingPattern(compile("^java-io-(base|xml|xml-bind|win|picocsv)$")))
                .hasSize(5);

        assertThatGroupId(coordinates, "com.github.nbbrd.picocsv")
                .extracting(GAV::getArtifactId)
                .containsExactlyInAnyOrder("picocsv");

        assertThatGroupId(coordinates, "javax.xml.bind")
                .extracting(GAV::getArtifactId)
                .containsExactlyInAnyOrder("jaxb-api");

        assertThatGroupId(coordinates, "javax.activation")
                .extracting(GAV::getArtifactId)
                .containsExactlyInAnyOrder("javax.activation-api");

        assertThatGroupId(coordinates, "org.glassfish.jaxb")
                .has(sameVersion())
                .extracting(GAV::getArtifactId)
                .containsExactlyInAnyOrder("jaxb-runtime", "txw2");

        assertThatGroupId(coordinates, "com.sun.istack")
                .extracting(GAV::getArtifactId)
                .containsExactlyInAnyOrder("istack-commons-runtime");

        assertThatGroupId(coordinates, "com.sun.xml.fastinfoset")
                .extracting(GAV::getArtifactId)
                .containsExactlyInAnyOrder("FastInfoset");

        assertThatGroupId(coordinates, "org.jvnet.staxex")
                .extracting(GAV::getArtifactId)
                .containsExactlyInAnyOrder("stax-ex");
    }

    @MightBePromoted
    private static ListAssert<? extends GAV> assertThatGroupId(List<? extends GAV> coordinates, String groupId) {
        return assertThat(coordinates)
                .describedAs("Check " + groupId)
                .filteredOn(GAV::getGroupId, groupId);
    }

    @MightBePromoted
    private static Condition<List<? extends GAV>> sameVersion() {
        return new Condition<>(GAV::haveSameVersion, "same version");
    }

    @MightBePromoted
    private static Condition<String> matchingPattern(Pattern regex) {
        return new Condition<>(regex.asMatchPredicate(), "matching pattern");
    }

    @MightBePromoted
    private static List<GAV> getRuntimeDependencies(Class<?> anchor) throws IOException {
        return TextParser.onParsingLines(GAV::parseResolvedMavenDependencies)
                .parseResource(anchor, "/runtime-dependencies.txt", UTF_8);
    }
}
