package jdplus.sdmx.desktop.plugin.web.actions;

import static jdplus.toolkit.desktop.plugin.tsproviders.TsProviderNodes.PROVIDER_ACTION_PATH;

import java.awt.*;
import java.util.stream.Stream;
import javax.swing.*;
import jdplus.sdmx.base.api.web.SdmxWebProvider;
import jdplus.toolkit.base.tsp.DataSourceProvider;
import jdplus.toolkit.desktop.plugin.actions.AbilityNodeAction;
import jdplus.toolkit.desktop.plugin.actions.Actions;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;
import org.openide.windows.TopComponent;
import sdmxdl.web.SdmxWebManager;

@ActionID(category = "Edit", id = ListSourcesAction.ID)
@ActionRegistration(displayName = "#CTL_ListSourcesAction", lazy = false)
@Messages("CTL_ListSourcesAction=List sources")
@ActionReference(
        path = PROVIDER_ACTION_PATH,
        position = 530,
        separatorBefore = 500,
        id = @ActionID(category = "Edit", id = ListSourcesAction.ID))
public final class ListSourcesAction extends AbilityNodeAction<DataSourceProvider> implements Presenter.Popup {

    static final String ID = "jdplus.sdmx.desktop.plugin.web.actions.ListSourcesAction";

    public ListSourcesAction() {
        super(DataSourceProvider.class, true);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return Actions.hideWhenDisabled(new JMenuItem(this));
    }

    @Override
    protected void performAction(Stream<DataSourceProvider> items) {
        items.map(SdmxWebProvider.class::cast).forEach(item -> {
            createComponent("WebSource", item.getSdmxManager());
        });
    }

    private static TopComponent createComponent(String name, SdmxWebManager sdmxManager) {
        JSdmxWebSourcePanel main = new JSdmxWebSourcePanel();
        main.setSdmxManager(sdmxManager);

        TopComponent c = new TopComponent() {
            @Override
            public int getPersistenceType() {
                return TopComponent.PERSISTENCE_NEVER;
            }
        };
        c.setName(name);
        c.setLayout(new BorderLayout());
        c.add(main, BorderLayout.CENTER);
        c.open();
        return c;
    }

    @Override
    protected boolean enable(Stream<DataSourceProvider> items) {
        return items.anyMatch(SdmxWebProvider.class::isInstance);
    }

    @Override
    public String getName() {
        return Bundle.CTL_ListSourcesAction();
    }
}
