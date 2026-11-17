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
package jdplus.toolkit.desktop.plugin.search;

import lombok.NonNull;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;

/** Bridges a {@link DesktopSearchProvider} to the NetBeans Quick Search SPI. */
@lombok.experimental.UtilityClass
public class QuickSearchSupport {

    /** Evaluates a NetBeans Quick Search request using the given desktop search provider. */
    public void evaluate(
            @NonNull DesktopSearchProvider provider, @NonNull SearchRequest request, @NonNull SearchResponse response) {
        String text = request.getText();
        if (text == null || text.isBlank() || response.isObsolete()) {
            return;
        }
        provider.search(text, new QuickSearchResults(response));
    }

    private static final class QuickSearchResults implements DesktopSearchResults {

        private final SearchResponse response;
        private boolean stopped;

        QuickSearchResults(SearchResponse response) {
            this.response = response;
        }

        @Override
        public boolean isObsolete() {
            return stopped || response.isObsolete();
        }

        @Override
        public boolean addResult(DesktopSearchResult result) {
            stopped = !response.addResult(result.action(), result.getFullDisplayName());
            return !stopped;
        }
    }
}
