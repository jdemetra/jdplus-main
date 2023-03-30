package _util;

import jdplus.toolkit.base.tspbridge.TsConverter;
import ec.tss.tsproviders.IDataSourceProvider;

public final class MockedDataSourceProviderV2 implements IDataSourceProvider {

    @lombok.experimental.Delegate
    private final IDataSourceProvider delegate = (IDataSourceProvider) TsConverter.fromTsProvider(new MockedDataSourceProvider());
}
