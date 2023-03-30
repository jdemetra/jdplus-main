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
package jdplus.toolkit.base.tsp.poc;

import jdplus.toolkit.base.api.timeseries.TsProvider;
import jdplus.toolkit.base.tsp.DataSourceLoader;
import jdplus.toolkit.base.tsp.HasDataMoniker;
import jdplus.toolkit.base.tsp.HasDataSourceBean;
import jdplus.toolkit.base.tsp.HasDataSourceMutableList;
import jdplus.toolkit.base.tsp.cube.CubeConnection;
import jdplus.toolkit.base.tsp.cube.CubeSupport;
import jdplus.toolkit.base.tsp.stream.HasTsStream;
import jdplus.toolkit.base.tsp.stream.TsStreamAsProvider;
import jdplus.toolkit.base.tsp.util.ResourcePool;

/**
 * @author Philippe Charles
 */
public final class FakeDbProvider implements DataSourceLoader<FakeDbBean> {

    private static final String NAME = "FakeDbProvider";

    @lombok.experimental.Delegate
    private final HasDataSourceMutableList mutableListSupport;

    @lombok.experimental.Delegate
    private final HasDataMoniker monikerSupport;

    @lombok.experimental.Delegate
    private final HasDataSourceBean<FakeDbBean> beanSupport;

    @lombok.experimental.Delegate(excludes = HasTsStream.class)
    private final CubeSupport cubeSupport;

    @lombok.experimental.Delegate
    private final TsProvider tsSupport;

    public FakeDbProvider() {
        ResourcePool<CubeConnection> pool = CubeSupport.newConnectionPool();
        FakeDbParam param = new FakeDbParam.V1();

        this.mutableListSupport = HasDataSourceMutableList.of(NAME, pool::remove);
        this.monikerSupport = HasDataMoniker.usingUri(NAME);
        this.beanSupport = HasDataSourceBean.of(NAME, param, param.getVersion());
        this.cubeSupport = CubeSupport.of(NAME, pool.asFactory(o -> new FakeDbConnection()), param::getCubeIdParam);
        this.tsSupport = TsStreamAsProvider.of(NAME, cubeSupport, monikerSupport, pool::clear);
    }
}
