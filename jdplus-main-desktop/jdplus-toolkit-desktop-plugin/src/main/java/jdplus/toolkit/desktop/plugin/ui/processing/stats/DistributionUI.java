/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.processing.stats;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.desktop.plugin.ui.JDistributionView;
import jdplus.toolkit.desktop.plugin.ui.processing.ItemUI;

import javax.swing.JComponent;

/**
 *
 * @author Jean Palate
 */
public class DistributionUI implements ItemUI<DoubleSeq> {

    @Override
    public JComponent getView(DoubleSeq information) {
        JDistributionView distView = new JDistributionView();
        if (information != null) {
            distView.set(information);
        }else
            distView.reset();
        return distView;
    }

}
