/*
 * Copyright 2016 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package jdplus.tramoseats.desktop.plugin.tramo.ui.actions;

import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.xml.information.XmlInformationSet;
import jdplus.toolkit.desktop.plugin.Config;
import jdplus.toolkit.desktop.plugin.interchange.Exportable;
import jdplus.toolkit.desktop.plugin.interchange.InterchangeManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.nodes.ItemWsNode;
import jdplus.tramoseats.base.api.tramo.TramoSpec;
import jdplus.tramoseats.base.information.TramoSpecMapping;
import jdplus.tramoseats.desktop.plugin.tramo.documents.TramoSpecManager;
import nbbrd.io.text.Formatter;
import nbbrd.io.xml.bind.Jaxb;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;

import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Action on Tramo specification workspace node allowing the export
 *
 * @author Mats Maggi
 */
@ActionID(category = "Tools",
        id = "demetra.desktop.tramo.ui.actions.ExportTramoSpec")
@ActionRegistration(displayName = "#CTL_ExportTramoSpec", lazy = false)
@ActionReferences({
    @ActionReference(path = TramoSpecManager.ITEMPATH, position = 1000, separatorAfter = 1090)
})
@NbBundle.Messages("CTL_ExportTramoSpec=Export to")
public class ExportTramoSpec extends NodeAction implements Presenter.Popup {

    public ExportTramoSpec() {
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenuItem result = InterchangeManager.get().newExportMenu(getExportables(getActivatedNodes()));
        result.setText(Bundle.CTL_ExportTramoSpec());
        return result;
    }

    @Override
    protected void performAction(Node[] nodes) {

    }

    @Override
    protected boolean enable(Node[] nodes) {
        return Stream.of(nodes).anyMatch(ExportTramoSpec::isExportable);
    }

    private static boolean isExportable(Node o) {
        return o instanceof ItemWsNode && isExportable((ItemWsNode) o);
    }

    private static boolean isExportable(ItemWsNode o) {
        final WorkspaceItem<TramoSpec> xdoc = o.getWorkspace().searchDocument(o.lookup(), TramoSpec.class);
        return xdoc != null && xdoc.getStatus() != WorkspaceItem.Status.System;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    private static List<Exportable> getExportables(Node[] activatedNodes) {
        return Stream.of(activatedNodes)
                .filter(ExportTramoSpec::isExportable)
                .map(ItemWsNode.class::cast)
                .map(ExportableTramoSpec::new)
                .collect(Collectors.toList());
    }

    private static final class ExportableTramoSpec implements Exportable {

        private final ItemWsNode input;

        public ExportableTramoSpec(ItemWsNode input) {
            this.input = input;
        }

        @Override
        public Config exportConfig() {
            final WorkspaceItem<TramoSpec> xdoc = input.getWorkspace().searchDocument(input.lookup(), TramoSpec.class);
            InformationSet set = TramoSpecMapping.SERIALIZER_V3.write(xdoc.getElement(), true);
            Config.Builder b = Config.builder(TramoSpec.class.getName(), input.getDisplayName(), "3.0.0")
                    .parameter("specification", INFORMATIONFORMATTER.formatAsString(set));
            return b.build();
        }
    }

    private static final Formatter<InformationSet> INFORMATIONFORMATTER = Jaxb.Formatter.of(XmlInformationSet.class).asFormatter()
            .compose(o -> {
                XmlInformationSet result = new XmlInformationSet();
                result.copy(o);
                return result;
            });
}
