/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package jdplus.tramoseats.desktop.plugin.anomalydetection.ui;

import ec.util.list.swing.JLists;
import jdplus.toolkit.base.api.timeseries.TsCollection;
import jdplus.toolkit.desktop.plugin.components.parts.HasTsCollection;
import jdplus.toolkit.desktop.plugin.DemetraIcons;
import jdplus.tramoseats.desktop.plugin.anomalydetection.AnomalyItem;
import jdplus.toolkit.desktop.plugin.components.JTsTable;
import ec.util.table.swing.JTables;
import ec.util.various.swing.JCommand;
import jdplus.toolkit.desktop.plugin.components.TsSelectionBridge;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import jdplus.toolkit.desktop.plugin.components.TsIdentifier;
import jdplus.toolkit.desktop.plugin.datatransfer.DataTransferManager;
import jdplus.main.desktop.design.SwingComponent;
import jdplus.main.desktop.design.SwingProperty;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsMoniker;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.tramoseats.base.api.tramo.TramoSpec;
import jdplus.toolkit.base.api.util.MultiLineNameUtil;
import jdplus.toolkit.base.api.util.Table;
import java.util.LinkedHashMap;
import java.util.OptionalInt;
import jdplus.toolkit.base.api.timeseries.TsInformationType;

import jdplus.toolkit.base.core.regsarima.regular.CheckLast;
import jdplus.tramoseats.base.core.tramo.TramoKernel;
import nbbrd.design.SkipProcessing;

/**
 * List component containing input and output results of a Check Last batch
 * processing
 *
 * @author Mats Maggi
 */
@SwingComponent
public final class JTsCheckLastList extends JComponent {

    @SkipProcessing(target = SwingProperty.class, reason = "to be refactored")
    @SwingProperty
    public static final String COLOR_VALUES_PROPERTY = "colorValues";

    @SwingProperty
    public static final String LAST_CHECKS_PROPERTY = "lastChecks";

    @SwingProperty
    public static final String SPEC_PROPERTY = "spec";

    @SkipProcessing(target = SwingProperty.class, reason = "to be refactored")
    @SwingProperty
    public static final String COLLECTION_CHANGE_PROPERTY = "collectionChange";

    @SkipProcessing(target = SwingProperty.class, reason = "to be refactored")
    @SwingProperty
    public static final String ITEM_SELECTION_PROPERTY = "itemSelection";

    @lombok.experimental.Delegate(types = HasTsCollection.class)
    private final JTsTable table;

    private double orangeCells;
    private double redCells;
    private int lastChecks;
    private TramoSpec spec;

    private final LinkedHashMap<TsMoniker, AnomalyItem> map = new LinkedHashMap<>();
    private CheckLast checkLast;

    public JTsCheckLastList() {
        this.table = new JTsTable(TsInformationType.Data);
        this.orangeCells = 4.0;
        this.redCells = 5.0;
        this.lastChecks = 1;
        this.spec = TramoSpec.TRfull;

        initTable();

        checkLast = new CheckLast(TramoKernel.of(spec, null), 12);

//        onComponentPopupMenuChange();
        enableProperties();

        setLayout(new BorderLayout());
        add(table, BorderLayout.CENTER);
    }

    private void enableProperties() {
        table.addPropertyChangeListener(evt -> {
            switch (evt.getPropertyName()) {
                case HasTsCollection.TS_COLLECTION_PROPERTY:
                    onCollectionChange();
                    break;
                case TsSelectionBridge.TS_SELECTION_PROPERTY:
                    onSelectionChange();
                    break;
            }
        });

        addPropertyChangeListener(evt -> {
            switch (evt.getPropertyName()) {
                case COLOR_VALUES_PROPERTY:
                    onColorValuesChange();
                    break;
                case LAST_CHECKS_PROPERTY:
                    onLastChecksChange();
                    break;
                case SPEC_PROPERTY:
                    onSpecChange();
                    break;
            }
        });
    }
    
    //<editor-fold defaultstate="collapsed" desc="Getters/Setters">

    public double getOrangeCells() {
        return orangeCells;
    }

    public void setOrangeCells(double orangeCells) {
        if (orangeCells < 0 || orangeCells > redCells) {
            throw new IllegalArgumentException("Orange value must be >= 0 and < Red value");
        }
        double old = this.orangeCells;
        this.orangeCells = orangeCells;
        firePropertyChange(COLOR_VALUES_PROPERTY, old, this.orangeCells);
    }

    public double getRedCells() {
        return redCells;
    }

    public void setRedCells(double redCells) {
        if (redCells < orangeCells) {
            throw new IllegalArgumentException("Red value must be greater than Orange value");
        }
        double old = this.redCells;
        this.redCells = redCells;
        firePropertyChange(COLOR_VALUES_PROPERTY, old, this.redCells);
    }

    public int getLastChecks() {
        return lastChecks;
    }

    public void setLastChecks(int lastChecks) {
        if (lastChecks < 1 || lastChecks > 3) {
            throw new IllegalArgumentException("Number of last checked values can only be 1, 2 or 3 !");
        }
        int old = this.lastChecks;
        this.lastChecks = lastChecks;
        firePropertyChange(LAST_CHECKS_PROPERTY, old, this.lastChecks);
    }

    public TramoSpec getSpec() {
        return spec;
    }

    public void setSpec(TramoSpec spec) {
        TramoSpec old = this.spec;
        this.spec = spec;
        firePropertyChange(SPEC_PROPERTY, old, this.spec);
    }
    //</editor-fold>

    private void resetValues() {

        for (AnomalyItem item : map.values()) {
            item.reset(lastChecks);
        }
        checkLast = new CheckLast(TramoKernel.of(spec, null), lastChecks);
    }

    public CheckLast getCheckLast() {
        return checkLast;
    }

    public Map<TsMoniker, AnomalyItem> getMap() {
        return map;
    }

    public AnomalyItem[] getItems() {
        AnomalyItem[] all = new AnomalyItem[map.size()];
        return map.values().toArray(all);
    }

    public Ts getSelectedItem() {
        OptionalInt singleSelection = JLists.getSelectionIndexStream(table.getTsSelectionModel()).findFirst();
        return singleSelection.isPresent() ? table.getTsCollection().get(singleSelection.getAsInt()) : null;
    }
    
    public void fireTableStructureChanged() {
        List<JTsTable.Column> columns = new ArrayList<>();
        columns.add(seriesColumn);
        columns.add(lastPeriodColumn);
        columns.add(abs1Column);
        columns.add(rel1Column);
        if (lastChecks > 1) {
            columns.add(abs2Column);
            columns.add(rel2Column);
        }
        if (lastChecks > 2) {
            columns.add(abs3Column);
            columns.add(rel3Column);
        }
        table.setColumns(columns);
        switch (lastChecks) {
            case 1:
                table.setWidthAsPercentages(new double[]{.7, .1, .1, .1});
                break;
            case 2:
                table.setWidthAsPercentages(new double[]{.5, .1, .1, .1, .1, .1});
                break;
            case 3:
                table.setWidthAsPercentages(new double[]{.3, .1, .1, .1, .1, .1, .1, .1});
                break;
        }
    }

    public void fireTableDataChanged() {
        table.repaint();
    }

//    private JPopupMenu buildPopupMenu() {
//        JPopupMenu result = HasTsCollectionSupport.newDefaultMenu(this).getPopupMenu();
//
//        int index = 11;
//        JMenuItem item;
//
//        result.insert(new JSeparator(), index++);
//
//        JMenu sub = new JMenu("Export results to");
//        sub.add(new CopyToClipoard().toAction(this)).setText("Clipboard");
//        result.insert(sub, index++);
//
//        item = new JMenuItem(new AbstractAction("Original Order") {
//            @Override
//            public void actionPerformed(ActionEvent arg0) {
////                table.getRowSorter().setSortKeys(null);
//            }
//        });
//        item.setEnabled(true);
//        result.insert(item, index++);
//
//        return result;
//    }
//
    private void initTable() {
//        table.setMultiSelection(false);
//        table.getTsSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        result.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
//        ((JLabel) result.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        table.addPropertyChangeListener(evt -> {
            switch (evt.getPropertyName()) {
                case HasTsCollection.DROP_CONTENT_PROPERTY:
                case HasTsCollection.FREEZE_ON_IMPORT_PROPERTY:
                case HasTsCollection.TS_COLLECTION_PROPERTY:
                case HasTsCollection.TS_SELECTION_MODEL_PROPERTY:
                case HasTsCollection.TS_UPDATE_MODE_PROPERTY:
                case TsSelectionBridge.TS_SELECTION_PROPERTY:
                    firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                    break;
            }
        });

//        ActionMaps.copyEntries(getActionMap(), false, table.getActionMap());
//        InputMaps.copyEntries(getInputMap(), false, table.getInputMap());
        fireTableStructureChanged();
    }

    private void onColorValuesChange() {
        fireTableDataChanged();
    }

    private void onLastChecksChange() {
        resetValues();
        fireTableStructureChanged();
    }

    private void onSpecChange() {
        resetValues();
        fireTableDataChanged();
    }

    private void onCollectionChange() {
        TsCollection collection = getTsCollection();
        LinkedHashMap<TsMoniker, AnomalyItem> omap=new LinkedHashMap<>(map);
        map.clear();
        for (Ts s : collection) {
            AnomalyItem item = omap.get(s.getMoniker());
            if (item == null) {
                String name = s.getName();
                name = MultiLineNameUtil.join(name);
                AnomalyItem a = new AnomalyItem(name, s.getData(), lastChecks);
                map.put(s.getMoniker(), a);
            }
            else
                map.put(s.getMoniker(), item);
        }
        table.getTsSelectionModel().clearSelection();
        fireTableDataChanged();
        firePropertyChange(COLLECTION_CHANGE_PROPERTY, null, collection);
    }

    private void onSelectionChange() {
        Optional<Ts> fts = table.getTsSelectionStream().findFirst();
        AnomalyItem selected = null;
        if (fts.isPresent()) {
            selected = map.get(fts.orElseThrow().getMoniker());
            if (!selected.isProcessed()) {
                CheckLast cl = new CheckLast(TramoKernel.of(spec, null), lastChecks);
                selected.process(cl);
                table.repaint();
            }
        }
        firePropertyChange(ITEM_SELECTION_PROPERTY, null, selected);
    }

    private Optional<AnomalyItem> getAnomaly(Ts ts) {
        AnomalyItem item = map.get(ts.getMoniker());
        return Optional.ofNullable(item);
    }

//    private void onComponentPopupMenuChange() {
//        JPopupMenu popupMenu = getComponentPopupMenu();
//        table.setComponentPopupMenu(popupMenu != null ? popupMenu : buildPopupMenu());
//    }
//
    private final JTsTable.Column seriesColumn = JTsTable.Column.builder()
            .name("<html><center>&nbsp;<br>Series Name<br>&nbsp;")
            .type(Ts.class)
            .mapper(ts -> TsIdentifier.of(ts))
            .comparator(TS_COMP)
            .comparator(JTsTable.Column.TS_IDENTIFIER.getComparator())
            .renderer(o -> new Decorator(JTsTable.Column.TS_IDENTIFIER.getRenderer().apply(o)))
            .build();

    private final JTsTable.Column lastPeriodColumn = JTsTable.Column.builder()
            .name("<html><center>&nbsp;Last<br>Period<br>&nbsp;")
            .type(TsPeriod.class)
            .mapper(ts -> getAnomaly(ts).map(o -> o.getData().isEmpty() ? null : o.getData().getDomain().getLastPeriod()).orElse(null))
            .comparator(JTsTable.Column.LAST.getComparator())
            .renderer(o -> new Decorator(JTsTable.Column.LAST.getRenderer().apply(o)))
            .build();

    private final JTsTable.Column abs1Column = JTsTable.Column.builder()
            .name("<html><center>Abs.<br>Error<br>N-1")
            .type(Double.class)
            .mapper(ts -> getAnomaly(ts).map(o -> o.isProcessed() ? o.getAbsoluteError(0) : null).orElse(null))
            .comparator(DOUBLE_COMP)
            .renderer(o -> new Decorator(JTables.cellRendererOf(JTsCheckLastList::apply)))
            .build();

    private final JTsTable.Column rel1Column = JTsTable.Column.builder()
            .name("<html><center>Rel.<br>Error<br>N-1")
            .type(Double.class)
            .mapper(ts -> getAnomaly(ts).map(o -> o.isProcessed() ? o.getRelativeError(0) : null).orElse(null))
            .comparator(DOUBLE_COMP)
            .renderer(o -> new Decorator(JTables.cellRendererOf(JTsCheckLastList::apply)))
            .build();

    private final JTsTable.Column abs2Column = JTsTable.Column.builder()
            .name("<html><center>Abs.<br>Error<br>N-2")
            .type(Double.class)
            .mapper(ts -> getAnomaly(ts).map(o -> o.isProcessed() ? o.getAbsoluteError(1) : null).orElse(null))
            .comparator(DOUBLE_COMP)
            .renderer(o -> new Decorator(JTables.cellRendererOf(JTsCheckLastList::apply)))
            .build();

    private final JTsTable.Column rel2Column = JTsTable.Column.builder()
            .name("<html><center>Rel.<br>Error<br>N-2")
            .type(Double.class)
            .mapper(ts -> getAnomaly(ts).map(o -> o.isProcessed() ? o.getRelativeError(1) : null).orElse(null))
            .comparator(DOUBLE_COMP)
            .renderer(o -> new Decorator(JTables.cellRendererOf(JTsCheckLastList::apply)))
            .build();

    private final JTsTable.Column abs3Column = JTsTable.Column.builder()
            .name("<html><center>Abs.<br>Error<br>N-3")
            .type(Double.class)
            .mapper(ts -> getAnomaly(ts).map(o -> o.isProcessed() ? o.getAbsoluteError(2) : null).orElse(null))
            .comparator(DOUBLE_COMP)
            .renderer(o -> new Decorator(JTables.cellRendererOf(JTsCheckLastList::apply)))
            .build();

    private final JTsTable.Column rel3Column = JTsTable.Column.builder()
            .name("<html><center>Rel.<br>Error<br>N-3")
            .type(Double.class)
            .mapper(ts -> getAnomaly(ts).map(o -> o.isProcessed() ? o.getRelativeError(2) : null).orElse(null))
            .comparator(DOUBLE_COMP)
            .renderer(o -> new Decorator(JTables.cellRendererOf(JTsCheckLastList::apply)))
            .build();

    private static void apply(JLabel l, Double value) {
        l.setHorizontalAlignment(JLabel.TRAILING);
    }

    public Map getReportParameters() {
        Map parameters = new HashMap();
        parameters.put("_SPECIFICATION", spec.toString());
        parameters.put("_NB_CHECK_LAST", lastChecks);
        parameters.put("_NB_OF_SERIES", map.size());
        parameters.put("_ORANGE_CELLS", orangeCells);
        parameters.put("_RED_CELLS", redCells);

        return parameters;
    }

    private static abstract class ModelCommand extends JCommand<JTsCheckLastList> {

        @Override
        public boolean isEnabled(JTsCheckLastList list) {
            return !list.getMap().isEmpty();
        }

        @Override
        public JCommand.ActionAdapter toAction(JTsCheckLastList list) {
            return super.toAction(list).withWeakPropertyChangeListener(list, COLLECTION_CHANGE_PROPERTY);
        }
    }

    private static Table<Object> toTable(JTsCheckLastList list) {
        int nback = list.getLastChecks();
        int cols = nback < 2 ? 5 : nback > 2 ? 9 : 7;
        Table<Object> table = new Table<>(list.getMap().size() + 1, cols);

        table.set(0, 0, "Series name");
        table.set(0, 1, "Last Period");
        table.set(0, 2, "Status");
        table.set(0, 3, "Abs. Error (n-1)");
        table.set(0, 4, "Rel. Error (n-1)");
        if (nback > 1) {
            table.set(0, 5, "Abs. Error (n-2)");
            table.set(0, 6, "Rel. Error (n-2)");
        }
        if (nback > 2) {
            table.set(0, 7, "Abs. Error (n-3)");
            table.set(0, 8, "Rel. Error (n-3)");
        }

        int row = 1;
        for (Map.Entry<TsMoniker, AnomalyItem> entry : list.map.entrySet()) {
            AnomalyItem item = entry.getValue();
            table.set(row, 0, item.getName());

            if (!item.getData().isEmpty()) {
                table.set(row, 1, item.getData().getDomain().getLastPeriod().getStartAsShortString());
            }

            table.set(row, 2, item.getStatus().toString());
            table.set(row, 3, item.getAbsoluteError(0));
            table.set(row, 4, item.getRelativeError(0));

            if (nback > 1) {
                table.set(row, 5, item.getAbsoluteError(1));
                table.set(row, 6, item.getRelativeError(1));
            }

            if (nback > 2) {
                table.set(row, 7, item.getAbsoluteError(2));
                table.set(row, 8, item.getRelativeError(2));
            }
            row++;
        }
        return table;
    }

    private static final class CopyToClipoard extends ModelCommand {

        @Override
        public void execute(JTsCheckLastList component) throws Exception {
            Transferable t = DataTransferManager.get().fromTable(toTable(component));
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(t, null);
        }
    }

    @lombok.AllArgsConstructor
    private final class Decorator implements TableCellRenderer {

        @lombok.NonNull
        private final TableCellRenderer delegate;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component result = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (result instanceof JLabel) {
                JLabel c = (JLabel) result;
                int rowIndex = table.convertRowIndexToModel(row);
                TsCollection coll = getTsCollection();
                if (coll.size() > row) {
                    AnomalyItem item = map.get(coll.get(rowIndex).getMoniker());
                    c.setOpaque(true);
                    if (!isSelected) {
                        c.setOpaque(true);
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                        c.setToolTipText(null);
                        c.setEnabled(true);
                        if (item.isNotProcessable()) {
                            if (column == 0) {
                                c.setIcon(DemetraIcons.WARNING);
                            }
                            c.setBackground(new Color(255, 255, 204));
                            c.setToolTipText(UNPROCESSABLE_MSG);
                        } else if (item.isProcessed()) {
                            if (column > 2 && column % 2 != 0) {
                                int relIndex = (column / 2) - 1;
                                Double relative_err = item.getRelativeError(relIndex);
                                if (relative_err != null) {
                                    relative_err = Math.abs(relative_err);
                                    if (relative_err >= orangeCells && relative_err < redCells) {
                                        c.setBackground(Color.ORANGE);
                                    } else if (relative_err > redCells) {
                                        c.setBackground(new Color(255, 102, 102));
                                    }
                                }
                            }
                        } else if (item.isInvalid()) {
                            if (column == 0) {
                                c.setIcon(DemetraIcons.EXCLAMATION_MARK_16);
                            }
                            c.setBackground(new Color(255, 204, 204));
                            c.setToolTipText(NO_DATA_MSG);
                        }
                    } else if (item.isInvalid()) {
                        if (column == 0) {
                            c.setIcon(DemetraIcons.EXCLAMATION_MARK_16);
                        }
                        c.setToolTipText(NO_DATA_MSG);
                    } else if (item.isNotProcessable()) {
                        if (column == 0) {
                            c.setIcon(DemetraIcons.WARNING);
                        }
                        c.setToolTipText(UNPROCESSABLE_MSG);
                    }
                }
            }
            return result;
        }
    }

    private static final String UNPROCESSABLE_MSG = "Check Last can't be processed !";
    private static final String NO_DATA_MSG = "Invalid or empty data !";
    private static final Comparator<Double> DOUBLE_COMP = Comparator.comparingDouble(Math::abs);
    private static final Comparator<Ts> TS_COMP = Comparator.comparing(Ts::getName);
}
