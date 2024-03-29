/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jdplus.sa.desktop.plugin.ui;

import jdplus.toolkit.desktop.plugin.ui.processing.ItemUI;
import javax.swing.JComponent;


/**
 *
 * @author Jean Palate
 */
public class WkComponentsUI implements ItemUI<WkInformation>{


    @Override
    public JComponent getView(WkInformation information) {
        return new ComponentsView(information.getEstimators(), information.getDescriptors(), information.getFrequency());
    }

}
