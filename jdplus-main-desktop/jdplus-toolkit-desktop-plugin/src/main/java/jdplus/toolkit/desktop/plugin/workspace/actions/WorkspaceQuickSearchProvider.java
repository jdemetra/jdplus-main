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
package jdplus.toolkit.desktop.plugin.workspace.actions;

import ec.util.completion.ExtAutoCompletionSource;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.function.Predicate;
import jdplus.toolkit.desktop.plugin.workspace.Workspace;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemManager;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = SearchProvider.class, path = "QuickSearch/Workspace")
@Messages({
    "ERR_WorkspaceChanged=Active workspace changed; search again",
    "ERR_WorkspaceItemUnavailable=Workspace item is no longer available"
})
public final class WorkspaceQuickSearchProvider implements SearchProvider {

    @Override
    public void evaluate(SearchRequest request, SearchResponse response) {
        String text = request.getText();
        if (text == null || text.isBlank() || response.isObsolete()) {
            return;
        }

        Predicate<String> filter = searchFilter(text);
        Workspace workspace = WorkspaceFactory.getInstance().getActiveWorkspace();
        List<WorkspaceItem<?>> items = List.copyOf(workspace.getItems());
        for (WorkspaceItem<?> item : items) {
            if (response.isObsolete()) {
                return;
            }

            WorkspaceItemManager<?> manager = WorkspaceFactory.getInstance().getManager(item.getFamily());
            if (manager == null || !matches(item, manager, filter)) {
                continue;
            }

            String displayName =
                    item.getDisplayName() + " (" + manager.getItemClass().getSimpleName() + ")";
            if (!response.addResult(() -> openItem(workspace, item), displayName)) {
                return;
            }
        }
    }

    static Predicate<String> searchFilter(String text) {
        return ExtAutoCompletionSource.basicFilter(text.strip());
    }

    static boolean matches(WorkspaceItem<?> item, WorkspaceItemManager<?> manager, Predicate<String> filter) {
        String comments = item.getComments();
        return filter.test(item.getDisplayName())
                || filter.test(manager.getItemClass().getSimpleName())
                || comments != null && filter.test(comments);
    }

    private static void openItem(Workspace workspace, WorkspaceItem<?> item) {
        WorkspaceFactory factory = WorkspaceFactory.getInstance();
        if (factory.getActiveWorkspace() != workspace) {
            StatusDisplayer.getDefault().setStatusText(Bundle.ERR_WorkspaceChanged());
            return;
        }

        WorkspaceItem<?> currentItem = workspace.searchDocument(item.getId());
        if (currentItem == null) {
            StatusDisplayer.getDefault().setStatusText(Bundle.ERR_WorkspaceItemUnavailable());
            return;
        }

        WorkspaceItemManager<?> manager = factory.getManager(currentItem.getFamily());
        if (manager == null) {
            StatusDisplayer.getDefault().setStatusText(Bundle.ERR_WorkspaceItemUnavailable());
            return;
        }

        manager.getPreferredItemAction(currentItem.getId())
                .actionPerformed(new ActionEvent(currentItem, ActionEvent.ACTION_PERFORMED, "quick-search"));
    }
}
