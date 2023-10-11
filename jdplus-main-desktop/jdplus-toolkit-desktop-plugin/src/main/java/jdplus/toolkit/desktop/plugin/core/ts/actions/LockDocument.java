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

import static javax.swing.Action.NAME;
import jdplus.toolkit.base.api.timeseries.TsDocument;
import jdplus.toolkit.desktop.plugin.ui.ActiveViewAction;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.ui.WorkspaceTsTopComponent;
import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author palatej
 */
@ActionID(category = "Processing", id = LockDocument.ID)
@ActionRegistration(displayName = "#CTL_LockDocument", lazy = false)
@ActionReferences({
    @ActionReference(path = WorkspaceFactory.TSCONTEXTPATH, position = 1500),
    @ActionReference(path = "Shortcuts", name = "L")
})
@NbBundle.Messages("CTL_LockDocument=Lock")
public final class LockDocument extends ActiveViewAction<WorkspaceTsTopComponent> {

    @ClassNameConstant
    public static final String ID = "jdplus.toolkit.desktop.plugin.core.ts.actions.LockDocument";

    private boolean locked;

    public LockDocument() {
        super(WorkspaceTsTopComponent.class);
        putValue(NAME, Bundle.CTL_LockDocument());
        refreshAction();
    }

    @Override
    protected void refreshAction() {
        enabled=false;
        locked=false;
        WorkspaceTsTopComponent<TsDocument<?, ?>>  cur = context();
        if (cur != null) {
            enabled = true;
            TsDocument element = cur.getDocument().getElement();
            locked = element.isLocked();
        }
        if (locked) {
            putValue(NAME, "Unlock");
        }
        else {
            putValue(NAME, "Lock");
        }
    }

    @Override
    protected void process(WorkspaceTsTopComponent cur) {
       WorkspaceTsTopComponent<TsDocument<?, ?>> tcur = cur;
        tcur.getDocument().getElement().setLocked(!locked);
    }
}
