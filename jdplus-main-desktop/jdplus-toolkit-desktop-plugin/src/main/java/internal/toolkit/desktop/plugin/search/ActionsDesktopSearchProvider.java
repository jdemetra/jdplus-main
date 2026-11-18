/*
 * Copyright 2026 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the Licence;
 * you may not use this work except in compliance with the Licence.
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
package internal.toolkit.desktop.plugin.search;

import ec.util.completion.ExtAutoCompletionSource;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;
import jdplus.toolkit.desktop.plugin.search.DesktopSearchProvider;
import jdplus.toolkit.desktop.plugin.search.DesktopSearchResult;
import jdplus.toolkit.desktop.plugin.search.DesktopSearchResults;
import org.jspecify.annotations.Nullable;
import org.openide.awt.Actions;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/** Searches the actions available in the main menu. */
@lombok.extern.java.Log
@ServiceProvider(service = DesktopSearchProvider.class, position = 100)
@NbBundle.Messages({
    "ActionsSearchCategory=Actions",
    "# {0} - action name",
    "ERR_ActionDisabled=Action is not available in the current context: {0}"
})
public final class ActionsDesktopSearchProvider implements DesktopSearchProvider {

    private static final String CATEGORY_ID = "actions";
    private static final String MENU_FOLDER = "Menu";
    private static final String PATH_SEPARATOR = " > ";

    private record Entry(String name, String path, @Nullable Icon icon, Action action) {}

    @Override
    public String getCategoryId() {
        return CATEGORY_ID;
    }

    @Override
    public String getCategoryName() {
        return Bundle.ActionsSearchCategory();
    }

    @Override
    public void search(String text, DesktopSearchResults results) {
        if (text == null || text.isBlank() || results.isObsolete()) {
            return;
        }

        Predicate<String> filter = ExtAutoCompletionSource.basicFilter(text.strip());
        // Actions are created and queried on the EDT, like menus do
        List<Entry> entries =
                Mutex.EVENT.readAccess((Mutex.Action<List<Entry>>) ActionsDesktopSearchProvider::loadEntries);
        for (Entry entry : entries) {
            if (results.isObsolete()) {
                return;
            }
            if (filter.test(entry.name())
                    && !results.addResult(
                            new DesktopSearchResult(entry.name(), entry.path(), entry.icon(), () -> perform(entry)))) {
                return;
            }
        }
    }

    private static List<Entry> loadEntries() {
        FileObject menu = FileUtil.getConfigFile(MENU_FOLDER);
        if (menu == null) {
            return List.of();
        }
        List<Entry> result = new ArrayList<>();
        collect(menu, null, result, Collections.newSetFromMap(new IdentityHashMap<>()));
        return result;
    }

    private static void collect(FileObject folder, @Nullable String path, List<Entry> result, Set<Action> visited) {
        for (FileObject child : FileUtil.getOrder(Arrays.asList(folder.getChildren()), false)) {
            if (child.isFolder()) {
                String label = getLabel(child);
                collect(child, path == null ? label : path + PATH_SEPARATOR + label, result, visited);
            } else if (path != null) {
                Action action = getAction(child);
                String name = action != null ? getName(action) : null;
                if (name != null && visited.add(action)) {
                    result.add(new Entry(name, withShortcut(path, action), getIcon(action), action));
                }
            }
        }
    }

    private static String getLabel(FileObject folder) {
        try {
            return Actions.cutAmpersand(
                    folder.getFileSystem().getDecorator().annotateName(folder.getName(), Set.of(folder)));
        } catch (FileStateInvalidException ex) {
            return folder.getName();
        }
    }

    private static @Nullable Action getAction(FileObject file) {
        String path = file.getPath();
        if ("shadow".equals(file.getExt())) {
            if (!(file.getAttribute("originalFile") instanceof String originalFile)) {
                return null;
            }
            path = originalFile;
        }
        try {
            return FileUtil.getConfigObject(path, Action.class);
        } catch (RuntimeException ex) {
            log.log(Level.FINE, "Cannot load action " + path, ex);
            return null;
        }
    }

    private static @Nullable String getName(Action action) {
        if (!(action.getValue(Action.NAME) instanceof String name)) {
            return null;
        }
        String result = Actions.cutAmpersand(name).strip();
        if (result.endsWith("...")) {
            result = result.substring(0, result.length() - 3).strip();
        } else if (result.endsWith("\u2026")) {
            result = result.substring(0, result.length() - 1).strip();
        }
        return result.isEmpty() ? null : result;
    }

    private static String withShortcut(String path, Action action) {
        return action.getValue(Action.ACCELERATOR_KEY) instanceof KeyStroke shortcut
                ? path + " (" + Actions.keyStrokeToString(shortcut) + ")"
                : path;
    }

    private static @Nullable Icon getIcon(Action action) {
        if (action.getValue(Action.SMALL_ICON) instanceof Icon icon) {
            return icon;
        }
        if (action.getValue("iconBase") instanceof String iconBase) {
            return ImageUtilities.loadImageIcon(iconBase, true);
        }
        return null;
    }

    private static void perform(Entry entry) {
        Action action = entry.action() instanceof ContextAwareAction contextAware
                ? contextAware.createContextAwareInstance(Utilities.actionsGlobalContext())
                : entry.action();
        if (!action.isEnabled()) {
            StatusDisplayer.getDefault().setStatusText(Bundle.ERR_ActionDisabled(entry.name()));
            return;
        }
        action.actionPerformed(new ActionEvent(action, ActionEvent.ACTION_PERFORMED, "search-everywhere"));
    }
}
