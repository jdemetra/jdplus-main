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
package internal.toolkit.desktop.plugin.tsproviders;

import ec.util.completion.ExtAutoCompletionSource;
import java.beans.BeanInfo;
import java.util.List;
import java.util.function.Predicate;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.base.tsp.DataSourceProvider;
import jdplus.toolkit.desktop.plugin.TsManager;
import jdplus.toolkit.desktop.plugin.search.DesktopSearchProvider;
import jdplus.toolkit.desktop.plugin.search.DesktopSearchResult;
import jdplus.toolkit.desktop.plugin.search.DesktopSearchResults;
import jdplus.toolkit.desktop.plugin.search.QuickSearchSupport;
import jdplus.toolkit.desktop.plugin.tsproviders.DataSourceManager;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/** Searches the opened data sources and selects the chosen one in the providers window. */
@ServiceProviders({
    @ServiceProvider(service = DesktopSearchProvider.class, position = 250),
    @ServiceProvider(service = SearchProvider.class, path = "QuickSearch/DataSources")
})
@NbBundle.Messages({
    "DataSourcesSearchCategory=Data Sources",
    "# {0} - data source name",
    "ERR_DataSourceUnavailable=Data source is no longer available: {0}"
})
public final class DataSourcesDesktopSearchProvider implements DesktopSearchProvider, SearchProvider {

    private static final String CATEGORY_ID = "datasources";

    @Override
    public String getCategoryId() {
        return CATEGORY_ID;
    }

    @Override
    public String getCategoryName() {
        return Bundle.DataSourcesSearchCategory();
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
        List<DataSourceProvider> providers = TsManager.get()
                .getProviders()
                .filter(DataSourceProvider.class::isInstance)
                .map(DataSourceProvider.class::cast)
                .toList();
        for (DataSourceProvider provider : providers) {
            for (DataSource dataSource : provider.getDataSources()) {
                if (results.isObsolete()) {
                    return;
                }
                String name = provider.getDisplayName(dataSource);
                String providerName = provider.getDisplayName();
                if ((filter.test(name) || filter.test(providerName))
                        && !results.addResult(new DesktopSearchResult(
                                name,
                                providerName,
                                DataSourceManager.get().getIcon(dataSource, BeanInfo.ICON_COLOR_16x16, false),
                                () -> select(dataSource, name)))) {
                    return;
                }
            }
        }
    }

    private static void select(DataSource dataSource, String name) {
        if (!ProvidersTopComponent.selectDataSource(dataSource)) {
            StatusDisplayer.getDefault().setStatusText(Bundle.ERR_DataSourceUnavailable(name));
        }
    }
}
