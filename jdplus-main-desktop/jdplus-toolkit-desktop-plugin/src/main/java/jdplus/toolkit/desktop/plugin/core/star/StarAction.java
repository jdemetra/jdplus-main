/*
 * Copyright 2015 National Bank of Belgium
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
package jdplus.toolkit.desktop.plugin.core.star;

import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.desktop.plugin.actions.Repaintable;
import jdplus.toolkit.desktop.plugin.nodes.SingleNodeAction;
import jdplus.toolkit.desktop.plugin.star.StarListManager;
import lombok.NonNull;
import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;

import java.util.Optional;

@ActionID(category = "File", id = StarAction.ID)
@ActionRegistration(lazy = false, displayName = "#starAction.add")
@ActionReferences({
        @ActionReference(path = "Menu/File", position = 800, separatorBefore = 799)
})
@Messages({
        "starAction.add=Add star",
        "starAction.remove=Remove star"
})
public final class StarAction extends SingleNodeAction<Node> {

    @ClassNameConstant
    public static final String ID = "jdplus.toolkit.desktop.plugin.core.star.StarAction";

    public StarAction() {
        super(Node.class);
    }

    @Override
    protected boolean enable(@NonNull Node activatedNode) {
        return !getDataSource(activatedNode).stream().peek(this::updateActionName).toList().isEmpty();
    }

    @Override
    protected void performAction(@NonNull Node activatedNode) {
        getDataSource(activatedNode).ifPresent(dataSource -> {
            StarListManager.get().toggle(dataSource);
            updateActionName(dataSource);
            Repaintable.repaintNode(activatedNode);
        });
    }

    @Override
    public String getName() {
        return Bundle.starAction_add();
    }

    private void updateActionName(DataSource dataSource) {
        putValue(NAME, StarListManager.get().isStarred(dataSource) ? Bundle.starAction_remove() : Bundle.starAction_add());
    }

    private static Optional<DataSource> getDataSource(Node node) {
        return Optional.ofNullable(node.getLookup().lookup(DataSource.class));
    }
}
