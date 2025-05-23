/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.desktop.plugin.x13.documents;

import jdplus.x13.desktop.plugin.x13.descriptors.X13SpecUI;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.PropertiesDialog;
import jdplus.toolkit.desktop.plugin.workspace.AbstractWorkspaceItemManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemManager;
import jdplus.x13.base.api.x13.X13Spec;
import jdplus.toolkit.base.api.util.Id;
import jdplus.toolkit.base.api.util.LinearId;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import jdplus.x13.base.workspace.X13Handlers;

import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = WorkspaceItemManager.class, position = 1000)
public class X13SpecManager extends AbstractWorkspaceItemManager<X13Spec> {

    public static final LinearId ID = new LinearId(X13Spec.FAMILY, WorkspaceFactory.SPECIFICATIONS, X13Spec.METHOD);
    public static final String PATH = "x13.spec";
    public static final String ITEMPATH = "x13.spec.item";

    @Override
    protected String getItemPrefix() {
        return X13Handlers.X13SPEC_PREFIX;
    }

    @Override
    public Id getId() {
        return ID;
    }

    @Override
    public X13Spec createNewObject() {
        return X13Spec.RSA4;
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
         final WorkspaceItem<X13Spec> xdoc = WorkspaceFactory.getInstance().getActiveWorkspace().searchDocument(child, X13Spec.class);
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
        return ImageUtilities.loadImageIcon("jdplus/x13/desktop/plugin/x13/api/blog_16x16.png", false);
   }

    @Override
    public Icon getItemIcon(WorkspaceItem<X13Spec> doc) {
        return getManagerIcon();
    }

    @Override
    public List<WorkspaceItem<X13Spec>> getDefaultItems() {
        List<WorkspaceItem<X13Spec>> result = new ArrayList<>();
        result.add(WorkspaceItem.system(ID, "X11", X13Spec.RSAX11));
        result.add(WorkspaceItem.system(ID, "RSA0", X13Spec.RSA0));
        result.add(WorkspaceItem.system(ID, "RSA1", X13Spec.RSA1));
        result.add(WorkspaceItem.system(ID, "RSA2", X13Spec.RSA2));
        result.add(WorkspaceItem.system(ID, "RSA3", X13Spec.RSA3));
        result.add(WorkspaceItem.system(ID, "RSA4", X13Spec.RSA4));
        result.add(WorkspaceItem.system(ID, "RSA5", X13Spec.RSA5));
        return result;
    }

    public void edit(final WorkspaceItem<X13Spec> xdoc) {
        if (xdoc == null || xdoc.getElement() == null) {
            return;

        }
        final X13SpecUI ui = new X13SpecUI(xdoc.getElement(), xdoc.isReadOnly());
        Frame owner = WindowManager.getDefault().getMainWindow();
        PropertiesDialog propDialog =
                new PropertiesDialog(owner, true, ui,
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
    public Class<X13Spec> getItemClass() {
        return X13Spec.class;
    }

}
