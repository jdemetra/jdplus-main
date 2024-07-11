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
package jdplus.sql.base.api.jdbc;

import internal.sql.base.api.DefaultConnectionSource;
import internal.sql.base.api.jdbc.JdbcParam;
import jdplus.sql.base.api.ConnectionManager;
import jdplus.sql.base.api.ConnectionSource;
import jdplus.sql.base.api.HasSqlProperties;
import jdplus.sql.base.api.SqlTableAsCubeResource;
import jdplus.toolkit.base.api.timeseries.TsProvider;
import jdplus.toolkit.base.tsp.*;
import jdplus.toolkit.base.tsp.cube.*;
import jdplus.toolkit.base.tsp.stream.HasTsStream;
import jdplus.toolkit.base.tsp.stream.TsStreamAsProvider;
import jdplus.toolkit.base.tsp.util.ResourcePool;
import jdplus.toolkit.base.tsp.util.ShortLivedCachingLoader;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import nbbrd.sql.jdbc.SqlConnectionSupplier;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider(TsProvider.class)
public final class JdbcProvider implements DataSourceLoader<JdbcBean>, HasSqlProperties {

    private static final String NAME = "JNDI-JDBC";

    @lombok.experimental.Delegate
    private final HasSqlProperties properties;

    @lombok.experimental.Delegate
    private final HasDataSourceMutableList mutableListSupport;

    @lombok.experimental.Delegate
    private final HasDataMoniker monikerSupport;

    @lombok.experimental.Delegate
    private final HasDataSourceBean<JdbcBean> beanSupport;

    @lombok.experimental.Delegate(excludes = HasTsStream.class)
    private final CubeSupport cubeSupport;

    @lombok.experimental.Delegate
    private final TsProvider tsSupport;

    public JdbcProvider() {
        ResourcePool<CubeConnection> pool = CubeSupport.newConnectionPool();
        JdbcParam param = new JdbcParam.V1();

        this.properties = HasSqlProperties.of(JdbcConnectionManager::new, pool::clear);
        this.mutableListSupport = HasDataSourceMutableList.of(NAME, pool::remove);
        this.monikerSupport = HasDataMoniker.usingUri(NAME);
        this.beanSupport = HasDataSourceBean.of(NAME, param, param.getVersion());
        this.cubeSupport = CubeSupport.of(NAME, pool.asFactory(o -> openConnection(o, properties, param)), param::getCubeIdParam);
        this.tsSupport = TsStreamAsProvider.of(NAME, cubeSupport, monikerSupport, pool::clear);
    }

    @Override
    public @NonNull String getDisplayName() {
        return "JDBC resource";
    }

    private static CubeConnection openConnection(DataSource key, HasSqlProperties properties, JdbcParam param) {
        JdbcBean bean = param.get(key);

        SqlTableAsCubeResource sqlResource = SqlTableAsCubeResource
                .builder()
                .source(properties.getConnectionManager().getSource(bean.getDatabase()))
                .table(bean.getTable())
                .root(toRoot(bean))
                .tdp(toDataParams(bean))
                .gathering(bean.getCube().getGathering())
                .labelColumn(bean.getCube().getLabel())
                .build();

        CubeConnection result = TableAsCubeConnection.of(sqlResource);
        return BulkCubeConnection.of(result, bean.getCache(), ShortLivedCachingLoader.get());
    }

    private static CubeId toRoot(JdbcBean bean) {
        return CubeId.root(bean.getCube().getDimensions());
    }

    private static TableDataParams toDataParams(JdbcBean bean) {
        return TableDataParams.builder()
                .periodColumn(bean.getCube().getTimeDimension())
                .valueColumn(bean.getCube().getMeasure())
                .versionColumn(bean.getCube().getVersion())
                .obsFormat(bean.getCube().getFormat())
                .build();
    }

    private static final class JdbcConnectionManager implements ConnectionManager {

        private final SqlConnectionSupplier supplier = SqlConnectionSupplier.ofJndi();

        @Override
        public @NonNull String getId() {
            return "jndi";
        }

        @Override
        public @NonNull ConnectionSource getSource(@NonNull String connectionString) {
            return new DefaultConnectionSource(supplier, connectionString);
        }
    }
}
