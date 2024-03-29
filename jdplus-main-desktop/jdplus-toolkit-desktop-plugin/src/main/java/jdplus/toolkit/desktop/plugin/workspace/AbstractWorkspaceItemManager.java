/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.workspace;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author Jean Palate
 * @param <D>
 */
public abstract class AbstractWorkspaceItemManager<D> implements WorkspaceItemManager<D> {
    
    protected abstract String getItemPrefix();

    protected boolean isUsed(String name) {
        if (null != WorkspaceFactory.getInstance().getActiveWorkspace().searchDocument(getId(), name)) {
            return true;
        }
        if (null != WorkspaceFactory.getInstance().getActiveWorkspace().searchDocumentByName(getId(), name)) {
            return true;
        }
        return false;
    }

    public String getNextItemName() {
        return getNextItemName(null);
    }

    @Override
    public String getNextItemName(final String pname) {
        String name = pname;
        int id = 1;
        while (name == null || isUsed(name)) {
            StringBuilder builder = new StringBuilder();
            builder.append(getItemPrefix());
            builder.append("-").append(id++);
            name = builder.toString();
        }
        return name;
    }

    @Override
    public WorkspaceItem<D> create(Workspace ws) {
        D newObject = createNewObject();
        if (newObject == null)
            return null;
        WorkspaceItem<D> item = WorkspaceItem.newItem(getId(), getNextItemName(), newObject);
        if (ws != null) {
            ws.add(item);
        }
        return item;
    }

    @Override
    public List<WorkspaceItem<D>> getDefaultItems() {
        return Collections.emptyList();
    }

    @Override
    public boolean isAutoLoad(){
        return false;
    }
    
    protected void cloneItem(WorkspaceItem<D> doc) {
    }
    
}
