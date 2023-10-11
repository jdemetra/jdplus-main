package jdplus.toolkit.desktop.plugin;

import nbbrd.design.swing.OnEDT;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.EventListener;

/**
 *
 */
public interface TsListener extends EventListener {

    @OnEDT
    void tsUpdated(@NonNull TsEvent event);
}
