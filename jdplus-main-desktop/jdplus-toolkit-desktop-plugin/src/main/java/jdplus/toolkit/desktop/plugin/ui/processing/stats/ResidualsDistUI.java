/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.processing.stats;

import jdplus.toolkit.desktop.plugin.ui.JResDistributionView;
import jdplus.toolkit.desktop.plugin.ui.processing.ItemUI;
import jdplus.toolkit.base.api.timeseries.TsData;

import javax.swing.JComponent;

/**
 *
 * @author Jean Palate
 */
public class ResidualsDistUI implements ItemUI<TsData> {

    @Override
    public JComponent getView(TsData information) {
        JResDistributionView resdistView = new JResDistributionView();
        if (information != null) {
            int n = information.getAnnualFrequency();
            resdistView.setAutocorrelationsCount(Math.max(8, n * 3));
            resdistView.setData(information.getValues());
        }else
            resdistView.reset();
        return resdistView;
    }

}
