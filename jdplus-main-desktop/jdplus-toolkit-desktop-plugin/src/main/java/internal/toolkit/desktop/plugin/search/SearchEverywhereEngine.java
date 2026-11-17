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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.logging.Level;
import jdplus.toolkit.desktop.plugin.search.DesktopSearchProvider;
import jdplus.toolkit.desktop.plugin.search.DesktopSearchResult;
import jdplus.toolkit.desktop.plugin.search.DesktopSearchResults;
import lombok.NonNull;

/** Runs searches over all desktop search providers, grouping results by category. */
@lombok.extern.java.Log
final class SearchEverywhereEngine {

    private static final int CANDIDATES_FACTOR = 5;

    record Group(String categoryId, String categoryName, List<DesktopSearchResult> results) {}

    interface Listener {

        /** Receives the current results; {@code done} is true once all providers have been queried. */
        void onUpdate(List<Group> groups, boolean done);
    }

    interface Search {

        boolean isObsolete();
    }

    private final Supplier<? extends Collection<? extends DesktopSearchProvider>> providers;
    private final Executor executor;
    private final int maxResultsPerCategory;
    private final AtomicLong generation = new AtomicLong();

    SearchEverywhereEngine(
            @NonNull Supplier<? extends Collection<? extends DesktopSearchProvider>> providers,
            @NonNull Executor executor,
            int maxResultsPerCategory) {
        this.providers = providers;
        this.executor = executor;
        this.maxResultsPerCategory = maxResultsPerCategory;
    }

    /** Starts a new search and makes previous ones obsolete. The listener is called from the executor. */
    Search search(@NonNull String text, @NonNull Listener listener) {
        long id = generation.incrementAndGet();
        Search search = () -> generation.get() != id;
        executor.execute(() -> run(text, search, listener));
        return search;
    }

    /** Makes the current search obsolete. */
    void cancel() {
        generation.incrementAndGet();
    }

    private void run(String text, Search search, Listener listener) {
        Map<String, GroupBuilder> groups = new LinkedHashMap<>();
        for (DesktopSearchProvider provider : providers.get()) {
            if (search.isObsolete()) {
                return;
            }
            GroupBuilder group = groups.computeIfAbsent(
                    provider.getCategoryId(), categoryId -> new GroupBuilder(categoryId, provider.getCategoryName()));
            if (group.isFull()) {
                continue;
            }
            try {
                provider.search(text, new GroupResults(group, search));
            } catch (RuntimeException ex) {
                log.log(
                        Level.WARNING,
                        "Search failed in provider " + provider.getClass().getName(),
                        ex);
            }
            if (search.isObsolete()) {
                return;
            }
            listener.onUpdate(snapshot(groups, text), false);
        }
        if (!search.isObsolete()) {
            listener.onUpdate(snapshot(groups, text), true);
        }
    }

    private List<Group> snapshot(Map<String, GroupBuilder> groups, String text) {
        Comparator<DesktopSearchResult> byScore = Comparator.comparingInt(result -> score(result.displayName(), text));
        return groups.values().stream()
                .filter(group -> !group.results.isEmpty())
                .map(group -> new Group(
                        group.categoryId,
                        group.categoryName,
                        group.results.stream()
                                .sorted(byScore)
                                .limit(maxResultsPerCategory)
                                .toList()))
                .toList();
    }

    /**
     * Scores how well a name matches the searched text; lower is better: exact match, prefix, word start, contains,
     * no match in the name (matched by the provider on other criteria).
     */
    static int score(String name, String text) {
        String term = text.strip().toLowerCase(Locale.ROOT);
        String value = name.toLowerCase(Locale.ROOT);
        if (term.isEmpty()) {
            return 4;
        }
        if (value.equals(term)) {
            return 0;
        }
        if (value.startsWith(term)) {
            return 1;
        }
        int index = value.indexOf(term);
        if (index < 0) {
            return 4;
        }
        for (; index > 0; index = value.indexOf(term, index + 1)) {
            if (!Character.isLetterOrDigit(value.charAt(index - 1))) {
                return 2;
            }
        }
        return 3;
    }

    private final class GroupBuilder {

        private final String categoryId;
        private final String categoryName;
        private final List<DesktopSearchResult> results = new ArrayList<>();

        private GroupBuilder(String categoryId, String categoryName) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
        }

        // Collects more candidates than displayed so that the best matches can be ranked first
        private boolean isFull() {
            return results.size() >= maxResultsPerCategory * CANDIDATES_FACTOR;
        }
    }

    private record GroupResults(GroupBuilder group, Search search) implements DesktopSearchResults {

        @Override
        public boolean isObsolete() {
            return search.isObsolete() || group.isFull();
        }

        @Override
        public boolean addResult(DesktopSearchResult result) {
            if (isObsolete()) {
                return false;
            }
            group.results.add(result);
            return !group.isFull();
        }
    }
}
