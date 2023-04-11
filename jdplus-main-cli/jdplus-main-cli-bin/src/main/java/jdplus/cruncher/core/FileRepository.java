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
package jdplus.cruncher.core;

import jdplus.sa.base.api.SaItems;
import jdplus.sa.base.workspace.SaHandlers;
import jdplus.toolkit.base.api.timeseries.calendars.CalendarDefinition;
import jdplus.toolkit.base.api.timeseries.calendars.CalendarManager;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.api.timeseries.regression.TsDataSuppliers;
import jdplus.toolkit.base.api.util.NameManager;
import jdplus.toolkit.base.workspace.WorkspaceFamily;
import jdplus.toolkit.base.workspace.WorkspaceItemDescriptor;
import jdplus.toolkit.base.workspace.file.FileWorkspace;
import jdplus.toolkit.base.api.util.Paths;
import internal.toolkit.base.workspace.file.GenericHandlers;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class FileRepository {

    public void storeSaProcessing(FileWorkspace ws, WorkspaceItemDescriptor item, SaItems processing) throws IOException {
        makeSaProcessingBackup(ws, item);
        ws.store(item, processing);
    }

    public Map<WorkspaceItemDescriptor, SaItems> loadAllSaProcessing(FileWorkspace ws, ModellingContext context) throws IOException {
        Map<WorkspaceItemDescriptor, SaItems> result = new LinkedHashMap<>();
        for (WorkspaceItemDescriptor item : ws.getItems()) {
            WorkspaceFamily family = item.getKey().getFamily();
            if (family.equals(SaHandlers.SA_MULTI)) {
                result.put(item, (SaItems) ws.load(item.getKey()));
            }
        }
        return result;
    }

    private void makeSaProcessingBackup(FileWorkspace ws, WorkspaceItemDescriptor item) throws IOException {
        Path source = ws.getFile(item);
        Path target = source.getParent().resolve(Paths.changeExtension(source.getFileName().toString(), "bak"));
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

//    public Map<WorkspaceItemDescriptor, CalendarManager> loadAllCalendars(FileWorkspace ws, ModellingContext context) throws IOException {
//        Map<WorkspaceItemDescriptor, CalendarManager> result = new LinkedHashMap<>();
//        for (WorkspaceItemDescriptor item : ws.getItems()) {
//            WorkspaceFamily family = item.getKey().getFamily();
//            if (family.equals(demetra.workspace.WorkspaceFamily.UTIL_CAL)) {
//                CalendarManager calendar = (CalendarManager) ws.load(item.getKey());
//                result.put(item, calendar);
//                applyCalendars(context, calendar);
//            }
//        }
//        return result;
//    }
//
//    public Map<WorkspaceItemDescriptor, TsDataSuppliers> loadAllVariables(FileWorkspace ws, ModellingContext context) throws IOException {
//        Map<WorkspaceItemDescriptor, TsDataSuppliers> result = new LinkedHashMap<>();
//        for (WorkspaceItemDescriptor item : ws.getItems()) {
//            WorkspaceFamily family = item.getKey().getFamily();
//            if (family.equals(demetra.workspace.WorkspaceFamily.UTIL_VAR)) {
//                TsDataSuppliers vars = (TsDataSuppliers) ws.load(item.getKey());
//                result.put(item, vars);
//                applyVariables(context, item.getAttributes().getLabel(), vars);
//            }
//        }
//        return result;
//    }
//
//    private void applyVariables(ModellingContext context, String id, TsDataSuppliers value) {
//        NameManager<TsDataSuppliers> manager = context.getTsVariableManagers();
//        manager.set(id, value);
//        manager.resetDirty();
//    }
//
//    private void applyCalendars(ModellingContext context, CalendarManager source) {
//        CalendarManager target = context.getCalendars();
//        for (String s : source.getNames()) {
//            if (!target.contains(s)) {
//                CalendarDefinition cal = source.get(s);
//                target.set(s, cal);
//            }
//        }
//        target.resetDirty();
//    }
}
