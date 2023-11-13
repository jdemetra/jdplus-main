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
package jdplus.toolkit.base.tsp.util;

import jdplus.toolkit.base.api.timeseries.TsMoniker;
import jdplus.toolkit.base.tsp.DataSet;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.base.tsp.HasDataMoniker;
import lombok.NonNull;

import java.util.Optional;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(staticName = "of")
public final class FallbackDataMoniker implements HasDataMoniker {

    @lombok.NonNull
    private final HasDataMoniker first;

    @lombok.NonNull
    private final HasDataMoniker second;

    @Override
    public @NonNull TsMoniker toMoniker(@NonNull DataSource dataSource) throws IllegalArgumentException {
        return first.toMoniker(dataSource);
    }

    @Override
    public @NonNull TsMoniker toMoniker(@NonNull DataSet dataSet) throws IllegalArgumentException {
        return first.toMoniker(dataSet);
    }

    @Override
    public @NonNull Optional<DataSource> toDataSource(@NonNull TsMoniker moniker) throws IllegalArgumentException {
        Optional<DataSource> result = first.toDataSource(moniker);
        return result.isPresent() ? result : second.toDataSource(moniker);
    }

    @Override
    public @NonNull Optional<DataSet> toDataSet(@NonNull TsMoniker moniker) throws IllegalArgumentException {
        Optional<DataSet> result = first.toDataSet(moniker);
        return result.isPresent() ? result : second.toDataSet(moniker);
    }
}
