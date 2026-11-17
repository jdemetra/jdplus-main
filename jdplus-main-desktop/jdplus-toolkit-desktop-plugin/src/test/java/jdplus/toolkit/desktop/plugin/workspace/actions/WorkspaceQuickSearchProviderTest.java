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

import static jdplus.toolkit.desktop.plugin.workspace.actions.WorkspaceQuickSearchProvider.matches;
import static jdplus.toolkit.desktop.plugin.workspace.actions.WorkspaceQuickSearchProvider.searchFilter;
import static org.assertj.core.api.Assertions.assertThat;

import jdplus.toolkit.base.api.util.Id;
import jdplus.toolkit.base.api.util.LinearId;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemManager;
import org.junit.jupiter.api.Test;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.openide.util.lookup.Lookups;

class WorkspaceQuickSearchProviderTest {

    @Test
    void matchesItemNameTypeAndCommentsIgnoringCaseAndOuterSpaces() {
        WorkspaceItem<?> item = WorkspaceItem.newItem(new LinearId("test"), "Quarterly series", new Object());
        item.setComments("Euro area");
        WorkspaceItemManager<?> manager = manager();

        assertThat(matches(item, manager, searchFilter(" QUARTERLY "))).isTrue();
        assertThat(matches(item, manager, searchFilter(" object "))).isTrue();
        assertThat(matches(item, manager, searchFilter(" EURO "))).isTrue();
    }

    @Test
    void registersQuickSearchProvider() {
        SearchProvider provider = Lookups.forPath("QuickSearch/Workspace").lookup(SearchProvider.class);

        assertThat(provider).isInstanceOf(WorkspaceQuickSearchProvider.class);
    }

    private static WorkspaceItemManager<?> manager() {
        return new WorkspaceItemManager<>() {
            @Override
            public Status getStatus() {
                return Status.User;
            }

            @Override
            public ItemType getItemType() {
                return ItemType.Doc;
            }

            @Override
            public Class<Object> getItemClass() {
                return Object.class;
            }

            @Override
            public Object createNewObject() {
                return new Object();
            }

            @Override
            public Id getId() {
                return new LinearId("test");
            }

            @Override
            public String getActionsPath() {
                return "test";
            }

            @Override
            public String getNextItemName(String name) {
                return name;
            }

            @Override
            public java.util.List<WorkspaceItem<Object>> getDefaultItems() {
                return java.util.List.of();
            }

            @Override
            public WorkspaceItem<Object> create(jdplus.toolkit.desktop.plugin.workspace.Workspace workspace) {
                return WorkspaceItem.newItem(getId(), "test", new Object());
            }

            @Override
            public boolean isAutoLoad() {
                return false;
            }
        };
    }
}
