/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.desktop.plugin.multiprocessing.actions;

import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingManager;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaBatchUI;
import jdplus.toolkit.desktop.plugin.ui.ActiveViewAction;

import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingController;
import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "SaProcessing", id = Process.ID)
@ActionRegistration(displayName = "#CTL_Process", lazy = false)
@ActionReferences({
    @ActionReference(path = MultiProcessingManager.CONTEXTPATH, position = 1100),
    @ActionReference(path = "Shortcuts", name = "S")
})
@Messages("CTL_Process=Start")
public final class Process extends ActiveViewAction<SaBatchUI> {

    @ClassNameConstant
    public static final String ID = "jdplus.sa.desktop.plugin.multiprocessing.actions.Process";

    public Process() {
        super(SaBatchUI.class);
        refreshAction();
        putValue(NAME, Bundle.CTL_CommentSaItem());
    }

    private boolean start;

    @Override
    protected void refreshAction() {
        SaBatchUI cur = context();
        enabled = false;
        start = true;
        if (cur != null && !cur.getElement().isProcessed()) {
            MultiProcessingController.SaProcessingState state = cur.getState();
            switch (state) {
                case PENDING -> enabled = true;
                case STARTED -> {
                    start = false;
                    enabled = true;
                }
                case DONE -> // not finished
                    enabled = true;
                case READY -> enabled = true;
            }
        }
        if (start) {
            putValue(NAME, "Start");
        } else {
            putValue(NAME, "Stop");
        }
    }

    @Override
    public boolean isEnabled() {
        refreshAction();
        SaBatchUI ui = context();
        return enabled && !ui.getElement().getCurrent().isEmpty();
    }

    @Override
    protected void process(SaBatchUI cur) {
        if (start) {
            cur.start(true);
        } else {
            cur.stop();
        }
    }
}
