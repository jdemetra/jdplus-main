/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui;

import jdplus.toolkit.desktop.plugin.components.tools.AutoRegressiveSpectrumView;
import jdplus.toolkit.desktop.plugin.components.tools.PeriodogramView;
import jdplus.main.desktop.design.SwingComponent;
import jdplus.toolkit.desktop.plugin.util.NbComponents;
import jdplus.toolkit.base.api.timeseries.TsData;
import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JSplitPane;

/**
 *
 * @author Jean Palate
 */
@SwingComponent
public final class JSpectralView extends JComponent {

    private final JSplitPane m_splitter;
    AutoRegressiveSpectrumView m_arView;
    PeriodogramView m_pView;

    public JSpectralView() {
        m_splitter = NbComponents.newJSplitPane(JSplitPane.VERTICAL_SPLIT);
        m_arView = new AutoRegressiveSpectrumView();
        m_pView = new PeriodogramView();
        build();
    }

    public void set(TsData series, boolean wn) {
        int freq = series.getAnnualFrequency();
        m_pView.setLimitVisible(wn);
        m_pView.setData("Periodogram", freq, series.getValues());
        m_arView.setData("Auto-regressive spectrum", freq, series.getValues());
    }

    public void setDifferencingOrder(int order) {
        m_pView.setDifferencingOrder(order);
        m_arView.setDifferencingOrder(order);
    }

    public void setDifferencingLag(int lag) {
        m_pView.setDifferencingLag(lag);
        m_arView.setDifferencingLag(lag);
    }

    public void setLogTransformation(boolean log) {
        m_pView.setLogTransformation(log);
        m_arView.setLogTransformation(log);
    }

    public void setMeanCorrection(boolean mean) {
        m_pView.setMeanCorrection(mean);
        m_arView.setMeanCorrection(mean);
    }

    private void build() {
        setLayout(new BorderLayout());
        add(m_splitter, BorderLayout.CENTER);

        m_splitter.setTopComponent(m_pView);
        m_splitter.setBottomComponent(m_arView);
        m_splitter.setDividerLocation(.5);
        m_splitter.setResizeWeight(.5);

        m_pView.setDifferencingOrder(0);
        m_arView.setDifferencingOrder(0);
    }
}
