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
import jdplus.toolkit.base.api.timeseries.TsDataTable;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.desktop.plugin.components.TsGridObs;
import lombok.NonNull;

import java.util.List;

/**
 *
 * @author Philippe Charles
 */
final class ByTsColumnGridData implements TsGridData {

    private final List<String> names;
    private final TsDataTable dataTable;
    private final TsDomain domain;
    private final TsDataTable.Cursor cursor;
    private final TsGridObs obs;

    public ByTsColumnGridData(List<Ts> col) {
        this.names = col.stream().map(Ts::getName).toList();
        this.dataTable = TsDataTable.of(col, Ts::getData);
        this.domain = dataTable.getDomain();
        this.cursor = dataTable.cursor(TsDataTable.DistributionType.FIRST);
        this.obs = new TsGridObs();
    }

    @Override
    public @NonNull String getRowName(int i) {
        return domain.get(i).getStartAsShortString();
    }

    @Override
    public @NonNull String getColumnName(int j) {
        return names.get(j);
    }

    @Override
    public @NonNull TsGridObs getObs(int period, int series) {
        cursor.moveTo(period, series);
        obs.setIndex(cursor.getIndex());
        obs.setPeriod(dataTable.getData().get(series).getDomain().get(cursor.getIndex()));
        obs.setSeriesIndex(series);
        obs.setStatus(cursor.getStatus());
        obs.setValue(cursor.getValue());
        return obs;
    }

    @Override
    public int getRowCount() {
        return cursor.getPeriodCount();
    }

    @Override
    public int getColumnCount() {
        return cursor.getSeriesCount();
    }

    @Override
    public int getRowIndex(@NonNull ObsIndex index) {
        if (ObsIndex.NULL.equals(index)) {
            return NO_OBS_INDEX;
        }
        TsPeriod x = dataTable
                .getData().get(index.getSeries())
                .getDomain().get(index.getObs())
                .withUnit(domain.getTsUnit());
        return domain.indexOf(x);
    }

    @Override
    public int getColumnIndex(@NonNull ObsIndex index) {
        return index.getSeries();
    }
}
