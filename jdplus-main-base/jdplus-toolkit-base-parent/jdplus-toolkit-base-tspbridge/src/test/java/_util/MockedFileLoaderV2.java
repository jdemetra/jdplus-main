package _util;

import jdplus.toolkit.base.tspbridge.TsConverter;
import ec.tss.tsproviders.IFileLoader;

public final class MockedFileLoaderV2 implements IFileLoader {

    @lombok.experimental.Delegate
    private final IFileLoader delegate = (IFileLoader) TsConverter.fromTsProvider(new MockedFileLoader());
}
