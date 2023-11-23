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
package internal.spreadsheet.base.api;

import jdplus.spreadsheet.base.api.SpreadSheetBean;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.timeseries.util.ObsGathering;
import jdplus.toolkit.base.tsp.DataSet;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.base.tsp.HasDataDisplayName;
import jdplus.toolkit.base.tsp.util.DataSourcePreconditions;
import jdplus.toolkit.base.api.util.MultiLineNameUtil;
import lombok.NonNull;

import java.util.Locale;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(staticName = "of")
public final class SpreadSheetDataDisplayName implements HasDataDisplayName {

    private final String providerName;
    private final SpreadSheetParam resource;

    @Override
    public @NonNull String getDisplayName(@NonNull DataSource dataSource) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);
        SpreadSheetBean bean = resource.get(dataSource);
        return bean.getFile().getPath() + toString(bean.getGathering());
    }

    @Override
    public @NonNull String getDisplayName(@NonNull DataSet dataSet) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSet);
        switch (dataSet.getKind()) {
            case COLLECTION:
                return getCollectionId(dataSet);
            case SERIES:
                return getCollectionId(dataSet) + MultiLineNameUtil.SEPARATOR + getSeriesId(dataSet);
        }
        throw new IllegalArgumentException(dataSet.getKind().name());
    }

    @Override
    public @NonNull String getDisplayNodeName(@NonNull DataSet dataSet) {
        DataSourcePreconditions.checkProvider(providerName, dataSet);
        switch (dataSet.getKind()) {
            case COLLECTION:
                return getCollectionId(dataSet);
            case SERIES:
                return getSeriesId(dataSet);
        }
        throw new IllegalArgumentException(dataSet.getKind().name());
    }

    private String getCollectionId(DataSet dataSet) {
        return resource.getSheetParam().get(dataSet);
    }

    private String getSeriesId(DataSet dataSet) {
        return resource.getSeriesParam().get(dataSet);
    }

    private static String toString(ObsGathering gathering) {
        return TsUnit.UNDEFINED.equals(gathering.getUnit()) ? "" : String.format(Locale.ROOT, "(%s/%s)", gathering.getUnit(), gathering.getAggregationType());
    }
}
