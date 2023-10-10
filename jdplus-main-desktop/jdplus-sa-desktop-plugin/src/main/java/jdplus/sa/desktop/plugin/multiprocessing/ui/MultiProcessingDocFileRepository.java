/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.desktop.plugin.multiprocessing.ui;

import jdplus.toolkit.base.api.DemetraVersion;
import jdplus.toolkit.desktop.plugin.workspace.AbstractFileItemRepository;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemRepository;
import jdplus.sa.base.api.SaItems;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = WorkspaceItemRepository.class)
public class MultiProcessingDocFileRepository extends AbstractFileItemRepository<MultiProcessingDocument> {

    @Override
    public boolean load(WorkspaceItem<MultiProcessingDocument> item) {
        return loadFile(item, (SaItems o) -> {
            item.setElement(MultiProcessingDocument.open(o));
            item.resetDirty();
        });
    }

    @Override
    public boolean save(WorkspaceItem<MultiProcessingDocument> doc, DemetraVersion version) {
        MultiProcessingDocument element = doc.getElement();
        MultiProcessingDocument saved = element.save();
        // we update the current document
        return storeFile(doc, saved.getInitial(), version, ()
                -> {
            doc.setElement(saved);
            doc.resetDirty();
        });
    }

    @Override
    public boolean delete(WorkspaceItem<MultiProcessingDocument> doc) {
        return deleteFile(doc);
    }

    @Override
    public Class<MultiProcessingDocument> getSupportedType() {
        return MultiProcessingDocument.class;
    }
}
