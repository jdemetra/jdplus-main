/*
 * Copyright 2017 National Bank of Belgium
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

import jdplus.tramoseats.desktop.plugin.tramoseats.documents.TramoSeatsDocumentManager;
import jdplus.tramoseats.desktop.plugin.tramoseats.documents.TramoSeatsSpecManager;
import jdplus.tramoseats.desktop.plugin.tramoseats.ui.TramoSeatsTopComponent;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.nodes.WsNode;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsSpec;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsDocument;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Tools",
        id = "demetra.desktop.tramoseats.ui.actions.CreateTramoSeatsDoc")
@ActionRegistration(displayName = "#CTL_CreateTramoSeatsDoc")
@ActionReferences({
    @ActionReference(path = TramoSeatsSpecManager.ITEMPATH, position = 1620, separatorBefore = 1300)
})
@Messages("CTL_CreateTramoSeatsDoc=Create Document")
public final class CreateTramoSeatsDoc implements ActionListener {

    private final WsNode context;

    public CreateTramoSeatsDoc(WsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        final WorkspaceItem<TramoSeatsSpec> xdoc = context.getWorkspace().searchDocument(context.lookup(), TramoSeatsSpec.class);
        if (xdoc == null || xdoc.getElement() == null) {
            return;
        }
        TramoSeatsDocumentManager dmgr = (TramoSeatsDocumentManager) WorkspaceFactory.getInstance().getManager(TramoSeatsDocumentManager.ID);
        WorkspaceItem<TramoSeatsDocument> doc = dmgr.create(context.getWorkspace());
        doc.setComments(xdoc.getComments());
        doc.getElement().set(xdoc.getElement());
        TramoSeatsTopComponent view = new TramoSeatsTopComponent(doc);
        view.open();
        view.requestActive();
    }
}
