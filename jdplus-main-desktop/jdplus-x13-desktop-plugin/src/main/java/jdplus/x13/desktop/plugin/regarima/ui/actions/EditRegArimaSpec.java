/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.desktop.plugin.regarima.ui.actions;

import jdplus.x13.desktop.plugin.regarima.documents.RegArimaSpecManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.nodes.WsNode;
import jdplus.x13.base.api.regarima.RegArimaSpec;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle;

@ActionID(category = "Tools", id = EditRegArimaSpec.ID)
@ActionRegistration(displayName = "#CTL_EditRegArimaSpec")
@ActionReferences({
    @ActionReference(path = RegArimaSpecManager.ITEMPATH, position = 1000, separatorAfter=1090)
})
@NbBundle.Messages("CTL_EditRegArimaSpec=Open")
public class EditRegArimaSpec implements ActionListener {

    @ClassNameConstant
    public static final String ID = "jdplus.x13.desktop.plugin.regarima.ui.actions.EditRegArimaSpec";

    private final WsNode context;

    public EditRegArimaSpec(WsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        final WorkspaceItem<RegArimaSpec> xdoc = context.getWorkspace().searchDocument(context.lookup(), RegArimaSpec.class);
        if (xdoc == null || xdoc.getElement() == null) {
            return;
        }
        RegArimaSpecManager mgr = (RegArimaSpecManager) WorkspaceFactory.getInstance().getManager(xdoc.getFamily());
        if (mgr != null) {
            mgr.edit(xdoc);
        }
    }
}
