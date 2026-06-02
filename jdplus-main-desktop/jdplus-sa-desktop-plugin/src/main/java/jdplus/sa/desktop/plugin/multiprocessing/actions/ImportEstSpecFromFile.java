package jdplus.sa.desktop.plugin.multiprocessing.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingController;
import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingDocument;
import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingManager;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaBatchUI;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaNode;
import jdplus.toolkit.desktop.plugin.interchange.InterchangeManager;
import jdplus.toolkit.desktop.plugin.interchange.InterchangeSpi;
import jdplus.toolkit.desktop.plugin.ui.ActiveViewAction;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import nbbrd.design.ClassNameConstant;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "SaProcessing", id = ImportEstSpecFromFile.ID)
@ActionRegistration(displayName = "#CTL_ImportEstSpecFromFile", lazy = false)
@ActionReferences({
    @ActionReference(path = MultiProcessingManager.CONTEXTPATH + EstSpecification.PATH, position = 1518),
    @ActionReference(path = MultiProcessingManager.LOCALPATH + EstSpecification.PATH, position = 1518)
})
@Messages("CTL_ImportEstSpecFromFile=Import from file")
public final class ImportEstSpecFromFile extends ActiveViewAction<SaBatchUI> {//implements Presenter.Popup, Presenter.Menu {

    @ClassNameConstant
    public static final String ID = "jdplus.sa.desktop.plugin.multiprocessing.actions.ImportEstSpecFromFile";

    public static final String PATH = EstSpecification.PATH + "/Import";

    public ImportEstSpecFromFile() {
        super(SaBatchUI.class);
        refreshAction();
        putValue(NAME, Bundle.CTL_ImportEstSpecFromFile());
    }

    @Override
    protected void refreshAction() {
        SaBatchUI ui = context();
        enabled = ui != null && ui.getSelectionCount() == 1;
    }

    @Override
    protected void process(SaBatchUI cur) {
        SaNode node = cur.getSelection()[0];
        List<ImportData> specs = ImportableSpec.LIST;
        specs.clear();

        new Thread(() -> {
            ProgressHandle progress = ProgressHandle.createHandle("Reading Specs");
            progress.start();
            for (InterchangeSpi ics : InterchangeManager.get().all()) {
                if (ics.getName().contains("File")) {
                    try {
                        ics.performImport(new ArrayList<>(Lookup.getDefault().lookupAll(ImportableSpec.class)));
                        break;
                    } catch (IOException ex) {
                        Logger.getLogger(ImportEstSpecFromFile.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            progress.finish();

            ImportData spec;
            switch (specs.size()) {
                case 0:
                    return;
                case 1:
                    spec = specs.get(0);
                    break;
                default:
                    spec = (ImportData) JOptionPane.showInputDialog(cur, "Spec", "Choose Specification", 
                            JOptionPane.QUESTION_MESSAGE, null, specs.toArray(ImportData[]::new), null);
            }

            if (spec == null) {
                return;
            }

            MultiProcessingController controller = cur.getController();
            WorkspaceItem<MultiProcessingDocument> document = controller.getDocument();
            SaNode newNode = node.withEstimationSpecification(spec.getSpec());
            document.getElement().replace(newNode.getId(), newNode);
            cur.redrawAll();
            document.setDirty();
            controller.setSaProcessingState(MultiProcessingController.SaProcessingState.READY);
        }).start();
    } 
}
