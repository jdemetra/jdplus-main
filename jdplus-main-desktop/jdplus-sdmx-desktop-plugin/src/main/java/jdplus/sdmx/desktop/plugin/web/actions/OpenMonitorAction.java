package jdplus.sdmx.desktop.plugin.web.actions;

import static jdplus.toolkit.desktop.plugin.tsproviders.TsProviderNodes.SOURCE_ACTION_PATH;

import ec.util.desktop.Desktop;
import ec.util.desktop.DesktopManager;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.stream.Stream;
import javax.swing.*;
import jdplus.sdmx.base.api.web.SdmxWebProvider;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.desktop.plugin.TsManager;
import jdplus.toolkit.desktop.plugin.actions.AbilityNodeAction;
import jdplus.toolkit.desktop.plugin.actions.Actions;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;
import sdmxdl.web.WebSource;

@ActionID(category = "Edit", id = OpenMonitorAction.ID)
@ActionRegistration(displayName = "#CTL_OpenMonitorAction", lazy = false)
@Messages("CTL_OpenMonitorAction=Open monitor")
@ActionReference(
        path = SOURCE_ACTION_PATH,
        position = 730,
        separatorBefore = 700,
        id = @ActionID(category = "Edit", id = OpenMonitorAction.ID))
public final class OpenMonitorAction extends AbilityNodeAction<DataSource> implements Presenter.Popup {

    static final String ID = "jdplus.sdmx.desktop.plugin.web.actions.OpenMonitorAction";

    public OpenMonitorAction() {
        super(DataSource.class, true);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return Actions.hideWhenDisabled(new JMenuItem(this));
    }

    @Override
    protected void performAction(Stream<DataSource> items) {
        items.forEach(item -> {
            try {
                DesktopManager.get().browse(getMonitorWebsite(item).toURI());
            } catch (IOException | URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }

    @Override
    protected boolean enable(Stream<DataSource> items) {
        return DesktopManager.get().isSupported(Desktop.Action.BROWSE)
                && items.anyMatch(item -> getMonitorWebsite(item) != null);
    }

    private URL getMonitorWebsite(DataSource dataSource) {
        return TsManager.get()
                .getProvider(SdmxWebProvider.class, dataSource)
                .map(provider -> getMonitorWebsite(provider, dataSource))
                .orElse(null);
    }

    private URL getMonitorWebsite(SdmxWebProvider provider, DataSource dataSource) {
        WebSource source = provider.getSdmxManager()
                .getSources()
                .get(provider.decodeBean(dataSource).getSource());
        return source != null ? source.getMonitorWebsite() : null;
    }

    @Override
    public String getName() {
        return Bundle.CTL_OpenMonitorAction();
    }
}
