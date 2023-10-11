/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.toolkit.desktop.plugin.workspace.nodes;

import jdplus.toolkit.desktop.plugin.nodes.SingleNodeAction;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemManager;
import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

@ActionID(category = "Edit", id = NewAction.ID)
@ActionRegistration(
        displayName = "#CTL_NewAction", lazy=false)
@NbBundle.Messages("CTL_NewAction=New")
public final class NewAction extends SingleNodeAction<ManagerWsNode> {

    @ClassNameConstant
    public static final String ID = "jdplus.toolkit.desktop.plugin.workspace.nodes.NewAction";

    public NewAction() {
        super(ManagerWsNode.class);
    }

    @Override
    protected void performAction(ManagerWsNode context) {
        WorkspaceItemManager<?> manager = context.getManager();
         if (manager != null) {
             manager.create(context.getWorkspace());
        }
    }

    @Override
    protected boolean enable(ManagerWsNode context) {
        WorkspaceItemManager<?> manager = context.getManager();
        return manager != null;
    }

    @Override
    public String getName() {
        return Bundle.CTL_NewAction();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
