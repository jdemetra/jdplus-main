/*
 * Copyright 2026 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the Licence;
 * you may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package internal.toolkit.desktop.plugin.search;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import jdplus.toolkit.desktop.plugin.search.DesktopSearchResult;
import org.jspecify.annotations.Nullable;

/** Renders category headers and search hits (icon, highlighted name and right-aligned detail). */
final class SearchEverywhereRenderer implements ListCellRenderer<SearchEverywhereRenderer.Item> {

    sealed interface Item {}

    record Header(String categoryName, @Nullable String actionText) implements Item {

        Header(String categoryName) {
            this(categoryName, null);
        }
    }

    record Hit(DesktopSearchResult result, String query) implements Item {}

    private final JPanel header = new JPanel(new BorderLayout(12, 0));
    private final JLabel headerName = new JLabel();
    private final JLabel headerAction = new JLabel();
    private final JPanel hit = new JPanel(new BorderLayout(12, 0));
    private final JLabel name = new JLabel();
    private final JLabel detail = new JLabel();

    SearchEverywhereRenderer() {
        header.setOpaque(false);
        header.add(headerName, BorderLayout.CENTER);
        header.add(headerAction, BorderLayout.EAST);
        name.setIconTextGap(6);
        hit.setBorder(BorderFactory.createEmptyBorder(2, 18, 2, 8));
        hit.add(name, BorderLayout.CENTER);
        hit.add(detail, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends Item> list, Item value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof Hit(DesktopSearchResult result, String query)) {
            hit.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            name.setFont(list.getFont());
            name.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            name.setIcon(result.icon());
            name.setText(highlight(result.displayName(), query));
            detail.setFont(list.getFont());
            detail.setForeground(isSelected ? list.getSelectionForeground() : getDisabledForeground());
            detail.setText(result.detail());
            return hit;
        }
        Header headerValue = (Header) value;
        header.setBorder(BorderFactory.createEmptyBorder(index == 0 ? 2 : 8, 6, 2, 8));
        headerName.setFont(list.getFont().deriveFont(Font.BOLD));
        headerName.setForeground(getDisabledForeground());
        headerName.setText(headerValue.categoryName());
        headerAction.setFont(list.getFont());
        headerAction.setForeground(getLinkForeground());
        headerAction.setText(headerValue.actionText() != null ? "<html><u>" + escape(headerValue.actionText()) : null);
        return header;
    }

    /** Checks if the point (in list coordinates) is located on the action link of the header at the given index. */
    boolean isOnHeaderAction(JList<Item> list, int index, Point point) {
        Rectangle bounds = list.getCellBounds(index, index);
        if (bounds == null
                || !bounds.contains(point)
                || !(list.getModel().getElementAt(index) instanceof Header value)
                || value.actionText() == null) {
            return false;
        }
        getListCellRendererComponent(list, value, index, false, false);
        header.setBounds(bounds);
        header.doLayout();
        return headerAction.getBounds().contains(point.x - bounds.x, point.y - bounds.y);
    }

    private static Color getLinkForeground() {
        Color result = UIManager.getColor("nb.html.link.foreground");
        return result != null ? result : Color.BLUE.darker();
    }

    private static Color getDisabledForeground() {
        return UIManager.getColor("Label.disabledForeground");
    }

    /** Returns an HTML representation of the text with the first case-insensitive occurrence of the query in bold. */
    static String highlight(String text, String query) {
        String term = query.strip();
        int index = term.isEmpty() ? -1 : indexOfIgnoreCase(text, term);
        if (index < 0) {
            return "<html>" + escape(text);
        }
        return "<html>" + escape(text.substring(0, index))
                + "<b>" + escape(text.substring(index, index + term.length())) + "</b>"
                + escape(text.substring(index + term.length()));
    }

    private static int indexOfIgnoreCase(String text, String term) {
        for (int i = 0; i + term.length() <= text.length(); i++) {
            if (text.regionMatches(true, i, term, 0, term.length())) {
                return i;
            }
        }
        return -1;
    }

    private static String escape(String text) {
        StringBuilder result = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            switch (c) {
                case '&' -> result.append("&amp;");
                case '<' -> result.append("&lt;");
                case '>' -> result.append("&gt;");
                case '"' -> result.append("&quot;");
                default -> result.append(c);
            }
        }
        return result.toString();
    }
}
