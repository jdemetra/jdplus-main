/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.desktop.plugin.multiprocessing.actions;

import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingManager;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaBatchUI;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaNode;
import jdplus.toolkit.desktop.plugin.ui.ActiveViewAction;
import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(category = "SaProcessing", id = Accept.ID)
@ActionRegistration(displayName = "#CTL_Accept", lazy = false)
@ActionReferences({
    @ActionReference(path = MultiProcessingManager.LOCALPATH, position = 1600, separatorBefore = 1599)
})
@NbBundle.Messages("CTL_Accept=Accept")
public final class Accept extends ActiveViewAction<SaBatchUI> {

    @ClassNameConstant
    public static final String ID = "jdplus.sa.desktop.plugin.multiprocessing.actions.Accept";

    public Accept() {
        super(SaBatchUI.class);
        refreshAction();
        putValue(NAME, Bundle.CTL_Accept());
    }

    @Override
    protected void refreshAction() {
        SaBatchUI ui = context();
        enabled = ui != null && ui.getSelectionCount() > 0;
        if (enabled) {
            SaNode[] selection = ui.getSelection();
            boolean accepted = canAccept(selection);

            if (accepted) {
                putValue(NAME, "Accept");
            } else {
                putValue(NAME, "Reset Quality");
            }
        }
    }

    @Override
    protected void process(SaBatchUI cur) {
        SaNode[] selection = cur.getSelection();
        boolean accepted = canAccept(selection);
        for (SaNode o : selection) {
            if (o.getOutput() != null) {
                if (accepted) {
                    o.getOutput().accept();
                } else {
                   o.getOutput().resetQuality();
                }
            }
        }
        cur.getController().getDocument().setDirty();
        cur.redrawAll();
    }

    public boolean canAccept(SaNode[] selection) {
        for (SaNode node : selection) {
            if (node.getOutput() == null) {
                return false;
            }
        }
        return true;
    }
}
