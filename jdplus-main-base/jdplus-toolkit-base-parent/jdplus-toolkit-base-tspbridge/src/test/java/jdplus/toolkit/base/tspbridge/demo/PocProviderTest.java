/*
 * Copyright 2018 National Bank of Belgium
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
package jdplus.toolkit.base.tspbridge.demo;

import jdplus.toolkit.base.tsp.DataSet;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.base.tsp.DataSourceProvider;
import lombok.NonNull;
import org.junit.jupiter.api.Test;
import tck.demetra.tsp.DataSourceProviderAssert;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;

import static jdplus.toolkit.base.api.timeseries.TsInformationType.All;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Philippe Charles
 */
@lombok.extern.java.Log
public class PocProviderTest {

    @Test
    public void testTspCompliance() {
        var sampler = new DataSourceProviderAssert.Sampler<>() {
            @Override
            public @NonNull Optional<DataSource> dataSource(@NonNull DataSourceProvider p) {
                return p.getDataSources().stream().findFirst();
            }

            @Override
            public @NonNull Optional<DataSet> tsDataSet(@NonNull DataSourceProvider p) {
                return dataSource(p).map(o -> {
                    try {
                        return p.children(o).get(0);
                    } catch (IllegalArgumentException | IOException ex) {
                        log.log(Level.SEVERE, "Unexpected error in '#toDataSet(DataSourceProvider)'", ex);
                    }
                    return null;
                });
            }

            @Override
            public @NonNull Optional<DataSet> tsCollectionDataSet(@NonNull DataSourceProvider p) {
                return dataSource(p).map(o -> {
                    try {
                        return p.children(o).get(0);
                    } catch (IllegalArgumentException | IOException ex) {
                        log.log(Level.SEVERE, "Unexpected error in '#tsCollectionDataSet(DataSourceProvider)'", ex);
                    }
                    return null;
                });
            }
        };
        DataSourceProviderAssert.assertCompliance(PocProvider::new, sampler);
    }

    @Test
    public void testSample() throws IOException {
        try (var x = new PocProvider()) {
            DataSource normalSource = DataSource.of(PocProvider.NAME, "");

            assertThat(x.getDataSources())
                    .hasSize(6)
                    .contains(normalSource, atIndex(0));

            assertThat(x.getDisplayName()).isEqualTo("Proof-of-concept");
            assertThat(x.getSource()).isEqualTo("poc");

            assertThatObject(x.getTsCollection(x.toMoniker(normalSource), All))
                    .returns(PocProvider.NAME, o -> o.getMoniker().getSource())
                    .returns(25, o -> o.getItems().size());
        }
    }
}
