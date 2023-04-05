/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.desktop.plugin.x13.documents;

import jdplus.toolkit.base.api.DemetraVersion;
import jdplus.toolkit.desktop.plugin.workspace.AbstractFileItemRepository;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemRepository;
import jdplus.x13.base.api.x13.X13Spec;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = WorkspaceItemRepository.class)
public class X13SpecFileRepository extends AbstractFileItemRepository<X13Spec> {

    @Override
    public boolean load(WorkspaceItem<X13Spec> item) {
        return loadFile(item, (X13Spec o) -> {
            item.setElement(o);
            item.resetDirty();
        });
    }

    @Override
    public boolean save(WorkspaceItem<X13Spec> item, DemetraVersion version) {
        return storeFile(item, item.getElement(), version, item::resetDirty);
    }

    @Override
    public boolean delete(WorkspaceItem<X13Spec> doc) {
        return deleteFile(doc);
    }

    @Override
    public Class<X13Spec> getSupportedType() {
        return X13Spec.class;
    }
}
