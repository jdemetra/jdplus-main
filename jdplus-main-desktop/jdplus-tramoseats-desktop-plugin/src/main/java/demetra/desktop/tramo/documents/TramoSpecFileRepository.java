/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.desktop.tramo.documents;

import jdplus.toolkit.base.api.DemetraVersion;
import demetra.desktop.workspace.AbstractFileItemRepository;
import demetra.desktop.workspace.WorkspaceItem;
import demetra.desktop.workspace.WorkspaceItemRepository;
import jdplus.tramoseats.base.api.tramo.TramoSpec;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = WorkspaceItemRepository.class)
public class TramoSpecFileRepository extends AbstractFileItemRepository<TramoSpec> {

    @Override
    public boolean load(WorkspaceItem<TramoSpec> item) {
        return loadFile(item, (TramoSpec o) -> {
            item.setElement(o);
            item.resetDirty();
        });
    }

    @Override
    public boolean save(WorkspaceItem<TramoSpec> item, DemetraVersion version) {
        return storeFile(item, item.getElement(), version, item::resetDirty);
    }

    @Override
    public boolean delete(WorkspaceItem<TramoSpec> doc) {
        return deleteFile(doc);
    }

    @Override
    public Class<TramoSpec> getSupportedType() {
        return TramoSpec.class;
    }
}
