/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.variables.actions;

import jdplus.toolkit.desktop.plugin.ui.variables.VariablesDocumentManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.nodes.WsNode;
import jdplus.toolkit.base.api.timeseries.regression.TsDataSuppliers;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(category = "Edit", id = OpenAction.ID)
@ActionRegistration(displayName = "#CTL_OpenAction")
@ActionReferences({
    @ActionReference(path = VariablesDocumentManager.ITEMPATH, position = 1600, separatorBefore = 1590)
})
@NbBundle.Messages("CTL_OpenAction=Open")
public class OpenAction implements ActionListener {

    @ClassNameConstant
    public static final String ID = "jdplus.toolkit.desktop.plugin.ui.variables.actions.OpenAction";

    private final WsNode context;

    public OpenAction(WsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        WorkspaceItem<TsDataSuppliers> doc = context.getWorkspace().searchDocument(context.lookup(), TsDataSuppliers.class);
        VariablesDocumentManager mgr = WorkspaceFactory.getInstance().getManager(VariablesDocumentManager.class);
        mgr.openDocument(doc);
    }
}
