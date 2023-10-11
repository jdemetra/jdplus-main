/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.desktop.plugin.multiprocessing.actions;

import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingManager;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaBatchUI;
import jdplus.toolkit.desktop.plugin.ui.ActiveViewAction;
import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "SaProcessing", id = DeleteItems.ID)
@ActionRegistration(displayName = "#CTL_DeleteItems", lazy=false)
@ActionReferences({
    @ActionReference(path = MultiProcessingManager.CONTEXTPATH + Edit.PATH, position = 1340)
})
@Messages("CTL_DeleteItems=Delete")
public final class DeleteItems extends ActiveViewAction<SaBatchUI> {

    @ClassNameConstant
    public static final String ID = "jdplus.sa.desktop.plugin.multiprocessing.actions.DeleteItems";

    public static final String DELETE_MESSAGE = "Are you sure you want to delete the selected items?";

    public DeleteItems() {
        super(SaBatchUI.class);
        refreshAction();
        putValue(NAME, Bundle.CTL_DeleteItems());
    }

    @Override
    protected void refreshAction() {
        SaBatchUI ui = context();
        enabled = ui != null && ui.getSelectionCount() > 0;
    }

    @Override
    protected void process(SaBatchUI cur) {
        cur.remove(true);
    }
}
