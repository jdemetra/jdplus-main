/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.workspace;

import jdplus.toolkit.base.api.DemetraVersion;
import jdplus.toolkit.base.workspace.WorkspaceFamily;
import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.function.Consumer;

import jdplus.toolkit.base.workspace.WorkspaceItemDescriptor;
import jdplus.toolkit.base.workspace.file.FileWorkspace;
import org.openide.util.Exceptions;

/**
 *
 * @author Jean Palate
 * @param <D>
 */
public abstract class AbstractFileItemRepository<D> implements WorkspaceItemRepository<D> {

    private static WorkspaceItemDescriptor toFileItem(WorkspaceItem item) {
        WorkspaceFamily family = WorkspaceFamily.of(item.getFamily());
        return new WorkspaceItemDescriptor(
                new WorkspaceItemDescriptor.Key(family, item.getIdentifier()),
                new WorkspaceItemDescriptor.Attributes(item.getDisplayName(),
                        item.isReadOnly(),
                        item.getComments()));
    }

    private static WorkspaceItemDescriptor.Key key(WorkspaceItem item) {
        WorkspaceFamily family = WorkspaceFamily.of(item.getFamily());
        return new WorkspaceItemDescriptor.Key(family, item.getIdentifier());
    }

    private static File decodeFile(WorkspaceItem<?> item) {
        Workspace owner = item.getOwner();
        return owner != null ? FileRepository.decode(owner.getDataSource()) : null;
    }

    protected static <D, R> boolean loadFile(WorkspaceItem<?> item, Consumer<R> onSuccess) {
        File file = decodeFile(item);
        if (file != null) {
            try ( FileWorkspace storage = FileWorkspace.open(file.toPath())) {
                onSuccess.accept((R) storage.load(key(item)));
                return true;
            } catch (IOException | InvalidPathException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }

    protected static <D, R> boolean storeFile(WorkspaceItem<?> item, R value, DemetraVersion version, Runnable onSuccess) {
        File file = decodeFile(item);
        if (file != null) {
            try ( FileWorkspace storage = FileWorkspace.open(file.toPath(), version)) {
                storage.store(toFileItem(item), value);
                onSuccess.run();
                return true;
            } catch (IOException | InvalidPathException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }

    protected static <D, R> boolean deleteFile(WorkspaceItem<?> item) {
        File file = decodeFile(item);
        if (file != null) {
            try ( FileWorkspace storage = FileWorkspace.open(file.toPath())) {
                storage.delete(key(item));
                return true;
            } catch (IOException | InvalidPathException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }
}
