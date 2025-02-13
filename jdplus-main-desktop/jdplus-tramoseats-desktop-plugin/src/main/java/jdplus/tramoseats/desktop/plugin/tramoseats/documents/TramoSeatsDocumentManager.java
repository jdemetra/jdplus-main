/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.desktop.plugin.tramoseats.documents;

import jdplus.toolkit.desktop.plugin.workspace.AbstractWorkspaceTsItemManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemManager;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsSpec;
import jdplus.toolkit.base.api.util.Id;
import jdplus.toolkit.base.api.util.LinearId;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsDocument;
import jdplus.tramoseats.base.workspace.TramoSeatsHandlers;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = WorkspaceItemManager.class,
        position = 500)
public class TramoSeatsDocumentManager extends AbstractWorkspaceTsItemManager<TramoSeatsSpec, TramoSeatsDocument> {


    public static final LinearId ID = new LinearId(TramoSeatsSpec.FAMILY, "documents", TramoSeatsSpec.METHOD);
    public static final String PATH = "tramoseats.doc";
    public static final String ITEMPATH = "tramoseats.doc.item";
    public static final String CONTEXTPATH = "tramoseats.doc.context";

    @Override
    protected String getItemPrefix() {
        return TramoSeatsHandlers.TRAMOSEATSDOC_PREFIX;
    }

    @Override
    public Id getId() {
        return ID;
    }

    @Override
    public TramoSeatsDocument createNewObject() {
        return new TramoSeatsDocument();
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
    public Class<TramoSeatsDocument> getItemClass() {
        return TramoSeatsDocument.class;
    }

}
