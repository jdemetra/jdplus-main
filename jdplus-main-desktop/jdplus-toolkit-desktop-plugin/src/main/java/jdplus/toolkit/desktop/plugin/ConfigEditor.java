package jdplus.toolkit.desktop.plugin;

import lombok.NonNull;

public interface ConfigEditor {

    @NonNull
    Config editConfig(@NonNull Config config) throws IllegalArgumentException;
}
