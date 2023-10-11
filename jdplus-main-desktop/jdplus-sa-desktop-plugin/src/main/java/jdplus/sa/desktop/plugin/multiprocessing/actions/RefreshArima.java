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
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "SaProcessing", id = RefreshArima.ID)
@ActionRegistration(displayName = "#CTL_RefreshArima", lazy = false)
@ActionReferences({
    @ActionReference(path = MultiProcessingManager.CONTEXTPATH + RefreshPartial.PATH, position = 1260)
})
@Messages("CTL_RefreshArima=+ Arima model")
public final class RefreshArima extends ActiveViewAction<SaBatchUI> {

    @ClassNameConstant
    public static final String ID = "jdplus.sa.desktop.plugin.multiprocessing.actions.RefreshArima";

    public RefreshArima() {
        super(SaBatchUI.class);
        refreshAction();
        putValue(NAME, Bundle.CTL_RefreshArima());
    }

    @Override
    protected void process(SaBatchUI ui) {
        ui.refresh(EstimationPolicyType.Outliers_StochasticComponent, true, true);
    }

    @Override
    protected void refreshAction() {
        SaBatchUI ui = context();
        enabled = ui != null && ui.getElement().isRefreshable();
    }
}
