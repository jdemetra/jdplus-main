/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.desktop.plugin.regarima.ui.actions;

import jdplus.x13.desktop.plugin.regarima.documents.RegArimaSpecManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemManager;
import jdplus.toolkit.desktop.plugin.workspace.nodes.WsNode;
import jdplus.x13.base.api.regarima.RegArimaSpec;
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
    @ActionReference(path = RegArimaSpecManager.ITEMPATH, position = 1700),
})
@Messages("CTL_CloneSpec=Clone")
public final class CloneSpec implements ActionListener {

    @ClassNameConstant
    public static final String ID = "jdplus.x13.desktop.plugin.regarima.ui.actions.CloneSpec";

    private final WsNode context;

    public CloneSpec(WsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
         WorkspaceItem<RegArimaSpec> xdoc = 
                 context.getWorkspace().searchDocument(context.lookup(), RegArimaSpec.class);
        if (xdoc == null) {
            return;
        }
        WorkspaceItemManager mgr=WorkspaceFactory.getInstance().getManager(xdoc.getFamily());
        WorkspaceItem<RegArimaSpec> ndoc = WorkspaceItem.newItem(xdoc.getFamily(), mgr.getNextItemName(null), xdoc.getElement());
        context.getWorkspace().add(ndoc);
    }
}
