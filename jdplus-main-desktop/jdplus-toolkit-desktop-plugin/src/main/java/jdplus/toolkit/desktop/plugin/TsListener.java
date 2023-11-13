package jdplus.toolkit.desktop.plugin;

import nbbrd.design.swing.OnEDT;
import lombok.NonNull;

import java.util.EventListener;

/**
 *
 */
public interface TsListener extends EventListener {

    @OnEDT
    void tsUpdated(@NonNull TsEvent event);
}
