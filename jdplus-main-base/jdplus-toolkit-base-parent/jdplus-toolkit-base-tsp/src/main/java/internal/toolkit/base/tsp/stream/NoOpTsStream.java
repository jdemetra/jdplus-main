/*
 * Copyright 2016 National Bank of Belgium
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
package internal.toolkit.base.tsp.stream;

import jdplus.toolkit.base.api.timeseries.TsInformationType;
import jdplus.toolkit.base.tsp.DataSet;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.base.tsp.stream.DataSetTs;
import jdplus.toolkit.base.tsp.stream.HasTsStream;
import jdplus.toolkit.base.tsp.util.DataSourcePreconditions;
import lombok.NonNull;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public final class NoOpTsStream implements HasTsStream {

    @lombok.NonNull
    private final String providerName;

    @Override
    public @NonNull Stream<DataSetTs> getData(@NonNull DataSource dataSource, @NonNull TsInformationType type) throws IllegalArgumentException, IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);
        Objects.requireNonNull(type);
        return Stream.empty();
    }

    @Override
    public @NonNull Stream<DataSetTs> getData(@NonNull DataSet dataSet, @NonNull TsInformationType type) throws IllegalArgumentException, IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSet);
        Objects.requireNonNull(type);
        return Stream.empty();
    }
}
