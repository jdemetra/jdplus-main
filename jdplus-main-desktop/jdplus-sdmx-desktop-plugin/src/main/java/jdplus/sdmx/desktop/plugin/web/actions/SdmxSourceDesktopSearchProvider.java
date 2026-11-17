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
package jdplus.sdmx.desktop.plugin.web.actions;

import ec.util.completion.ExtAutoCompletionSource;
import internal.sdmx.desktop.plugin.SdmxIcons;
import java.awt.Component;
import java.awt.Graphics;
import java.util.function.Predicate;
import javax.swing.CellRendererPane;
import javax.swing.Icon;
import jdplus.sdmx.base.api.web.SdmxWebProvider;
import jdplus.toolkit.desktop.plugin.TsManager;
import jdplus.toolkit.desktop.plugin.search.DesktopSearchProvider;
import jdplus.toolkit.desktop.plugin.search.DesktopSearchResult;
import jdplus.toolkit.desktop.plugin.search.DesktopSearchResults;
import jdplus.toolkit.desktop.plugin.search.QuickSearchSupport;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import sdmxdl.Languages;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Networking;

@ServiceProviders({
    @ServiceProvider(service = DesktopSearchProvider.class, position = 300),
    @ServiceProvider(service = SearchProvider.class, path = "QuickSearch/SDMX")
})
@Messages({
    "ERR_SdmxProviderUnavailable=SDMX provider is no longer available",
    "# {0} - sourceId",
    "ERR_SdmxSourceUnavailable=SDMX source is no longer available: {0}",
    "SdmxSearchCategory=SDMX Sources"
})
public final class SdmxSourceDesktopSearchProvider implements DesktopSearchProvider, SearchProvider {

    private static final String CATEGORY_ID = "sdmx";

    @Override
    public String getCategoryId() {
        return CATEGORY_ID;
    }

    @Override
    public String getCategoryName() {
        return Bundle.SdmxSearchCategory();
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

        Predicate<String> filter = searchFilter(text);
        TsManager.get().getProvider(SdmxWebProvider.class).ifPresent(provider -> {
            Languages languages = provider.getLanguages();
            for (WebSource source : provider.getSdmxManager().getSources().values()) {
                if (results.isObsolete()) {
                    return;
                }
                if (!matches(source, languages, filter)) {
                    continue;
                }
                String name = languages.select(source.getNames());
                boolean hasName = name != null && !name.isBlank();
                Networking networking = provider.getSdmxManager().getNetworking();
                if (!results.addResult(new DesktopSearchResult(
                        hasName ? name : source.getId(),
                        hasName ? source.getId() : null,
                        new FaviconIcon(networking, source),
                        () -> openSource(source.getId())))) {
                    return;
                }
            }
        });
    }

    static Predicate<String> searchFilter(String text) {
        return ExtAutoCompletionSource.basicFilter(text.strip());
    }

    static boolean matches(WebSource source, Languages languages, Predicate<String> filter) {
        return !source.isAlias()
                && (filter.test(source.getId())
                        || filter.test(languages.select(source.getNames()))
                        || source.getAliases().stream().anyMatch(filter));
    }

    private static void openSource(String sourceId) {
        var provider = TsManager.get().getProvider(SdmxWebProvider.class);
        if (provider.isEmpty()) {
            StatusDisplayer.getDefault().setStatusText(Bundle.ERR_SdmxProviderUnavailable());
            return;
        }
        WebSource source = provider.orElseThrow().getSdmxManager().getSources().get(sourceId);
        if (source == null) {
            StatusDisplayer.getDefault().setStatusText(Bundle.ERR_SdmxSourceUnavailable(sourceId));
            return;
        }
        JSdmxWebSourcePanel.openSource(provider.orElseThrow(), source);
    }

    /** Resolves the favicon lazily on the EDT, when painted. */
    private record FaviconIcon(Networking networking, WebSource source) implements Icon {

        private static final int SIZE = 16;

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Component target =
                    c.getParent() instanceof CellRendererPane pane && pane.getParent() != null ? pane.getParent() : c;
            SdmxIcons.getFavicon(networking, source, target::repaint, SIZE).paintIcon(c, g, x, y);
        }

        @Override
        public int getIconWidth() {
            return SIZE;
        }

        @Override
        public int getIconHeight() {
            return SIZE;
        }
    }
}
