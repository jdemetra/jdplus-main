package jdplus.sdmx.desktop.plugin.web.actions;

import static jdplus.toolkit.desktop.plugin.tsproviders.TsProviderNodes.SOURCE_ACTION_PATH;

import internal.sdmx.base.api.SdmxBeans;
import internal.sdmx.desktop.plugin.OnDemandMenuBuilder;
import internal.sdmx.desktop.plugin.SdmxCommand;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;
import javax.swing.*;
import jdplus.sdmx.base.api.web.SdmxWebBean;
import jdplus.sdmx.base.api.web.SdmxWebProvider;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.desktop.plugin.TsManager;
import jdplus.toolkit.desktop.plugin.actions.AbilityNodeAction;
import jdplus.toolkit.desktop.plugin.actions.Actions;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;
import sdmxdl.FlowRequest;
import sdmxdl.web.WebFlowRequest;
import sdmxdl.web.WebKeyRequest;

@ActionID(category = "Edit", id = CopyPathSourceAction.ID)
@ActionRegistration(displayName = "#CTL_CopyPathSourceAction", lazy = false)
@Messages("CTL_CopyPathSourceAction=Copy Path/Reference...")
@ActionReference(
        path = SOURCE_ACTION_PATH,
        position = 412,
        id = @ActionID(category = "Edit", id = CopyPathSourceAction.ID))
public final class CopyPathSourceAction extends AbilityNodeAction<DataSource> implements Presenter.Popup {

    static final String ID = "jdplus.sdmx.desktop.plugin.web.actions.CopyPathSourceAction";

    public CopyPathSourceAction() {
        super(DataSource.class, true);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return Actions.hideWhenDisabled(new JMenuItem(this));
    }

    @SuppressWarnings("resource")
    @Override
    protected void performAction(Stream<DataSource> items) {
        DataSource item = single(items).orElseThrow(NoSuchElementException::new);
        SdmxWebProvider provider = providerOf(item).orElseThrow(NoSuchElementException::new);
        SdmxWebBean bean = provider.decodeBean(item);
        WebFlowRequest flowRequest = WebFlowRequest.builder()
                .source(bean.getSource())
                .request(FlowRequest.builder()
                        .languages(provider.getLanguages())
                        .database(SdmxBeans.getDatabase(bean))
                        .flowOf(bean.getFlow())
                        .build())
                .build();
        new OnDemandMenuBuilder()
                .copyToClipboard("SDMX-DL URI", flowRequest.toString())
                .copyToClipboard("Source", bean.getSource())
                .copyToClipboard("Flow", flowRequest.getRequest().getFlow().toShortString())
                .addSeparator()
                .copyToClipboard("List dimensions command", SdmxCommand.listDimensions(flowRequest))
                .copyToClipboard("List attributes command", SdmxCommand.listAttributes(flowRequest))
                .copyToClipboard(
                        "Fetch all keys command",
                        SdmxCommand.fetchKeys(
                                WebKeyRequest.builderOf(flowRequest).build()))
                .showMenuAsPopup(null);
    }

    @Override
    protected boolean enable(Stream<DataSource> items) {
        Optional<DataSource> item = single(items);
        return item.isPresent() && providerOf(item.orElseThrow()).isPresent();
    }

    @Override
    public String getName() {
        return Bundle.CTL_CopyPathSourceAction();
    }

    private static Optional<SdmxWebProvider> providerOf(DataSource dataSource) {
        return TsManager.get().getProvider(SdmxWebProvider.class, dataSource);
    }

    private static <T> Optional<T> single(Stream<T> items) {
        List<T> list = items.toList();
        return list.size() == 1 ? Optional.of(list.get(0)) : Optional.empty();
    }
}
