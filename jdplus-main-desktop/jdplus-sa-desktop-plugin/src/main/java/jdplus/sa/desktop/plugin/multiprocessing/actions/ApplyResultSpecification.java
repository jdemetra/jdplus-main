/*
 * Copyright 2016 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.sa.desktop.plugin.multiprocessing.actions;

import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingController;
import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingDocument;
import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingManager;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaBatchUI;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaNode;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.ui.ActiveViewAction;
import jdplus.sa.base.api.EstimationPolicyType;
import jdplus.sa.base.api.SaManager;
import jdplus.sa.base.api.SaSpecification;
import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "SaProcessing", id = ApplyResultSpecification.ID)
@ActionRegistration(displayName = "#CTL_ApplyResultSpecification", lazy = false)
@ActionReferences({
    @ActionReference(path = MultiProcessingManager.CONTEXTPATH + Edit.PATH, position = 1520),
    @ActionReference(path = MultiProcessingManager.LOCALPATH + Edit.PATH, position = 1520)
})
@Messages("CTL_ApplyResultSpecification=Apply Result Specification")
public final class ApplyResultSpecification extends ActiveViewAction<SaBatchUI> {

    @ClassNameConstant
    public static final String ID = "jdplus.sa.desktop.plugin.multiprocessing.actions.ApplyResultSpecification";

    public ApplyResultSpecification() {
        super(SaBatchUI.class);
        refreshAction();
        putValue(NAME, Bundle.CTL_ApplyResultSpecification());
    }

    @Override
    protected void refreshAction() {
        SaBatchUI ui = context();
        enabled = ui != null && ui.getSelectionCount() > 0;
    }

    @Override
    protected void process(SaBatchUI cur) {
        cur.stop();
        SaNode[] selection = cur.getSelection();
        MultiProcessingController controller = cur.getController();
        WorkspaceItem<MultiProcessingDocument> document = controller.getDocument();
        for (SaNode o : selection) {
            if (o.isProcessed()) {
                SaSpecification dspec = o.domainSpec();
                SaSpecification pspec = o.getOutput().getEstimation().getPointSpec();
                pspec = SaManager.factoryFor(dspec).refreshSpec(pspec, dspec, EstimationPolicyType.FreeParameters, null);
                SaNode n = o.withEstimationSpecification((SaSpecification) pspec);
                document.getElement().replace(o.getId(), n);
            }
        }
        cur.redrawAll();
        document.setDirty();
        controller.setSaProcessingState(MultiProcessingController.SaProcessingState.READY);
    }
}
