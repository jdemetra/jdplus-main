package jdplus.sa.desktop.plugin.multiprocessing.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import jdplus.sa.base.api.SaSpecification;
import jdplus.sa.base.information.SaSpecificationMapping;
import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingController;
import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingDocument;
import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingManager;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaBatchUI;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaNode;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.desktop.plugin.interchange.Importable;
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

@ActionID(category = "SaProcessing", id = ImportRefSpecFromFile.ID)
@ActionRegistration(displayName = "#CTL_ImportRefSpecFromFile", lazy = false)
@ActionReferences({
    @ActionReference(path = MultiProcessingManager.CONTEXTPATH + RefSpecification.PATH, position = 1518),
    @ActionReference(path = MultiProcessingManager.LOCALPATH + RefSpecification.PATH, position = 1518)
})
@Messages("CTL_ImportRefSpecFromFile=Import from file")
public final class ImportRefSpecFromFile extends ActiveViewAction<SaBatchUI> {

    @ClassNameConstant
    public static final String ID = "jdplus.sa.desktop.plugin.multiprocessing.actions.ImportRefSpecFromFile";

    public static final String PATH = RefSpecification.PATH + "/Import";

    public ImportRefSpecFromFile() {
        super(SaBatchUI.class);
        refreshAction();
        putValue(NAME, Bundle.CTL_ImportRefSpecFromFile());
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

        List<Importable> importables = new ArrayList<>();
        for (ImportableSpecFactory factory : Lookup.getDefault().lookupAll(ImportableSpecFactory.class)) {
            importables.add(factory.create());
        }
//        ServiceLoader<SaSpecification> loader = ServiceLoader.load(SaSpecification.class, getClass().getClassLoader());
//        for (SaSpecification spec : loader) {
//            importables.add(new ImportableSpec(spec.getClass()));
//        }
//        for (WorkspaceItemRepository repo : Lookup.getDefault().lookupAll(WorkspaceItemRepository.class)) {
//            importables.add(new ImportableSpec(repo.getSupportedType()));
//        }

        new Thread(() -> {
            ProgressHandle progress = ProgressHandle.createHandle("Reading Specs");
            progress.start();
            for (InterchangeSpi ics : InterchangeManager.get().all()) {
                if (ics.getName().contains("File")) {
                    try {
                        ics.performImport(importables);
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
                    spec = (ImportData) JOptionPane.showInputDialog(cur, "Spec", "Choose Specification", JOptionPane.QUESTION_MESSAGE, null, specs.toArray(new ImportData[specs.size()]), null);
            }

            if (spec == null) {
                return;
            }

            MultiProcessingController controller = cur.getController();
            WorkspaceItem<MultiProcessingDocument> document = controller.getDocument();
            SaNode newNode = node.withDomainSpecification(spec.getSpec());
            document.getElement().replace(newNode.getId(), newNode);
//            List<SaNode> currentNodes = cur.getController().getDocument().getElement().getCurrent();
//            currentNodes.set(currentNodes.indexOf(node), newNode);
//            SwingUtilities.invokeLater(() -> {
//                cur.setSelection(new SaNode[]{newNode});
//            });
            cur.redrawAll();
            document.setDirty();
            controller.setSaProcessingState(MultiProcessingController.SaProcessingState.READY);
        }).start();
    }
}
