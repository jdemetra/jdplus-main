package jdplus.text.base.api;

import jdplus.toolkit.base.api.timeseries.TsCollection;
import jdplus.toolkit.base.api.timeseries.TsProvider;
import jdplus.toolkit.base.tsp.*;
import jdplus.toolkit.base.tsp.stream.HasTsStream;
import jdplus.toolkit.base.tsp.stream.TsStreamAsProvider;
import jdplus.toolkit.base.tsp.util.FallbackDataMoniker;
import jdplus.toolkit.base.tsp.util.ImmutableValuePool;
import internal.text.base.api.*;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import lombok.NonNull;

import java.io.IOException;

@DirectImpl
@ServiceProvider(TsProvider.class)
public final class TxtProvider implements FileLoader<TxtBean> {

    public static final String NAME = "Txt";

    @lombok.experimental.Delegate
    private final HasDataSourceMutableList mutableListSupport;

    @lombok.experimental.Delegate
    private final HasDataMoniker monikerSupport;

    @lombok.experimental.Delegate
    private final HasDataSourceBean<TxtBean> beanSupport;

    @lombok.experimental.Delegate
    private final HasFilePaths filePathSupport;

    @lombok.experimental.Delegate
    private final HasDataDisplayName displayNameSupport;

    @lombok.experimental.Delegate(excludes = HasTsStream.class, types = HasDataHierarchy.class)
    private final TxtSupport txtSupport;

    @lombok.experimental.Delegate
    private final TsProvider tsSupport;

    @lombok.experimental.Delegate
    private final TxtFileFilter fileFilter;

    public TxtProvider() {
        TxtParam param = new TxtParam.V1();

        ImmutableValuePool<TsCollection> pool = ImmutableValuePool.of();

        this.mutableListSupport = HasDataSourceMutableList.of(NAME, pool::remove);
        this.monikerSupport = FallbackDataMoniker.of(HasDataMoniker.usingUri(NAME), TxtLegacyMoniker.of(NAME, param));
        this.beanSupport = HasDataSourceBean.of(NAME, param, param.getVersion());
        this.filePathSupport = HasFilePaths.of(pool::clear);
        this.displayNameSupport = TxtDataDisplayName.of(NAME, param, pool::peek);
        this.txtSupport = TxtSupport.of(NAME, pool.asFactory(dataSource -> getData(dataSource, filePathSupport, param)), ignore -> param.getSeriesParam());
        this.tsSupport = TsStreamAsProvider.of(NAME, txtSupport, monikerSupport, pool::clear);
        this.fileFilter = new TxtFileFilter();
    }

    @Override
    public @NonNull String getDisplayName() {
        return "Txt files";
    }

    private static @NonNull TsCollection getData(@NonNull DataSource dataSource, HasFilePaths paths, TxtParam param) throws IOException {
        return TxtLoader.load(param.get(dataSource), paths);
    }
}
