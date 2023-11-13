/*
 * Copyright 2017 National Bank of Belgium
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
package internal.text.base.api;

import jdplus.toolkit.base.api.timeseries.TsCollection;
import jdplus.text.base.api.XmlBean;
import jdplus.toolkit.base.tsp.DataSet;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.base.tsp.HasDataDisplayName;
import jdplus.toolkit.base.tsp.util.DataSourcePreconditions;
import lombok.NonNull;

import java.util.List;
import java.util.function.Function;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(staticName = "of")
public final class XmlDataDisplayName implements HasDataDisplayName {

    private final String providerName;
    private final XmlParam param;
    private final Function<DataSource, List<TsCollection>> resources;

    @Override
    public @NonNull String getDisplayName(@NonNull DataSource dataSource) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);
        XmlBean bean = param.get(dataSource);
        return bean.getFile().getPath();
    }

    @Override
    public @NonNull String getDisplayName(@NonNull DataSet dataSet) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSet);
        Integer collectionIndex = param.getCollectionParam().get(dataSet);
        Integer seriesIndex = param.getSeriesParam().get(dataSet);

        List<TsCollection> data = resources.apply(dataSet.getDataSource());
        if (data != null) {
            TsCollection col = data.get(collectionIndex);
            switch (dataSet.getKind()) {
                case COLLECTION:
                    return col.getName();
                case SERIES:
                    return col.getName() + " - " + col.get(seriesIndex).getName();
            }
        } else {
            switch (dataSet.getKind()) {
                case COLLECTION:
                    return String.valueOf(collectionIndex);
                case SERIES:
                    return collectionIndex + " - " + seriesIndex;
            }
        }
        return "";
    }

    @Override
    public @NonNull String getDisplayNodeName(@NonNull DataSet dataSet) {
        DataSourcePreconditions.checkProvider(providerName, dataSet);
        Integer collectionIndex = param.getCollectionParam().get(dataSet);
        Integer seriesIndex = param.getSeriesParam().get(dataSet);

        List<TsCollection> data = resources.apply(dataSet.getDataSource());
        if (data != null) {
            TsCollection col = data.get(collectionIndex);
            switch (dataSet.getKind()) {
                case COLLECTION:
                    return col.getName();
                case SERIES:
                    return col.get(seriesIndex).getName();
            }
        } else {
            switch (dataSet.getKind()) {
                case COLLECTION:
                    return String.valueOf(collectionIndex);
                case SERIES:
                    return String.valueOf(seriesIndex);
            }
        }
        return "";
    }
}
