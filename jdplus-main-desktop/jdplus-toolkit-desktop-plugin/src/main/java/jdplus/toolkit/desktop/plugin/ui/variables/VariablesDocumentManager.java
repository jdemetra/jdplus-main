/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.variables;


import jdplus.toolkit.desktop.plugin.workspace.AbstractWorkspaceItemManager;
import jdplus.toolkit.desktop.plugin.workspace.Workspace;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemManager;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.api.timeseries.regression.TsDataSuppliers;
import jdplus.toolkit.base.api.util.Id;
import jdplus.toolkit.base.api.util.LinearId;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import jdplus.toolkit.base.workspace.file.spi.FamilyHandler;
import org.openide.util.lookup.ServiceProvider;

/**
 * 
 * @author Jean Palate
 */
@ServiceProvider(service = WorkspaceItemManager.class, position = 9910)
public class VariablesDocumentManager extends AbstractWorkspaceItemManager<TsDataSuppliers> {

    public static final LinearId ID = new LinearId("Utilities", "Variables");
    public static final String PATH = "variables";
    public static final String ITEMPATH = "variables.item";
    public static final String CONTEXTPATH = "variables.context";
    
    @Override
   public WorkspaceItem<TsDataSuppliers> create(Workspace ws) {
        WorkspaceItem<TsDataSuppliers> nvars = super.create(ws);
        ModellingContext.getActiveContext().getTsVariableManagers().set(nvars.getDisplayName(), nvars.getElement());
        return nvars;
   }

    @Override
    protected String getItemPrefix() {
        return FamilyHandler.VARS_PREFIX;
    }

    @Override
    public Id getId() {
        return ID;
    }

    @Override
    public TsDataSuppliers createNewObject() {
        return new TsDataSuppliers();
    }

    @Override
    public WorkspaceItemManager.ItemType getItemType() {
        return WorkspaceItemManager.ItemType.Doc;
    }

    @Override
    public String getActionsPath() {
        return PATH;
    }

    @Override
    public WorkspaceItemManager.Status getStatus() {
        return WorkspaceItemManager.Status.User;
    }

    @Override
    public Action getPreferredItemAction(final Id child) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WorkspaceItem<TsDataSuppliers> doc = (WorkspaceItem<TsDataSuppliers>) WorkspaceFactory.getInstance().getActiveWorkspace().searchDocument(child);
                if (doc != null) {
                    openDocument(doc);
                }
            }
        };
    }

    public void openDocument(WorkspaceItem<TsDataSuppliers> doc) {
        if (doc.isOpen()) {
            doc.getView().requestActive();
        } else {
            VariablesTopComponent view = new VariablesTopComponent(doc);
            view.open();
            view.requestActive();
        }
    }

    @Override
    public List<WorkspaceItem<TsDataSuppliers>> getDefaultItems() {
//        List<WorkspaceItem<TsVariables>> result = new ArrayList<>();
//        NameManager<TsVariables> manager = ProcessingContext.getActiveContext().getTsVariableManagers();
//        for (String o : manager.getNames()) {
//            result.add(systemItem(o, manager.get(o)));
//        }
//        return result;
        return Collections.emptyList();
    }

    @Override
    public Class<TsDataSuppliers> getItemClass() {
        return TsDataSuppliers.class;
    }

    @Override
    public Icon getManagerIcon() {
        return null;
    }

    @Override
    public Icon getItemIcon(WorkspaceItem<TsDataSuppliers> doc) {
        return null;
    }

    public static WorkspaceItem<TsDataSuppliers> systemItem(String name, TsDataSuppliers p) {
        return WorkspaceItem.system(ID, name, p);
    }

    public static WorkspaceItem<TsDataSuppliers> newItem(String name, TsDataSuppliers p) {
        return WorkspaceItem.newItem(ID, name, p);
    }

    @Override
    public boolean isAutoLoad() {
        return true;
    }
}
