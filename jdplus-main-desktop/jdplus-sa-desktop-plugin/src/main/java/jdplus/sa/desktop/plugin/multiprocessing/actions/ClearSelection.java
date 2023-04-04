/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.desktop.plugin.multiprocessing.actions;

import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingManager;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaBatchUI;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaNode;
import demetra.desktop.ui.ActiveViewAction;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "SaProcessing",
id = "demetra.desktop.sa.multiprocessing.actions.ClearSelection")
@ActionRegistration(displayName = "#CTL_ClearSelection", lazy=false)
@ActionReferences({
    @ActionReference(path = MultiProcessingManager.CONTEXTPATH, position = 1600),
    @ActionReference(path = "Shortcuts", name = "C")
})
@Messages("CTL_ClearSelection=Clear selection")
public final class ClearSelection extends ActiveViewAction<SaBatchUI> {


    public ClearSelection() {
        super(SaBatchUI.class);
        refreshAction();
        putValue(NAME, Bundle.CTL_ClearSelection());
    }

    @Override
    protected void refreshAction() {
        SaBatchUI ui = context();
        enabled = ui != null && ui.getSelectionCount()>0;
    }

    @Override
    protected void process(SaBatchUI cur) {
        cur.setSelection(new SaNode[0]);
    }
}

