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
package internal.sql.base.api.odbc.legacy;

import jdplus.toolkit.base.api.design.DemetraPlusLegacy;
import internal.sql.base.api.odbc.OdbcParam;
import jdplus.sql.base.api.odbc.OdbcBean;
import jdplus.toolkit.base.api.timeseries.TsMoniker;
import jdplus.toolkit.base.tsp.DataSet;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.base.tsp.HasDataMoniker;
import jdplus.toolkit.base.tsp.cube.TableAsCube;
import jdplus.toolkit.base.tsp.util.DataSourcePreconditions;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Philippe Charles
 */
@DemetraPlusLegacy
@lombok.AllArgsConstructor(staticName = "of")
public final class LegacyOdbcMoniker implements HasDataMoniker {

    private final String providerName;
    private final OdbcParam param;

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

        LegacyOdbcId id = LegacyOdbcId.parse(moniker.getId());
        return id != null ? Optional.of(toDataSource(id)) : Optional.empty();
    }

    @Override
    public @NonNull Optional<DataSet> toDataSet(@NonNull TsMoniker moniker) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, moniker);

        LegacyOdbcId id = LegacyOdbcId.parse(moniker.getId());
        return id != null ? Optional.of(toDataSet(id)) : Optional.empty();
    }

    private DataSource toDataSource(LegacyOdbcId id) {
        DataSource.Builder result = DataSource.builder(providerName, param.getVersion());
        param.set(result, toBean(id));
        return result.build();
    }

    private OdbcBean toBean(LegacyOdbcId id) {
        OdbcBean result = new OdbcBean();
        result.setDsn(id.getDbName());
        result.setTable(id.getTable());
        result.setCube(TableAsCube
                .builder()
                .dimensions(Arrays.asList(id.getDomainColumn(), id.getSeriesColumn()))
                .timeDimension(id.getPeriodColumn())
                .measure(id.getValueColumn())
                .build());
        return result;
    }

    private DataSet toDataSet(LegacyOdbcId id) {
        DataSource source = toDataSource(id);
        if (id.isMultiCollection()) {
            return DataSet.of(source, DataSet.Kind.COLLECTION);
        }
        if (id.isCollection()) {
            return DataSet
                    .builder(source, DataSet.Kind.COLLECTION)
                    .parameter(id.getDomainColumn(), id.getDomainName())
                    .build();
        }
        return DataSet
                .builder(source, DataSet.Kind.SERIES)
                .parameter(id.getDomainColumn(), id.getDomainName())
                .parameter(id.getSeriesColumn(), id.getSeriesName())
                .build();
    }
}
