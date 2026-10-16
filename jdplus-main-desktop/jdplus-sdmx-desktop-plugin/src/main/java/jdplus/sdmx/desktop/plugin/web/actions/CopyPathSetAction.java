package jdplus.sdmx.desktop.plugin.web.actions;

import static jdplus.toolkit.desktop.plugin.tsproviders.TsProviderNodes.COLLECTION_ACTION_PATH;
import static jdplus.toolkit.desktop.plugin.tsproviders.TsProviderNodes.SERIES_ACTION_PATH;

import internal.sdmx.base.api.SdmxBeans;
import internal.sdmx.desktop.plugin.OnDemandMenuBuilder;
import internal.sdmx.desktop.plugin.SdmxCommand;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;
import javax.swing.*;
import jdplus.sdmx.base.api.web.SdmxWebBean;
import jdplus.sdmx.base.api.web.SdmxWebProvider;
import jdplus.toolkit.base.tsp.DataSet;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.desktop.plugin.TsManager;
import jdplus.toolkit.desktop.plugin.actions.AbilityNodeAction;
import jdplus.toolkit.desktop.plugin.actions.Actions;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;
import sdmxdl.*;
import sdmxdl.web.WebKeyRequest;

@ActionID(category = "Edit", id = CopyPathSetAction.ID)
@ActionRegistration(displayName = "#CTL_CopyPathSetAction", lazy = false)
@Messages("CTL_CopyPathSetAction=Copy Path/Reference...")
@ActionReferences({
    @ActionReference(
            path = COLLECTION_ACTION_PATH,
            position = 422,
            id = @ActionID(category = "Edit", id = CopyPathSetAction.ID)),
    @ActionReference(
            path = SERIES_ACTION_PATH,
            position = 422,
            id = @ActionID(category = "Edit", id = CopyPathSetAction.ID))
})
public final class CopyPathSetAction extends AbilityNodeAction<DataSet> implements Presenter.Popup {

    static final String ID = "jdplus.sdmx.desktop.plugin.web.actions.CopyPathSetAction";

    public CopyPathSetAction() {
        super(DataSet.class, true);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return Actions.hideWhenDisabled(new JMenuItem(this));
    }

    @Override
    protected void performAction(Stream<DataSet> items) {
        DataSet item = single(items).orElseThrow(NoSuchElementException::new);
        SdmxWebProvider provider = providerOf(item.getDataSource()).orElseThrow(NoSuchElementException::new);
        SdmxWebBean bean = provider.decodeBean(item.getDataSource());
        DatabaseRef databaseRef = SdmxBeans.getDatabase(bean);
        FlowRef flowRef = FlowRef.parse(bean.getFlow());
        Key key = getKey(provider, bean.getSource(), databaseRef, flowRef, item);
        WebKeyRequest keyRequest = WebKeyRequest.builder()
                .source(bean.getSource())
                .request(KeyRequest.builder()
                        .languages(provider.getLanguages())
                        .database(databaseRef)
                        .flow(flowRef)
                        .key(key)
                        .build())
                .build();
        new OnDemandMenuBuilder()
                .copyToClipboard("SDMX-DL URI", keyRequest.toString())
                .copyToClipboard("Source", bean.getSource())
                .copyToClipboard("Flow", flowRef.toShortString())
                .copyToClipboard("Key", key.toString())
                .addSeparator()
                .copyToClipboard("Fetch data command", SdmxCommand.fetchData(keyRequest))
                .copyToClipboard("Fetch meta command", SdmxCommand.fetchMeta(keyRequest))
                .copyToClipboard("Fetch keys command", SdmxCommand.fetchKeys(keyRequest))
                .showMenuAsPopup(null);
    }

    @Override
    protected boolean enable(Stream<DataSet> items) {
        Optional<DataSet> item = single(items);
        return item.isPresent()
                && providerOf(item.orElseThrow().getDataSource()).isPresent();
    }

    @Override
    public String getName() {
        return Bundle.CTL_CopyPathSetAction();
    }

    private static Optional<SdmxWebProvider> providerOf(DataSource dataSource) {
        return TsManager.get().getProvider(SdmxWebProvider.class, dataSource);
    }

    private static <T> Optional<T> single(Stream<T> items) {
        List<T> list = items.toList();
        return list.size() == 1 ? Optional.of(list.get(0)) : Optional.empty();
    }

    private static Key getKey(
            SdmxWebProvider provider, String source, DatabaseRef databaseRef, FlowRef flowRef, DataSet dataSet) {
        try {
            Structure structure = provider.getSdmxManager()
                    .usingName(source)
                    .getMeta(FlowRequest.builder()
                            .languages(provider.getLanguages())
                            .database(databaseRef)
                            .flow(flowRef)
                            .build())
                    .getStructure();
            Key.Builder result = Key.builder(structure);
            structure
                    .getDimensions()
                    .forEach(dimension -> result.put(dimension.getId(), dataSet.getParameter(dimension.getId())));
            return result.build();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
