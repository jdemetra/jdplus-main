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
package jdplus.toolkit.desktop.plugin.core.ts.actions;

import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.ui.ActiveViewAction;
import jdplus.toolkit.desktop.plugin.workspace.ui.WorkspaceTsTopComponent;
import jdplus.toolkit.base.api.processing.ProcSpecification;
import jdplus.toolkit.base.api.timeseries.TsDocument;
import nbbrd.design.ClassNameConstant;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Action used in the SA views (for single time series) to allow the export of
 * the specification used to the workspace for further use.
 *
 * @author Mats Maggi
 */
@ActionID(category = "Tools", id = ExportSpecToWorkSpace.ID)
@ActionRegistration(
        displayName = "#CTL_ExportSpecToWorkSpace", lazy = false)
@ActionReferences({
    @ActionReference(path = WorkspaceFactory.TSCONTEXTPATH, position = 1950)
})
@Messages("CTL_ExportSpecToWorkSpace=Copy spec. to workspace")
public final class ExportSpecToWorkSpace extends ActiveViewAction<WorkspaceTsTopComponent> {

    @ClassNameConstant
    public static final String ID = "jdplus.toolkit.desktop.plugin.core.ts.actions.ExportSpecToWorkSpace";

    public ExportSpecToWorkSpace() {
        super(WorkspaceTsTopComponent.class);
        refreshAction();
        putValue(NAME, Bundle.CTL_ExportSpecToWorkSpace());
    }

    @Override
    protected void refreshAction() {
        if (context() != null) {
            WorkspaceItem<?> cur = context().getDocument();
            enabled = cur.getElement() instanceof TsDocument;
        } else {
            enabled = false;
        }
    }

    @Override
    protected void process(WorkspaceTsTopComponent ws) {
        WorkspaceItem cur = ws.getDocument();
        TsDocument doc = (TsDocument) cur.getElement();
        ProcSpecification spec = doc.getSpecification();
        Class<? extends ProcSpecification> specClass = spec.getClass();
        WorkspaceFactory.getInstance().getManagers().stream()
                .filter(mgr -> mgr.getItemClass().equals(specClass))
                .findFirst()
                .ifPresentOrElse(wsMgr -> {
                    WorkspaceItem ndoc = WorkspaceItem.newItem(wsMgr.getId(), wsMgr.getNextItemName(null), spec);
                    ndoc.setComments(cur.getComments());
                    WorkspaceFactory.getInstance().getActiveWorkspace().add(ndoc);
                }, () -> {
                    NotifyDescriptor nd = new NotifyDescriptor.Message("Could not copy specification to workspace (No manager found)");
                    DialogDisplayer.getDefault().notify(nd);
                });
    }
}
