/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.desktop.plugin.ui;

import jdplus.toolkit.desktop.plugin.ui.processing.ItemUI;
import jdplus.toolkit.desktop.plugin.ui.processing.TsViewToolkit;
import jdplus.toolkit.desktop.plugin.html.HtmlFragment;
import javax.swing.JComponent;

/**
 *
 * @author Jean Palate
 */
public class WkFinalEstimatorsUI implements ItemUI<WkInformation> {

    @Override
    public JComponent getView(WkInformation information) {
        try {
            return new FinalEstimatorsView(information.getEstimators(), information.getDescriptors(), information.getFrequency());
        } catch (Exception err) {
            return TsViewToolkit.getHtmlViewer(new HtmlFragment("Unable to compute the final estimators"));
        }
    }

}
