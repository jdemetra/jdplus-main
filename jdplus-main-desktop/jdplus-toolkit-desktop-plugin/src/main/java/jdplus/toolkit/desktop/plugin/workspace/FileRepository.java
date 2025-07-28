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
import jdplus.toolkit.base.api.timeseries.calendars.CalendarDefinition;
import jdplus.toolkit.base.api.timeseries.calendars.CalendarManager;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.base.api.util.LinearId;
import jdplus.toolkit.base.api.util.Paths;
import jdplus.toolkit.base.workspace.WorkspaceFamily;
import jdplus.toolkit.base.workspace.WorkspaceItemDescriptor;
import jdplus.toolkit.base.workspace.file.FileWorkspace;
import ec.util.desktop.Desktop;
import ec.util.desktop.Desktop.KnownFolder;
import ec.util.desktop.DesktopManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.function.UnaryOperator;

import nbbrd.io.sys.SystemProperties;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 * @since 1.0.0
 */
@ServiceProvider(service = WorkspaceRepository.class, position = 10)
public class FileRepository extends AbstractWorkspaceRepository implements LookupListener {

    public static final String NAME = "File", FILENAME = "fileName", VERSION = "20120925";

    private final Lookup.Result<WorkspaceItemRepository> repositoryLookup;
    private final FileChooserBuilder wsFileChooser;

    public FileRepository() {
        this.repositoryLookup = Lookup.getDefault().lookupResult(WorkspaceItemRepository.class);
        this.wsFileChooser = new FileChooserBuilder(FileRepository.class)
                .setDefaultWorkingDirectory(getDefaultWorkingDirectory(DesktopManager.get(), System::getProperty))
                .setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Xml files", "xml"));
    }

    @NonNull
    public static DataSource encode(@Nullable File file) {
        if (file != null) {
            String sfile = file.getAbsolutePath();
            sfile = Paths.changeExtension(sfile, "xml");
            return DataSource.of(NAME, VERSION, FILENAME, sfile);
        }
        return DataSource.of(NAME, VERSION);
    }

    @Nullable
    public static File decode(@NonNull DataSource source) {
        if (!source.getProviderName().equals(NAME)) {
            return null;
        }
        if (source.getVersion().equals(VERSION)) {
            String file = source.getParameter(FILENAME);
            if (file != null) {
                return Path.of(file).toFile();
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public DataSource getDefaultDataSource() {
        return encode(null);
    }

    @Override
    public boolean saveAs(Workspace ws, DemetraVersion version) {
        File file = wsFileChooser.showSaveDialog();
        if (file == null) {
            return false;
        }

        try {
            ws.loadAll();
            ws.setName(Paths.changeExtension(file.getName(), null));
            ws.setDataSource(encode(file));
            return save(ws, version, true);
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    protected boolean saveWorkspace(Workspace ws, DemetraVersion version) {
        File file = decode(ws.getDataSource());
        if (file == null) {
            return saveAs(ws, version);
        }

        boolean exist = file.exists();
        try (FileWorkspace storage = exist
                ? FileWorkspace.open(file.toPath(), version)
                : FileWorkspace.create(file.toPath(), version)) {
            storage.setName(ws.getName());
            storeCalendar(storage, ws.getContext().getCalendars());
            if (exist) {
                removeDeletedItems(storage, ws);
            }
        } catch (IOException | InvalidPathException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }

        ws.resetDirty();
        ws.getContext().resetDirty();
        return true;
    }

    @Override
    protected boolean deleteWorkspace(Workspace ws) {
        // TODO
        return false;
    }

    @Override
    public Workspace open() {
        java.io.File file = wsFileChooser.showOpenDialog();
        if (file == null) {
            return null;
        }

        Workspace ws = new Workspace(encode(file), Paths.changeExtension(file.getName(), null));
        if (!load(ws)) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(file.getName() + " is not a valid workspace!");
            DialogDisplayer.getDefault().notify(nd);
            return null;
        } else {
            return ws;
        }
    }

    @Override
    public boolean load(Workspace ws) {
        if (!(ws.getRepository() instanceof FileRepository)) {
            return false;
        }

        File file = decode(ws.getDataSource());
        if (file == null || !file.exists()) {
            return false;
        }
        
 
        try (FileWorkspace storage = FileWorkspace.open(file.toPath())) {
            ws.setName(storage.getName());
            loadCalendars(storage, ws);
            loadItems(storage.getItems(), ws);
            WorkspaceRepository.updateModellingContext(ws);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }

        ws.resetDirty();
        ws.getContext().resetDirty();
        return true;
    }

    private static void loadItems(Collection<WorkspaceItemDescriptor> items, Workspace ws) {
        items.forEach(o -> {
            WorkspaceItemDescriptor.Key key = o.getKey();
            WorkspaceItemDescriptor.Attributes attributes = o.getAttributes();
            if (!key.getFamily().equals(WorkspaceFamily.UTIL_CAL)) {
                WorkspaceItem<?> witem = WorkspaceItem.item(LinearId.of(key.getFamily()), attributes.getLabel(), key.getId(), attributes.getComments());
                ws.quietAdd(witem);
                WorkspaceItemManager<?> manager = WorkspaceFactory.getInstance().getManager(witem.getFamily());
                if (manager != null && manager.isAutoLoad()) {
                    witem.load();
                }
            }
        });
    }

    private static final WorkspaceItemDescriptor CAL_ID =
            new WorkspaceItemDescriptor(
                    new WorkspaceItemDescriptor.Key(WorkspaceFamily.UTIL_CAL, "Calendars"),
                    new WorkspaceItemDescriptor.Attributes("Calendars", false, null));

    private static void storeCalendar(FileWorkspace storage, CalendarManager value) throws IOException {
        storage.store(CAL_ID, value);
        value.resetDirty();
    }

    private static void loadCalendars(FileWorkspace storage, Workspace ws) throws IOException {
        CalendarManager source = (CalendarManager) storage.load(CAL_ID.getKey());
//        CalendarManager target = ws.getContext().getCalendars();
        for (String name : source.getNames()) {
            CalendarDefinition cal = source.get(name);
//            target.set(name, cal);
            if (ws.searchDocumentByElement(cal) == null) {
                WorkspaceItem<CalendarDefinition> item = WorkspaceItem.loadedItem(CalendarDocumentManager.ID, name, cal);
                ws.quietAdd(item);
            }
        }
    }

    private static void removeDeletedItems(FileWorkspace storage, Workspace ws) throws IOException {
        for (WorkspaceItemDescriptor o : storage.getItems()) {
            if (!isCalendar(o) && isDeleted(ws, o)) {
                storage.delete(o.getKey());
            }
        }
    }

    private static boolean isCalendar(WorkspaceItemDescriptor o) {
        return WorkspaceFamily.UTIL_CAL.equals(o.getKey().getFamily());
    }

    private static boolean isDeleted(Workspace ws, WorkspaceItemDescriptor o) {
        return ws.searchDocument(LinearId.of(o.getKey().getFamily()), o.getKey().getId()) == null;
    }

    @Override
    public Collection<Class> getSupportedTypes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void initialize() {
        repositoryLookup.allInstances().forEach(o -> register(o.getSupportedType(), o));
    }

    @Override
    public void resultChanged(LookupEvent le) {
        initialize();
    }

    private static File getDefaultWorkingDirectory(Desktop desktop, UnaryOperator<String> properties) {
        File documents = getDocumentsDirectory(desktop).orElseGet(() -> getUserHome(properties));
        return documents.toPath().resolve("Demetra+").toFile();
    }

    private static Optional<File> getDocumentsDirectory(Desktop desktop) {
        if (desktop.isSupported(Desktop.Action.KNOWN_FOLDER_LOOKUP)) {
            try {
                return Optional.ofNullable(desktop.getKnownFolderPath(KnownFolder.DOCUMENTS));
            } catch (IOException ex) {
                // log this?
            }
        }
        return Optional.empty();
    }

    private static File getUserHome(UnaryOperator<String> properties) {
        return Path.of(properties.apply(SystemProperties.USER_HOME)).toFile();
    }
}
