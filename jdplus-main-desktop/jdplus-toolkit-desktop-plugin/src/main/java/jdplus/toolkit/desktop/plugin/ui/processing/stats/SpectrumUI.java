/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.processing.stats;

import jdplus.toolkit.desktop.plugin.ui.JSpectralView;
import jdplus.toolkit.desktop.plugin.ui.processing.ItemUI;
import jdplus.toolkit.desktop.plugin.ui.processing.TsViewToolkit;
import jdplus.toolkit.base.api.timeseries.TsData;
import javax.swing.*;
import jdplus.toolkit.base.core.stats.DescriptiveStatistics;

/**
 * @author Jean Palate
 */
public class SpectrumUI implements ItemUI<SpectrumUI.Information> {

    @lombok.Builder
    @lombok.Value
    public static class Information {

        TsData series;
        int differencingOrder, differencingLag;
        boolean log, mean, whiteNoise;

    }

    @Override
    public JComponent getView(Information information) {
        DescriptiveStatistics stats = DescriptiveStatistics.of(information.getSeries().getValues());
        if (stats.isConstant()) {
            return TsViewToolkit.getMessageViewer("Constant series. No spectral analysis");
        }
        return getSpectralView(information);
    }

    public static JComponent getSpectralView(Information info) {
        if (info.getSeries().isEmpty()) {
            return null;
        }
        JSpectralView spectrum = new JSpectralView();
        spectrum.setDifferencingOrder(info.differencingOrder);
        if (info.differencingOrder > 0) {
            spectrum.setDifferencingLag(info.differencingLag);
        }
        spectrum.setLogTransformation(info.log);
        spectrum.setMeanCorrection(info.mean);

        spectrum.set(info.getSeries(), info.isWhiteNoise());
        return spectrum;
    }
}
