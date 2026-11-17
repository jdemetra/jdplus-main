package internal.toolkit.desktop.plugin.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import jdplus.toolkit.desktop.plugin.search.DesktopSearchProvider;
import jdplus.toolkit.desktop.plugin.search.DesktopSearchResult;
import jdplus.toolkit.desktop.plugin.search.DesktopSearchResults;
import org.junit.jupiter.api.Test;

class SearchEverywhereEngineTest {

    @Test
    void groupsResultsByCategoryAndCapsEachCategory() {
        List<DesktopSearchProvider> providers =
                List.of(provider("a", "A", 3), provider("b", "B", 0), provider("a", "A", 3), provider("c", "C", 1));
        List<List<SearchEverywhereEngine.Group>> updates = new ArrayList<>();
        List<Boolean> dones = new ArrayList<>();

        new SearchEverywhereEngine(() -> providers, Runnable::run, 4).search("x", (groups, done) -> {
            updates.add(groups);
            dones.add(done);
        });

        assertThat(dones).containsExactly(false, false, false, false, true);
        assertThat(updates.getLast())
                .extracting(
                        SearchEverywhereEngine.Group::categoryId,
                        group -> group.results().size())
                .containsExactly(tuple("a", 4), tuple("c", 1));
    }

    @Test
    void stopsWhenSearchBecomesObsolete() {
        List<Runnable> tasks = new ArrayList<>();
        Executor deferred = tasks::add;
        List<Boolean> dones = new ArrayList<>();
        SearchEverywhereEngine engine = new SearchEverywhereEngine(() -> List.of(provider("a", "A", 1)), deferred, 10);

        SearchEverywhereEngine.Search first = engine.search("x", (groups, done) -> dones.add(done));
        engine.cancel();
        tasks.forEach(Runnable::run);

        assertThat(first.isObsolete()).isTrue();
        assertThat(dones).isEmpty();
    }

    @Test
    void ignoresFailingProvider() {
        DesktopSearchProvider failing = new TestProvider("a", "A", 0) {
            @Override
            public void search(String text, DesktopSearchResults results) {
                throw new IllegalStateException();
            }
        };
        List<List<SearchEverywhereEngine.Group>> updates = new ArrayList<>();

        new SearchEverywhereEngine(() -> List.of(failing, provider("b", "B", 1)), Runnable::run, 10)
                .search("x", (groups, done) -> updates.add(groups));

        assertThat(updates.getLast())
                .extracting(SearchEverywhereEngine.Group::categoryId)
                .containsExactly("b");
    }

    @Test
    void ranksBestMatchesFirstAmongMoreCandidatesThanDisplayed() {
        DesktopSearchProvider provider = new TestProvider("a", "A", 0) {
            @Override
            public void search(String text, DesktopSearchResults results) {
                List.of("xx", "a ab", "ab", "zab", "b", "abc", "AB")
                        .forEach(name -> results.addResult(new DesktopSearchResult(name, () -> {})));
            }
        };
        List<List<SearchEverywhereEngine.Group>> updates = new ArrayList<>();

        new SearchEverywhereEngine(() -> List.of(provider), Runnable::run, 4)
                .search("ab", (groups, done) -> updates.add(groups));

        assertThat(updates.getLast().getFirst().results())
                .extracting(DesktopSearchResult::displayName)
                .containsExactly("ab", "AB", "abc", "a ab");
    }

    @Test
    void scoresMatchQuality() {
        assertThat(SearchEverywhereEngine.score("Seasonal", " seasonal ")).isEqualTo(0);
        assertThat(SearchEverywhereEngine.score("Seasonal adjustment", "sea")).isEqualTo(1);
        assertThat(SearchEverywhereEngine.score("x_adjustment", "adj")).isEqualTo(2);
        assertThat(SearchEverywhereEngine.score("readjust adjust", "adj")).isEqualTo(2);
        assertThat(SearchEverywhereEngine.score("readjust", "adj")).isEqualTo(3);
        assertThat(SearchEverywhereEngine.score("other", "adj")).isEqualTo(4);
    }

    private static DesktopSearchProvider provider(String id, String name, int count) {
        return new TestProvider(id, name, count);
    }

    private static class TestProvider implements DesktopSearchProvider {

        private final String id;
        private final String name;
        private final int count;

        TestProvider(String id, String name, int count) {
            this.id = id;
            this.name = name;
            this.count = count;
        }

        @Override
        public String getCategoryId() {
            return id;
        }

        @Override
        public String getCategoryName() {
            return name;
        }

        @Override
        public void search(String text, DesktopSearchResults results) {
            for (int i = 0; i < count && !results.isObsolete(); i++) {
                if (!results.addResult(new DesktopSearchResult(text + i, () -> {}))) {
                    return;
                }
            }
        }
    }
}
