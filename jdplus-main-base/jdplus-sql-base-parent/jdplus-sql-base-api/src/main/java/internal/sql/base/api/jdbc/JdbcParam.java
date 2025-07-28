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
package internal.sql.base.api.jdbc;

import jdplus.sql.base.api.jdbc.JdbcBean;
import jdplus.toolkit.base.api.timeseries.util.ObsGathering;
import jdplus.toolkit.base.tsp.DataSet;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.base.tsp.cube.*;
import jdplus.toolkit.base.tsp.legacy.LegacyHandler;
import jdplus.toolkit.base.tsp.util.ObsFormat;
import jdplus.toolkit.base.tsp.util.PropertyHandler;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static jdplus.toolkit.base.tsp.util.PropertyHandler.*;

/**
 * @author Philippe Charles
 */
public interface JdbcParam extends DataSource.Converter<JdbcBean> {

    String getVersion();

    DataSet.@NonNull Converter<CubeId> getCubeIdParam(@NonNull CubeConnection connection) throws IOException;

    final class V1 implements JdbcParam {

        private static final BulkCube DEFAULT_BULK = BulkCube.builder().ttl(Duration.ofMinutes(5)).depth(1).build();

        @lombok.experimental.Delegate
        private final DataSource.Converter<JdbcBean> converter =
                JdbcBeanHandler
                        .builder()
                        .database(onString("dbName", ""))
                        .table(onString("tableName", ""))
                        .cube(
                                TableAsCubeHandler
                                        .builder()
                                        .dimensions(onStringList("dimColumns", Collections.emptyList(), ','))
                                        .timeDimension(onString("periodColumn", ""))
                                        .measure(onString("valueColumn", ""))
                                        .format(LegacyHandler.onObsFormat("locale", "datePattern", "numberPattern", ObsFormat.getSystemDefault()))
                                        .version(onString("versionColumn", ""))
                                        .label(onString("labelColumn", ""))
                                        .gathering(LegacyHandler.onObsGathering("frequency", "aggregationType", "cleanMissing", ObsGathering.DEFAULT))
                                        .build()
                        )
                        .cache(
                                BulkCubeHandler
                                        .builder()
                                        .ttl(PropertyHandler.onDurationInMillis("cacheTtl", DEFAULT_BULK.getTtl()))
                                        .depth(PropertyHandler.onInteger("cacheDepth", DEFAULT_BULK.getDepth()))
                                        .build()
                        )
                        .build()
                        .asDataSourceConverter();

        @Override
        public String getVersion() {
            return "20131203";
        }


        @Override
        public DataSet.Converter<CubeId> getCubeIdParam(CubeConnection connection) throws IOException {
            return CubeSupport.idByName(connection.getRoot());
        }
    }

    @lombok.Builder(toBuilder = true)
    final class JdbcBeanHandler implements PropertyHandler<JdbcBean> {

        @lombok.NonNull
        private final PropertyHandler<String> database;

        @lombok.NonNull
        private final PropertyHandler<String> table;

        @lombok.NonNull
        private final PropertyHandler<TableAsCube> cube;

        @lombok.NonNull
        private final PropertyHandler<BulkCube> cache;

        @Override
        public @NonNull JdbcBean get(@NonNull Function<? super String, ? extends CharSequence> properties) {
            JdbcBean result = new JdbcBean();
            result.setDatabase(database.get(properties));
            result.setTable(table.get(properties));
            result.setCube(cube.get(properties));
            result.setCache(cache.get(properties));
            return result;
        }

        @Override
        public void set(@NonNull BiConsumer<? super String, ? super String> properties, @Nullable JdbcBean value) {
            if (value != null) {
                database.set(properties, value.getDatabase());
                table.set(properties, value.getTable());
                cube.set(properties, value.getCube());
                cache.set(properties, value.getCache());
            }
        }
    }
}
