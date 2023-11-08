/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package jdplus.toolkit.desktop.plugin.workspace;

import jdplus.toolkit.base.api.DemetraVersion;
import jdplus.toolkit.base.api.design.ExtensionPoint;
import jdplus.toolkit.base.api.timeseries.calendars.CalendarDefinition;
import jdplus.toolkit.base.api.timeseries.calendars.CalendarManager;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.api.timeseries.regression.TsDataSuppliers;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.base.api.util.NameManager;
import java.util.Collection;
import java.util.List;
import nbbrd.service.ServiceDefinition;

/**
 *
 * @author Jean Palate
 * @since 1.0.0
 */
@ExtensionPoint
@ServiceDefinition
public interface WorkspaceRepository {

    Collection<Class> getSupportedTypes();

    void initialize();

    String getName();

    default Object getProperties() {
        return null;
    }

    default void setProperties() {
    }

    Workspace open();

    boolean saveAs(Workspace ws, DemetraVersion version);

    boolean load(Workspace ws);

    boolean save(Workspace ws, DemetraVersion version, boolean force);

    boolean delete(Workspace ws);

    <D> boolean canHandleItem(Class<D> dclass);

    boolean loadItem(WorkspaceItem<?> item);

    boolean saveItem(WorkspaceItem<?> item, DemetraVersion version);

    boolean deleteItem(WorkspaceItem<?> item);

    void close(Workspace ws_);

    DataSource getDefaultDataSource();
    
    public static void updateModellingContext(Workspace ws){
        ModellingContext ncontext=ws.getContext();
        CalendarManager cm = ncontext.getCalendars();
        NameManager<TsDataSuppliers> vm = ncontext.getTsVariableManagers();
        List<WorkspaceItem<CalendarDefinition>> cals = ws.searchDocuments(CalendarDefinition.class);
        for (WorkspaceItem<CalendarDefinition> item : cals){
            if (item.getStatus() != WorkspaceItem.Status.System){
                cm.set(item.getDisplayName(), item.getElement());
            }
        }
        cm.resetDirty();
        List<WorkspaceItem<TsDataSuppliers>> vars = ws.searchDocuments(TsDataSuppliers.class);
        for (WorkspaceItem<TsDataSuppliers> item : vars){
            if (item.getStatus() != WorkspaceItem.Status.System){
                vm.set(item.getDisplayName(), item.getElement());
             }
        }
        vm.resetDirty();
    }
}
