package jdplus.toolkit.desktop.plugin;

import lombok.NonNull;

public interface Persistable {

    @NonNull
    Config getConfig();

    void setConfig(@NonNull Config config) throws IllegalArgumentException;
}
