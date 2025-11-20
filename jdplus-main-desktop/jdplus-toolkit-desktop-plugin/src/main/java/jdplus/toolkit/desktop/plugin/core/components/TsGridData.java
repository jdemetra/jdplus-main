/*
 * Copyright 2013 National Bank of Belgium
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
package jdplus.toolkit.desktop.plugin.core.components;

import ec.util.chart.ObsIndex;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.desktop.plugin.components.TsGridObs;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.NonNegative;

import java.util.Collections;
import java.util.List;

/**
 * @author Philippe Charles
 */
sealed interface TsGridData permits ByTsColumnGridData, ByAnnualFrequencyColumnGridData, EmptyGridData {

    @NonNegative
    int getColumnCount();

    @NonNull
    String getColumnName(int j);

    @NonNegative
    int getRowCount();

    @NonNull
    String getRowName(int i);

    @NonNull
    TsGridObs getObs(int i, int j);

    int getRowIndex(@NonNull ObsIndex index);

    int getColumnIndex(@NonNull ObsIndex index);

    int NO_SINGLE_SERIES_INDEX = -1;

    int NO_OBS_INDEX = -1;

    @StaticFactoryMethod
    static @NonNull TsGridData create(@NonNull List<Ts> col, int singleSeriesIndex) {
        if (col.isEmpty() || (singleSeriesIndex != NO_SINGLE_SERIES_INDEX && col.get(singleSeriesIndex).getData().isEmpty())) {
            return EmptyGridData.INSTANCE;
        }
        if (singleSeriesIndex == NO_SINGLE_SERIES_INDEX) {
            return new ByTsColumnGridData(col);
        }
        Ts series = col.get(singleSeriesIndex);
        return series.getData().getAnnualFrequency() == TsUnit.NO_ANNUAL_FREQUENCY
                ? new ByTsColumnGridData(Collections.singletonList(series))
                : new ByAnnualFrequencyColumnGridData(series, singleSeriesIndex);
    }
}
