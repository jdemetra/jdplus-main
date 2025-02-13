/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.desktop.plugin.tramoseats.documents;

import jdplus.tramoseats.desktop.plugin.tramoseats.descriptors.TramoSeatsSpecUI;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.PropertiesDialog;
import jdplus.toolkit.desktop.plugin.workspace.AbstractWorkspaceItemManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemManager;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsSpec;
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
 */
@ServiceProvider(service = WorkspaceItemManager.class, position = 500)
public class TramoSeatsSpecManager extends AbstractWorkspaceItemManager<TramoSeatsSpec> {

    public static final LinearId ID = new LinearId(TramoSeatsSpec.FAMILY, WorkspaceFactory.SPECIFICATIONS, TramoSeatsSpec.METHOD);
    public static final String PATH = "tramoseats.spec";
    public static final String ITEMPATH = "tramoseats.spec.item";

    @Override
    protected String getItemPrefix() {
        return TramoSeatsHandlers.TRAMOSEATSSPEC_PREFIX;
    }

    @Override
    public Id getId() {
        return ID;
    }

    @Override
    public TramoSeatsSpec createNewObject() {
        return TramoSeatsSpec.RSAfull;
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
    public Action getPreferredItemAction(final Id child) {
        final WorkspaceItem<TramoSeatsSpec> xdoc = WorkspaceFactory.getInstance().getActiveWorkspace().searchDocument(child, TramoSeatsSpec.class);
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
    public Icon getItemIcon(WorkspaceItem<TramoSeatsSpec> doc) {
        return getManagerIcon();
    }

    @Override
    public List<WorkspaceItem<TramoSeatsSpec>> getDefaultItems() {
        List<WorkspaceItem<TramoSeatsSpec>> result = new ArrayList<>();
        result.add(WorkspaceItem.system(ID, "RSA0", TramoSeatsSpec.RSA0));
        result.add(WorkspaceItem.system(ID, "RSA1", TramoSeatsSpec.RSA1));
        result.add(WorkspaceItem.system(ID, "RSA2", TramoSeatsSpec.RSA2));
        result.add(WorkspaceItem.system(ID, "RSA3", TramoSeatsSpec.RSA3));
        result.add(WorkspaceItem.system(ID, "RSA4", TramoSeatsSpec.RSA4));
        result.add(WorkspaceItem.system(ID, "RSA5", TramoSeatsSpec.RSA5));
        result.add(WorkspaceItem.system(ID, "RSAfull", TramoSeatsSpec.RSAfull));
        return result;
    }

    public void edit(final WorkspaceItem<TramoSeatsSpec> xdoc) {
        if (xdoc == null || xdoc.getElement() == null) {
            return;

        }
        final TramoSeatsSpecUI ui = new TramoSeatsSpecUI(xdoc.getElement(), xdoc.isReadOnly());
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
    public Class<TramoSeatsSpec> getItemClass() {
        return TramoSeatsSpec.class;
    }
}
