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

@ActionID(category = "SaProcessing", id = InitialOrder.ID)
@ActionRegistration(displayName = "#CTL_InitialOrder", lazy = true)
@ActionReferences({
    @ActionReference(path = MultiProcessingManager.CONTEXTPATH, position = 1800),
    @ActionReference(path = "Shortcuts", name = "I")
})
@Messages("CTL_InitialOrder=InitialOrder")
public final class InitialOrder extends ActiveViewAction<SaBatchUI> {

    @ClassNameConstant
    public static final String ID = "jdplus.sa.desktop.plugin.multiprocessing.actions.InitialOrder";

    public InitialOrder() {
        super(SaBatchUI.class);
        refreshAction();
        putValue(NAME, Bundle.CTL_InitialOrder());
    }

    @Override
    protected void refreshAction() {
        SaBatchUI ui = context();
        enabled = ui != null && !ui.getElement().getCurrent().isEmpty();
    }

    @Override
    protected void process(SaBatchUI cur) {
        SaBatchUI ui = context();
        if (ui != null) {
            ui.setInitialOrder();
        }
    }

}
