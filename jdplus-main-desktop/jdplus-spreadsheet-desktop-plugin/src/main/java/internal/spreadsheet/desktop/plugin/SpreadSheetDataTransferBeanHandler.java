package internal.spreadsheet.desktop.plugin;

import jdplus.toolkit.base.tsp.grid.GridReader;
import jdplus.toolkit.base.tsp.util.PropertyHandler;
import lombok.NonNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

@lombok.Builder(toBuilder = true)
final class SpreadSheetDataTransferBeanHandler implements PropertyHandler<SpreadSheetDataTransferBean> {

    @lombok.NonNull
    private final PropertyHandler<Boolean> importTs;

    @lombok.NonNull
    private final PropertyHandler<GridReader> tsReader;

    @lombok.NonNull
    private final PropertyHandler<Boolean> exportTs;

    @lombok.NonNull
    private final PropertyHandler<Boolean> importMatrix;

    @lombok.NonNull
    private final PropertyHandler<Boolean> exportMatrix;

    @lombok.NonNull
    private final PropertyHandler<Boolean> importTable;

    @lombok.NonNull
    private final PropertyHandler<Boolean> exportTable;

    @Override
    public @NonNull SpreadSheetDataTransferBean get(@NonNull Function<? super String, ? extends CharSequence> properties) {
        SpreadSheetDataTransferBean result = new SpreadSheetDataTransferBean();
        result.setImportTs(importTs.get(properties));
        result.setTsReader(tsReader.get(properties));
        result.setExportTs(exportTs.get(properties));
        result.setImportMatrix(importMatrix.get(properties));
        result.setExportMatrix(exportMatrix.get(properties));
        result.setImportTable(importTable.get(properties));
        result.setExportTable(exportTable.get(properties));
        return result;
    }

    @Override
    public void set(@NonNull BiConsumer<? super String, ? super String> properties, SpreadSheetDataTransferBean value) {
        if (value != null) {
            importTs.set(properties, value.isImportTs());
            tsReader.set(properties, value.getTsReader());
            exportTs.set(properties, value.isExportTs());
            importMatrix.set(properties, value.isImportMatrix());
            exportMatrix.set(properties, value.isExportMatrix());
            importTable.set(properties, value.isImportTable());
            exportTable.set(properties, value.isExportTable());
        }
    }
}
