/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.workspace.nodes;

import jdplus.toolkit.desktop.plugin.workspace.Workspace;
import jdplus.toolkit.base.api.util.Id;
import java.awt.Image;

/**
 *
 * @author Philippe Charles
 */
public class DummyWsNode extends WsNode {

    public DummyWsNode(Workspace ws, Id id) {
        super(createItems(ws, id), ws, id);
    }

    @Override
    public Image getIcon(int type) {
        return super.getIcon(type);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return super.getOpenedIcon(type);
    }
}
