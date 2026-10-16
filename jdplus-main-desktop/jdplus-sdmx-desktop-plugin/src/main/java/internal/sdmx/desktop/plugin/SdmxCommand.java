package internal.sdmx.desktop.plugin;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Map;
import sdmxdl.DatabaseRef;
import sdmxdl.Key;
import sdmxdl.web.WebFlowRequest;
import sdmxdl.web.WebKeyRequest;

@lombok.Value
@lombok.Builder
public class SdmxCommand {

    @lombok.Singular
    List<String> parameters;

    @lombok.Singular
    Map<String, String> options;

    public String toText() {
        return ("sdmx-dl "
                        + String.join(" ", parameters)
                        + options.entrySet().stream()
                                .map(SdmxCommand::toOptionText)
                                .collect(joining(" ", " ", "")))
                .trim();
    }

    public static Builder builderOf(DatabaseRef ref) {
        return builder().option("d", !ref.equals(DatabaseRef.NO_DATABASE) ? ref.toString() : null);
    }

    private static String toOptionText(Map.Entry<String, String> e) {
        switch (e.getKey().length()) {
            case 0:
                return "";
            case 1:
                return e.getValue() != null ? ("-" + e.getKey() + " " + e.getValue()) : "";
            default:
                return e.getValue() != null ? ("--" + e.getKey() + " " + e.getValue()) : "";
        }
    }

    public static String of(DatabaseRef database, String... parameters) {
        return builderOf(database).parameters(asList(parameters)).build().toText();
    }

    public static String fetchData(WebKeyRequest request) {
        return of(
                request.getRequest().getDatabase(),
                "fetch",
                "data",
                request.getSource(),
                request.getRequest().getFlow().toString(),
                toCommandParameter(request.getRequest().getKey()));
    }

    public static String fetchMeta(WebKeyRequest request) {
        return of(
                request.getRequest().getDatabase(),
                "fetch",
                "meta",
                request.getSource(),
                request.getRequest().getFlow().toString(),
                toCommandParameter(request.getRequest().getKey()));
    }

    public static String fetchKeys(WebKeyRequest request) {
        return of(
                request.getRequest().getDatabase(),
                "fetch",
                "keys",
                request.getSource(),
                request.getRequest().getFlow().toString(),
                toCommandParameter(request.getRequest().getKey()));
    }

    public static String listDimensions(WebFlowRequest request) {
        return of(
                request.getRequest().getDatabase(),
                "list",
                "dimensions",
                request.getSource(),
                request.getRequest().getFlow().toString());
    }

    public static String listAttributes(WebFlowRequest request) {
        return of(
                request.getRequest().getDatabase(),
                "list",
                "attributes",
                request.getSource(),
                request.getRequest().getFlow().toString());
    }

    private static String toCommandParameter(Key key) {
        return Key.ALL.equals(key) ? "all" : key.toString();
    }
}
