/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.variables.actions;

import jdplus.toolkit.desktop.plugin.ui.variables.VariablesDocumentManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.base.api.timeseries.DynamicTsDataSupplier;
import jdplus.toolkit.base.api.timeseries.TsDataSupplier;
import jdplus.toolkit.base.api.timeseries.regression.TsDataSuppliers;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import nbbrd.design.ClassNameConstant;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Tools", id = RefreshAllAction.ID)
@ActionRegistration(displayName = "#CTL_RefreshAllAction")
@ActionReferences({
    @ActionReference(path = VariablesDocumentManager.PATH, position = 1700, separatorBefore = 1699)
})
@Messages("CTL_RefreshAllAction=Refresh all")
public final class RefreshAllAction implements ActionListener {

    @ClassNameConstant
    public static final String ID = "jdplus.toolkit.desktop.plugin.ui.variables.actions.RefreshAllAction";

    public RefreshAllAction() {
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        // Warning
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(WARNING_MESSAGE, NotifyDescriptor.OK_CANCEL_OPTION);
        if (DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.OK_OPTION) {
            return;
        }

        List<WorkspaceItem<TsDataSuppliers>> documents = WorkspaceFactory.getInstance().getActiveWorkspace().searchDocuments(TsDataSuppliers.class);
        for (WorkspaceItem<TsDataSuppliers> document : documents) {
            for (TsDataSupplier var : document.getElement().variables()) {
                if (var instanceof DynamicTsDataSupplier dvar) {
                    dvar.refresh();
                }
            }
        }
    }
    
    public static final String WARNING_MESSAGE = "Refreshing variables may modify some estimations. Are you sure you want to continue?";
}
