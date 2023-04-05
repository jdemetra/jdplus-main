/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.variables;

import jdplus.toolkit.base.api.DemetraVersion;
import jdplus.toolkit.desktop.plugin.workspace.AbstractFileItemRepository;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemRepository;
import jdplus.toolkit.base.api.timeseries.regression.TsDataSuppliers;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Jean Palate
 */
@ServiceProvider(service = WorkspaceItemRepository.class)
public class VariablesFileRepository extends AbstractFileItemRepository<TsDataSuppliers> {

    @Override
    public boolean load(WorkspaceItem<TsDataSuppliers> item) {
        return loadFile(item, (TsDataSuppliers o) -> {
            item.setElement(o);
            item.resetDirty();
        });
    }

    @Override
    public boolean save(WorkspaceItem<TsDataSuppliers> item, DemetraVersion version) {
        return storeFile(item, item.getElement(), version, item::resetDirty);
    }

    @Override
    public boolean delete(WorkspaceItem<TsDataSuppliers> doc) {
        return deleteFile(doc);
    }

    @Override
    public Class<TsDataSuppliers> getSupportedType() {
        return TsDataSuppliers.class;
    }
    
}
