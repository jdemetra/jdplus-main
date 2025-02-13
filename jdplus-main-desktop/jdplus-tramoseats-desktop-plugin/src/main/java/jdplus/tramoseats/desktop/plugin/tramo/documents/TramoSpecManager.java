/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.tramoseats.desktop.plugin.tramo.documents;

import jdplus.tramoseats.desktop.plugin.tramo.descriptors.TramoSpecUI;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.PropertiesDialog;
import jdplus.toolkit.desktop.plugin.workspace.AbstractWorkspaceItemManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemManager;
import jdplus.tramoseats.base.api.tramo.TramoSpec;
import jdplus.toolkit.base.api.util.Id;
import jdplus.toolkit.base.api.util.LinearId;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import jdplus.tramoseats.base.workspace.TramoSeatsHandlers;

import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jean Palate
 * @author Mats Maggi
 */
@ServiceProvider(service = WorkspaceItemManager.class,
        position = 500)
public class TramoSpecManager extends AbstractWorkspaceItemManager<TramoSpec> {

    public static final LinearId ID = new LinearId(TramoSpec.FAMILY, "specifications", TramoSpec.METHOD);
    public static final String PATH = "tramo.spec";
    public static final String ITEMPATH = "tramo.spec.item";

    @Override
    protected String getItemPrefix() {
        return TramoSeatsHandlers.TRAMOSPEC_PREFIX;
    }

    @Override
    public Id getId() {
        return ID;
    }

    @Override
    public TramoSpec createNewObject() {
        return TramoSpec.TRfull;
    }

    @Override
    public ItemType getItemType() {
        return ItemType.Spec;
    }

    @Override
    public String getActionsPath() {
        return PATH;
    }

    @Override
    public Status getStatus() {
        return Status.Certified;
    }

    @Override
    public Action getPreferredItemAction(Id child) {
        final WorkspaceItem<TramoSpec> xdoc = WorkspaceFactory.getInstance().getActiveWorkspace().searchDocument(child, TramoSpec.class);
        if (xdoc == null || xdoc.getElement() == null) {
            return null;
        }
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                edit(xdoc);
            }
        };
    }

    @Override
    public Icon getManagerIcon() {
        return ImageUtilities.loadImageIcon("jdplus/tramoseats/desktop/plugin/tramoseats/api/blog_16x16.png", false);
   }

    @Override
    public Icon getItemIcon(WorkspaceItem<TramoSpec> doc) {
        return getManagerIcon();
    }

    @Override
    public List<WorkspaceItem<TramoSpec>> getDefaultItems() {
        ArrayList<WorkspaceItem<TramoSpec>> defspecs = new ArrayList<>();
        defspecs.add(WorkspaceItem.system(ID, "TR0", TramoSpec.TR0));
        defspecs.add(WorkspaceItem.system(ID, "TR1", TramoSpec.TR1));
        defspecs.add(WorkspaceItem.system(ID, "TR2", TramoSpec.TR2));
        defspecs.add(WorkspaceItem.system(ID, "TR3", TramoSpec.TR3));
        defspecs.add(WorkspaceItem.system(ID, "TR4", TramoSpec.TR4));
        defspecs.add(WorkspaceItem.system(ID, "TR5", TramoSpec.TR5));
        defspecs.add(WorkspaceItem.system(ID, "TRfull", TramoSpec.TRfull));
        return defspecs;
    }

    public void edit(final WorkspaceItem<TramoSpec> xdoc) {
        if (xdoc == null || xdoc.getElement() == null) {
            return;

        }
        final TramoSpecUI ui = new TramoSpecUI(xdoc.getElement(), xdoc.isReadOnly());
        Frame owner = WindowManager.getDefault().getMainWindow();
        PropertiesDialog propDialog
                = new PropertiesDialog(owner, true, ui,
                        new AbstractAction("OK") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        xdoc.setElement(ui.getCore());
                    }
                });
        propDialog.setTitle(xdoc.getDisplayName());
        propDialog.setLocationRelativeTo(owner);
        propDialog.setVisible(true);
    }

    @Override
    public Class getItemClass() {
        return TramoSpec.class;
    }
}
