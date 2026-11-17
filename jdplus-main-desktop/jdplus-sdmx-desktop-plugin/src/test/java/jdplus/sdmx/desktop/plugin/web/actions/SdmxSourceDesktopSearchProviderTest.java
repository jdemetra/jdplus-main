package jdplus.sdmx.desktop.plugin.web.actions;

import static jdplus.sdmx.desktop.plugin.web.actions.SdmxSourceDesktopSearchProvider.matches;
import static jdplus.sdmx.desktop.plugin.web.actions.SdmxSourceDesktopSearchProvider.searchFilter;
import static org.assertj.core.api.Assertions.assertThat;

import jdplus.toolkit.desktop.plugin.search.DesktopSearchProvider;
import org.junit.jupiter.api.Test;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import sdmxdl.Languages;
import sdmxdl.web.WebSource;

class SdmxSourceDesktopSearchProviderTest {

    @Test
    void matchesNameIdAndAliasIgnoringCaseAndOuterSpaces() {
        WebSource source = source();

        assertThat(matches(source, Languages.ANY, searchFilter(" euro "))).isTrue();
        assertThat(matches(source, Languages.ANY, searchFilter(" DOT "))).isTrue();
        assertThat(matches(source, Languages.ANY, searchFilter(" legacy "))).isTrue();
    }

    @Test
    void excludesAliasEntries() {
        WebSource alias = source().toBuilder().id("legacy-stat").build();

        assertThat(matches(alias, Languages.ANY, searchFilter("legacy"))).isFalse();
    }

    @Test
    void registersQuickSearchProvider() {
        SearchProvider provider = Lookups.forPath("QuickSearch/SDMX").lookup(SearchProvider.class);

        assertThat(provider).isInstanceOf(SdmxSourceDesktopSearchProvider.class);
    }

    @Test
    void registersDesktopSearchProvider() {
        assertThat(Lookup.getDefault().lookupAll(DesktopSearchProvider.class))
                .anyMatch(provider -> "sdmx".equals(provider.getCategoryId()));
    }

    private static WebSource source() {
        return WebSource.builder()
                .id("dotstat")
                .name("en", "Eurostat")
                .driver("test")
                .endpointOf("https://example.com")
                .alias("legacy-stat")
                .build();
    }
}
