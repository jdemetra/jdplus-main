package internal.toolkit.desktop.plugin.components;

import jdplus.toolkit.desktop.plugin.components.ComponentBackendSpi;
import jdplus.toolkit.desktop.plugin.components.JTsChart;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;

import javax.swing.*;

@DirectImpl
@ServiceProvider
public final class TsChartBackend implements ComponentBackendSpi {

    @Override
    public boolean handles(Class<? extends JComponent> type) {
        return JTsChart.class.equals(type);
    }

    @Override
    public void install(JComponent component) {
        new TsChartUI().install((JTsChart) component);
    }
}
