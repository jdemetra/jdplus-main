package jdplus.main.cli.design;

import lombok.NonNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@lombok.Value
public class GAV {

    @NonNull String groupId;
    @NonNull String artifactId;
    @NonNull String version;

    public static boolean haveSameVersion(@NonNull List<? extends GAV> list) {
        return list
                .stream()
                .map(GAV::getVersion)
                .distinct()
                .count() == 1;
    }

    public static @NonNull List<GAV> parseResolvedMavenDependencies(@NonNull Stream<String> lines) throws IllegalArgumentException {
        return lines
                .filter(GAV::isValidResolvedMavenDependency)
                .map(GAV::removeAnsiControlChars)
                .map(GAV::extractResolvedMavenDependency)
                .map(GAV::parseResolvedMavenDependency)
                .collect(Collectors.toList());
    }

    private static GAV parseResolvedMavenDependency(CharSequence input) throws IllegalArgumentException {
        String[] items = input.toString().split(":", -1);
        if (items.length < 4) {
            throw new IllegalArgumentException("Invalid GAV: '" + input + "'");
        }
        return new GAV(items[0], items[1], items[3]);
    }

    private static final String PREFIX = "   ";
    private static final String SUFFIX = " -- module";

    private static boolean isValidResolvedMavenDependency(String line) {
        return line.startsWith(PREFIX);
    }

    private static String removeAnsiControlChars(String input) {
        return input.replaceAll("\u001B\\[[;\\d]*m", "");
    }

    private static String extractResolvedMavenDependency(String input) {
        int start = PREFIX.length();
        int end = input.indexOf(SUFFIX, start);
        return input.substring(start, end == -1 ? input.length() : end);
    }
}
