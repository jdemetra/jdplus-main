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
import java.awt.Image;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import javax.swing.Icon;
import jdplus.toolkit.desktop.plugin.search.DesktopSearchProvider;
import jdplus.toolkit.desktop.plugin.search.DesktopSearchResult;
import jdplus.toolkit.desktop.plugin.search.DesktopSearchResults;
import jdplus.toolkit.desktop.plugin.search.QuickSearchSupport;
import org.jspecify.annotations.Nullable;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.awt.StatusDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.openide.windows.TopComponent;

/** Searches the opened windows and activates the selected one. */
@ServiceProviders({
    @ServiceProvider(service = DesktopSearchProvider.class, position = 150),
    @ServiceProvider(service = SearchProvider.class, path = "QuickSearch/Windows")
})
@NbBundle.Messages({
    "WindowsSearchCategory=Windows",
    "# {0} - window name",
    "ERR_WindowClosed=Window is no longer open: {0}"
})
public final class WindowsDesktopSearchProvider implements DesktopSearchProvider, SearchProvider {

    private static final String CATEGORY_ID = "windows";

    // Weak reference so that results kept in history don't retain closed windows
    private record Entry(String name, @Nullable Icon icon, WeakReference<TopComponent> window) {}

    @Override
    public String getCategoryId() {
        return CATEGORY_ID;
    }

    @Override
    public String getCategoryName() {
        return Bundle.WindowsSearchCategory();
    }

    @Override
    public void evaluate(SearchRequest request, SearchResponse response) {
        QuickSearchSupport.evaluate(this, request, response);
    }

    @Override
    public void search(String text, DesktopSearchResults results) {
        if (text == null || text.isBlank() || results.isObsolete()) {
            return;
        }

        Predicate<String> filter = ExtAutoCompletionSource.basicFilter(text.strip());
        List<Entry> entries =
                Mutex.EVENT.readAccess((Mutex.Action<List<Entry>>) WindowsDesktopSearchProvider::loadEntries);
        for (Entry entry : entries) {
            if (results.isObsolete()) {
                return;
            }
            if (filter.test(entry.name())
                    && !results.addResult(
                            new DesktopSearchResult(entry.name(), null, entry.icon(), () -> activate(entry)))) {
                return;
            }
        }
    }

    private static List<Entry> loadEntries() {
        List<Entry> result = new ArrayList<>();
        for (TopComponent window : TopComponent.getRegistry().getOpened()) {
            String name = getName(window);
            if (name != null) {
                result.add(new Entry(name, getIcon(window), new WeakReference<>(window)));
            }
        }
        return result;
    }

    private static @Nullable String getName(TopComponent window) {
        String result = window.getDisplayName();
        if (result == null || result.isBlank()) {
            result = window.getName();
        }
        return result == null || result.isBlank() ? null : result;
    }

    private static @Nullable Icon getIcon(TopComponent window) {
        Image image = window.getIcon();
        return image != null ? ImageUtilities.image2Icon(image) : null;
    }

    private static void activate(Entry entry) {
        TopComponent window = entry.window().get();
        if (window == null || !window.isOpened()) {
            StatusDisplayer.getDefault().setStatusText(Bundle.ERR_WindowClosed(entry.name()));
            return;
        }
        window.requestActive();
    }
}
