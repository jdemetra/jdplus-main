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
package jdplus.toolkit.desktop.plugin.components;

import ec.util.table.swing.JTables;
import ec.util.various.swing.StandardSwingColor;
import internal.ui.components.DemoTsBuilder;
import jdplus.main.desktop.design.SwingComponent;
import jdplus.main.desktop.design.SwingProperty;
import jdplus.toolkit.base.api.timeseries.*;
import jdplus.toolkit.base.api.util.MultiLineNameUtil;
import jdplus.toolkit.base.tsp.util.ObsFormat;
import jdplus.toolkit.desktop.plugin.DemetraUI;
import jdplus.toolkit.desktop.plugin.beans.PropertyChangeSource;
import jdplus.toolkit.desktop.plugin.components.parts.*;
import jdplus.toolkit.desktop.plugin.jfreechart.TsSparklineCellRenderer;
import jdplus.toolkit.desktop.plugin.tsproviders.DataSourceManager;
import nbbrd.io.text.Formatter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.beans.BeanInfo;
import java.beans.Beans;
import java.util.*;
import java.util.List;
import java.util.function.Function;

/**
 * @author Kristof Bayens
 * @author Philippe Charles
 */
@SwingComponent
public final class JTsTable extends JComponent implements TimeSeriesComponent, PropertyChangeSource.WithWeakListeners,
        HasTsCollection, HasTsAction, HasObsFormat {

    @lombok.Value
    @lombok.Builder
    public static class Column {

        public static final Column NAME = builder()
                .name("Name")
                .type(String.class)
                .mapper(Ts::getName)
                .build();

        public static final Column FREQ = builder()
                .name("TsUnit")
                .type(TsUnit.class)
                .mapper(ts -> ts.getData().getTsUnit())
                .comparator(Comparator.comparing(TsUnit::toString))
                .renderer(table -> JTables.cellRendererOf(JTsTable::renderTsUnit))
                .build();

        public static final Column START = builder()
                .name("First period")
                .type(TsPeriod.class)
                .mapper(ts -> ts.getData().isEmpty() ? null : ts.getData().getDomain().getStartPeriod())
                .comparator(Comparator.comparing(TsPeriod::start))
                .renderer(table -> JTables.cellRendererOf(JTsTable::renderTsPeriod))
                .build();

        public static final Column LAST = builder()
                .name("Last period")
                .type(TsPeriod.class)
                .mapper(ts -> ts.getData().isEmpty() ? null : ts.getData().getDomain().getLastPeriod())
                .comparator(Comparator.comparing(TsPeriod::end))
                .renderer(table -> JTables.cellRendererOf(JTsTable::renderTsPeriod))
                .build();

        public static final Column LENGTH = builder()
                .name("Obs count")
                .type(Integer.class)
                .mapper(ts -> ts.getData().length())
                .renderer(table -> JTables.cellRendererOf(JTsTable::renderTsLength))
                .build();

        public static final Column DATA = builder()
                .name("Values")
                .type(TsData.class)
                .mapper(Ts::getData)
                .comparator((l, r) -> -1)
                .renderer(TsDataTableCellRenderer::new)
                .build();

        public static final Column TS_IDENTIFIER = builder()
                .name("Series Name")
                .type(TsIdentifier.class)
                .mapper(TsIdentifier::of)
                .comparator(Comparator.comparing(TsIdentifier::getName))
                .renderer(table -> JTables.cellRendererOf(JTsTable::renderTsIdentifier))
                .build();

        @lombok.NonNull
        String name;

        @lombok.NonNull
        @lombok.Builder.Default
        Class<?> type = Object.class;

        @lombok.NonNull
        @lombok.Builder.Default
        Function<Ts, ?> mapper = Function.identity();

        @lombok.NonNull
        @lombok.Builder.Default
        Comparator<?> comparator = Comparator.naturalOrder();

        @lombok.NonNull
        @lombok.Builder.Default
        Function<JTsTable, TableCellRenderer> renderer = ignore -> new DefaultTableCellRenderer();
    }

    @SwingProperty
    public static final String SHOW_HEADER_PROPERTY = "showHeader";

    @SwingProperty
    public static final String COLUMNS_PROPERTY = "columns";

    @SwingProperty
    public static final String WIDTH_AS_PERCENTAGES_PROPERTY = "widthAsPercentages";

    // DEFAULT PROPERTIES
    private static final boolean DEFAULT_SHOW_HEADER = true;
    private static final List<Column> DEFAULT_COLUMNS = Collections.unmodifiableList(Arrays.asList(Column.TS_IDENTIFIER, Column.START, Column.LAST, Column.LENGTH, Column.DATA));

    // PROPERTIES
    private boolean showHeader;
    private List<Column> columns;
    private double[] widthAsPercentages;

    @lombok.experimental.Delegate
    private final HasTsCollection collection;

    @lombok.experimental.Delegate
    private final HasTsAction tsAction;

    @lombok.experimental.Delegate
    private final HasObsFormat obsFormat;

    private final TsSelectionBridge tsSelectionBridge;

    public JTsTable() {
        this(TsInformationType.None);
    }

    public JTsTable(TsInformationType info) {
        this.collection = HasTsCollectionSupport.of(this::firePropertyChange, info);
        this.tsAction = HasTsActionSupport.of(this::firePropertyChange);
        this.obsFormat = HasObsFormatSupport.of(this::firePropertyChange);
        this.showHeader = DEFAULT_SHOW_HEADER;
        this.columns = DEFAULT_COLUMNS;
        this.widthAsPercentages = new double[]{.4, .1, .1, .1, .3};

        this.tsSelectionBridge = new TsSelectionBridge(this::firePropertyChange);
        tsSelectionBridge.register(this);

        ComponentBackendManager.get().install(this);

        applyDesignTimeProperties();
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        List<Column> old = this.columns;
        this.columns = columns != null ? Collections.unmodifiableList(columns) : DEFAULT_COLUMNS;
        firePropertyChange(COLUMNS_PROPERTY, old, this.columns);
    }

    public double[] getWidthAsPercentages() {
        return widthAsPercentages;
    }

    public void setWidthAsPercentages(double[] widthAsPercentages) {
        double[] old = this.widthAsPercentages;
        this.widthAsPercentages = widthAsPercentages;
        firePropertyChange(WIDTH_AS_PERCENTAGES_PROPERTY, old, this.widthAsPercentages);
    }

    public boolean isShowHeader() {
        return showHeader;
    }

    public void setShowHeader(boolean showHeader) {
        boolean old = this.showHeader;
        this.showHeader = showHeader;
        firePropertyChange(SHOW_HEADER_PROPERTY, old, this.showHeader);
    }

    private void applyDesignTimeProperties() {
        if (Beans.isDesignTime()) {
            setTsCollection(DemoTsBuilder.randomTsCollection(3));
            setTsUpdateMode(TsUpdateMode.None);
            setPreferredSize(new Dimension(200, 150));
        }
    }

    private static void renderTsUnit(JLabel label, TsUnit value) {
        label.setHorizontalAlignment(JLabel.LEADING);
        label.setText(value != null ? value.toString() : null);
    }

    private static void renderTsPeriod(JLabel label, TsPeriod value) {
        label.setHorizontalAlignment(JLabel.LEADING);
        label.setText(value != null ? value.getStartAsShortString() : null);
    }

    private static void renderTsLength(JLabel label, Integer value) {
        label.setHorizontalAlignment(JLabel.TRAILING);
        label.setText(value != null ? value.toString() : null);
    }

    private static void renderTsIdentifier(JLabel label, TsIdentifier id) {
        String text = id.getName();
        if (text.isEmpty()) {
            label.setText(" ");
            label.setToolTipText(null);
        } else if (text.startsWith("<html>")) {
            label.setText(text);
            label.setToolTipText(text);
        } else {
            label.setText(MultiLineNameUtil.join(text));
            label.setToolTipText(MultiLineNameUtil.toHtml(text));
        }
        label.setIcon(DataSourceManager.get().getIcon(id.getMoniker(), BeanInfo.ICON_COLOR_16x16, false));
    }

    private static final class TsDataTableCellRenderer implements TableCellRenderer {

        private final HasObsFormat target;
        private final TsSparklineCellRenderer dataRenderer;
        private final DefaultTableCellRenderer labelRenderer;

        private ObsFormat currentFormat;
        private Formatter<Number> currentFormatter;

        public TsDataTableCellRenderer(HasObsFormat target) {
            this.target = target;
            this.dataRenderer = new TsSparklineCellRenderer();
            this.labelRenderer = new DefaultTableCellRenderer();
            StandardSwingColor.TEXT_FIELD_INACTIVE_FOREGROUND.lookup().ifPresent(labelRenderer::setForeground);
            labelRenderer.setHorizontalAlignment(SwingConstants.CENTER);

            this.currentFormat = null;
            this.currentFormatter = null;
        }

        private ObsFormat lookupObsFormat() {
            ObsFormat result = target.getObsFormat();
            return result != null ? result : DemetraUI.get().getObsFormat();
        }

        private String formatValue(Number o) {
            ObsFormat x = lookupObsFormat();
            if (!Objects.equals(x, currentFormat)) {
                currentFormat = x;
                currentFormatter = x.numberFormatter();
            }
            return currentFormatter.formatAsString(o);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof TsData data) {
                return switch (data.length()) {
                    case 0 -> renderLabel(table, toEmptyCauseLabel(data), isSelected, hasFocus, row, column);
                    case 1 ->
                            renderLabel(table, "Single: " + formatValue(data.getValue(0)), isSelected, hasFocus, row, column);
                    default -> renderSparkline(table, data, isSelected, hasFocus, row, column);
                };
            }
            return renderLabel(table, Objects.toString(value), isSelected, hasFocus, row, column);
        }

        private static String toEmptyCauseLabel(TsData data) {
            String result = data.getEmptyCause();
            return result == null || result.isEmpty() ? "loading? invalid?" : result;
        }

        private Component renderSparkline(JTable table, TsData value, boolean isSelected, boolean hasFocus, int row, int column) {
            return dataRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }

        private Component renderLabel(JTable table, String value, boolean isSelected, boolean hasFocus, int row, int column) {
            labelRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            labelRenderer.setToolTipText(labelRenderer.getText());
            return labelRenderer;
        }
    }
}
