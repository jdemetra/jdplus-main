package jdplus.sql.desktop.plugin;

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

public class SqlRuntimeDependenciesTest {

    @Test
    public void test() throws IOException {
        assertThat(getRuntimeDependencies(SqlRuntimeDependenciesTest.class))
                .describedAs("Check runtime dependencies")
                .satisfies(SqlRuntimeDependenciesTest::checkSql)
                .satisfies(SqlRuntimeDependenciesTest::checkJavaSqlUtil)
                .hasSize(4);
    }

    private static void checkSql(List<? extends GAV> coordinates) {
        assertThatGroupId(coordinates, "eu.europa.ec.joinup.sat")
                .has(sameVersion())
                .extracting(GAV::getArtifactId)
                .are(matchingPattern(compile("^jdplus-sql-base-\\w+$")))
                .hasSize(1);
    }

    private static void checkJavaSqlUtil(List<? extends GAV> coordinates) {
        assertThatGroupId(coordinates, "com.github.nbbrd.java-sql-util")
                .has(sameVersion())
                .extracting(GAV::getArtifactId)
                .are(matchingPattern(compile("^java-sql-(jdbc|odbc|lhod)$")))
                .hasSize(3);
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
