package demetra.desktop.components.parts;

import jdplus.main.desktop.design.SwingProperty;

/**
 *
 * @author Philippe Charles
 */
public interface HasCrosshair {

    @SwingProperty
    String CROSSHAIR_VISIBLE_PROPERTY = "crosshairVisible";

    boolean isCrosshairVisible();

    void setCrosshairVisible(boolean crosshairVisible);
}
