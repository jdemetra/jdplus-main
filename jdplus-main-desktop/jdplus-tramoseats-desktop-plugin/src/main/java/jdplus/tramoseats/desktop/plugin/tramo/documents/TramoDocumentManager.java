/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.desktop.plugin.tramo.documents;

import jdplus.tramoseats.base.core.tramo.TramoDocument;
import jdplus.toolkit.desktop.plugin.workspace.AbstractWorkspaceTsItemManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemManager;
import jdplus.tramoseats.base.api.tramo.TramoSpec;
import jdplus.toolkit.base.api.util.Id;
import jdplus.toolkit.base.api.util.LinearId;
import jdplus.tramoseats.base.workspace.TramoSeatsHandlers;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = WorkspaceItemManager.class,
        position = 500)
public class TramoDocumentManager extends AbstractWorkspaceTsItemManager<TramoSpec, TramoDocument> {


    public static final LinearId ID = new LinearId(TramoSpec.FAMILY, "documents", TramoSpec.METHOD);
    public static final String PATH = "tramo.doc";
    public static final String ITEMPATH = "tramo.doc.item";
    public static final String CONTEXTPATH = "tramo.doc.context";

    @Override
    protected String getItemPrefix() {
        return TramoSeatsHandlers.TRAMODOC_PREFIX;
    }

    @Override
    public Id getId() {
        return ID;
    }

    @Override
    public TramoDocument createNewObject() {
        return new TramoDocument();
    }

    @Override
    public ItemType getItemType() {
        return ItemType.Doc;
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
    public Class<TramoDocument> getItemClass() {
        return TramoDocument.class;
    }

}
