/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.core.ts.actions;

import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.desktop.plugin.datatransfer.DataTransferManager;
import jdplus.toolkit.desktop.plugin.datatransfer.DataTransfers;
import nbbrd.design.ClassNameConstant;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.ui.ActiveViewAction;
import jdplus.toolkit.desktop.plugin.workspace.ui.WorkspaceTsTopComponent;
import java.util.Optional;

@ActionID(category = "Processing", id = PasteTs.ID)
@ActionRegistration(displayName = "#CTL_PasteTs", lazy = false)
@ActionReferences({
    @ActionReference(path = WorkspaceFactory.TSCONTEXTPATH, position = 1310),
    @ActionReference(path = "Shortcuts", name = "P")
})
@Messages("CTL_PasteTs=Paste")
public final class PasteTs extends ActiveViewAction<WorkspaceTsTopComponent> {

    @ClassNameConstant
    public static final String ID = "jdplus.toolkit.desktop.plugin.core.ts.actions.PasteTs";

    public PasteTs() {
        super(WorkspaceTsTopComponent.class);
        putValue(NAME, Bundle.CTL_PasteTs());
        refreshAction();
    }

    @Override
    protected void refreshAction() {
    }

    @Override
    protected void process(WorkspaceTsTopComponent cur) {
        WorkspaceTsTopComponent top = context();
        if (top != null) {
            Optional<Ts> s = DataTransferManager.get().toTs(DataTransfers.systemClipboardAsTransferable());
            if (!s.isPresent()) {
                NotifyDescriptor nd = new NotifyDescriptor.Message("Unable to paste ts");
                DialogDisplayer.getDefault().notify(nd);
            } else {
                top.setTs(s.orElseThrow());
            }
        }
    }
}
