package jdplus.toolkit.desktop.plugin.core.components;

import jdplus.toolkit.desktop.plugin.components.ComponentBackendSpi;
import jdplus.toolkit.desktop.plugin.components.JHtmlView;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import lombok.NonNull;

import javax.swing.*;

@DirectImpl
@ServiceProvider
public final class HtmlViewBackend implements ComponentBackendSpi {

    @Override
    public boolean handles(@NonNull Class<? extends JComponent> type) {
        return JHtmlView.class.equals(type);
    }

    @Override
    public void install(@NonNull JComponent component) {
        new HtmlViewUI().install((JHtmlView) component);
    }
}
