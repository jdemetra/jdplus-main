/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.desktop.plugin.regarima.documents;

import jdplus.toolkit.base.api.DemetraVersion;
import jdplus.toolkit.desktop.plugin.workspace.AbstractFileItemRepository;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemRepository;
import jdplus.x13.base.api.regarima.RegArimaSpec;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = WorkspaceItemRepository.class)
public class RegArimaSpecFileRepository extends AbstractFileItemRepository<RegArimaSpec> {

    @Override
    public boolean load(WorkspaceItem<RegArimaSpec> item) {
        return loadFile(item, (RegArimaSpec o) -> {
            item.setElement(o);
            item.resetDirty();
        });
    }

    @Override
    public boolean save(WorkspaceItem<RegArimaSpec> item, DemetraVersion version) {
        return storeFile(item, item.getElement(), version, item::resetDirty);
    }

    @Override
    public boolean delete(WorkspaceItem<RegArimaSpec> doc) {
        return deleteFile(doc);
    }

    @Override
    public Class<RegArimaSpec> getSupportedType() {
        return RegArimaSpec.class;
    }
}
