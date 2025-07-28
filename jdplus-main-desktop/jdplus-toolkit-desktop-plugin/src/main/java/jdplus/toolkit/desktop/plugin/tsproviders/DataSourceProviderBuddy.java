/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
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
package jdplus.toolkit.desktop.plugin.tsproviders;

import jdplus.toolkit.base.api.design.ExtensionPoint;
import jdplus.toolkit.base.api.timeseries.TsMoniker;
import jdplus.toolkit.base.tsp.DataSet;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.desktop.plugin.beans.BeanEditor;
import jdplus.toolkit.desktop.plugin.util.NetBeansServiceBackend;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import org.openide.nodes.Sheet;

import java.awt.*;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.util.List;

/**
 * @author Philippe Charles
 * @since 1.0.0
 */
@ExtensionPoint
@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        backend = NetBeansServiceBackend.class
)
public interface DataSourceProviderBuddy {

    @NonNull
    String getProviderName();

    @Nullable
    default Image getIconOrNull(int type, boolean opened) {
        return null;
    }

    @Nullable
    default Image getIconOrNull(@NonNull DataSource dataSource, int type, boolean opened) {
        return null;
    }

    @Nullable
    default Image getIconOrNull(@NonNull DataSet dataSet, int type, boolean opened) {
        return null;
    }

    @Nullable
    default Image getIconOrNull(@NonNull IOException ex, int type, boolean opened) {
        return null;
    }

    @Nullable
    default Image getIconOrNull(@NonNull TsMoniker moniker, int type, boolean opened) {
        return null;
    }

    @Nullable
    default List<Sheet.Set> getSheetOrNull() {
        return null;
    }

    @Nullable
    default List<Sheet.Set> getSheetOrNull(@NonNull DataSource dataSource) {
        return null;
    }

    @Nullable
    default List<Sheet.Set> getSheetOrNull(@NonNull DataSet dataSet) {
        return null;
    }

    @Nullable
    default List<Sheet.Set> getSheetOrNull(@NonNull IOException ex) {
        return null;
    }

    @Nullable
    default List<Sheet.Set> getSheetOfBeanOrNull(@NonNull Object bean) throws IntrospectionException {
        return null;
    }

    @Nullable
    default BeanEditor getBeanEditorOrNull(@NonNull String title) {
        return null;
    }
}
