/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.processing;

import jdplus.toolkit.base.api.timeseries.TsCollection;
import jdplus.toolkit.desktop.plugin.components.JHtmlView;
import jdplus.toolkit.desktop.plugin.components.JTsChart;
import jdplus.toolkit.desktop.plugin.components.JTsGrid;
import jdplus.toolkit.desktop.plugin.components.JTsGrowthChart;
import jdplus.toolkit.desktop.plugin.components.parts.HasTsCollection;
import jdplus.toolkit.desktop.plugin.html.HtmlElement;
import jdplus.toolkit.desktop.plugin.html.HtmlUtil;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsInformationType;
import jdplus.toolkit.base.api.util.Collections2;

import javax.swing.*;

/**
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class TsViewToolkit {

    public static JTsGrid getGrid(Iterable<Ts> series) {
        TsCollection all = Collections2.streamOf(series).collect(TsCollection.toTsCollection());
        JTsGrid result = new JTsGrid();
        result.setTsUpdateMode(HasTsCollection.TsUpdateMode.None);
        result.setTsCollection(all);
        boolean single = all.size() == 1 && all.get(0).getData().getAnnualFrequency()>0;
        result.setMode(single ? JTsGrid.Mode.SINGLETS : JTsGrid.Mode.MULTIPLETS);
        return result;
    }

    public static JTsChart getChart(Iterable<Ts> series) {
        JTsChart result = new JTsChart(TsInformationType.Data);
        result.setTsUpdateMode(HasTsCollection.TsUpdateMode.None);
        result.setTsCollection(Collections2.streamOf(series).collect(TsCollection.toTsCollection()));
        return result;
    }

    public static JTsGrowthChart getGrowthChart(Iterable<Ts> series) {
        JTsGrowthChart result = new JTsGrowthChart(TsInformationType.Data);
        result.setTsUpdateMode(HasTsCollection.TsUpdateMode.None);
        result.setTsCollection(Collections2.streamOf(series).collect(TsCollection.toTsCollection()));
        return result;
    }

    public static JHtmlView getHtmlViewer(HtmlElement html) {
        JHtmlView result = new JHtmlView();
        result.setHtml(HtmlUtil.toString(html));
        return result;
    }

    public static JLabel getMessageViewer(String msg) {
        JLabel result = new JLabel();
        result.setHorizontalAlignment(SwingConstants.CENTER);
        result.setFont(result.getFont().deriveFont(result.getFont().getSize2D() * 3 / 2));
        result.setText("<html><center>" + msg);
        return result;
    }
}
