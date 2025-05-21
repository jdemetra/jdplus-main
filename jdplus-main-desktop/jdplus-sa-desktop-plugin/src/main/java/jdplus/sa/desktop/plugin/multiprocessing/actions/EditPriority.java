/*
 * Copyright 2025 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.sa.desktop.plugin.multiprocessing.actions;

import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingManager;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaBatchUI;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaNode;
import jdplus.toolkit.desktop.plugin.ui.ActiveViewAction;
import java.awt.Dimension;
import java.util.Arrays;
import javax.swing.JFormattedTextField;
import javax.swing.JScrollPane;

import nbbrd.design.ClassNameConstant;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "SaProcessing", id = EditPriority.ID)
@ActionRegistration(displayName = "#CTL_EditPriority", lazy = false)
@ActionReferences({
    @ActionReference(path = MultiProcessingManager.CONTEXTPATH + Edit.PATH, position = 1810)
})
@Messages("CTL_EditPriority=Priority...")
public final class EditPriority extends ActiveViewAction<SaBatchUI> {

    @ClassNameConstant
    public static final String ID = "jdplus.sa.desktop.plugin.multiprocessing.actions.EditPriority";

    public static final String TITLE = "Priority";

    public EditPriority() {
        super(SaBatchUI.class);
        refreshAction();
        putValue(NAME, Bundle.CTL_EditPriority());
    }

    @Override
    protected void process(SaBatchUI cur) {
        SaNode[] items = cur.getSelection();
        if (items.length == 0) {
            return;
        }
        int priority = items[0].getOutput().getPriority();
        JFormattedTextField area = new JFormattedTextField(priority);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(100, 25));

        NotifyDescriptor nd = new NotifyDescriptor(scroll,
                TITLE,
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE,
                null,
                NotifyDescriptor.OK_OPTION);

        if (DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.OK_OPTION) {
            return;
        }
        priority = Integer.parseInt(area.getText());
        for (int i = 0; i < items.length; ++i) {
            items[i].setOutput(items[i].getOutput().withPriority(priority));
        }
        cur.getController().getDocument().setDirty();
    }

    @Override
    protected void refreshAction() {
        SaBatchUI ui = context();
        enabled = ui != null && ui.getSelectionCount() > 0;
    }
}
