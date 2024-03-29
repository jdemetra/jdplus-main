/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin;

import jdplus.toolkit.desktop.plugin.util.IconFactory;
import java.awt.*;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import ec.util.various.swing.FontAwesome;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Philippe Charles
 */
public enum DemetraIcons implements Icon, IconFactory {

    COLOR_SWATCH_16("color-swatch_16x16.png"),
    DOCUMENT_PRINT_16("document-print_16x16.png"),
    EDIT_CLEAR_16("edit-clear_16x16.png"),
    EDIT_COPY_16("edit-copy_16x16.png"),
    SORT_DATE_DESCENDING_16("sort-date-descending_16x16.png"),
    SORT_DATE_16("sort-date_16x16.png"),
    TABLE_RELATION_16("tables-relation_16x16.png"),
    HORIZONTAL_16("horizontal_16x16.png"),
    VERTICAL_16("vertical_16x16.png"),
    GO_DOWN_16("go-down_16x16.png"),
    GO_UP_16("go-up_16x16.png"),
    LIST_ADD_16("list-add_16x16.png"),
    LIST_REMOVE_16("list-remove_16x16.png"),
    PUZZLE_16("puzzle_16x16.png"),
    COMPILE_16("compile_16x16.png"),
    DOCUMENT_16("document_16x16.png"),
    DELETE_16("delete_16x16.png"),
    LOCALE_ALTERNATE_16("locale-alternate_16x16.png"),
    DOCUMENT_TASK_16("document-task_16x16.png"),
    CALENDAR_16("calendar_16x16.png"),
    CLIPBOARD_PASTE_DOCUMENT_TEXT_16("clipboard-paste-document-text_16x16.png"),
    MAGNIFYING_TOOL("zoom_16x16.png"),
    NB_CHECK_LAST_16("nb-check-last_16x16.png"),
    PREFERENCES("preferences-system_16x16.png"),
    EXCLAMATION_MARK_16("exclamation-red.png"),
    EXCLAMATION_MARK_SMALL_16("exclamation-small-red.png"),
    BLOG_16("blog_16x16.png"),
    WARNING("warning.png"),
    COMMENT("comment_16x16.png"),
    BROOM("broom_16x16.png"),
    BULLET_STAR("bullet_star.png");
    
    final String path;

    DemetraIcons(String path) {
        this.path = "jdplus/toolkit/desktop/plugin/icons/" + path;
    }
    
    public ImageIcon getImageIcon() {
        return ImageUtilities.loadImageIcon(path, true);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        getImageIcon().paintIcon(c, g, x, y);
    }

    @Override
    public int getIconWidth() {
        return getImageIcon().getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return getImageIcon().getIconHeight();
    }

    @Deprecated
    public static Icon getPopupMenuIcon(FontAwesome icon) {
        return DemetraUI.get().isPopupMenuIconsVisible() ? icon.getIcon(Color.BLACK, 13f) : null;
    }

    @Override
    public Image getIcon(int type, boolean opened) {
        return getImageIcon().getImage();
    }
}
