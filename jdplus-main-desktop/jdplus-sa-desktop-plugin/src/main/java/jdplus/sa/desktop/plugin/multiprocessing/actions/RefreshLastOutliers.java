/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.desktop.plugin.multiprocessing.actions;

import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingManager;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaBatchUI;
import jdplus.toolkit.desktop.plugin.ui.ActiveViewAction;
import jdplus.sa.base.api.EstimationPolicyType;
import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "SaProcessing", id = RefreshLastOutliers.ID)
@ActionRegistration(displayName = "#CTL_RefreshLastOutliers", lazy = false)
@ActionReferences({
    @ActionReference(path = MultiProcessingManager.CONTEXTPATH + RefreshPartial.PATH, position = 1240)
})
@Messages("CTL_RefreshLastOutliers=+ Last outliers")
public final class RefreshLastOutliers extends ActiveViewAction<SaBatchUI> {

    @ClassNameConstant
    public static final String ID = "jdplus.sa.desktop.plugin.multiprocessing.actions.RefreshLastOutliers";

    public RefreshLastOutliers() {
        super(SaBatchUI.class);
        refreshAction();
        putValue(NAME, Bundle.CTL_RefreshLastOutliers());
    }

    @Override
    protected void process(SaBatchUI ui) {
        // TODO customize the length of the last outliers
        ui.refresh(EstimationPolicyType.LastOutliers, -1, true, true);
    }

    @Override
    protected void refreshAction() {
        SaBatchUI ui = context();
        enabled = ui != null && ui.getElement().isRefreshable();
    }
}
