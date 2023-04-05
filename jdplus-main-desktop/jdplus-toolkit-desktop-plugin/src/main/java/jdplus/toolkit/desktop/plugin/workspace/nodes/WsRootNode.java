/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.workspace.nodes;

import jdplus.toolkit.desktop.plugin.properties.NodePropertySetBuilder;
import jdplus.toolkit.desktop.plugin.util.NbUtilities;
import jdplus.toolkit.desktop.plugin.workspace.Workspace;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;

/**
 *
 * @author Jean Palate
 */
public class WsRootNode extends AbstractNode {

    Workspace workspace;

    public WsRootNode(Workspace ws) {
        super(Children.create(new WsRootChildFactory(ws), false));
        workspace = ws;
    }

    @Override
    public String getDisplayName() {
        return workspace.getName();

    }

    @Override
    public Sheet createSheet() {
        Sheet sheet = super.createSheet();
        NodePropertySetBuilder b = new NodePropertySetBuilder().name("Active workspace");
        b.with(String.class).selectConst("Name", workspace.getName()).add();
        sheet.put(b.build());
        sheet.put(NbUtilities.creatDataSourcePropertiesSet(workspace.getDataSource()));
        return sheet;
    }
}
