package jdplus.toolkit.desktop.plugin.actions;

import ec.util.various.swing.FontAwesome;
import ec.util.various.swing.JCommand;
import jdplus.toolkit.desktop.plugin.DemetraIcons;
import lombok.NonNull;

import javax.swing.*;

public final class ConfigurableSupport {

    private ConfigurableSupport() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void registerActions(@NonNull Configurable configurable, @NonNull ActionMap am) {
        am.put(Configurable.CONFIGURE_ACTION, ConfigureCommand.INSTANCE.toAction(configurable));
    }

    public static <C extends JComponent & Configurable> JMenuItem newConfigureMenu(@NonNull C component) {
        JMenuItem result = new JMenuItem(component.getActionMap().get(Configurable.CONFIGURE_ACTION));
        result.setIcon(DemetraIcons.getPopupMenuIcon(FontAwesome.FA_COGS));
        result.setText("Configure...");
        return result;
    }

    private static final class ConfigureCommand extends JCommand<Configurable> {

        private static final ConfigureCommand INSTANCE = new ConfigureCommand();

        @Override
        public void execute(Configurable c) throws Exception {
            c.configure();
        }
    }
}
