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
package jdplus.x13.desktop.plugin.regarima.ui.actions;

import jdplus.x13.desktop.plugin.regarima.documents.RegArimaDocumentManager;
import jdplus.x13.desktop.plugin.regarima.documents.RegArimaSpecManager;
import jdplus.x13.desktop.plugin.regarima.ui.RegArimaTopComponent;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.nodes.WsNode;
import jdplus.x13.base.api.regarima.RegArimaSpec;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jdplus.x13.base.core.x13.regarima.RegArimaDocument;
import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Tools", id = CreateRegArimaDoc.ID)
@ActionRegistration(displayName = "#CTL_CreateRegArimaDoc")
@ActionReferences({
    @ActionReference(path = RegArimaSpecManager.ITEMPATH, position = 1620, separatorBefore = 1300)
})
@Messages("CTL_CreateRegArimaDoc=Create Document")
public final class CreateRegArimaDoc implements ActionListener {

    @ClassNameConstant
    public static final String ID = "jdplus.x13.desktop.plugin.regarima.ui.actions.CreateRegArimaDoc";

    private final WsNode context;

    public CreateRegArimaDoc(WsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        final WorkspaceItem<RegArimaSpec> xdoc = context.getWorkspace().searchDocument(context.lookup(), RegArimaSpec.class);
        if (xdoc == null || xdoc.getElement() == null) {
            return;
        }
        RegArimaDocumentManager dmgr = (RegArimaDocumentManager) WorkspaceFactory.getInstance().getManager(RegArimaDocumentManager.ID);
        WorkspaceItem<RegArimaDocument> doc = dmgr.create(context.getWorkspace());
        doc.setComments(xdoc.getComments());
        doc.getElement().set(xdoc.getElement());
        RegArimaTopComponent view = new RegArimaTopComponent(doc);
        view.open();
        view.requestActive();
    }
}
