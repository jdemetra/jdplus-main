/*
 * Copyright 2022 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.toolkit.desktop.plugin.core.ts.actions;

import jdplus.toolkit.base.api.timeseries.TsDocument;
import jdplus.toolkit.base.api.timeseries.TsFactory;
import jdplus.toolkit.base.api.timeseries.TsInformationType;
import jdplus.toolkit.desktop.plugin.ui.ActiveViewAction;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.ui.WorkspaceTsTopComponent;
import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Processing", id = RefreshTs.ID)
@ActionRegistration(displayName = "#CTL_RefreshTs", lazy = false)
@ActionReferences({
    @ActionReference(path = WorkspaceFactory.TSCONTEXTPATH, position = 1510),
    @ActionReference(path = "Shortcuts", name = "R")
})
@Messages("CTL_RefreshTs=Refresh Data")
public final class RefreshTs extends ActiveViewAction<WorkspaceTsTopComponent> {

    @ClassNameConstant
    public static final String ID = "jdplus.toolkit.desktop.plugin.core.ts.actions.RefreshTs";

    public RefreshTs() {
        super(WorkspaceTsTopComponent.class);
        putValue(NAME, Bundle.CTL_RefreshTs());
        refreshAction();
    }

    @Override
    protected void refreshAction() {
        enabled = false;
        WorkspaceTsTopComponent<TsDocument<?, ?>> cur = context();
        if (cur != null) {
            enabled = true;
            TsDocument element = cur.getDocument().getElement();
            enabled = element.isFrozen();
        }
    }

    @Override
    protected void process(WorkspaceTsTopComponent cur) {
        if (cur != null) {
            enabled = true;
            WorkspaceItem wcur = cur.getDocument();
            TsDocument element = (TsDocument) wcur.getElement();
            element.refreshTs(TsFactory.getDefault(), TsInformationType.Data);
            WorkspaceFactory.Event ev = new WorkspaceFactory.Event(wcur.getOwner(), wcur.getId(), WorkspaceFactory.Event.ITEMCHANGED, null);
            WorkspaceFactory.getInstance().notifyEvent(ev);
            wcur.setDirty();
        }
    }
}
