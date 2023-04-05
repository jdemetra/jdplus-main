/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.workspace.nodes;

import jdplus.toolkit.desktop.plugin.workspace.Workspace;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.base.api.util.Id;
import java.util.Arrays;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author Philippe Charles
 */
public class WsRootChildFactory extends ChildFactory<Id> {

    private final Workspace workspace;

    public WsRootChildFactory(Workspace ws) {
        workspace = ws;
    }

    @Override
    protected Node createNodeForKey(Id id) {
        if (ManagerWsNode.isManager(id)) {
            return new ManagerWsNode(workspace, id);
        } else {
            return new DummyWsNode(workspace, id);
        }
    }

    @Override
    protected boolean createKeys(List<Id> list) {
        list.addAll(Arrays.asList(WorkspaceFactory.getInstance().getTree().roots()));
        return true;
    }

}
