/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.variables.actions;

import jdplus.toolkit.desktop.plugin.nodes.SingleNodeAction;
import jdplus.toolkit.desktop.plugin.ui.variables.VariablesDocumentManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.nodes.ItemWsNode;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.api.timeseries.regression.TsDataSuppliers;
import jdplus.toolkit.base.api.util.NameManager;
import nbbrd.design.ClassNameConstant;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Edit", id = RemoveVariablesAction.ID)
@ActionRegistration(
        displayName = "#CTL_RemoveVariablesAction", lazy=false)
@ActionReferences({
    //    @ActionReference(path = "Menu/Edit"),
    @ActionReference(path = VariablesDocumentManager.ITEMPATH, position = 1100)
})
@Messages("CTL_RemoveVariablesAction=Remove")
public final class RemoveVariablesAction extends SingleNodeAction<ItemWsNode> {

    @ClassNameConstant
    public static final String ID = "jdplus.toolkit.desktop.plugin.ui.variables.actions.RemoveVariablesAction";

    public static final String DELETE_MESSAGE ="Are you sure you want to delete this item?";

    public RemoveVariablesAction() {
        super(ItemWsNode.class);
    }

    @Override
    protected void performAction(ItemWsNode activeNode) {
        WorkspaceItem<TsDataSuppliers> cur = (WorkspaceItem<TsDataSuppliers>) activeNode.getItem();
        if (cur != null && !cur.isReadOnly()) {
            TsDataSuppliers o=cur.getElement();
            removeVariables(o, activeNode);
        }
    }

    @Override
    protected boolean enable(ItemWsNode context) {
        WorkspaceItem<?> cur = context.getItem();
        return cur != null && !cur.isReadOnly();
    }

    @Override
    public String getName() {
        return Bundle.CTL_RemoveVariablesAction();
    }

    @Messages({
        "RemoveVariables.dialog.title=Remove variables",
        "RemoveVariables.dialog.message=Are you sure?"
    })
    static void removeVariables(TsDataSuppliers p, ItemWsNode node) {
        DialogDescriptor.Confirmation dd = new DialogDescriptor.Confirmation(
                Bundle.RemoveVariables_dialog_message(),
                Bundle.RemoveVariables_dialog_title(),
                NotifyDescriptor.YES_NO_OPTION);
        if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.YES_OPTION) {
            NameManager<TsDataSuppliers> manager = ModellingContext.getActiveContext().getTsVariableManagers();
            manager.remove(p);
            node.getWorkspace().remove(node.getItem());
        }
    }
}
