/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jdplus.toolkit.desktop.plugin.ui.processing.stats;

import jdplus.toolkit.desktop.plugin.components.JResidualsView;
import jdplus.toolkit.desktop.plugin.ui.processing.ItemUI;
import jdplus.toolkit.base.api.timeseries.TsData;

import javax.swing.JComponent;

/**
 *
 * @author Jean Palate
 */
public class ResidualsUI implements ItemUI<TsData> {

    @Override
    public JComponent getView(TsData information) {
        JResidualsView resView = new JResidualsView();
        resView.setTsData(information);
        return resView;
    }

}
