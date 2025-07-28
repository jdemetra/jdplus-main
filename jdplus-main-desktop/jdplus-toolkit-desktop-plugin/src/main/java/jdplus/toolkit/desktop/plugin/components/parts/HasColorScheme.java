package jdplus.toolkit.desktop.plugin.components.parts;

import jdplus.main.desktop.design.SwingAction;
import jdplus.main.desktop.design.SwingProperty;
import ec.util.chart.ColorScheme;
import org.jspecify.annotations.Nullable;

/**
 *
 * @author Philippe Charles
 */
public interface HasColorScheme {

    @SwingProperty
    String COLOR_SCHEME_PROPERTY = "colorScheme";

    @Nullable
    ColorScheme getColorScheme();

    void setColorScheme(@Nullable ColorScheme colorScheme);

    default boolean hasColorScheme() {
        return false;
        //return getColorScheme() != null;
    }

    @SwingAction
    String APPLY_MAIN_COLOR_SCHEME_ACTION = "applyMainColorScheme";
}
