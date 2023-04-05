package jdplus.toolkit.desktop.plugin.components.parts;

import jdplus.main.desktop.design.SwingProperty;

/**
 *
 * @author Philippe Charles
 */
public interface HasZoomRatio {

    @SwingProperty
    String ZOOM_RATIO_PROPERTY = "zoomRatio";

    int getZoomRatio();

    void setZoomRatio(int zoomRatio);
}
