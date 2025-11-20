package internal.ui.components;

import jdplus.toolkit.desktop.plugin.components.JExceptionPanel;
import jdplus.toolkit.desktop.plugin.util.NbComponents;
import nbbrd.io.WrappedIOException;
import org.openide.DialogDisplayer;
import org.openide.windows.TopComponent;

import javax.xml.bind.JAXBException;
import java.awt.*;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionException;

@lombok.experimental.UtilityClass
public class ExceptionUtil {

    public static void showException(String id, Throwable ex) {
        if (isInModalDialog()) {
            showDialog(ex);
        } else {
            showTopComponent(id, ex);
        }
    }

    public static void showTopComponent(String topComponentName, Throwable exception) {
        NbComponents.findTopComponentByName(topComponentName)
                .orElseGet(() -> createComponent(topComponentName, exception))
                .requestActive();
    }

    private static TopComponent createComponent(String name, Throwable exception) {
        TopComponent c = new TopComponent() {
            @Override
            public int getPersistenceType() {
                return TopComponent.PERSISTENCE_NEVER;
            }
        };
        c.setName(name);
        c.setDisplayName(exception.getClass().getSimpleName());
        c.setLayout(new BorderLayout());
        c.add(newPanel(exception), BorderLayout.CENTER);
        c.open();
        return c;
    }

    public static void showDialog(Throwable exception) {
        JExceptionPanel p = newPanel(exception);
        DialogDisplayer.getDefault().notify(p.createDialogDescriptor(exception.getClass().getSimpleName()));
    }

    public static boolean isInModalDialog() {
        return KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow() instanceof Dialog;
    }

    public static Throwable unwrapException(Throwable ex, List<Class<? extends Throwable>> types) {
        return ex.getCause() != null
                && ex.getSuppressed().length == 0
                && types.stream().anyMatch(o -> o.isInstance(ex))
                ? unwrapException(ex.getCause(), types)
                : ex;
    }

    public static Throwable unwrapException(Throwable ex) {
        return unwrapException(ex, DEFAULT_UNWRAP_TYPES);
    }

    private static final List<Class<? extends Throwable>> DEFAULT_UNWRAP_TYPES = Arrays.asList(
            UncheckedIOException.class,
            WrappedIOException.class,
            CompletionException.class,
            JAXBException.class
    );

    private static JExceptionPanel newPanel(Throwable t) {
        return JExceptionPanel.create(t instanceof Exception ? (Exception) t : new RuntimeException(t));
    }
}
