package jdplus.toolkit.desktop.plugin.actions;

import jdplus.main.desktop.design.SwingAction;
import jdplus.toolkit.desktop.plugin.ConfigEditor;
import jdplus.toolkit.desktop.plugin.Persistable;

public interface Configurable {

    @SwingAction
    String CONFIGURE_ACTION = "configure";

    void configure();

    static void configure(Persistable persistable, ConfigEditor editor) {
        persistable.setConfig(editor.editConfig(persistable.getConfig()));
    }
}
