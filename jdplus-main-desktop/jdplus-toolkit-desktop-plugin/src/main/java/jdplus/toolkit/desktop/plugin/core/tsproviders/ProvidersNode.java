/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.toolkit.desktop.plugin.core.tsproviders;

import jdplus.toolkit.base.api.timeseries.TsProvider;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.base.tsp.DataSourceListener;
import jdplus.toolkit.base.tsp.DataSourceLoader;
import jdplus.toolkit.base.tsp.DataSourceProvider;
import jdplus.toolkit.desktop.plugin.Config;
import jdplus.toolkit.desktop.plugin.DemetraBehaviour;
import jdplus.toolkit.desktop.plugin.TsManager;
import jdplus.toolkit.desktop.plugin.core.actions.ConfigureNodeAction;
import jdplus.toolkit.desktop.plugin.core.interchange.ImportNodeAction;
import jdplus.toolkit.desktop.plugin.datatransfer.DataSourceTransferManager;
import jdplus.toolkit.desktop.plugin.interchange.Importable;
import jdplus.toolkit.desktop.plugin.nodes.Nodes;
import jdplus.toolkit.desktop.plugin.tsproviders.DataSourceProviderBuddyUtil;
import jdplus.toolkit.desktop.plugin.ui.mru.ProviderMruAction;
import lombok.NonNull;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

import javax.swing.*;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static jdplus.toolkit.desktop.plugin.tsproviders.TsProviderNodes.PROVIDERS_ACTION_PATH;

/**
 * A root node that represents the parent of all providers.
 *
 * @author Philippe Charles
 */
@ActionReferences({
        @ActionReference(path = PROVIDERS_ACTION_PATH, separatorBefore = 300, position = 310, id = @ActionID(category = "File", id = OpenProvidersNodeAction.ID)),
        @ActionReference(path = PROVIDERS_ACTION_PATH, separatorBefore = 300, position = 320, id = @ActionID(category = "File", id = ProviderMruAction.ID)),
        @ActionReference(path = PROVIDERS_ACTION_PATH, separatorBefore = 400, position = 410, id = @ActionID(category = "Edit", id = PasteProvidersNodeAction.ID)),
        @ActionReference(path = PROVIDERS_ACTION_PATH, separatorBefore = 400, position = 430, id = @ActionID(category = "File", id = ImportNodeAction.ID)),
        @ActionReference(path = PROVIDERS_ACTION_PATH, separatorBefore = 450, position = 460, id = @ActionID(category = "Edit", id = ShowProvidersNodeAction.ID)),
        @ActionReference(path = PROVIDERS_ACTION_PATH, separatorBefore = 500, position = 520, id = @ActionID(category = "File", id = ConfigureNodeAction.ID))
})
public final class ProvidersNode extends AbstractNode {

    public ProvidersNode() {
        this(new InstanceContent());
    }

    private ProvidersNode(InstanceContent abilities) {
        // 1. Children and lookup
        super(Children.create(new ProvidersChildFactory(), false), new AbstractLookup(abilities));
        // 2. Abilities
        {
//        abilities.add(DemetraUI.getInstance());// IConfigurable
            abilities.add(new ImportableDataSource());
        }
        // 3. Name and display name
    }

    @Override
    public Action[] getActions(boolean context) {
        return Nodes.actionsForPath(PROVIDERS_ACTION_PATH);
    }

    @Override
    public PasteType getDropType(Transferable t, int action, int index) {
        if (DataSourceTransferManager.get().canHandle(t)) {
            return new PasteTypeImpl(t);
        }
        return null;
    }

    private static final class ProvidersChildFactory extends ChildFactory.Detachable<Object> implements LookupListener, PropertyChangeListener, DataSourceListener {

        // FIXME: use TsManager instead of lookup
        private final Lookup.Result<DataSourceProvider> lookupResult;

        public ProvidersChildFactory() {
            this.lookupResult = Lookup.getDefault().lookupResult(DataSourceProvider.class);
        }

        @Override
        protected void addNotify() {
            lookupResult.addLookupListener(this);
            DemetraBehaviour.get().addPropertyChangeListener(this);
            providerStream().forEach(o -> o.addDataSourceListener(this));
        }

        @Override
        protected void removeNotify() {
            providerStream().forEach(o -> o.removeDataSourceListener(this));
            DemetraBehaviour.get().removePropertyChangeListener(this);
            lookupResult.removeLookupListener(this);
        }

        @Override
        protected boolean createKeys(List<Object> list) {
            list.addAll(getKeys());
            return true;
        }

        @Override
        protected Node createNodeForKey(Object key) {
            return DemetraBehaviour.get().isShowTsProviderNodes()
                    ? new ProviderNode((DataSourceProvider) key)
                    : new DataSourceNode((DataSource) key);
        }

        private List<?> getKeys() {
            return DemetraBehaviour.get().isShowTsProviderNodes()
                    ? providerStream().sorted(ON_CLASS_SIMPLENAME).collect(Collectors.toList())
                    : providerStream().flatMap(o -> o.getDataSources().stream()).sorted(ON_TO_STRING).collect(Collectors.toList());
        }

        private Stream<? extends DataSourceProvider> providerStream() {
            return TsManager.get().getProviders()
                    .filter(DataSourceProvider.class::isInstance)
                    .map(DataSourceProvider.class::cast)
                    .filter(DemetraBehaviour.get().isShowUnavailableTsProviders() ? (o -> true) : TsProvider::isAvailable);
        }

        //<editor-fold defaultstate="collapsed" desc="LookupListener">
        @Override
        public void resultChanged(LookupEvent ev) {
            refresh(true);
            providerStream().forEach(o -> o.addDataSourceListener(this));
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="PropertyChangeListener">
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            switch (evt.getPropertyName()) {
                case DemetraBehaviour.SHOW_UNAVAILABLE_TS_PROVIDERS_PROPERTY:
                case DemetraBehaviour.SHOW_TS_PROVIDER_NODES_PROPERTY:
                    refresh(true);
                    break;

            }
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="DataSourceListener">
        @Override
        public void opened(@NonNull DataSource dataSource) {
            if (!DemetraBehaviour.get().isShowTsProviderNodes()) {
                refresh(true);
            }
        }

        @Override
        public void closed(@NonNull DataSource dataSource) {
            if (!DemetraBehaviour.get().isShowTsProviderNodes()) {
                refresh(true);
            }
        }

        @Override
        public void changed(@NonNull DataSource dataSource) {
            if (!DemetraBehaviour.get().isShowTsProviderNodes()) {
                refresh(true);
            }
        }

        @Override
        public void allClosed(@NonNull String providerName) {
            if (!DemetraBehaviour.get().isShowTsProviderNodes()) {
                refresh(true);
            }
        }
        //</editor-fold>
    }

    private final class ImportableDataSource implements Importable {

        @Override
        public String getDomain() {
            return DataSourceProviderBuddyUtil.getDataSourceDomain();
        }

        @Override
        public void importConfig(Config config) throws IllegalArgumentException {
            DataSource dataSource = DataSourceProviderBuddyUtil.getDataSource(config);
            Optional<DataSourceLoader> loader = TsManager.get().getProvider(DataSourceLoader.class, dataSource);
            if (loader.isPresent()) {
                loader.orElseThrow().open(dataSource);
                ProvidersNode.findNode(dataSource, ProvidersNode.this)
                        .ifPresent(value -> value.setDisplayName(config.getName()));
            }
        }
    }

    private static final class PasteTypeImpl extends PasteType {

        private final Transferable t;

        public PasteTypeImpl(Transferable t) {
            this.t = t;
        }

        @Override
        public Transferable paste() throws IOException {
            DataSourceTransferManager.get().getDataSource(t).ifPresent(source -> TsManager.get().open(source));
            return null;
        }
    }

    private static final Comparator<DataSourceProvider> ON_CLASS_SIMPLENAME = Comparator.comparing(o -> o.getClass().getSimpleName());

    private static final Comparator<DataSource> ON_TO_STRING = Comparator.comparing(Object::toString);

    public static Optional<Node> findNode(DataSource dataSource, Node node) {
        if (node instanceof ProvidersNode) {
            return find(dataSource, (ProvidersNode) node);
        }
        if (node instanceof ProviderNode) {
            return find(dataSource, (ProviderNode) node);
        }
        return Optional.empty();
    }

    private static Optional<Node> find(DataSource dataSource, ProvidersNode node) {
        for (Node o : node.getChildren().getNodes()) {
            if (dataSource.getProviderName().equals(o.getName())) {
                return find(dataSource, (ProviderNode) o);
            }
        }
        return Optional.empty();
    }

    private static Optional<Node> find(DataSource dataSource, ProviderNode node) {
        for (Node o : node.getChildren().getNodes()) {
            if (dataSource.equals(o.getLookup().lookup(DataSource.class))) {
                return Optional.of(o);
            }
        }
        return Optional.empty();
    }
}
