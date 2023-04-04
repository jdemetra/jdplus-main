package internal.spreadsheet.desktop.plugin;

import jdplus.toolkit.base.tsp.grid.GridReader;
import jdplus.toolkit.base.tsp.grid.GridWriter;

@lombok.Data
class SpreadSheetDataTransferBean {

    private boolean importTs = true;
    private GridReader tsReader = GridReader.DEFAULT;

    private boolean exportTs = true;
    private GridWriter tsWriter = GridWriter.DEFAULT;
    
    private boolean importMatrix = true;
    private boolean exportMatrix = true;

    private boolean importTable = true;
    private boolean exportTable = true;
}
