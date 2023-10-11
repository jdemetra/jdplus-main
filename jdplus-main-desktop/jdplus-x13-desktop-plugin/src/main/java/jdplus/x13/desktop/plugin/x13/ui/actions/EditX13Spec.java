/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.desktop.plugin.x13.ui.actions;

import jdplus.x13.desktop.plugin.x13.documents.X13SpecManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.nodes.WsNode;
import jdplus.x13.base.api.x13.X13Spec;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle;

@ActionID(category = "Tools", id = EditX13Spec.ID)
@ActionRegistration(displayName = "#CTL_EditX13Spec")
@ActionReferences({
    @ActionReference(path = X13SpecManager.ITEMPATH, position = 1000, separatorAfter=1090)
})
@NbBundle.Messages("CTL_EditX13Spec=Open")
public class EditX13Spec implements ActionListener {

    @ClassNameConstant
    public static final String ID = "jdplus.x13.desktop.plugin.x13.ui.actions.EditX13Spec";

    private final WsNode context;

    public EditX13Spec(WsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        final WorkspaceItem<X13Spec> xdoc = context.getWorkspace().searchDocument(context.lookup(), X13Spec.class);
        if (xdoc == null || xdoc.getElement() == null) {
            return;
        }
        X13SpecManager mgr = (X13SpecManager) WorkspaceFactory.getInstance().getManager(xdoc.getFamily());
        if (mgr != null) {
            mgr.edit(xdoc);
        }
    }
}
