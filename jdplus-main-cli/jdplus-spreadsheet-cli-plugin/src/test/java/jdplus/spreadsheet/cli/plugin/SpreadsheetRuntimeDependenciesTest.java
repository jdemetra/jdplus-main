package jdplus.spreadsheet.cli.plugin;

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

public class SpreadsheetRuntimeDependenciesTest {

    @Test
    public void test() throws IOException {
        assertThat(getRuntimeDependencies(SpreadsheetRuntimeDependenciesTest.class))
                .describedAs("Check runtime dependencies")
                .satisfies(SpreadsheetRuntimeDependenciesTest::checkSpreadsheet)
                .satisfies(SpreadsheetRuntimeDependenciesTest::checkSpreadsheet4j)
                .hasSize(3);
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
                .are(matchingPattern(compile("^spreadsheet-(api|standalone)$")))
                .hasSize(2);
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
