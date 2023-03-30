package _util;

import jdplus.toolkit.base.tspbridge.TsConverter;
import ec.tss.tsproviders.IDataSourceLoader;

public final class MockedDataSourceLoaderV2 implements IDataSourceLoader {

    @lombok.experimental.Delegate
    private final IDataSourceLoader delegate = (IDataSourceLoader) TsConverter.fromTsProvider(new MockedDataSourceLoader());
}
