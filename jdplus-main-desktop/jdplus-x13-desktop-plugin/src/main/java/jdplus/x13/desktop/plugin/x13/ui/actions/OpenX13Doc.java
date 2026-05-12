/*
 * Copyright 2026 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.x13.desktop.plugin.x13.ui.actions;

import jdplus.toolkit.desktop.plugin.workspace.DocumentUIServices;
import jdplus.x13.desktop.plugin.x13.documents.X13DocumentManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.nodes.WsNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jdplus.x13.base.core.x13.X13Document;
import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(category = "Tools", id = OpenX13Doc.ID)
@ActionRegistration(displayName = "#CTL_OpenX13Doc")
@ActionReferences({
    @ActionReference(path = X13DocumentManager.ITEMPATH, position = 1600, separatorBefore = 1590)
})
@NbBundle.Messages("CTL_OpenX13Doc=Open")
public class OpenX13Doc implements ActionListener {

    @ClassNameConstant
    public static final String ID = "jdplus.x13.desktop.plugin.x13.ui.actions.OpenX13Doc";

    private final WsNode context;

    public OpenX13Doc(WsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        WorkspaceItem<X13Document> doc = context.getWorkspace().searchDocument(context.lookup(), X13Document.class);
        DocumentUIServices ui = DocumentUIServices.forDocument(X13Document.class);
        if (ui != null) {
            ui.showDocument(doc);
        }
    }
}
