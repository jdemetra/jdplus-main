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

@ActionID(category = "SaProcessing", id = Paste.ID)
@ActionRegistration(displayName = "#CTL_Paste", lazy = false)
@ActionReferences({
    @ActionReference(path = MultiProcessingManager.CONTEXTPATH + Edit.PATH, position = 1330)
})
@Messages("CTL_Paste=Paste")
public final class Paste extends ActiveViewAction<SaBatchUI> {

    @ClassNameConstant
    public static final String ID = "jdplus.sa.desktop.plugin.multiprocessing.actions.Paste";

    public Paste() {
        super(SaBatchUI.class);
        putValue(NAME, Bundle.CTL_Paste());
        refreshAction();
    }

    @Override
    protected void refreshAction() {
        enabled = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().getAvailableDataFlavors().length > 0;
    }

    @Override
    protected void process(SaBatchUI cur) {
        SaBatchUI ui = context();
        if (ui != null) {
            ui.paste(true);
        }
    }
}
