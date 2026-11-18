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

import jdplus.toolkit.base.api.design.ExtensionPoint;

/**
 * Search provider used by desktop search interfaces.
 * Implementations are registered in the default NetBeans lookup and may also be exposed to NetBeans Quick Search
 * through {@link QuickSearchSupport}.
 * Categories are displayed in lookup order, which can be controlled with the {@code position} attribute of
 * {@code @ServiceProvider}.
 */
@ExtensionPoint
public interface DesktopSearchProvider {

    /** Returns the stable identifier used to group this provider's results. */
    String getCategoryId();

    /** Returns the localized category name shown to users. */
    String getCategoryName();

    /**
     * Searches for results, stopping when {@code results.isObsolete()} is true or
     * {@code results.addResult(...)} returns false.
     */
    void search(String text, DesktopSearchResults results);
}
