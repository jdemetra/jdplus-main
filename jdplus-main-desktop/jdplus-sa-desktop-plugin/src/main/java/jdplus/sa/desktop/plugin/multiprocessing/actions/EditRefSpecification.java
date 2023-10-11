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

import jdplus.toolkit.desktop.plugin.descriptors.IObjectDescriptor;
import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingController;
import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingDocument;
import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingManager;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaBatchUI;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaNode;
import jdplus.toolkit.desktop.plugin.ui.ActiveViewAction;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.PropertiesDialog;
import jdplus.toolkit.desktop.plugin.workspace.DocumentUIServices;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.sa.base.api.SaSpecification;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(category = "SaProcessing", id = EditRefSpecification.ID)
@ActionRegistration(displayName = "#CTL_EditRefSpecification", lazy = false)
@ActionReferences({
    @ActionReference(path = MultiProcessingManager.CONTEXTPATH + RefSpecification.PATH , position = 1510),
    @ActionReference(path = MultiProcessingManager.LOCALPATH + RefSpecification.PATH, position = 1510)
})
@Messages("CTL_EditRefSpecification=Modify...")
public final class EditRefSpecification extends ActiveViewAction<SaBatchUI> {

    @ClassNameConstant
    public static final String ID = "jdplus.sa.desktop.plugin.multiprocessing.actions.EditRefSpecification";

    public EditRefSpecification() {
        super(SaBatchUI.class);
        refreshAction();
        putValue(NAME, Bundle.CTL_EditRefSpecification());
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
        MultiProcessingController controller = cur.getController();
        WorkspaceItem<MultiProcessingDocument> document = controller.getDocument();
        SaSpecification refSpec = selection[0].domainSpec();
        DocumentUIServices ui = DocumentUIServices.forSpec(refSpec.getClass());
        IObjectDescriptor<SaSpecification> desc = ui.getSpecificationDescriptor(refSpec);
        Frame owner = WindowManager.getDefault().getMainWindow();
        PropertiesDialog propDialog
                = new PropertiesDialog(owner, true, desc,
                        new AbstractAction("OK") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        SaSpecification nspec = desc.getCore();
                        SaNode n = o.withDomainSpecification(nspec);
                        document.getElement().replace(o.getId(), n);
                        cur.redrawAll();
                        document.setDirty();
                        controller.setSaProcessingState(MultiProcessingController.SaProcessingState.READY);
                    }
                });
        propDialog.setTitle("Reference specification");
        propDialog.setLocationRelativeTo(owner);
        propDialog.setVisible(true);

    }
}
