/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.desktop.plugin.tramo.ui.actions;

import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.nodes.WsNode;
import jdplus.tramoseats.desktop.plugin.tramo.documents.TramoDocumentManager;
import jdplus.toolkit.desktop.plugin.workspace.DocumentUIServices;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jdplus.tramoseats.base.core.tramo.TramoDocument;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(category = "Tools",
id = "demetra.desktop.tramo.ui.actions.OpenTramoDoc")
@ActionRegistration(displayName = "#CTL_OpenTramoDoc")
@ActionReferences({
    @ActionReference(path = TramoDocumentManager.ITEMPATH, position = 1600, separatorBefore = 1590)
})
@NbBundle.Messages("CTL_OpenTramoDoc=Open")
public class OpenTramoDoc implements ActionListener {

    private final WsNode context;

    public OpenTramoDoc(WsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        WorkspaceItem<TramoDocument> doc = context.getWorkspace().searchDocument(context.lookup(), TramoDocument.class);
        DocumentUIServices ui = DocumentUIServices.forDocument(TramoDocument.class);
        if (ui != null) {
            ui.showDocument(doc);
        }
    }
}
