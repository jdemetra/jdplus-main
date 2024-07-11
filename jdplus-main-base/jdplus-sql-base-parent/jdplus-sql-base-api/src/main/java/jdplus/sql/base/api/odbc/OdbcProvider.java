/*
 * Copyright 2015 National Bank of Belgium
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
package jdplus.sql.base.api.odbc;

import internal.sql.base.api.DefaultConnectionSource;
import internal.sql.base.api.odbc.OdbcParam;
import internal.sql.base.api.odbc.legacy.LegacyOdbcMoniker;
import jdplus.sql.base.api.ConnectionManager;
import jdplus.sql.base.api.ConnectionSource;
import jdplus.sql.base.api.HasSqlProperties;
import jdplus.sql.base.api.SqlTableAsCubeResource;
import jdplus.toolkit.base.api.timeseries.TsProvider;
import jdplus.toolkit.base.tsp.*;
import jdplus.toolkit.base.tsp.cube.*;
import jdplus.toolkit.base.tsp.stream.HasTsStream;
import jdplus.toolkit.base.tsp.stream.TsStreamAsProvider;
import jdplus.toolkit.base.tsp.util.FallbackDataMoniker;
import jdplus.toolkit.base.tsp.util.ResourcePool;
import jdplus.toolkit.base.tsp.util.ShortLivedCachingLoader;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import nbbrd.sql.jdbc.SqlConnectionSupplier;
import nbbrd.sql.odbc.OdbcConnectionSupplier;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author Philippe Charles
 */
@lombok.extern.java.Log
@DirectImpl
@ServiceProvider(TsProvider.class)
public final class OdbcProvider implements DataSourceLoader<OdbcBean>, HasSqlProperties {

    private static final String NAME = "ODBCPRVDR";

    @lombok.experimental.Delegate
    private final HasSqlProperties properties;

    @lombok.experimental.Delegate
    private final HasDataSourceMutableList mutableListSupport;

    @lombok.experimental.Delegate
    private final HasDataMoniker monikerSupport;

    @lombok.experimental.Delegate
    private final HasDataSourceBean<OdbcBean> beanSupport;

    @lombok.experimental.Delegate(excludes = HasTsStream.class)
    private final CubeSupport cubeSupport;

    @lombok.experimental.Delegate
    private final TsProvider tsSupport;

    public OdbcProvider() {
        ResourcePool<CubeConnection> pool = CubeSupport.newConnectionPool();
        OdbcParam param = new OdbcParam.V1();

        this.properties = HasSqlProperties.of(OdbcConnectionManager::new, pool::clear);
        this.mutableListSupport = HasDataSourceMutableList.of(NAME, pool::remove);
        this.monikerSupport = FallbackDataMoniker.of(HasDataMoniker.usingUri(NAME), LegacyOdbcMoniker.of(NAME, param));
        this.beanSupport = HasDataSourceBean.of(NAME, param, param.getVersion());
        this.cubeSupport = CubeSupport.of(NAME, pool.asFactory(o -> openConnection(o, properties, param)), param::getIdParam);
        this.tsSupport = TsStreamAsProvider.of(NAME, cubeSupport, monikerSupport, pool::clear);
    }

    @Override
    public @NonNull String getDisplayName() {
        return "ODBC DSNs";
    }

    private static CubeConnection openConnection(DataSource key, HasSqlProperties properties, OdbcParam param) {
        OdbcBean bean = param.get(key);

        SqlTableAsCubeResource sqlResource = SqlTableAsCubeResource
                .builder()
                .source(properties.getConnectionManager().getSource(bean.getDsn()))
                .table(bean.getTable())
                .root(toRoot(bean))
                .tdp(toDataParams(bean))
                .gathering(bean.getCube().getGathering())
                .labelColumn(bean.getCube().getLabel())
                .build();

        CubeConnection result = TableAsCubeConnection.of(sqlResource);
        return BulkCubeConnection.of(result, bean.getCache(), ShortLivedCachingLoader.get());
    }

    private static CubeId toRoot(OdbcBean bean) {
        return CubeId.root(bean.getCube().getDimensions());
    }

    private static TableDataParams toDataParams(OdbcBean bean) {
        return TableDataParams.builder()
                .periodColumn(bean.getCube().getTimeDimension())
                .valueColumn(bean.getCube().getMeasure())
                .versionColumn(bean.getCube().getVersion())
                .obsFormat(bean.getCube().getFormat())
                .build();
    }

    private static final class OdbcConnectionManager implements ConnectionManager {

        private final OdbcConnectionSupplier supplier = OdbcConnectionSupplier.ofServiceLoader().orElse(null);

        @Override
        public @NonNull String getId() {
            return "odbc";
        }

        @Override
        public @NonNull ConnectionSource getSource(@NonNull String connectionString) {
            if (supplier != null) {
                if (supplier.getName().equals("internal.sql.lhod.LhodDriver")) {
                    return new LhodConnectionSource(connectionString, supplier, new ArrayBlockingQueue<>(2));
                }
                return new DefaultConnectionSource(supplier, connectionString);
            }
            return new DefaultConnectionSource(SqlConnectionSupplier.noOp(), connectionString);
        }
    }

    @lombok.AllArgsConstructor
    private static final class LhodConnectionSource implements ConnectionSource {

        private final String id;
        private final OdbcConnectionSupplier delegate;
        private final ArrayBlockingQueue<ShortLivedConnection> cache;

        @Override
        public @NonNull String getId() {
            return id;
        }

        @Override
        public @NonNull Connection open() throws SQLException {
            ShortLivedConnection result = cache.poll();
            if (result != null) return result;
//            log.info("Opening new connection to " + id + " (cacheSize=" + cache.size() + ")");
            return new ShortLivedConnection(delegate.getConnection(id), cache);
        }
    }

    @lombok.AllArgsConstructor
    private static final class ShortLivedConnection implements Connection {

        @lombok.experimental.Delegate
        private final Connection delegate;
        private final Queue<ShortLivedConnection> cache;

        @Override
        public void close() throws SQLException {
            if (!cache.offer(this)) {
                delegate.close();
            }
        }
    }
}
