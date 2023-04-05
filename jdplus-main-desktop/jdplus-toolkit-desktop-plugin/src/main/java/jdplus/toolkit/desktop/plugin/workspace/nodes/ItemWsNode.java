/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.workspace.nodes;

import jdplus.toolkit.desktop.plugin.util.NbUtilities;
import jdplus.toolkit.desktop.plugin.workspace.Workspace;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemManager;
import jdplus.toolkit.base.api.util.Documented;
import jdplus.toolkit.base.api.util.Id;
import jdplus.toolkit.base.api.util.MultiLineNameUtil;
import java.awt.Image;
import java.util.Map;
import javax.swing.Action;
import javax.swing.Icon;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Philippe Charles
 */
public class ItemWsNode extends WsNode {

    public static boolean isItem(Workspace ws, Id id) {
        return ws.searchDocument(id) != null;
    }

    public ItemWsNode(Workspace ws, Id id) {
        super(Children.LEAF, ws, id);
    }

    public WorkspaceItem<?> getItem() {
        return workspace.searchDocument(lookup());
    }

    public <T> WorkspaceItem<T> getItem(Class<T> tclass) {
        return workspace.searchDocument(lookup(), tclass);
    }

    @Override
    public String getDisplayName() {
        WorkspaceItem<?> item = getItem();
        return item == null ? "" : item.getDisplayName();
    }

    @Override
    public String getShortDescription() {
        WorkspaceItem<?> item = getItem();
        if (item != null && item.getComments() != null && !item.getComments().isEmpty()) {
            return MultiLineNameUtil.toHtml(item.getComments());
        } else {
            return null;
        }
    }

    @Override
    public boolean canDestroy() {
        WorkspaceItem<?> item = getItem();
        return item != null && !item.isReadOnly();
    }

    @Override
    public void destroy() {
        WorkspaceItem<?> item = getItem();
        if (item != null) {
            workspace.remove(item);
        }
    }

    @Override
    public Image getIcon(int type) {
        WorkspaceItemManager manager = WorkspaceFactory.getInstance().getManager(lookup().parent());
        Icon result = manager.getItemIcon(getItem());
        return result != null ? ImageUtilities.icon2Image(result) : super.getIcon(type);
    }

    @Override
    public Action getPreferredAction() {
        WorkspaceItemManager<?> manager = WorkspaceFactory.getInstance().getManager(lookup().parent());
        return manager != null ? manager.getPreferredItemAction(lookup()) : null;
    }

    @Override
    public Sheet createSheet() {
        Sheet sheet = super.createSheet();
        WorkspaceItem<Documented> doc = workspace.searchDocument(lookup(), Documented.class);
        if (doc == null) {
            return sheet;
        }
        final Map<String, String> metaData = doc.getElement().getMetadata();
        if (metaData.isEmpty()) {
            return sheet;
        }

        Sheet.Set info = NbUtilities.createMetadataPropertiesSet(metaData);
        sheet.put(info);
        return sheet;
    }
}
