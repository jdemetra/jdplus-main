/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.desktop.plugin.tramoseats.ui.actions;

import jdplus.tramoseats.desktop.plugin.tramoseats.documents.TramoSeatsDocumentManager;
import jdplus.toolkit.desktop.plugin.workspace.DocumentUIServices;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.nodes.WsNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsDocument;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(category = "Tools",
id = "demetra.desktop.tramoseats.ui.OpenTramoSeatsDoc")
@ActionRegistration(displayName = "#CTL_OpenTramoSeatsDoc")
@ActionReferences({
    @ActionReference(path = TramoSeatsDocumentManager.ITEMPATH, position = 1600, separatorBefore = 1590)
})
@NbBundle.Messages("CTL_OpenTramoSeatsDoc=Open")
public class OpenTramoSeatsDoc implements ActionListener {

    private final WsNode context;

    public OpenTramoSeatsDoc(WsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        WorkspaceItem<TramoSeatsDocument> doc = context.getWorkspace().searchDocument(context.lookup(), TramoSeatsDocument.class);
        DocumentUIServices ui = DocumentUIServices.forDocument(TramoSeatsDocument.class);
        if (ui != null) {
            ui.showDocument(doc);
        }
    }
}
