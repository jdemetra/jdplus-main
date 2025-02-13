/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.desktop.plugin.regarima.documents;

import jdplus.toolkit.desktop.plugin.workspace.AbstractWorkspaceTsItemManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemManager;
import jdplus.x13.base.api.regarima.RegArimaSpec;
import jdplus.toolkit.base.api.util.Id;
import jdplus.toolkit.base.api.util.LinearId;
import jdplus.x13.base.core.x13.regarima.RegArimaDocument;
import jdplus.x13.base.workspace.X13Handlers;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = WorkspaceItemManager.class,
        position = 1000)
public class RegArimaDocumentManager extends AbstractWorkspaceTsItemManager<RegArimaSpec, RegArimaDocument> {

    public static final LinearId ID = new LinearId(RegArimaSpec.FAMILY, "documents", RegArimaSpec.METHOD);
    public static final String PATH = "regarima.doc";
    public static final String ITEMPATH = "regarima.doc.item";
    public static final String CONTEXTPATH = "regarima.doc.context";

    @Override
    protected String getItemPrefix() {
        return X13Handlers.REGARIMADOC_PREFIX;
    }

    @Override
    public Id getId() {
        return ID;
    }

    @Override
    public RegArimaDocument createNewObject() {
        return new RegArimaDocument();
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
    public Class<RegArimaDocument> getItemClass() {
        return RegArimaDocument.class;
    }

}
