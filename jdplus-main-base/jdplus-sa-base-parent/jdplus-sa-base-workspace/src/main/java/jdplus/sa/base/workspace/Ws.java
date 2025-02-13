/*
 * Copyright 2022 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.sa.base.workspace;

import jdplus.toolkit.base.api.DemetraVersion;
import jdplus.sa.base.api.SaItems;
import jdplus.toolkit.base.api.timeseries.StaticTsDataSupplier;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.calendars.CalendarDefinition;
import jdplus.toolkit.base.api.timeseries.calendars.CalendarManager;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.api.timeseries.regression.TsDataSuppliers;
import jdplus.toolkit.base.api.util.Paths;
import jdplus.toolkit.base.workspace.WorkspaceFamily;
import jdplus.toolkit.base.workspace.WorkspaceItemDescriptor;
import jdplus.toolkit.base.workspace.WorkspaceUtility;
import jdplus.toolkit.base.workspace.file.FileWorkspace;
import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.workspace.file.spi.FamilyHandler;

/**
 *
 * @author PALATEJ
 */
public class Ws {

    private final String source;
    private ModellingContext context;
    private final List<MultiProcessing> multiProcessing = new ArrayList<>();

    public Ws() {
        this.source = null;
        this.context = new ModellingContext();
    }

    public Ws(final String source, final ModellingContext context) {
        this.source = source;
        this.context = context;
    }

    public static Ws create(ModellingContext context) {
        return new Ws(null, context);
    }

    public static Ws open(String fileName) throws IOException {
        File file = Path.of(fileName).toFile();
        FileWorkspace fws = FileWorkspace.open(file.toPath(), DemetraVersion.JD3);
        ModellingContext context = WorkspaceUtility.context(fws, false);
        Ws ws = new Ws(fileName, context);

        List<WorkspaceItemDescriptor> sa = WorkspaceUtility.select(fws, SaHandlers.SA_MULTI);
        for (WorkspaceItemDescriptor s : sa) {
            SaItems mp = (SaItems) fws.load(s.getKey());
            ws.multiProcessing.add(MultiProcessing.of(s.getAttributes().getLabel(), mp));
        }
        return ws;
    }

    @Deprecated
    public ModellingContext context() {
        return context;
    }

    public boolean save(String v) {
        if (source == null) {
            return false;
        }
        return saveAs(source, v, false);
    }

    public boolean saveAs(String sfile, String v, boolean failIfExists) {
        DemetraVersion version = DemetraVersion.JD3;
        if (v != null && v.equalsIgnoreCase("jd2")) {
            version = DemetraVersion.JD2;
        }
        File file = Path.of(sfile).toFile();

        boolean exist = file.exists();
        if (exist && failIfExists) {
            return false;
        }
        try (FileWorkspace storage = FileWorkspace.create(file.toPath(), version)) {
            storage.setName(Paths.changeExtension(file.getName(), null));
            storeCalendar(storage, context.getCalendars());
            // store variables
            String[] vars = context.getTsVariableManagers().getNames();
            for (int i = 0; i < vars.length; ++i) {
                  StringBuilder name=new StringBuilder().append(FamilyHandler.VARS_PREFIX).append('-').append(i+1);
              WorkspaceItemDescriptor cur
                        = new WorkspaceItemDescriptor(
                                new WorkspaceItemDescriptor.Key(WorkspaceFamily.UTIL_VAR, name.toString()),
                                new WorkspaceItemDescriptor.Attributes(vars[i], false, null));
                storage.store(cur, context.getTsVariables(vars[i]));
            }
            // store multi-processing
            int j = 1;
            for (MultiProcessing mp : multiProcessing) {
                StringBuilder name=new StringBuilder().append(SaHandlers.PREFIX).append('-').append(j++);
                WorkspaceItemDescriptor cur
                        = new WorkspaceItemDescriptor(
                                new WorkspaceItemDescriptor.Key(SaHandlers.SA_MULTI, name.toString()),
                                new WorkspaceItemDescriptor.Attributes(mp.getName(), false, null));
                // build the multiProcessing
                Map<String, String> meta = new HashMap<>(mp.getMetaData());
                meta.put("@timestamp", LocalDateTime.now(Clock.systemDefaultZone()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                meta.putAll(mp.getMetaData());
                SaItems items = SaItems.builder()
                        .meta(meta)
                        .items(mp.getItems())
                        .name(mp.getName())
                        .build();
                storage.store(cur, items);
            }

        } catch (IOException | InvalidPathException ex) {
            return false;
        }
        return true;
    }

//    public static Path getRootFolder(Path indexFile) throws IOException {
//        Path parent = indexFile.toAbsolutePath().getParent();
//        if (parent == null) {
//            throw new IOException();
//        }
//        return parent.resolve(Paths.changeExtension(indexFile.getFileName().toString(), null));
//    }
//    
    private static final WorkspaceItemDescriptor CAL_ID
            = new WorkspaceItemDescriptor(
                    new WorkspaceItemDescriptor.Key(WorkspaceFamily.UTIL_CAL, FamilyHandler.CALENDAR_REPO),
                    new WorkspaceItemDescriptor.Attributes("Calendars", false, null));

    private static void storeCalendar(FileWorkspace storage, CalendarManager value) throws IOException {
        storage.store(CAL_ID, value);
    }

    public MultiProcessing getMultiProcessing(int idx) {
        return multiProcessing.get(idx);
    }

    public int getMultiProcessingCount() {
        return multiProcessing.size();
    }

    public MultiProcessing newMultiProcessing(String name) {
        MultiProcessing n = new MultiProcessing(name);
        multiProcessing.add(n);
        return n;
    }

    public ModellingContext getContext() {
        return (this.context);
    }

    public void setContext(ModellingContext context) {
        this.context = context;
    }

    public void addVariable(String family, String name, TsData data) {
        TsDataSuppliers tsfamily = context.getTsVariables(family);
        if (tsfamily == null) {
            tsfamily = new TsDataSuppliers();
            context.getTsVariableManagers().set(name, tsfamily);
        }
        tsfamily.set(name, new StaticTsDataSupplier(data));
    }

    public void addCalendar(String name, CalendarDefinition calendar) {
        context.getCalendars().set(name, calendar);
    }

    public void computeAll() {
        multiProcessing.stream().forEach(p -> p.compute(context));
    }

    public void compute(String name) {
        multiProcessing.stream().filter(p -> p.getName().equals(name)).forEach(q -> q.compute(context));
    }

    public void processAll() {
        multiProcessing.stream().forEach(p -> p.process(context));
    }

    public void process(String name) {
        multiProcessing.stream().filter(p -> p.getName().equals(name)).forEach(q -> q.process(context));
    }

    public void add(MultiProcessing sap) {
        multiProcessing.add(sap);
    }

    public void refreshAll(String policy, TsDomain domain, String info) {
        multiProcessing.replaceAll(sap -> sap.refresh(policy, domain, info));
    }
    
    public Ws makeCopy(){
        Ws copy=new Ws();
        copy.context=context; // ? perhaps should it be changed  
        copy.multiProcessing
                .addAll(multiProcessing.stream()
                        .map(p->p.makeCopy())
                        .toList());
        return copy;
     }

//    public boolean save(String fileName) {
//        File file = new File(fileName);
//        try {
//            try (FileWorkspace fws = FileWorkspace.create(file.toPath(), FileFormat.GENERIC)) {
//                fws.setName(Paths.getBaseName(fileName));
//                for (MultiProcessing p : multiProcessing) {
//                    WorkspaceItem cur = WorkspaceItem.builder()
//                            .family(WorkspaceFamily.SA_MULTI)
//                            .id(p.getName())
//                            .label(p.getName())
//                            .build();
//                    SaProcessingType sa = p.toType();
//                    //sa.getMetaData().put(SaProcessingType.TIMESTAMP, LocalDate.now().toString());
//                    fws.store(cur, sa);
//                }
//                String[] names = context.getTsVariableManagers().getNames();
//                for (int i = 0; i < names.length; ++i) {
//                    WorkspaceItem cur = WorkspaceItem.builder()
//                            .family(WorkspaceFamily.UTIL_VAR)
//                            .id(names[i])
//                            .label(names[i])
//                            .build();
//                    fws.store(cur, context.getTsVariables(names[i]));
//                }
//                WorkspaceItem cal = WorkspaceItem.builder()
//                        .family(WorkspaceFamily.UTIL_CAL)
//                        .id("Calendars")
//                        .label("Calendars")
//                        .build();
//                fws.store(cal, context.getGregorianCalendars());
//            }
//            return true;
//        } catch (IOException err) {
//            return false;
//        }
//    }
}
