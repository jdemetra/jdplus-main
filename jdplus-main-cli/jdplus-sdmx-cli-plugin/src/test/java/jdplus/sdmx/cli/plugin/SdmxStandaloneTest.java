package jdplus.sdmx.cli.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ServiceLoader;
import jdplus.sdmx.base.api.file.SdmxFileProvider;
import jdplus.sdmx.base.api.web.SdmxWebProvider;
import jdplus.toolkit.base.api.timeseries.TsProvider;
import org.junit.jupiter.api.Test;

public class SdmxStandaloneTest {

    @Test
    public void testProviders() {
        assertThat(ServiceLoader.load(TsProvider.class).stream().map(ServiceLoader.Provider::type))
                .contains(SdmxFileProvider.class, SdmxWebProvider.class);
    }

    @Test
    public void testStandaloneManagers() {
        try (SdmxFileProvider fileProvider = new SdmxFileProvider();
                SdmxWebProvider webProvider = new SdmxWebProvider()) {
            assertThat(fileProvider.getSdmxManager().getReaders()).isNotEmpty();
            assertThat(webProvider.getSdmxManager().getDrivers()).isNotEmpty();
            assertThat(webProvider.getSdmxManager().getDefaultSources()).isNotEmpty();
        }
    }
}
