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
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.timeseries.util.ObsGathering;
import jdplus.text.base.api.TxtBean;
import jdplus.toolkit.base.tsp.DataSet;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.base.tsp.HasDataDisplayName;
import jdplus.toolkit.base.tsp.util.DataSourcePreconditions;
import lombok.NonNull;

import java.util.Locale;
import java.util.function.Function;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(staticName = "of")
public final class TxtDataDisplayName implements HasDataDisplayName {

    private final String providerName;
    private final TxtParam param;
    private final Function<DataSource, TsCollection> data;

    @Override
    public @NonNull String getDisplayName(@NonNull DataSource dataSource) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);
        TxtBean bean = param.get(dataSource);
        return bean.getFile().getPath() + toString(bean.getGathering());
    }

    @Override
    public @NonNull String getDisplayName(@NonNull DataSet dataSet) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSet);
        Integer index = param.getSeriesParam().get(dataSet);

        TsCollection data = this.data.apply(dataSet.getDataSource());
        return data != null ? data.get(index).getName() : "Column " + index;
    }

    @Override
    public @NonNull String getDisplayNodeName(@NonNull DataSet dataSet) {
        return getDisplayName(dataSet);
    }

    private static String toString(ObsGathering gathering) {
        return TsUnit.UNDEFINED.equals(gathering.getUnit()) ? "" : String.format(Locale.ROOT, "(%s/%s)", gathering.getUnit(), gathering.getAggregationType());
    }
}
