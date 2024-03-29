/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.util;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 *
 * @author Philippe Charles
 */
public abstract class ListTableModel<T> extends AbstractTableModel {

    abstract protected List<String> getColumnNames();

    abstract protected List<T> getValues();

    abstract protected Object getValueAt(T row, int columnIndex);

    @Override
    public int getColumnCount() {
        return getColumnNames().size();
    }

    @Override
    public String getColumnName(int column) {
        return getColumnNames().get(column);
    }

    @Override
    public int getRowCount() {
        return getValues().size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return getValueAt(getValues().get(rowIndex), columnIndex);
    }
}
