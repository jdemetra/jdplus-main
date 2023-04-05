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
package jdplus.tramoseats.desktop.plugin.tramoseats.ui.actions;

import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.xml.information.XmlInformationSet;
import jdplus.toolkit.desktop.plugin.Config;
import jdplus.toolkit.desktop.plugin.interchange.Exportable;
import jdplus.toolkit.desktop.plugin.interchange.InterchangeManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.nodes.ItemWsNode;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsSpec;
import jdplus.tramoseats.base.information.TramoSeatsSpecMapping;
import jdplus.tramoseats.desktop.plugin.tramoseats.documents.TramoSeatsSpecManager;
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
        id = "demetra.desktop.tramoseats.ui.actions.ExportTramoSeatsSpec")
@ActionRegistration(displayName = "#CTL_ExportTramoSeatsSpec", lazy = false)
@ActionReferences({
    @ActionReference(path = TramoSeatsSpecManager.ITEMPATH, position = 1000, separatorAfter = 1090)
})
@NbBundle.Messages("CTL_ExportTramoSeatsSpec=Export to")
public class ExportTramoSeatsSpec extends NodeAction implements Presenter.Popup {
    
    public ExportTramoSeatsSpec() {
    }
    
    @Override
    public JMenuItem getPopupPresenter() {
        JMenuItem result = InterchangeManager.get().newExportMenu(getExportables(getActivatedNodes()));
        result.setText(Bundle.CTL_ExportTramoSeatsSpec());
        return result;
    }
    
    @Override
    protected void performAction(Node[] nodes) {
        
    }
    
    @Override
    protected boolean enable(Node[] nodes) {
        return Stream.of(nodes).anyMatch(ExportTramoSeatsSpec::isExportable);
    }
    
    private static boolean isExportable(Node o) {
        return o instanceof ItemWsNode && isExportable((ItemWsNode) o);
    }
    
    private static boolean isExportable(ItemWsNode o) {
        final WorkspaceItem<TramoSeatsSpec> xdoc = o.getWorkspace().searchDocument(o.lookup(), TramoSeatsSpec.class);
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
                .filter(ExportTramoSeatsSpec::isExportable)
                .map(ItemWsNode.class::cast)
                .map(ExportableTramoSeatsSpec::new)
                .collect(Collectors.toList());
    }
    
    private static final class ExportableTramoSeatsSpec implements Exportable {
        
        private final ItemWsNode input;
        
        public ExportableTramoSeatsSpec(ItemWsNode input) {
            this.input = input;
        }
        
        @Override
        public Config exportConfig() {
            final WorkspaceItem<TramoSeatsSpec> xdoc = input.getWorkspace().searchDocument(input.lookup(), TramoSeatsSpec.class);
            InformationSet set = TramoSeatsSpecMapping.SERIALIZER_V3.write(xdoc.getElement(), true);
            Config.Builder b = Config.builder(TramoSeatsSpec.class.getName(), input.getDisplayName(), "3.0.0")
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
