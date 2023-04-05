package jdplus.toolkit.desktop.plugin;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface ConfigEditor {

    @NonNull
    Config editConfig(@NonNull Config config) throws IllegalArgumentException;
}
