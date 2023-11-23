/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.desktop.plugin.regarima.ui.actions;

import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.nodes.WsNode;
import jdplus.x13.desktop.plugin.regarima.documents.RegArimaDocumentManager;
import jdplus.toolkit.desktop.plugin.workspace.DocumentUIServices;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jdplus.x13.base.core.x13.regarima.RegArimaDocument;
import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(category = "Tools", id = OpenRegArimaDoc.ID)
@ActionRegistration(displayName = "#CTL_OpenRegArimaDoc")
@ActionReferences({
    @ActionReference(path = RegArimaDocumentManager.ITEMPATH, position = 1600, separatorBefore = 1590)
})
@NbBundle.Messages("CTL_OpenRegArimaDoc=Open")
public class OpenRegArimaDoc implements ActionListener {

    @ClassNameConstant
    public static final String ID = "jdplus.x13.desktop.plugin.regarima.ui.actions.OpenRegArimaDoc";

    private final WsNode context;

    public OpenRegArimaDoc(WsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        WorkspaceItem<RegArimaDocument> doc = context.getWorkspace().searchDocument(context.lookup(), RegArimaDocument.class);
        DocumentUIServices ui = DocumentUIServices.forDocument(RegArimaDocument.class);
        if (ui != null) {
            ui.showDocument(doc);
        }
    }
}
