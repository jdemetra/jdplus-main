package jdplus.sa.desktop.plugin.multiprocessing.actions;

import jdplus.sa.base.api.SaSpecification;

@lombok.Getter
@lombok.RequiredArgsConstructor
public class ImportData {

    private static final int MAX_LENGTH = 35;

    private final SaSpecification spec;
    private final String name;

    @Override
    public String toString() {
        String value = spec.longDisplay()+ ":" + name;
        if (value.length() > MAX_LENGTH) {
            return value.substring(0, MAX_LENGTH) + "...";
        }
        return value;
    }
}
