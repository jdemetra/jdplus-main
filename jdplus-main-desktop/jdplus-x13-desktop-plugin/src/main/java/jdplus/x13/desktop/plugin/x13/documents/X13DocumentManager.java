/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.desktop.plugin.x13.documents;

import jdplus.toolkit.desktop.plugin.workspace.AbstractWorkspaceTsItemManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemManager;
import jdplus.toolkit.base.api.util.Id;
import jdplus.toolkit.base.api.util.LinearId;
import jdplus.x13.base.api.x13.X13Spec;
import jdplus.x13.base.core.x13.X13Document;
import jdplus.x13.base.workspace.X13Handlers;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = WorkspaceItemManager.class,
        position = 1000)
public class X13DocumentManager extends AbstractWorkspaceTsItemManager<X13Spec, X13Document> {
 
    public static final LinearId ID = new LinearId(X13Spec.FAMILY, "documents", X13Spec.METHOD);
    public static final String PATH = "x13.doc";
    public static final String ITEMPATH = "x13.doc.item";
    public static final String CONTEXTPATH = "x13.doc.context";

    @Override
    protected String getItemPrefix() {
        return X13Handlers.X13DOC_PREFIX;
    }

    @Override
    public Id getId() {
        return ID;
    }

    @Override
    public X13Document createNewObject() {
        return new X13Document();
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
    public Class<X13Document> getItemClass() {
        return X13Document.class;
    }

}
