/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
