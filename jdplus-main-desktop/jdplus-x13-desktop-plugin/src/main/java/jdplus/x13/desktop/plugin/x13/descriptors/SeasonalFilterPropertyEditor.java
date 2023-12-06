/*
 * Copyright 2023 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.x13.desktop.plugin.x13.descriptors;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;
import jdplus.toolkit.desktop.plugin.util.NbComponents;
import jdplus.toolkit.base.api.timeseries.calendars.CalendarUtility;
import jdplus.x13.base.api.x11.SeasonalFilterOption;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Jean Palate
 */
public class SeasonalFilterPropertyEditor extends AbstractPropertyEditor {

    private SeasonalFilterOption[] filters;

    public SeasonalFilterPropertyEditor() {
        editor = new SeasonalFilterEditor();
    }
    
    void fireChanged(SeasonalFilterOption[] filters) {
        SeasonalFilterOption[] old = this.filters;
        this.filters = filters;
        firePropertyChange(old, this.filters);
    }

    @Override
    public Object getValue() {
        return filters;
    }

    @Override
    public void setValue(Object value) {
        if (null != value && value instanceof SeasonalFilterOption[]) {
            filters = (SeasonalFilterOption[]) value;
            ((SeasonalFilterEditor) editor).setFilters(filters);
        }
    }

    class SeasonalFilterEditor extends JPanel {

        private SeasonalFilterOption[] nfilters;

        public SeasonalFilterEditor() {
            final JButton button = new JButton("...");
            button.addActionListener(event -> {
                JPanel pane = new JPanel(new BorderLayout());
                final JTable table = new JTable(
                        new DefaultTableModel() {
                            
                            @Override
                            public int getColumnCount() {
                                return 2;
                            }
                            
                            @Override
                            public String getColumnName(int column) {
                                if (column == 0) {
                                    return "Period";
                                } else {
                                    return "Filter";
                                }
                            }
                            
                            @Override
                            public Class<?> getColumnClass(int columnIndex) {
                                if (columnIndex == 0) {
                                    return String.class;
                                } else {
                                    return SeasonalFilterOption.class;
                                }
                            }
                            
                            @Override
                            public boolean isCellEditable(int row, int column) {
                                return column == 1;
                            }
                            
                            @Override
                            public int getRowCount() {
                                return nfilters.length;
                            }
                            
                            @Override
                            public Object getValueAt(int row, int column) {
                                if (column == 0) {
                                    return CalendarUtility.formatPeriod(nfilters.length, row);
                                } else {
                                    return nfilters[row];
                                }
                            }
                            
                            @Override
                            public void setValueAt(Object aValue, int row, int column) {
                                if (column == 1) {
                                    nfilters[row] = (SeasonalFilterOption) aValue;
                                }
                                fireTableCellUpdated(row, column);
                            }
                        });
                
                table.setDefaultEditor(SeasonalFilterOption.class, new CustomFilterEditor());
                table.setFillsViewportHeight(true);
                pane.add(NbComponents.newJScrollPane(table), BorderLayout.CENTER);
                
                Window ancestor = SwingUtilities.getWindowAncestor(button);
                JDialog dlg = new JDialog(ancestor, Dialog.ModalityType.TOOLKIT_MODAL);
                dlg.setContentPane(pane);
                dlg.addWindowListener(new WindowAdapter() {
                    
                    @Override
                    public void windowClosing(WindowEvent e) {
                        if (table.getCellEditor() != null) {
                            table.getCellEditor().stopCellEditing();
                        }
                        fireChanged(nfilters);
                    }
                });
                dlg.setMinimumSize(new Dimension(300, 300));
                dlg.setModal(true);
                dlg.setLocationRelativeTo(ancestor);
                dlg.setVisible(true);
                if (table.getCellEditor() != null) {
                    table.getCellEditor().stopCellEditing();
                }
            });

            setLayout(new BorderLayout());
            add(button, BorderLayout.CENTER);
        }

        public void setFilters(final SeasonalFilterOption[] param) {
            nfilters = param.clone();
        }
    }
}

class CustomFilterEditor extends AbstractCellEditor implements TableCellEditor {

    private final JComboBox cb;

    public CustomFilterEditor() {
        DefaultComboBoxModel model = new DefaultComboBoxModel(SeasonalFilterOption.values());
        cb = new JComboBox(model);
    }

    @Override
    public Object getCellEditorValue() {
        return cb.getSelectedItem();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        cb.setSelectedItem(value);
        return cb;
    }
}
