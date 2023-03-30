package _util;

import jdplus.toolkit.base.api.timeseries.TsProvider;
import jdplus.toolkit.base.tsp.*;
import jdplus.toolkit.base.tsp.stream.HasTsStream;
import jdplus.toolkit.base.tsp.stream.TsStreamAsProvider;
import nbbrd.io.function.IORunnable;

import java.util.Collections;

public final class MockedDataSourceProvider implements DataSourceProvider {

    private static final String NAME = "mocked";

    @lombok.experimental.Delegate
    private final HasDataSourceList dataSourceMutableList = HasDataSourceList.of(NAME, Collections.emptyList());

    @lombok.experimental.Delegate
    private final HasDataHierarchy dataHierarchy = HasDataHierarchy.noOp(NAME);

    @lombok.experimental.Delegate
    private final HasDataDisplayName dataDisplayName = HasDataDisplayName.usingUri(NAME);

    @lombok.experimental.Delegate
    private final HasDataMoniker dataMoniker = HasDataMoniker.usingUri(NAME);

    @lombok.experimental.Delegate
    private final TsProvider provider = TsStreamAsProvider.of(NAME, HasTsStream.noOp(NAME), dataMoniker, IORunnable.noOp().asUnchecked());
}
