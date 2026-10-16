package jdplus.sdmx.desktop.plugin.web.actions;

import static jdplus.toolkit.desktop.plugin.tsproviders.TsProviderNodes.PROVIDER_ACTION_PATH;

import internal.sdmx.desktop.plugin.SdmxIcons;
import java.awt.*;
import java.util.stream.Stream;
import javax.swing.*;
import jdplus.sdmx.base.api.web.SdmxWebBean;
import jdplus.sdmx.base.api.web.SdmxWebProvider;
import jdplus.toolkit.base.tsp.DataSourceProvider;
import jdplus.toolkit.desktop.plugin.actions.AbilityNodeAction;
import jdplus.toolkit.desktop.plugin.actions.Actions;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;
import sdmxdl.swing.FlowBrowserPanel;
import sdmxdl.swing.SdmxLogo;

@ActionID(category = "Edit", id = BrowseFlowAction.ID)
@ActionRegistration(displayName = "#CTL_BrowseFlowAction", lazy = false)
@Messages("CTL_BrowseFlowAction=Browse flow")
@ActionReference(
        path = PROVIDER_ACTION_PATH,
        position = 540,
        separatorBefore = 500,
        id = @ActionID(category = "Edit", id = BrowseFlowAction.ID))
public final class BrowseFlowAction extends AbilityNodeAction<DataSourceProvider> implements Presenter.Popup {

    static final String ID = "jdplus.sdmx.desktop.plugin.web.actions.BrowseFlowAction";

    public BrowseFlowAction() {
        super(DataSourceProvider.class, true);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return Actions.hideWhenDisabled(new JMenuItem(this));
    }

    @Override
    protected void performAction(Stream<DataSourceProvider> items) {
        items.map(SdmxWebProvider.class::cast).forEach(BrowseFlowAction::browseFlow);
    }

    private static void browseFlow(SdmxWebProvider provider) {
        FlowBrowserPanel panel = new FlowBrowserPanel();
        panel.setManager(provider.getSdmxManager());
        panel.setLanguages(provider.getLanguages());
        panel.setSourceIconProvider((source, callback) ->
                SdmxIcons.getFavicon(provider.getSdmxManager().getNetworking(), source, callback, 32));
        panel.setPreferredSize(new Dimension(600, 450));

        DialogDescriptor d = new DialogDescriptor(panel, "Browse Flow");
        Dialog dialog = DialogDisplayer.getDefault().createDialog(d);
        dialog.setIconImage(ImageUtilities.icon2Image(new SdmxLogo(16)));
        dialog.setVisible(true);

        if (d.getValue() == DialogDescriptor.OK_OPTION) {
            SdmxWebBean bean = provider.newBean();
            bean.setSource(panel.getSelection().getSource());
            bean.setDatabase(panel.getSelection().getRequest().getDatabase().toString());
            bean.setFlow(panel.getSelection().getRequest().getFlow().toString());
            provider.open(provider.encodeBean(bean));
        }
    }

    @Override
    protected boolean enable(Stream<DataSourceProvider> items) {
        return items.anyMatch(SdmxWebProvider.class::isInstance);
    }

    @Override
    public String getName() {
        return Bundle.CTL_BrowseFlowAction();
    }
}
