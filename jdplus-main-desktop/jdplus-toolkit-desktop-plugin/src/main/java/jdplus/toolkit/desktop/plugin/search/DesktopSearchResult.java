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

import java.util.Objects;
import javax.swing.Icon;
import org.jspecify.annotations.Nullable;

/**
 * A search hit.
 *
 * @param displayName the main text shown to users
 * @param detail an optional secondary text, such as a type or a location
 * @param icon an optional icon
 * @param action the action performed when the hit is selected
 */
public record DesktopSearchResult(
        String displayName,
        @Nullable String detail,
        @Nullable Icon icon,
        Runnable action) {

    public DesktopSearchResult {
        Objects.requireNonNull(displayName);
        Objects.requireNonNull(action);
    }

    public DesktopSearchResult(String displayName, Runnable action) {
        this(displayName, null, null, action);
    }

    /** Returns the display name followed by the detail, if any. */
    public String getFullDisplayName() {
        return detail == null || detail.isBlank() ? displayName : displayName + " (" + detail + ")";
    }
}
