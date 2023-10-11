/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.variables.actions;

import jdplus.toolkit.desktop.plugin.ui.variables.VariablesDocumentManager;
import jdplus.toolkit.desktop.plugin.workspace.Workspace;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemManager;
import jdplus.toolkit.desktop.plugin.workspace.nodes.WsNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Tools", id = AddVariablesAction.ID)
@ActionRegistration(displayName = "#CTL_NewAction")
@ActionReferences({
    @ActionReference(path = VariablesDocumentManager.PATH, position = 1000)
})
@Messages("CTL_NewAction=New")
public class AddVariablesAction implements ActionListener {

    @ClassNameConstant
    public static final String ID = "jdplus.toolkit.desktop.plugin.ui.variables.actions.AddVariablesAction";

    private final WsNode context;

    public AddVariablesAction(WsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        WorkspaceItemManager mgr=WorkspaceFactory.getInstance().getManager(context.lookup());
        if (mgr != null){
            Workspace ws=context.getWorkspace();
            mgr.create(ws);
        }
    }
}
