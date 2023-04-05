/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package internal.sql.desktop.plugin;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Philippe Charles
 */
public enum DbIcon implements Icon {

    DATABASE("database.png"),
    DATA_TYPE_BINARY("data_type_binary.gif"),
    DATA_TYPE_BOOLEAN("data_type_boolean.png"),
    DATA_TYPE_DATETIME("data_type_datetime.gif"),
    DATA_TYPE_DOUBLE("data_type_double.gif"),
    DATA_TYPE_INTEGER("data_type_integer.gif"),
    DATA_TYPE_NULL("data_type_null.png"),
    DATA_TYPE_STRING("data_type_string.gif");
    final String path;

    DbIcon(String path) {
        this.path = "jdplus/sql/desktop/plugin/" + path;
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
}
