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
import java.util.function.Predicate;
import jdplus.sdmx.base.api.web.SdmxWebProvider;
import jdplus.toolkit.desktop.plugin.TsManager;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import sdmxdl.Languages;
import sdmxdl.web.WebSource;

@ServiceProvider(service = SearchProvider.class, path = "QuickSearch/SDMX")
@Messages({
    "ERR_SdmxProviderUnavailable=SDMX provider is no longer available",
    "# {0} - sourceId",
    "ERR_SdmxSourceUnavailable=SDMX source is no longer available: {0}"
})
public final class SdmxSourceQuickSearchProvider implements SearchProvider {

    @Override
    public void evaluate(SearchRequest request, SearchResponse response) {
        String text = request.getText();
        if (text == null || text.isBlank() || response.isObsolete()) {
            return;
        }

        Predicate<String> filter = searchFilter(text);
        TsManager.get().getProvider(SdmxWebProvider.class).ifPresent(provider -> {
            Languages languages = provider.getLanguages();
            for (WebSource source : provider.getSdmxManager().getSources().values()) {
                if (response.isObsolete()) {
                    return;
                }
                if (!matches(source, languages, filter)) {
                    continue;
                }
                String name = languages.select(source.getNames());
                String displayName =
                        (name == null || name.isBlank() ? source.getId() : name) + " (" + source.getId() + ")";
                if (!response.addResult(() -> openSource(source.getId()), displayName)) {
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
}
