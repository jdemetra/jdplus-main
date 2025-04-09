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

@ActionID(category = "SaProcessing", id = ReRun.ID)
@ActionRegistration(displayName = "#CTL_ReRun", lazy=false)
@ActionReferences({
    @ActionReference(path = MultiProcessingManager.CONTEXTPATH, position = 1110, separatorAfter = 1190)
})
@Messages("CTL_ReRun=Re-Run")
public final class ReRun extends ActiveViewAction<SaBatchUI> {

    @ClassNameConstant
    public static final String ID = "jdplus.sa.desktop.plugin.multiprocessing.actions.ReRun";

    public ReRun() {
        super(SaBatchUI.class);
        refreshAction();
        putValue(NAME, Bundle.CTL_ReRun());
    }

    @Override
    protected void refreshAction() {
        SaBatchUI ui = context();
        enabled = ui != null && !ui.getElement().getCurrent().isEmpty();
    }

    @Override
    protected void process(SaBatchUI cur) {
        cur.resetProcessing(true);
        cur.start(true);
    }
}
