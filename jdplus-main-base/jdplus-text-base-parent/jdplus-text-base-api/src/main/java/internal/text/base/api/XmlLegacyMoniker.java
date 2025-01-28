/*
 * Copyright 2017 National Bank of Belgium
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
package internal.text.base.api;

import jdplus.toolkit.base.api.design.DemetraPlusLegacy;
import jdplus.toolkit.base.api.timeseries.TsMoniker;
import jdplus.text.base.api.XmlBean;
import jdplus.toolkit.base.tsp.DataSet;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.base.tsp.HasDataMoniker;
import jdplus.toolkit.base.tsp.legacy.LegacyFileId;
import jdplus.toolkit.base.tsp.util.DataSourcePreconditions;
import lombok.NonNull;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author Philippe Charles
 */
@DemetraPlusLegacy
@lombok.AllArgsConstructor(staticName = "of")
public final class XmlLegacyMoniker implements HasDataMoniker {

    private final String providerName;
    private final XmlParam param;

    @Override
    public @NonNull TsMoniker toMoniker(@NonNull DataSource dataSource) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);
        throw new IllegalArgumentException("Not supported yet.");
    }

    @Override
    public @NonNull TsMoniker toMoniker(@NonNull DataSet dataSet) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSet);
        throw new IllegalArgumentException("Not supported yet.");
    }

    @Override
    public @NonNull Optional<DataSource> toDataSource(@NonNull TsMoniker moniker) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, moniker);

        LegacyFileId id = LegacyFileId.parse(moniker.getId());
        return id != null ? Optional.of(toDataSource(Path.of(id.getFile()).toFile())) : Optional.empty();
    }

    @Override
    public @NonNull Optional<DataSet> toDataSet(@NonNull TsMoniker moniker) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, moniker);

        XmlLegacyId id = XmlLegacyId.parse(moniker.getId());
        return id != null ? Optional.of(toDataSet(id)) : Optional.empty();
    }

    private DataSet toDataSet(XmlLegacyId id) {
        DataSource source = toDataSource(Path.of(id.getFile()).toFile());
        if (!id.isSeries()) {
            DataSet.Builder result = DataSet.builder(source, DataSet.Kind.COLLECTION);
            param.getCollectionParam().set(result, id.getCollectionIndex());
            return result.build();
        }
        DataSet.Builder result = DataSet.builder(source, DataSet.Kind.COLLECTION);
        param.getCollectionParam().set(result, id.getCollectionIndex());
        param.getSeriesParam().set(result, id.getSeriesIndex());
        return result.build();
    }

    private DataSource toDataSource(File file) {
        XmlBean bean = new XmlBean();
        bean.setFile(file);
        DataSource.Builder result = DataSource.builder(providerName, param.getVersion());
        param.set(result, bean);
        return result.build();
    }
}
