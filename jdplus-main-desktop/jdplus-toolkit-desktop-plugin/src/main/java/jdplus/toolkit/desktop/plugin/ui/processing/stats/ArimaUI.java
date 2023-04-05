/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.processing.stats;

import jdplus.toolkit.desktop.plugin.components.tools.JArimaView;
import jdplus.toolkit.desktop.plugin.ui.processing.ItemUI;
import java.util.Map;
import javax.swing.JComponent;
import jdplus.toolkit.base.core.arima.IArimaModel;

/**
 *
 * @author Jean Palate
 */
public class ArimaUI implements ItemUI<Map<String, IArimaModel>> {

    @Override
    public JComponent getView(Map<String, IArimaModel> information) {
        JArimaView arimaView = new JArimaView(information);
        return arimaView;
    }
}
