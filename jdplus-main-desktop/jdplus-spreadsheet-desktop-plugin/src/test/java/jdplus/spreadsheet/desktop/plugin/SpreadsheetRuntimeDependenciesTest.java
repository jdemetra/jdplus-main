package jdplus.spreadsheet.desktop.plugin;

import jdplus.main.desktop.design.GAV;
import nbbrd.design.MightBePromoted;
import nbbrd.io.FileParser;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ListAssert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static org.assertj.core.api.Assertions.assertThat;

public class SpreadsheetRuntimeDependenciesTest {

    @Test
    public void test() throws IOException {
        assertThat(getRuntimeDependencies(SpreadsheetRuntimeDependenciesTest.class))
                .describedAs("Check runtime dependencies")
                .satisfies(SpreadsheetRuntimeDependenciesTest::checkSpreadsheet)
                .satisfies(SpreadsheetRuntimeDependenciesTest::checkSpreadsheet4j)
                .hasSize(12);
    }

    private static void checkSpreadsheet(List<? extends GAV> coordinates) {
        assertThatGroupId(coordinates, "eu.europa.ec.joinup.sat")
                .has(sameVersion())
                .extracting(GAV::getArtifactId)
                .are(matchingPattern(compile("^jdplus-spreadsheet-base-\\w+$")))
                .hasSize(1);
    }

    private static void checkSpreadsheet4j(List<? extends GAV> coordinates) {
        assertThatGroupId(coordinates, "com.github.nbbrd.spreadsheet4j")
                .has(sameVersion())
                .extracting(GAV::getArtifactId)
                .are(matchingPattern(compile("^spreadsheet-(api|util|xmlss|od|xl|html|fastexcel)$")))
                .hasSize(7);

        assertThatGroupId(coordinates, "com.github.miachm.sods")
                .extracting(GAV::getArtifactId)
                .containsExactlyInAnyOrder("SODS");

        assertThatGroupId(coordinates, "com.github.rzymek")
                .extracting(GAV::getArtifactId)
                .containsExactlyInAnyOrder("opczip");

        assertThatGroupId(coordinates, "org.dhatim")
                .extracting(GAV::getArtifactId)
                .containsExactlyInAnyOrder("fastexcel");

        assertThatGroupId(coordinates, "org.jsoup")
                .extracting(GAV::getArtifactId)
                .containsExactlyInAnyOrder("jsoup");
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
        return FileParser.onParsingStream(Manifest::new)
                .andThen(GAV::parseNbmMavenClassPath)
                .parseResource(anchor, "/runtime-dependencies.mf");
    }
}
