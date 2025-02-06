/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.desktop.plugin.multiprocessing.ui;

import jdplus.toolkit.desktop.plugin.workspace.AbstractWorkspaceItemManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemManager;
import jdplus.sa.base.api.SaSpecification;
import jdplus.toolkit.base.api.util.Id;
import jdplus.toolkit.base.api.util.LinearId;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import jdplus.sa.base.workspace.SaHandlers;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = WorkspaceItemManager.class,
        position = 1900)
public class MultiProcessingManager extends AbstractWorkspaceItemManager<MultiProcessingDocument> {

    public static final LinearId ID = new LinearId(SaSpecification.FAMILY, WorkspaceFactory.MULTIDOCUMENTS);
    public static final String PATH = "sa.mdoc";
    public static final String ITEMPATH = "sa.mdoc.item";
//    public static final String DOCUMENTPATH = "sa.mdoc.document";
    public static final String CONTEXTPATH = "sa.mdoc.context";
    public static final String LOCALPATH = "sa.mdoc.local";
    public static SaSpecification defSpec;

    public static void setDefaultSpecification(final SaSpecification spec) {
        synchronized (ID) {
            defSpec = spec;
        }
    }

    public static SaSpecification getDefaultSpecification() {
        synchronized (ID) {
            return defSpec;
        }
    }

    @Override
    public Status getStatus() {
        return Status.Certified;
    }

    @Override
    public ItemType getItemType() {
        return ItemType.MultiDoc;
    }

    @Override
    public Id getId() {
        return ID;
    }

    @Override
    public String getActionsPath() {
        return PATH;
    }

    @Override
    protected String getItemPrefix() {
        return SaHandlers.PREFIX;
    }

    @Override
    public MultiProcessingDocument createNewObject() {
        MultiProcessingDocument ndoc = MultiProcessingDocument.createNew();
        return ndoc;
    }

    @Override
    public Action getPreferredItemAction(final Id child) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WorkspaceItem<MultiProcessingDocument> doc = (WorkspaceItem<MultiProcessingDocument>) WorkspaceFactory.getInstance().getActiveWorkspace().searchDocument(child);
                if (doc != null) {
                    openDocument(doc);
                }
            }
        };
    }

    public void openDocument(final WorkspaceItem<MultiProcessingDocument> doc) {

        if (doc == null || doc.getElement() == null) {
            return;
        }
        if (doc.isOpen()) {
            doc.getView().requestActive();
        } else {
            TopComponent view = MultiAnalysisAction.createView(doc);
            view.open();
            view.requestActive();
        }
    }

    @Override
    public Class<MultiProcessingDocument> getItemClass() {
        return MultiProcessingDocument.class;
    }

    @Override
    public Icon getItemIcon(WorkspaceItem<MultiProcessingDocument> doc) {
        return ImageUtilities.loadImageIcon("jdplus/toolkit/desktop/plugin/icons/documents_16x16.png", false);
    }

    @Override
    public Icon getManagerIcon() {
        return ImageUtilities.loadImageIcon("jdplus/toolkit/desktop/plugin/icons/folder-open-document_16x16.png", false);
    }
}
