package jdplus.sa.desktop.plugin.multiprocessing.actions;

import jdplus.sa.base.api.SaItem;
import jdplus.sa.base.api.SaSpecification;
import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingController;
import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingDocument;
import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingManager;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaBatchUI;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaNode;
import jdplus.toolkit.desktop.plugin.ui.ActiveViewAction;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import nbbrd.design.ClassNameConstant;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "SaProcessing", id = ExportRefSpecToWorkspace.ID)
@ActionRegistration(displayName = "#CTL_ExportRefSpecToWorkspace", lazy = false)
@ActionReferences({
    @ActionReference(path = MultiProcessingManager.CONTEXTPATH + RefSpecification.PATH, position = 1516),
    @ActionReference(path = MultiProcessingManager.LOCALPATH + RefSpecification.PATH, position = 1516)
})
@Messages("CTL_ExportRefSpecToWorkspace=Copy to workspace")
public final class ExportRefSpecToWorkspace extends ActiveViewAction<SaBatchUI> {

    @ClassNameConstant
    public static final String ID = "jdplus.sa.desktop.plugin.multiprocessing.actions.ExportRefSpecToWorkspace";

    public ExportRefSpecToWorkspace() {
        super(SaBatchUI.class);
        refreshAction();
        putValue(NAME, Bundle.CTL_ExportRefSpecToWorkspace());
    }

    @Override
    protected void refreshAction() {
        SaBatchUI ui = context();
        enabled = ui != null && ui.getSelectionCount() == 1;
    }

    @Override
    protected void process(SaBatchUI cur) {
        SaNode[] selection = cur.getSelection();
        if (selection == null || selection.length != 1) {
            return;
        }
        SaNode o = selection[0];
        SaItem item = o.getOutput();
        SaSpecification spec = item.getDefinition().getDomainSpec();
        MultiProcessingController controller = cur.getController();
        WorkspaceItem<MultiProcessingDocument> document = controller.getDocument();
        WorkspaceFactory.getInstance().getManagers().stream()
                .filter(mgr -> mgr.getItemClass().equals(spec.getClass()))
                .findFirst()
                .ifPresentOrElse(wsMgr -> {
                    WorkspaceItem<SaSpecification> ndoc = WorkspaceItem.newItem(wsMgr.getId(), wsMgr.getNextItemName(null), spec);
                    ndoc.setComments(item.getComment());
                    WorkspaceFactory.getInstance().getActiveWorkspace().add(ndoc);
                    //The following code ensures the copied spec is displayed as the node's reference spec 
                    SaNode n = o.withDomainSpecification(ndoc.getElement());
                    document.getElement().replace(o.getId(), n);
                    cur.redrawAll();
                    document.setDirty();
                    controller.setSaProcessingState(MultiProcessingController.SaProcessingState.READY);
                }, () -> {
                    NotifyDescriptor nd = new NotifyDescriptor.Message("Could not copy specification to workspace (No manager found)");
                    DialogDisplayer.getDefault().notify(nd);
                });

    }

}