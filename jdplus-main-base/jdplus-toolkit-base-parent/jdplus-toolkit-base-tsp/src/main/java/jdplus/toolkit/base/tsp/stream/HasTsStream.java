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
package jdplus.toolkit.base.tsp.stream;

import jdplus.toolkit.base.api.timeseries.TsInformationType;
import jdplus.toolkit.base.tsp.DataSet;
import jdplus.toolkit.base.tsp.DataSource;
import internal.toolkit.base.tsp.stream.NoOpTsStream;
import nbbrd.design.ThreadSafe;
import lombok.NonNull;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * Defines the ability to create a time series stream on a DataSource or a
 * DataSet. Note that the implementations must be thread-safe.
 *
 * @author Philippe Charles
 */
@ThreadSafe
public interface HasTsStream {

    /**
     * Creates a stream from a DataSource.
     *
     * @param dataSource the DataSource
     * @param type       the type of data to return
     * @return a new stream
     * @throws IllegalArgumentException if the DataSource doesn't belong to this
     *                                  provider.
     * @throws IOException              if an internal exception prevented data retrieval.
     */
    @NonNull
    Stream<DataSetTs> getData(@NonNull DataSource dataSource, @NonNull TsInformationType type) throws IllegalArgumentException, IOException;

    /**
     * Creates a stream from a DataSet.
     *
     * @param dataSet the DataSet
     * @param type    the type of data to return
     * @return a new stream
     * @throws IllegalArgumentException if the DataSet doesn't belong to this
     *                                  provider.
     * @throws IOException              if an internal exception prevented data retrieval.
     */
    @NonNull
    Stream<DataSetTs> getData(@NonNull DataSet dataSet, @NonNull TsInformationType type) throws IllegalArgumentException, IOException;

    static @NonNull HasTsStream noOp(@NonNull String providerName) {
        return new NoOpTsStream(providerName);
    }
}
