package jdplus.toolkit.desktop.plugin.core.components;

import ec.util.chart.ObsIndex;
import jdplus.toolkit.desktop.plugin.components.TsGridObs;
import lombok.NonNull;

enum EmptyGridData implements TsGridData {

    INSTANCE;

    @Override
    public int getColumnCount() {
        return 0;
    }

    @Override
    public @NonNull String getColumnName(int j) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getRowCount() {
        return 0;
    }

    @Override
    public @NonNull String getRowName(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public @NonNull TsGridObs getObs(int i, int j) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getRowIndex(@NonNull ObsIndex index) {
        return NO_OBS_INDEX;
    }

    @Override
    public int getColumnIndex(@NonNull ObsIndex index) {
        return NO_OBS_INDEX;
    }
}
