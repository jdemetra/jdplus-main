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
package jdplus.sdmx.base.api.web;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import jdplus.toolkit.base.tsp.DataSet;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.base.tsp.cube.*;
import jdplus.toolkit.base.tsp.util.PropertyHandler;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import sdmxdl.DatabaseRef;

/**
 * @author Philippe Charles
 */
interface SdmxWebParam extends DataSource.Converter<SdmxWebBean> {

    String getVersion();

    DataSet.Converter<CubeId> getCubeIdParam(CubeConnection connection) throws IOException;

    final class V1 implements SdmxWebParam {

        private static final BulkCube DEFAULT_BULK =
                BulkCube.builder().ttl(Duration.ofMinutes(5)).depth(1).build();

        @lombok.experimental.Delegate
        private final DataSource.Converter<SdmxWebBean> converter = SdmxWebBeanHandler.builder()
                .source(PropertyHandler.onString("dbName", ""))
                .database(PropertyHandler.onString("db", DatabaseRef.NO_DATABASE.toString()))
                .flow(PropertyHandler.onString("tableName", ""))
                .dimensions(PropertyHandler.onStringList("dimColumns", List.of(), 'c'))
                .labelAttribute(PropertyHandler.onString("l", ""))
                .cache(BulkCubeHandler.builder()
                        .ttl(PropertyHandler.onDurationInMillis("cacheTtl", DEFAULT_BULK.getTtl()))
                        .depth(PropertyHandler.onInteger("cacheDepth", DEFAULT_BULK.getDepth()))
                        .build())
                .build()
                .asDataSourceConverter();

        @Override
        public String getVersion() {
            return "20150203";
        }

        @Override
        public DataSet.Converter<CubeId> getCubeIdParam(CubeConnection connection) throws IOException {
            return CubeSupport.idByName(connection.getRoot());
        }
    }

    @lombok.Builder(toBuilder = true)
    final class SdmxWebBeanHandler implements PropertyHandler<SdmxWebBean> {

        @lombok.NonNull private final PropertyHandler<String> source;

        @lombok.NonNull private final PropertyHandler<String> database;

        @lombok.NonNull private final PropertyHandler<String> flow;

        @lombok.NonNull private final PropertyHandler<List<String>> dimensions;

        @lombok.NonNull private final PropertyHandler<String> labelAttribute;

        @lombok.NonNull private final PropertyHandler<BulkCube> cache;

        @Override
        public @NonNull SdmxWebBean get(@NonNull Function<? super String, ? extends CharSequence> properties) {
            SdmxWebBean result = new SdmxWebBean();
            result.setSource(source.get(properties));
            result.setDatabase(database.get(properties));
            result.setFlow(flow.get(properties));
            result.setDimensions(dimensions.get(properties));
            result.setLabelAttribute(labelAttribute.get(properties));
            result.setCache(cache.get(properties));
            return result;
        }

        @Override
        public void set(@NonNull BiConsumer<? super String, ? super String> properties, @Nullable SdmxWebBean value) {
            if (value != null) {
                source.set(properties, value.getSource());
                database.set(properties, value.getDatabase());
                flow.set(properties, value.getFlow());
                dimensions.set(properties, value.getDimensions());
                labelAttribute.set(properties, value.getLabelAttribute());
                cache.set(properties, value.getCache());
            }
        }
    }
}
