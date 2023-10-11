/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.desktop.plugin.x13.ui.actions;

import jdplus.x13.desktop.plugin.x13.documents.X13SpecManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemManager;
import jdplus.toolkit.desktop.plugin.workspace.nodes.WsNode;
import jdplus.x13.base.api.x13.X13Spec;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Tools", id = CloneSpec.ID)
@ActionRegistration(displayName = "#CTL_CloneSpec")
@ActionReferences({
    @ActionReference(path = X13SpecManager.ITEMPATH, position = 1700),
})
@Messages("CTL_CloneSpec=Clone")
public final class CloneSpec implements ActionListener {

    @ClassNameConstant
    public static final String ID = "jdplus.x13.desktop.plugin.x13.ui.actions.CloneSpec";

    private final WsNode context;

    public CloneSpec(WsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
         WorkspaceItem<X13Spec> xdoc = 
                 context.getWorkspace().searchDocument(context.lookup(), X13Spec.class);
        if (xdoc == null) {
            return;
        }
        WorkspaceItemManager mgr=WorkspaceFactory.getInstance().getManager(xdoc.getFamily());
        WorkspaceItem<X13Spec> ndoc = WorkspaceItem.newItem(xdoc.getFamily(), mgr.getNextItemName(null), xdoc.getElement());
        context.getWorkspace().add(ndoc);
    }
}
