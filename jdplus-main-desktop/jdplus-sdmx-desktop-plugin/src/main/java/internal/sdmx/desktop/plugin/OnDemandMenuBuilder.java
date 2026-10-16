package internal.sdmx.desktop.plugin;

import static j2html.TagCreator.*;

import j2html.tags.DomContent;
import j2html.tags.UnescapedText;
import j2html.tags.specialized.SpanTag;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.swing.*;

public final class OnDemandMenuBuilder {

    private final JMenu result = new JMenu();

    public OnDemandMenuBuilder copyToClipboard(String textMenu, String clipboardContent) {
        addCopyToClipboard(result, textMenu, clipboardContent);
        return this;
    }

    public static void addCopyToClipboard(JMenu menu, String textMenu, String clipboardContent) {
        JMenuItem item = menu.add(copyToClipboard(clipboardContent));
        item.setText(html(text(textMenu), SEPARATOR, preview(clipboardContent)).render());
        item.setToolTipText(clipboardContent);
    }

    public OnDemandMenuBuilder openFolder(String textMenu, File folder) {
        addOpenFolder(result, textMenu, folder);
        return this;
    }

    public static void addOpenFolder(JMenu menu, String textMenu, File folder) {
        JMenuItem item = menu.add(openFolder(folder));
        item.setText(html(text(textMenu), SEPARATOR, preview(folder.getPath())).render());
        item.setToolTipText(folder.getPath());
    }

    public OnDemandMenuBuilder addSeparator() {
        result.addSeparator();
        return this;
    }

    public JMenu build() {
        return result;
    }

    public void showMenuAsPopup(Component invoker) {
        showMenuAsPopup(invoker, result);
    }

    public static void showMenuAsPopup(Component invoker, JMenu menu) {
        if (invoker == null)
            invoker = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        Point mousePosition = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(mousePosition, invoker);
        menu.getPopupMenu().show(invoker, mousePosition.x, mousePosition.y);
    }

    private static DomContent preview(String text) {
        SpanTag result = span(text.length() > CHAR_LIMIT ? text.substring(0, CHAR_LIMIT - 1) + "…" : text);
        return lookupColor("MenuItem.acceleratorForeground", "MenuItem.disabledForeground", "Label.disabledForeground")
                .map(color -> result.withStyle("color:" + getHexString(color)))
                .orElse(result);
    }

    private static Optional<Color> lookupColor(String... keys) {
        return Stream.of(keys).map(UIManager::getColor).filter(Objects::nonNull).findFirst();
    }

    private static String getHexString(Color color) {
        return Integer.toHexString((color.getRGB() & 0xffffff) | 0x1000000).substring(1);
    }

    private static final int CHAR_LIMIT = 30;
    private static final UnescapedText SEPARATOR = rawHtml("&emsp;⇒&emsp;");

    private static AbstractAction copyToClipboard(String text) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
            }
        };
    }

    private static AbstractAction openFolder(File folder) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ec.util.desktop.DesktopManager.get().showInFolder(Objects.requireNonNull(folder));
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        };
    }
}
