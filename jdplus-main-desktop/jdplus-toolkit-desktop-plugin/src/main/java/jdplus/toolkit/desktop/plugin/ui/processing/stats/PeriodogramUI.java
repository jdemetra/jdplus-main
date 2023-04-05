/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jdplus.toolkit.desktop.plugin.ui.processing.stats;


import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.desktop.plugin.components.tools.PeriodogramView;
import jdplus.toolkit.desktop.plugin.ui.processing.ItemUI;
import jdplus.toolkit.desktop.plugin.ui.processing.TsViewToolkit;

import javax.swing.*;
import jdplus.toolkit.base.core.stats.DescriptiveStatistics;

/**
 * @author Jean Palate
 */
public class PeriodogramUI implements ItemUI<DoubleSeq> {

 
    public PeriodogramUI() {
    }

    @Override
    public JComponent getView(DoubleSeq information) {
        DescriptiveStatistics stats = DescriptiveStatistics.of(information);
        if (stats.isConstant())
            return TsViewToolkit.getMessageViewer("Constant series. No spectral analysis");
        return getPeriodogramView(information);
    }

    public static JComponent getPeriodogramView(DoubleSeq s) {
        if (s == null) {
            return null;
        }
        PeriodogramView periodogram = new PeriodogramView();
        periodogram.setDb(true);
        periodogram.setData("periodogram", 0, s);
        return periodogram;
    }
}
