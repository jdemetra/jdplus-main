/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.sql.desktop.plugin.jdbc;

import jdplus.toolkit.desktop.plugin.beans.BeanEditor;
import jdplus.toolkit.desktop.plugin.nodes.SingleNodeAction;
import jdplus.toolkit.desktop.plugin.tsproviders.DataSourceManager;
import jdplus.sql.base.api.jdbc.JdbcBean;
import jdplus.sql.base.api.jdbc.JdbcProvider;
import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

import java.beans.IntrospectionException;
import java.util.logging.Level;

@ActionID(category = "Edit", id = OpenJdbcDataSource.ID)
@ActionRegistration(displayName = "#CTL_OpenJdbcDataSource", lazy = false)
@ActionReferences({
        @ActionReference(path = "Databases/Explorer/Connection/Actions", position = 1, separatorAfter = 10),
        @ActionReference(path = "Databases/Explorer/Table/Actions", position = 1, separatorAfter = 10),
        @ActionReference(path = "Databases/Explorer/View/Actions", position = 1, separatorAfter = 10)
})
@Messages("CTL_OpenJdbcDataSource=Open as JDemetra+ DataSource")
@lombok.extern.java.Log
public final class OpenJdbcDataSource extends SingleNodeAction<Node> {

    @ClassNameConstant
    public static final String ID = "jdplus.sql.desktop.plugin.jdbc.OpenJdbcDataSource";

    private final JdbcProvider provider;

    public OpenJdbcDataSource() {
        super(Node.class);
        this.provider = Lookup.getDefault().lookup(JdbcProvider.class);
    }

    @Override
    protected void performAction(Node activatedNode) {
        JdbcBean bean = provider.newBean();
        preFillBean(bean, activatedNode);
        BeanEditor editor = DataSourceManager.get().getBeanEditor(provider.getSource(), "Open data source");
        try {
            if (editor.editBean(bean)) {
                provider.open(provider.encodeBean(bean));
            }
        } catch (IntrospectionException ex) {
            log.log(Level.SEVERE, "While opening", ex);
        }
    }

    @Override
    protected boolean enable(Node activatedNode) {
        return DbExplorerUtil.findConnection(activatedNode)
                .filter(DbExplorerUtil::isConnected)
                .isPresent();
    }

    @Override
    public String getName() {
        return Bundle.CTL_OpenJdbcDataSource();
    }

    static void preFillBean(JdbcBean bean, Node node) {
        DbExplorerUtil.findConnection(node)
                .ifPresent(o -> bean.setDatabase(o.getDisplayName()));
        if (DbExplorerUtil.isTableOrView(node)) {
            bean.setTable(node.getName());
        }
    }
}
