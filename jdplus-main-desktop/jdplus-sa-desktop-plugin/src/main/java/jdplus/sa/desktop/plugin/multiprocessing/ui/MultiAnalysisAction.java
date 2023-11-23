/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.desktop.plugin.multiprocessing.ui;

import jdplus.sa.desktop.plugin.ui.DemetraSaUI;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import nbbrd.design.ClassNameConstant;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

@ActionID(category = "Seasonal Adjustment", id = MultiAnalysisAction.ID)
@ActionRegistration(displayName = "#CTL_MultiAnalysisAction")
@ActionReferences({
    @ActionReference(path = "Menu/Statistical methods/Seasonal Adjustment/Multi Processing", position = 10000)
})
@Messages("CTL_MultiAnalysisAction=New")
public final class MultiAnalysisAction implements ActionListener {

    @ClassNameConstant
    public static final String ID = "jdplus.sa.desktop.plugin.multiprocessing.ui.MultiAnalysisAction";

    @Override
    public void actionPerformed(ActionEvent e) {
        MultiProcessingManager mgr = WorkspaceFactory.getInstance().getManager(MultiProcessingManager.class);
        WorkspaceItem<MultiProcessingDocument> doc = mgr.create(WorkspaceFactory.getInstance().getActiveWorkspace());
        TopComponent c = createView(doc);
        c.open();
        c.requestActive();
    }

    public static TopComponent createView(final WorkspaceItem<MultiProcessingDocument> doc) {
        boolean dirty = doc.isDirty();
        final MultiProcessingController controller = new MultiProcessingController(doc);
        SaBatchUI processingView = new SaBatchUI(controller);
        SummaryView summaryView = new SummaryView(controller);
        MatrixView matrixView = new MatrixView(controller);

        MultiViewDescription[] descriptions = {
            new QuickAndDirtyDescription("Processing", processingView),
            new QuickAndDirtyDescription("Summary", summaryView),
            new QuickAndDirtyDescription("Matrix", matrixView)};

        final TopComponent result = MultiViewFactory.createMultiView(descriptions, descriptions[0], states -> {
            controller.dispose();
            return true;
        });
        result.setName(doc.getDisplayName());
        doc.setView(result);
        if (!dirty) {
            doc.resetDirty();
        }

        DemetraSaUI demetraUI = DemetraSaUI.get();

        processingView.setDefaultSpecification(demetraUI.getDefaultSaSpec());

        controller.addPropertyChangeListener(evt -> {
            switch (controller.getSaProcessingState()) {
                case DONE -> {
                    result.makeBusy(false);
                    result.setAttentionHighlight(true);
                }
                case STARTED -> result.makeBusy(true);
                case CANCELLED -> result.makeBusy(false);
                case READY -> {
                    result.makeBusy(false);
                    result.setAttentionHighlight(false);
                }
                case PENDING -> result.makeBusy(false);
            }
        });
        return result;
    }

    static class QuickAndDirtyDescription implements MultiViewDescription {

        final String name;
        final MultiViewElement multiViewElement;

        public QuickAndDirtyDescription(String name, MultiViewElement multiViewElement) {
            this.name = name;
            this.multiViewElement = multiViewElement;
        }

        @Override
        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_NEVER;
        }

        @Override
        public String getDisplayName() {
            return name;
        }

        @Override
        public Image getIcon() {
            return null;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }

        @Override
        public String preferredID() {
            return name;
        }

        @Override
        public MultiViewElement createElement() {
            return multiViewElement;
        }
    }
}
