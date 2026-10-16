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
package jdplus.sdmx.base.api.web;

import internal.sdmx.base.api.SdmxBeans;
import internal.sdmx.base.api.SdmxCubeConnection;
import internal.sdmx.base.api.SdmxPropertiesSupport;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import jdplus.sdmx.base.api.HasSdmxProperties;
import jdplus.toolkit.base.api.timeseries.TsProvider;
import jdplus.toolkit.base.tsp.*;
import jdplus.toolkit.base.tsp.cube.BulkCubeConnection;
import jdplus.toolkit.base.tsp.cube.CubeConnection;
import jdplus.toolkit.base.tsp.cube.CubeSupport;
import jdplus.toolkit.base.tsp.stream.HasTsStream;
import jdplus.toolkit.base.tsp.stream.TsStreamAsProvider;
import jdplus.toolkit.base.tsp.util.ResourcePool;
import jdplus.toolkit.base.tsp.util.ShortLivedCachingLoader;
import nbbrd.io.Resource;
import nbbrd.service.ServiceProvider;
import sdmxdl.Connection;
import sdmxdl.FlowRef;
import sdmxdl.web.SdmxWebManager;

/**
 * @author Philippe Charles
 * @since 2.2.0
 */
@ServiceProvider(TsProvider.class)
public final class SdmxWebProvider implements DataSourceLoader<SdmxWebBean>, HasSdmxProperties<SdmxWebManager> {

    private static final String NAME = "DOTSTAT";

    private final AtomicBoolean displayCodes;

    @lombok.experimental.Delegate
    private final HasSdmxProperties<SdmxWebManager> properties;

    @lombok.experimental.Delegate
    private final HasDataSourceMutableList mutableListSupport;

    @lombok.experimental.Delegate
    private final HasDataMoniker monikerSupport;

    @lombok.experimental.Delegate
    private final HasDataSourceBean<SdmxWebBean> beanSupport;

    @lombok.experimental.Delegate(excludes = HasTsStream.class)
    private final CubeSupport cubeSupport;

    @lombok.experimental.Delegate
    private final TsProvider tsSupport;

    public SdmxWebProvider() {
        this.displayCodes = new AtomicBoolean(false);

        ResourcePool<CubeConnection> pool = CubeSupport.newConnectionPool();
        SdmxWebParam param = new SdmxWebParam.V1();

        this.properties = SdmxPropertiesSupport.of(SdmxWebManager::ofServiceLoader, pool::clear);
        this.mutableListSupport = HasDataSourceMutableList.of(NAME, pool::remove);
        this.monikerSupport = HasDataMoniker.usingUri(NAME);
        this.beanSupport = HasDataSourceBean.of(NAME, param, param.getVersion());
        this.cubeSupport = CubeSupport.of(
                NAME,
                pool.asFactory(o -> openConnection(o, properties, param, displayCodes.get())),
                param::getCubeIdParam);
        this.tsSupport = TsStreamAsProvider.of(NAME, cubeSupport, monikerSupport, pool::clear);
    }

    @Override
    public @lombok.NonNull String getDisplayName() {
        return "SDMX Web Services";
    }

    public boolean isDisplayCodes() {
        return displayCodes.get();
    }

    public void setDisplayCodes(boolean displayCodes) {
        boolean old = this.displayCodes.get();
        if (this.displayCodes.compareAndSet(old, displayCodes)) {
            clearCache();
        }
    }

    private static CubeConnection openConnection(
            DataSource source, HasSdmxProperties<SdmxWebManager> properties, SdmxWebParam param, boolean displayCodes)
            throws IOException {
        SdmxWebBean bean = param.get(source);

        FlowRef flow = FlowRef.parse(bean.getFlow());

        Connection conn = properties.getSdmxManager().getConnection(bean.getSource(), properties.getLanguages());
        try {
            CubeConnection result = SdmxCubeConnection.of(
                    conn,
                    SdmxBeans.getDatabase(bean),
                    flow,
                    bean.getDimensions(),
                    bean.getLabelAttribute(),
                    bean.getSource(),
                    displayCodes);
            return BulkCubeConnection.of(result, bean.getCache(), ShortLivedCachingLoader.get());
        } catch (IOException ex) {
            Resource.ensureClosed(ex, conn);
            throw ex;
        }
    }
}
