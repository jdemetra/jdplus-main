/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.datatransfer;

import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.timeseries.TsCollection;
import jdplus.toolkit.base.api.util.Table;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * @author Philippe Charles
 */
@ServiceProvider(service = DataTransferSpi.class, position = LocalObjectDataTransfer.POSITION)
public final class LocalObjectDataTransfer implements DataTransferSpi {

    static final int POSITION = 0;

    public static final DataFlavor DATA_FLAVOR = DataTransfers.newLocalObjectDataFlavor(LocalObjectDataTransfer.class);

    public LocalObjectDataTransfer() {
    }

    @Override
    public int getPosition() {
        return POSITION;
    }

    @Override
    public String getName() {
        return "LocalObject";
    }

    @Override
    public String getDisplayName() {
        return "Local Object";
    }

    @Override
    public DataFlavor getDataFlavor() {
        return DATA_FLAVOR;
    }

    @Override
    public boolean canExportTsCollection(TsCollection col) {
        return true;
    }

    @Override
    public Object exportTsCollection(TsCollection col) {
        return col;
    }

    @Override
    public boolean canImportTsCollection(Object obj) {
        return obj instanceof TsCollection;
    }

    @Override
    public TsCollection importTsCollection(Object obj) throws IOException {
        return (TsCollection) obj;
    }

    @Nullable
    public TsCollection peekTsCollection(@NonNull Transferable t) {
        if (t.isDataFlavorSupported(DATA_FLAVOR)) {
            try {
                Object data = t.getTransferData(DATA_FLAVOR);
                if (canImportTsCollection(data)) {
                    return importTsCollection(data);
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    @Override
    public boolean canExportMatrix(Matrix matrix) {
        return true;
    }

    @Override
    public Object exportMatrix(Matrix matrix) throws IOException {
        return matrix;
    }

    @Override
    public boolean canImportMatrix(Object obj) {
        return obj instanceof Matrix;
    }

    @Override
    public Matrix importMatrix(Object obj) throws IOException, ClassCastException {
        return (Matrix) obj;
    }

    @Override
    public boolean canExportTable(Table<?> table) {
        return true;
    }

    @Override
    public Object exportTable(Table<?> table) throws IOException {
        return table;
    }

    @Override
    public boolean canImportTable(Object obj) {
        return obj instanceof Table;
    }

    @Override
    public Table<?> importTable(Object obj) throws IOException, ClassCastException {
        return (Table<?>) obj;
    }
}
