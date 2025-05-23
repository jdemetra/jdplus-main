/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.desktop.plugin.multiprocessing.ui;

import jdplus.toolkit.desktop.plugin.components.GridCommands;
import jdplus.sa.desktop.plugin.util.ActionsHelper;
import jdplus.sa.desktop.plugin.util.ActionsHelpers;
import jdplus.toolkit.desktop.plugin.util.NbComponents;
import jdplus.toolkit.base.api.information.Explorable;
import jdplus.toolkit.base.api.information.formatters.TableFormatter;
import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;
import jdplus.sa.base.api.SaDictionaries;
import jdplus.sa.base.api.SaItem;
import jdplus.sa.base.api.SaManager;
import jdplus.sa.base.api.SaProcessingFactory;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.dictionaries.ArimaDictionaries;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import jdplus.toolkit.base.api.dictionaries.LikelihoodDictionaries;
import jdplus.toolkit.base.api.dictionaries.RegArimaDictionaries;
import jdplus.toolkit.base.api.dictionaries.RegressionDictionaries;
import jdplus.toolkit.base.api.dictionaries.ResidualsDictionaries;
import jdplus.toolkit.base.api.util.MultiLineNameUtil;
import jdplus.toolkit.base.api.util.Table;
import ec.util.grid.swing.AbstractGridModel;
import ec.util.grid.swing.JGrid;
import ec.util.list.swing.JLists;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.Map.Entry;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;

/**
 *
 * @author Philippe Charles
 */
public final class MatrixView extends AbstractSaProcessingTopComponent implements MultiViewElement {

    private final ExplorerManager mgr = new ExplorerManager();

    private static final DecimalFormat df3 = new DecimalFormat("0.000", DecimalFormatSymbols.getInstance(Locale.getDefault(Locale.Category.FORMAT)));
    private final static int MAXBIAS = 1, SKEWNESS = 2, KURTOSIS = 3, LB = 4, LBS = 5, LB2 = 6, TD_PEAK = 7, S_PEAK = 8, TD_VPEAK = 9, S_VPEAK = 10, S_VAR = 11, I_VAR = 12, SI_CORR = 13, M_START = 11;
//    private final static String[] TESTS_TS = new String[]{"max bias", "skewness", "kurtosis", "ljung-box", "lb on seas.", "lb on sq.", "td peak", "seas peak", "visual td peak", "visual s. peak", "s_var", "i var", "s-i corr"};
//    private final static String[] TESTS_X12 = new String[]{"max bias", "skewness", "kurtosis", "ljung-box", "lb on seas.", "lb on sq.", "td peak", "seas peak", "visual td peak", "visual s. peak", "m1", "m2", "m3", "m4", "m5", "m6", "m7", "m8", "m9", "m10", "m11", "q", "q-m2"};
    // main components
    private final JComponent visualRepresentation;
    private final JToolBar toolBarRepresentation;
    // subcomponents
    private final JComboBox<Entry<Integer, AlgorithmDescriptor>> comboBox;
    private final JGrid resMatrix_, calMatrix_, armaMatrix_, outMatrix_, testMatrix_, customMatrix_;
    private final JTabbedPane matrixTabPane_;
//    private final PropertyChangeListener listener;

    public MatrixView(MultiProcessingController controller) {
        super(controller);
        this.comboBox = new JComboBox<>();

        comboBox.setRenderer(JLists.cellRendererOf((label, value) -> {
            if (value != null) {
                label.setText(TaggedTreeNode.freqName(value.getKey()) + " > " + value.getValue().getName());
            }
        }));
        comboBox.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED && event.getItem() != null) {
                Entry<Integer, AlgorithmDescriptor> item = (Entry<Integer, AlgorithmDescriptor>) event.getItem();
                updateMatrix(item.getValue(), item.getKey());
            } else {
                clearMatrices();
            }
        });

        matrixTabPane_ = new JTabbedPane();
        matrixTabPane_.addTab("Main", resMatrix_ = createMatrix());
        matrixTabPane_.addTab("Calendar", calMatrix_ = createMatrix());
        matrixTabPane_.addTab("Outliers", outMatrix_ = createMatrix());
        matrixTabPane_.addTab("Arma", armaMatrix_ = createMatrix());
        matrixTabPane_.addTab("Tests", testMatrix_ = createMatrix());
        matrixTabPane_.addTab("Custom", customMatrix_ = createMatrix());

        toolBarRepresentation = NbComponents.newInnerToolbar();
        toolBarRepresentation.addSeparator();
        toolBarRepresentation.add(comboBox);

        visualRepresentation = matrixTabPane_;

//        listener = (PropertyChangeEvent evt) -> {
//            selectedComponents = selectedComponents();
//            Entry<Integer, AlgorithmDescriptor> item = (Entry<Integer, AlgorithmDescriptor>) comboBox.getSelectedItem();
//            customMatrix_.setModel(new TableModelAdapter(createTableModel(item.getValue(), item.getKey(), selectedComponents, selectedComponents)));
//        };
        updateData(new SaItem[0]);

        setLayout(new BorderLayout());
        add(toolBarRepresentation, BorderLayout.NORTH);
        add(visualRepresentation, BorderLayout.CENTER);
        associateLookup(ExplorerUtils.createLookup(mgr, getActionMap()));
    }

    public AlgorithmDescriptor activeMethod() {
        Entry<Integer, AlgorithmDescriptor> item = (Entry<Integer, AlgorithmDescriptor>) comboBox.getSelectedItem();
        if (item == null) {
            return null;
        } else {
            return item.getValue();
        }
    }

    public List<String> customItems() {
        AlgorithmDescriptor desc = activeMethod();
        if (desc == null) {
            return Collections.emptyList();
        }
        SaProcessingFactory factory = SaManager.factoryFor(desc);
        ActionsHelper helper = ActionsHelpers.getInstance().getHelperFor(factory);
        if (helper == null) {
            return Collections.emptyList();
        } else {
            return helper.selectedMatrixItems();
        }
    }

    @Override
    protected void onSaProcessingStateChange() {
        super.onSaProcessingStateChange();
        if (getState().isFinished()) {
            updateData(current());
        } else {
            updateData(new SaItem[0]);
        }

    }

//    @Override
//    protected void onSaProcessingSaved(){
//        updateData(new SaItem[0]);
//    }
//    
    private static JGrid createMatrix() {
        final JGrid result = new JGrid();
        result.setDefaultRenderer(Object.class, new TableCellRenderer() {

            final TableCellRenderer delegate = result.getDefaultRenderer(Object.class);

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component result = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (result instanceof JLabel) {
                    ((JLabel) result).setHorizontalAlignment(JLabel.CENTER);
                }
                return result;
            }
        });

        JMenu menu = new JMenu();
        menu.add(GridCommands.copyAll(true, true).toAction(result)).setText("Copy");
        result.setComponentPopupMenu(menu.getPopupMenu());

        return result;
    }

    // MultiViewElement >
    @Override
    public JComponent getVisualRepresentation() {
        return visualRepresentation;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return toolBarRepresentation;
    }

    @Override
    public void componentClosed() {
        clearMatrices();
        super.componentClosed();
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }
    // < MultiViewElement

    private static ComboBoxModel<Entry<Integer, AlgorithmDescriptor>> asComboBoxModel(Map<Integer, List<AlgorithmDescriptor>> m) {
        DefaultComboBoxModel<Entry<Integer, AlgorithmDescriptor>> result = new DefaultComboBoxModel<>();
        for (Map.Entry<Integer, List<AlgorithmDescriptor>> item : m.entrySet()) {
            for (AlgorithmDescriptor ritem : item.getValue()) {
                result.addElement(new HashMap.SimpleImmutableEntry<>(item.getKey(), ritem));
            }
        }
        return result;
    }

    private static Map<Integer, List<AlgorithmDescriptor>> methods(SaItem[] items) {
        Map<Integer, List<AlgorithmDescriptor>> map = new HashMap<>();
        for (SaItem item : items) {
            AlgorithmDescriptor alg = item.getDefinition().getDomainSpec().getAlgorithmDescriptor();
            int period = item.getDefinition().getTs().getData().getAnnualFrequency();
            if (period > 0) {
                List<AlgorithmDescriptor> list = map.get(period);
                if (list == null) {
                    list = new ArrayList<>();
                    map.put(period, list);
                }
                if (!list.contains(alg)) {
                    list.add(alg);
                }
            }
        }
        return map;
    }

    private void updateData(SaItem[] saItems) {
        Map<Integer, List<AlgorithmDescriptor>> methods = methods(saItems);
        long count = methods.values().stream().mapToLong(Collection::size).sum();
        comboBox.setVisible(count > 1);
        comboBox.setModel(asComboBoxModel(methods));
        comboBox.setSelectedIndex(-1);
        if (!methods.isEmpty()) {
            comboBox.setSelectedIndex(0);
        }else{
            clearMatrices();
        }
    }

    private void clearMatrices() {
        resMatrix_.setModel(null);
        calMatrix_.setModel(null);
        outMatrix_.setModel(null);
        armaMatrix_.setModel(null);
        testMatrix_.setModel(null);
        customMatrix_.setModel(null);
    }

    private static final boolean SHORT = true;
    private static final int MAXLENGTH = 8;

    private void updateMatrix(AlgorithmDescriptor desc, int freq) {
        List<String> customItems = customItems();
        SaItem[] items = selectionFor(desc, freq);
        resMatrix_.setModel(new TableModelAdapter(createTableModel(items, Arrays.asList(MAIN_TITLE), Arrays.asList(MAIN))));
        calMatrix_.setModel(new TableModelAdapter(createTableModel(items, Arrays.asList(CALENDAR_TITLE), Arrays.asList(CALENDAR))));
        armaMatrix_.setModel(new TableModelAdapter(createTableModel(items, Arrays.asList(ARMA_TITLE), Arrays.asList(ARMA))));
        outMatrix_.setModel(new TableModelAdapter(createTableModel(items, Arrays.asList(OUTLIERS_TITLE), Arrays.asList(OUTLIERS))));
        testMatrix_.setModel(new TableModelAdapter(createTableModel(items, Arrays.asList(TESTS_TITLE), Arrays.asList(TESTS))));

        List<String> titles = !SHORT ? customItems : customItems.stream().map(s -> {
            if (s.length() > MAXLENGTH) {
                int idx = s.lastIndexOf('.');
                if (idx >= 0) {
                    s = s.substring(idx + 1);
                }
            }
            return s;
        }).toList();

        customMatrix_.setModel(new TableModelAdapter(createTableModel(items, titles, customItems)));
    }

    private static String arimaItem(String key) {
        return Dictionary.concatenate(RegArimaDictionaries.ARIMA, key);
    }

    private static String regressionItem(String key) {
        return Dictionary.concatenate(RegArimaDictionaries.REGRESSION, key);
    }

    private static String parameterItem(String str, int pos, int n) {
        StringBuilder builder = new StringBuilder();
        builder.append(str);
        if (pos != 0) {
            builder.append('(').append(pos).append(')');
        }
        if (n != 0) {
            builder.append(':').append(n);
        }
        return builder.toString();
    }

    private static String residualsItem(String key) {
        return Dictionary.concatenate(RegArimaDictionaries.RESIDUALS, key);
    }

    private static String advancedItem(String key) {
        return Dictionary.concatenate(RegArimaDictionaries.ADVANCED, key);
    }

    private static String mlItem(String key) {
        return Dictionary.concatenate(RegArimaDictionaries.MAX, key);
    }

    private static String llItem(String key) {
        return Dictionary.concatenate(RegArimaDictionaries.LIKELIHOOD, key);
    }

    private static final String[] MAIN = {
        regressionItem(RegressionDictionaries.ESPAN_N), SaDictionaries.SEASONAL, RegArimaDictionaries.LOG,
        regressionItem(RegArimaDictionaries.MEAN),
        arimaItem(ArimaDictionaries.P), arimaItem(ArimaDictionaries.D), arimaItem(ArimaDictionaries.Q),
        arimaItem(ArimaDictionaries.BP), arimaItem(ArimaDictionaries.BD), arimaItem(ArimaDictionaries.BQ),
        llItem(LikelihoodDictionaries.BICC), residualsItem(ResidualsDictionaries.SER)};

    private static final String[] MAIN_TITLE = {"N", "Seasonal", "Log", "Mean", "P", "D", "Q", "BP", "BD", "BQ", "BIC", "SE(res)"};

    private SaItem[] selectionFor(AlgorithmDescriptor method, int freq) {
        return Arrays.stream(current()).filter(item -> {
            AlgorithmDescriptor alg = item.getDefinition().getDomainSpec().getAlgorithmDescriptor();
            TsData ts = item.getDefinition().getTs().getData();
            return alg.equals(method) && ts.getAnnualFrequency() == freq && item.isProcessed();
        }).toArray(SaItem[]::new);
    }

    private TableModel createTableModel(SaItem[] curItems, List<String> titles, List<String> items) {
        DefaultTableModel rslt = new DefaultTableModel();
        List<String> names = new ArrayList<>();
        List<Explorable> rslts = new ArrayList<>();
        for (SaItem sa : curItems) {
            rslts.add(sa.getEstimation().getResults());
            names.add(MultiLineNameUtil.join(sa.getName()));
        }

        Table<String> srslts = TableFormatter.formatProcResults(rslts, items, true);
        int ncols = srslts.getColumnsCount();
        boolean[] ok = new boolean[ncols];
        int nused = 0;

        rslt.addColumn("Series");

        for (int idx = 0; idx < ncols; ++idx) {
            if (srslts.column(idx).isEmpty()) {
                ok[idx] = false;
            } else {
                ++nused;
                ok[idx] = true;
                rslt.addColumn(titles.get(idx));
            }
        }
        if (nused == 0) {
            return new DefaultTableModel();
        }

        for (int i = 0; i < names.size(); ++i) {
            String[] row = new String[nused + 1];
            row[0] = names.get(i);
            for (int j = 0, k = 0; j < ncols; ++j) {
                if (ok[j]) {
                    row[++k] = srslts.get(i, j);
                }
            }
            rslt.addRow(row);
        }

        return rslt;
    }

    private static final String[] CALENDAR = {
        RegArimaDictionaries.ADJUST,
        parameterItem(regressionItem(RegressionDictionaries.LP), 0, 2),
        parameterItem(regressionItem(RegressionDictionaries.TD), 1, 2),
        parameterItem(regressionItem(RegressionDictionaries.TD), 2, 2),
        parameterItem(regressionItem(RegressionDictionaries.TD), 3, 2),
        parameterItem(regressionItem(RegressionDictionaries.TD), 4, 2),
        parameterItem(regressionItem(RegressionDictionaries.TD), 5, 2),
        parameterItem(regressionItem(RegressionDictionaries.TD), 6, 2),
        parameterItem(regressionItem(RegressionDictionaries.TD), 7, 2),
        parameterItem(regressionItem(RegressionDictionaries.EASTER), 0, 2)};

    private static final String[] CALENDAR_TITLE = {"Adjust", "Leap Year", "T-Stat", "TD(1)", "T-Stat", "TD(2)", "T-Stat", "TD(3)", "T-Stat", "TD(4)", "T-Stat", "TD(5)", "T-Stat", "TD(6)", "T-Stat", "TD(7)", "T-Stat", "Easter", "T-Stat"
    };

    private static final String[] ARMA = {
        parameterItem(arimaItem(ArimaDictionaries.PHI), 1, 2),
        parameterItem(arimaItem(ArimaDictionaries.PHI), 2, 2),
        parameterItem(arimaItem(ArimaDictionaries.PHI), 3, 2),
        parameterItem(arimaItem(ArimaDictionaries.PHI), 4, 2),
        parameterItem(arimaItem(ArimaDictionaries.THETA), 1, 2),
        parameterItem(arimaItem(ArimaDictionaries.THETA), 2, 2),
        parameterItem(arimaItem(ArimaDictionaries.THETA), 3, 2),
        parameterItem(arimaItem(ArimaDictionaries.THETA), 4, 2),
        parameterItem(arimaItem(ArimaDictionaries.BPHI), 1, 2),
        parameterItem(arimaItem(ArimaDictionaries.BTHETA), 1, 2)};

    private static final String[] ARMA_TITLE = {"phi(1)", "T-stat", "phi(2)", "T-stat", "phi(3)", "T-stat", "phi(4)", "T-stat",
        "th(1)", "T-stat", "th(2)", "T-stat", "th(3)", "T-stat", "th(4)", "T-stat",
        "bphi(1)", "T-stat", "bth(1)", "T-stat"
    };

    private static final String[] OUTLIERS = {
        parameterItem(regressionItem(RegressionDictionaries.OUT), 1, 2),
        parameterItem(regressionItem(RegressionDictionaries.OUT), 2, 2),
        parameterItem(regressionItem(RegressionDictionaries.OUT), 3, 2),
        parameterItem(regressionItem(RegressionDictionaries.OUT), 4, 2),
        parameterItem(regressionItem(RegressionDictionaries.OUT), 5, 2),
        parameterItem(regressionItem(RegressionDictionaries.OUT), 6, 2),
        parameterItem(regressionItem(RegressionDictionaries.OUT), 7, 2),
        parameterItem(regressionItem(RegressionDictionaries.OUT), 8, 2),
        parameterItem(regressionItem(RegressionDictionaries.OUT), 9, 2),
        parameterItem(regressionItem(RegressionDictionaries.OUT), 10, 2)
    };
    private static final String[] OUTLIERS_TITLE = {
        "OUT(1)", "T-stat", "OUT(2)", "T-stat", "OUT(3)", "T-stat", "OUT(4)", "T-stat", "OUT(5)", "T-stat", "OUT(6)", "T-stat", "OUT(7)", "T-stat", "OUT(8)", "T-stat", "OUT(9)", "T-stat", "OUT(10)", "T-stat"
    };

    private static final String[] TESTS_TITLE = new String[]{
        "Skewness", "Kurtosis", "Ljung-Box", "LB. on Seas", "LB on sq."
    };

    private static final String[] TESTS = new String[]{
        parameterItem(residualsItem(ResidualsDictionaries.SKEW), 0, -4),
        parameterItem(residualsItem(ResidualsDictionaries.KURT), 0, -4),
        parameterItem(residualsItem(ResidualsDictionaries.LB), 0, -4),
        parameterItem(residualsItem(ResidualsDictionaries.LB2), 0, -4),
        parameterItem(residualsItem(ResidualsDictionaries.SEASLB), 0, -4)
    };

    private static final class TableModelAdapter extends AbstractGridModel {

        private final TableModel source;

        public TableModelAdapter(TableModel source) {
            this.source = source;
        }

        @Override
        public int getRowCount() {
            return source.getRowCount();
        }

        @Override
        public int getColumnCount() {
            return source.getColumnCount() - 1;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return source.getValueAt(rowIndex, columnIndex + 1);
        }

        @Override
        public String getRowName(int rowIndex) {
            return (String) source.getValueAt(rowIndex, 0);
        }

        @Override
        public String getColumnName(int column) {
            return source.getColumnName(column + 1);
        }

    }
}
