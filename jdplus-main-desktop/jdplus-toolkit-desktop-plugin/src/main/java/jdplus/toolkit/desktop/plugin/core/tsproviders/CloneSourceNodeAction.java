/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.core.tsproviders;

import jdplus.toolkit.desktop.plugin.TsManager;
import jdplus.toolkit.desktop.plugin.actions.AbilityNodeAction;
import jdplus.toolkit.desktop.plugin.tsproviders.DataSourceManager;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.base.tsp.DataSourceLoader;
import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

import java.util.stream.Stream;

@ActionID(category = "Edit", id = CloneSourceNodeAction.ID)
@ActionRegistration(displayName = "#CTL_CloneSourceAction", lazy = false)
@Messages("CTL_CloneSourceAction=Clone")
public final class CloneSourceNodeAction extends AbilityNodeAction<DataSource> {

    @ClassNameConstant
    public static final String ID = "jdplus.toolkit.desktop.plugin.core.tsproviders.CloneSourceNodeAction";

    public CloneSourceNodeAction() {
        super(DataSource.class, true);
    }

    @Override
    protected void performAction(Stream<DataSource> items) {
        items.forEach(CloneSourceNodeAction::clone);
    }

    @Override
    protected boolean enable(Stream<DataSource> items) {
        return items.anyMatch(item -> TsManager.get().getProvider(DataSourceLoader.class, item).isPresent());
    }

    private static DataSource clone(DataSource dataSource) {
        DataSourceLoader loader = TsManager.get().getProvider(DataSourceLoader.class, dataSource).orElseThrow();
        final Object bean = loader.decodeBean(dataSource);
        if (DataSourceManager.get().getBeanEditor(loader.getSource(), "Clone data source").editBean(bean, Exceptions::printStackTrace)) {
            DataSource newDataSource = loader.encodeBean(bean);
            return loader.open(newDataSource) ? newDataSource : null;
        }
        return null;
    }

    @Override
    public String getName() {
        return Bundle.CTL_CloneSourceAction();
    }
}
