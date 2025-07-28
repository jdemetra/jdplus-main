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
import ec.util.chart.swing.SwingColorSchemeSupport;
import ec.util.grid.CellIndex;
import ec.util.grid.swing.GridModel;
import ec.util.grid.swing.JGrid;
import ec.util.grid.swing.XTable;
import ec.util.various.swing.FontAwesome;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsDataTable;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.util.MultiLineNameUtil;
import jdplus.toolkit.base.tsp.util.ObsFormat;
import jdplus.toolkit.desktop.plugin.DemetraIcons;
import jdplus.toolkit.desktop.plugin.components.JTsGrid;
import jdplus.toolkit.desktop.plugin.components.TsFeatureHelper;
import jdplus.toolkit.desktop.plugin.components.TsGridObs;
import jdplus.toolkit.desktop.plugin.components.TsSelectionBridge;
import jdplus.toolkit.desktop.plugin.components.parts.*;
import jdplus.toolkit.desktop.plugin.tsproviders.DataSourceManager;
import jdplus.toolkit.desktop.plugin.util.ActionMaps;
import jdplus.toolkit.desktop.plugin.util.Collections2;
import jdplus.toolkit.desktop.plugin.util.InputMaps;
import lombok.NonNull;
import nbbrd.io.text.Formatter;
import org.jspecify.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.DoubleSummaryStatistics;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

import static jdplus.toolkit.desktop.plugin.components.JTsGrid.TOGGLE_MODE_ACTION;

public final class TsGridUI implements InternalUI<JTsGrid> {

    private JTsGrid target;

    private final JGrid grid = new JGrid();
    private final JComboBox<Ts> combo = new JComboBox<>();
    private final TsGridChangeListener changeListener = new TsGridChangeListener();
    private final TsGridCellRenderer defaultCellRenderer = new TsGridCellRenderer(grid.getDefaultRenderer(Object.class));
    private Font originalFont;

    private TsGridSelectionListener selectionListener;
    private HasObsFormatResolver obsFormatResolver;
    private HasColorSchemeResolver colorSchemeResolver;

    @Override
    public void install(@NonNull JTsGrid component) {
        this.target = component;

        this.selectionListener = new TsGridSelectionListener(target);

        target.setCellRenderer(defaultCellRenderer);

        this.obsFormatResolver = new HasObsFormatResolver(target, this::onDataFormatChange);
        this.colorSchemeResolver = new HasColorSchemeResolver(target, this::onColorSchemeChange);

        registerActions();
        registerInputs();

        initGrid();

        enableSingleTsSelection();
        enableOpenOnDoubleClick();
        enableObsHovering();
        enableProperties();

        target.setLayout(new BorderLayout());
        target.add(grid, BorderLayout.CENTER);
        target.add(combo, BorderLayout.NORTH);
    }

    private void registerActions() {
        ActionMap am = target.getActionMap();
        am.put(JTsGrid.TRANSPOSE_ACTION, TsGridCommands.transpose().toAction(target));
        am.put(JTsGrid.REVERSE_ACTION, TsGridCommands.reverseChronology().toAction(target));
        am.put(JTsGrid.SINGLE_TS_ACTION, TsGridCommands.applyMode(JTsGrid.Mode.SINGLETS).toAction(target));
        am.put(JTsGrid.MULTI_TS_ACTION, TsGridCommands.applyMode(JTsGrid.Mode.MULTIPLETS).toAction(target));
        am.put(JTsGrid.TOGGLE_MODE_ACTION, TsGridCommands.toggleMode().toAction(target));
        HasObsFormatSupport.registerActions(target, am);
        HasTsCollectionSupport.registerActions(target, target.getActionMap());
        HasObsFormatSupport.registerActions(target, am);
        ActionMaps.copyEntries(target.getActionMap(), false, grid.getActionMap());
    }

    private void registerInputs() {
        HasTsCollectionSupport.registerInputs(target.getInputMap());
        InputMaps.copyEntries(target.getInputMap(), false, grid.getInputMap(JGrid.WHEN_IN_FOCUSED_WINDOW));
    }

    private void initGrid() {
        onColorSchemeChange();
        onDataFormatChange();
        onUpdateModeChange();
        updateGridModel();
        updateComboModel();
        updateSelectionBehavior();
        updateComboCellRenderer();
        onTransferHandlerChange();
        onComponentPopupMenuChange();
        grid.setDragEnabled(true);
        grid.setRowRenderer(new TsGridRowRenderer(grid.getRowRenderer()));
        grid.getRowSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        grid.setColumnRenderer(new TsGridColumnRenderer(grid.getColumnRenderer()));
        grid.setCornerRenderer(new TsGridCornerRenderer(new DefaultTableCellRenderer(), () -> target)); // FIXME: DefaultTableCellRenderer
    }

    //<editor-fold defaultstate="collapsed" desc="Interactive stuff">
    private void enableSingleTsSelection() {
        combo.addItemListener(event -> target.setSingleTsIndex(combo.getSelectedIndex()));
    }

    private void enableOpenOnDoubleClick() {
        ActionMaps.onDoubleClick(target.getActionMap(), HasTsCollection.OPEN_ACTION, grid);
    }

    private void enableObsHovering() {
        grid.addPropertyChangeListener(changeListener);
    }

    private void enableProperties() {
        target.addPropertyChangeListener(evt -> {
            switch (evt.getPropertyName()) {
                case HasTsCollection.TS_COLLECTION_PROPERTY:
                    onCollectionChange();
                    break;
                case TsSelectionBridge.TS_SELECTION_PROPERTY:
                    onSelectionChange();
                    break;
                case JTsGrid.TS_UPDATE_MODE_PROPERTY:
                    onUpdateModeChange();
                    break;
                case HasColorScheme.COLOR_SCHEME_PROPERTY:
                    onColorSchemeChange();
                    break;
                case HasObsFormat.OBS_FORMAT_PROPERTY:
                    onDataFormatChange();
                    break;
                case JTsGrid.ORIENTATION_PROPERTY:
                    onOrientationChange();
                    break;
                case JTsGrid.CHRONOLOGY_PROPERTY:
                    onChronologyChange();
                    break;
                case JTsGrid.MODE_PROPERTY:
                    onModeChange();
                    break;
                case JTsGrid.SINGLE_TS_INDEX_PROPERTY:
                    onSingleTsIndexChange();
                    break;
                case JTsGrid.ZOOM_RATIO_PROPERTY:
                    onZoomChange();
                    break;
                case JTsGrid.USE_COLOR_SCHEME_PROPERTY:
                    onUseColorSchemeChange();
                    break;
                case JTsGrid.SHOW_BARS_PROPERTY:
                    onShowBarsChange();
                    break;
                case JTsGrid.CELL_RENDERER_PROPERTY:
                    onCellRendererChange();
                    break;
                case JTsGrid.CROSSHAIR_VISIBLE_PROPERTY:
                    onCrosshairVisibleChange();
                    break;
                case JTsGrid.HOVERED_OBS_PROPERTY:
                    onHoveredObsChange();
                    break;
                case "transferHandler":
                    onTransferHandlerChange();
                    break;
                case "componentPopupMenu":
                    onComponentPopupMenuChange();
                    break;
            }
        });
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Event handlers">
    private void onDataFormatChange() {
        updateGridCellRenderer();
    }

    private void onColorSchemeChange() {
        if (target.isUseColorScheme()) {
            updateGridCellRenderer();
            updateComboCellRenderer();
        }
    }

    private void onCollectionChange() {
        selectionListener.setEnabled(false);
        updateGridModel();
        updateComboModel();
        updateNoDataMessage();
        updateGridCellRenderer();
        selectionListener.setEnabled(true);
    }

    private void onSelectionChange() {
        selectionListener.setEnabled(false);
        updateSelection();
        selectionListener.setEnabled(true);
    }

    private void onUpdateModeChange() {
        updateNoDataMessage();
    }

    private void onOrientationChange() {
        selectionListener.setEnabled(false);
        updateGridModel();
        updateSelectionBehavior();
        updateSelection();
        selectionListener.setEnabled(true);
    }

    private void onChronologyChange() {
        selectionListener.setEnabled(false);
        updateGridModel();
        updateSelection();
        selectionListener.setEnabled(true);
    }

    private void onModeChange() {
        selectionListener.setEnabled(false);
        updateGridModel();
        updateComboModel();
        updateSelectionBehavior();
        updateSelection();
        selectionListener.setEnabled(true);
    }

    private void onSingleTsIndexChange() {
        selectionListener.setEnabled(false);
        updateGridModel();
        updateSelection();
        selectionListener.setEnabled(true);
    }

    private void onUseColorSchemeChange() {
        updateGridCellRenderer();
        updateComboCellRenderer();
    }

    private void onShowBarsChange() {
        updateGridCellRenderer();
    }

    private void onCellRendererChange() {
        updateGridCellRenderer();
    }

    private void onCrosshairVisibleChange() {
        grid.setCrosshairVisible(target.isCrosshairVisible());
    }

    private void onHoveredObsChange() {
        changeListener.applyHoveredCell(target.getHoveredObs());
    }

    private void onZoomChange() {
        if (originalFont == null) {
            originalFont = target.getFont();
        }

        Font font = originalFont;

        if (target.getZoomRatio() != 100) {
            float floatRatio = ((float) target.getZoomRatio()) / 100.0f;
            float scaledSize = originalFont.getSize2D() * floatRatio;
            font = originalFont.deriveFont(scaledSize);
        }

        grid.setFont(font);
    }

    private void onTransferHandlerChange() {
        TransferHandler th = target.getTransferHandler();
        grid.setTransferHandler(th != null ? th : HasTsCollectionSupport.newTransferHandler(target));
    }

    private void onComponentPopupMenuChange() {
        JPopupMenu popupMenu = target.getComponentPopupMenu();
        grid.setComponentPopupMenu(popupMenu != null ? popupMenu : buildGridMenu().getPopupMenu());
    }
    //</editor-fold>

    private void updateNoDataMessage() {
        String msg = InternalComponents.getNoDataMessage(target);
        grid.setNoDataRenderer(new XTable.DefaultNoDataRenderer(msg.replace(System.lineSeparator(), " ")));
    }

    private void updateGridModel() {
        int index = target.getMode() == JTsGrid.Mode.SINGLETS
                ? Math.min(target.getSingleTsIndex(), target.getTsCollection().size() - 1)
                : TsGridData.NO_SINGLE_SERIES_INDEX;
        TsGridData data = TsGridData.create(target.getTsCollection().getItems(), index);
        boolean transposed = target.getOrientation().equals(JTsGrid.Orientation.REVERSED);
        boolean ascending = target.getChronology().equals(JTsGrid.Chronology.ASCENDING);
        boolean single = index != TsGridData.NO_SINGLE_SERIES_INDEX;
        grid.setModel(new TsGridModelAdapter(
                data,
                transposed,
                ascending ? i -> i : i -> data.getRowCount() - i - 1,
                ascending || !single ? j -> j : j -> data.getColumnCount() - j - 1
        ));
    }

    private void updateSelectionBehavior() {
        grid.getColumnSelectionModel().removeListSelectionListener(selectionListener);
        grid.getRowSelectionModel().removeListSelectionListener(selectionListener);
        grid.setColumnSelectionAllowed(false);
        grid.setRowSelectionAllowed(false);
        if (target.getMode() == JTsGrid.Mode.MULTIPLETS) {
            if (target.getOrientation() == JTsGrid.Orientation.NORMAL) {
                grid.getColumnSelectionModel().addListSelectionListener(selectionListener);
                grid.setColumnSelectionAllowed(true);
            } else {
                grid.getRowSelectionModel().addListSelectionListener(selectionListener);
                grid.setRowSelectionAllowed(true);
            }
        }
    }

    private void updateSelection() {
        if (selectionListener.isEnabled()) {
            if (target.getMode() == JTsGrid.Mode.MULTIPLETS) {
                selectionListener.changeSelection(target.getOrientation() == JTsGrid.Orientation.NORMAL ? grid.getColumnSelectionModel() : grid.getRowSelectionModel());
            } else if (!target.getTsCollection().isEmpty()) {
                int index = Math.min(target.getSingleTsIndex(), target.getTsCollection().size() - 1);
                if (combo.isVisible()) {
                    combo.setSelectedIndex(index);
                }
                target.getTsSelectionModel().clearSelection();
                target.getTsSelectionModel().setSelectionInterval(index, index);
            }
        }
    }

    private void updateComboModel() {
        if (target.getMode() == JTsGrid.Mode.SINGLETS && target.getTsCollection().size() > 1) {
            combo.setModel(new DefaultComboBoxModel<>(target.getTsCollection().toArray(Ts[]::new)));
            combo.setVisible(true);
        } else {
            combo.setVisible(false);
        }
    }

    private void updateGridCellRenderer() {
        defaultCellRenderer.update(
                obsFormatResolver.resolve(),
                target.isUseColorScheme() ? colorSchemeResolver.resolve() : null,
                target.isShowBars(),
                Collections2.memoize(() -> getTsFeatureHelper(target)),
                Collections2.memoize(() -> getDoubleSummaryStatistics(target))
        );
        grid.setDefaultRenderer(TsGridObs.class, target.getCellRenderer());
        grid.repaint();
    }

    private void updateComboCellRenderer() {
        combo.setRenderer(new TsGridComboCellRenderer(new DefaultListCellRenderer(), target.isUseColorScheme() ? colorSchemeResolver.resolve() : null));
    }

    private JMenu buildMenu() {
        JMenu result = new JMenu();

        result.add(HasTsCollectionSupport.newOpenMenu(target));
        result.add(HasTsCollectionSupport.newOpenWithMenu(target));

        JMenu menu = HasTsCollectionSupport.newSaveMenu(target);
        if (menu.getSubElements().length > 0) {
            result.add(menu);
        }

        result.add(HasTsCollectionSupport.newRenameMenu(target));
        result.add(HasTsCollectionSupport.newFreezeMenu(target));
        result.add(HasTsCollectionSupport.newCopyMenu(target));
        result.add(HasTsCollectionSupport.newPasteMenu(target));
        result.add(HasTsCollectionSupport.newDeleteMenu(target));
        result.addSeparator();
        result.add(HasTsCollectionSupport.newSelectAllMenu(target));
        result.add(HasTsCollectionSupport.newClearMenu(target));

        return result;
    }

    private JMenu buildGridMenu() {
        ActionMap am = target.getActionMap();

        JMenu result = buildMenu();

        int index = 0;
        JMenuItem item;

        index += 10;
        result.insertSeparator(index++);

        item = new JCheckBoxMenuItem(am.get(JTsGrid.TRANSPOSE_ACTION));
        item.setText("Transpose");
        result.add(item, index++);

        item = new JCheckBoxMenuItem(am.get(JTsGrid.REVERSE_ACTION));
        item.setText("Reverse chronology");
        item.setIcon(DemetraIcons.getPopupMenuIcon(FontAwesome.FA_SORT_NUMERIC_DESC));
        result.add(item, index++);

        item = new JCheckBoxMenuItem(am.get(TOGGLE_MODE_ACTION));
        item.setText("Single time series");
        result.add(item, index++);

        result.addSeparator();

        item = new JMenuItem(am.get(HasObsFormat.EDIT_FORMAT_ACTION));
        item.setText("Edit format...");
        item.setIcon(DemetraIcons.getPopupMenuIcon(FontAwesome.FA_GLOBE));
        result.add(item);

        item = new JCheckBoxMenuItem(TsGridCommands.toggleUseColorScheme().toAction(target));
        item.setText("Use color scheme");
        result.add(item);

        result.add(HasColorSchemeSupport.menuOf(target));

        item = new JCheckBoxMenuItem(TsGridCommands.toggleShowBars().toAction(target));
        item.setText("Show bars");
        item.setIcon(DemetraIcons.getPopupMenuIcon(FontAwesome.FA_TASKS));
        result.add(item);

        item = HasCrosshairSupport.newToggleCrosshairVisibilityMenu(target);
        result.add(item);

        result.add(HasZoomRatioSupport.newZoomRatioMenu(target));

        return result;
    }

    private class TsGridSelectionListener extends InternalTsSelectionAdapter {

        private TsGridSelectionListener(HasTsCollection outer) {
            super(outer);
        }

        @Override
        protected void selectionChanged(ListSelectionModel model) {
            if (target.getMode() == JTsGrid.Mode.MULTIPLETS) {
                super.selectionChanged(model);
            } else if (target.getTsCollection().size() > target.getSingleTsIndex()) {
                int index = target.getSingleTsIndex();
                target.getTsSelectionModel().clearSelection();
                target.getTsSelectionModel().setSelectionInterval(index, index);
            } else {
                target.getTsSelectionModel().clearSelection();
            }
        }
    }

    private final class TsGridChangeListener implements PropertyChangeListener {

        private boolean updating = false;

        @SuppressWarnings("SwitchStatementWithTooFewBranches")
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!updating) {
                updating = true;
                switch (evt.getPropertyName()) {
                    case JGrid.HOVERED_CELL_PROPERTY:
                        target.setHoveredObs(((TsGridModelAdapter) grid.getModel()).toObsIndex(grid.getHoveredCell()));
                        break;
                }
                updating = false;
            }
        }

        private void applyHoveredCell(ObsIndex hoveredObs) {
            if (!updating) {
                grid.setHoveredCell(((TsGridModelAdapter) grid.getModel()).toCellIndex(hoveredObs));
            }
        }
    }

    private record TsGridRowRenderer(TableCellRenderer delegate) implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component result = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (result instanceof JLabel label) {
                String text = label.getText();
                label.setText(MultiLineNameUtil.join(text));
                label.setToolTipText(MultiLineNameUtil.toHtml(text));
                label.setHorizontalAlignment(JLabel.TRAILING);
            }
            return result;
        }
    }

    private record TsGridColumnRenderer(TableCellRenderer delegate) implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component result = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (result instanceof JLabel label) {
                String text = label.getText();
                label.setText(MultiLineNameUtil.join(text));
                label.setToolTipText(MultiLineNameUtil.toHtml(text));
            }
            return result;
        }
    }

    private record TsGridCornerRenderer(TableCellRenderer delegate,
                                        Supplier<JTsGrid> target) implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component result = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (result instanceof JLabel label) {
                TsDomain domain = getDomain(target.get());
                label.setText(domain.getTsUnit().toString());
                label.setToolTipText(domain.toShortString());
                label.setHorizontalAlignment(JLabel.CENTER);
            }
            return result;
        }

        private static TsDomain getDomain(JTsGrid target) {
            return target.getMode() == JTsGrid.Mode.SINGLETS
                    ? target.getTsCollection().get(target.getSingleTsIndex()).getData().getDomain()
                    : target.getTsCollection().getDomain();
        }
    }

    private static final class TsGridCellRenderer extends BarTableCellRenderer {

        private final TableCellRenderer delegate;
        private Formatter<? super Number> valueCell;
        private SwingColorSchemeSupport colorSchemeSupport;
        private boolean showBars;
        private Supplier<TsFeatureHelper> tsFeatures;
        private Supplier<DoubleSummaryStatistics> stats;

        public TsGridCellRenderer(@NonNull TableCellRenderer delegate) {
            super(false);
            setHorizontalAlignment(JLabel.TRAILING);
            setOpaque(true);
            this.delegate = delegate;
            this.valueCell = ObsFormat.getSystemDefault().numberFormatter();
            this.colorSchemeSupport = null;
            this.showBars = false;
            this.tsFeatures = () -> TsFeatureHelper.EMPTY;
            this.stats = DoubleSummaryStatistics::new;
        }

        void update(@NonNull ObsFormat obsFormat, @Nullable SwingColorSchemeSupport colorSchemeSupport, boolean showBars, Supplier<TsFeatureHelper> tsFeatures, Supplier<DoubleSummaryStatistics> stats) {
            this.valueCell = obsFormat.numberFormatter();
            this.colorSchemeSupport = colorSchemeSupport;
            this.showBars = showBars;
            this.tsFeatures = tsFeatures;
            this.stats = stats;
        }

        @Override
        public JToolTip createToolTip() {
            JToolTip result = super.createToolTip();
            if (colorSchemeSupport != null) {
                result.setBackground(getForeground());
                result.setForeground(getBackground());
            }
            return result;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            TsGridObs obs = (TsGridObs) value;

            applyFontAndColors(isSelected, obs, delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column));

            switch (obs.getStatus()) {
                case AFTER:
                case BEFORE:
                case EMPTY:
                case UNUSED:
                    applyContent(null, null);
                    applyNoBar();
                    break;
                case PRESENT:
                    if (Double.isNaN(obs.getValue())) {
                        applyContent(".", obsToString(obs));
                        applyNoBar();
                    } else {
                        String text = valueCell.formatAsString(obs.getValue());
                        String toolTipText = obsToString(obs);
                        TsFeatureHelper featureHelper = tsFeatures.get();
                        if (featureHelper.hasFeature(TsFeatureHelper.Feature.Forecasts, obs.getSeriesIndex(), obs.getIndex())) {
                            applyContent("<html><i>" + text, "<html>" + toolTipText + "<br>Forecast");
                        } else if (featureHelper.hasFeature(TsFeatureHelper.Feature.Backcasts, obs.getSeriesIndex(), obs.getIndex())) {
                            applyContent("<html><i>" + text, "<html>" + toolTipText + "<br>Backcast");
                        } else {
                            applyContent(text, toolTipText);
                        }
                        if (showBars && !isSelected) {
                            applyBar(stats.get(), obs.getValue());
                        } else {
                            applyNoBar();
                        }
                        break;
                    }
            }

            return this;
        }

        private String obsToString(TsGridObs obs) {
            return obs.getPeriod().toShortString() + "=" + obs.getValue();
        }

        private void applyBar(DoubleSummaryStatistics stats, double value) {
            setBarValues(stats.getMin(), stats.getMax(), value);
        }

        private void applyNoBar() {
            setBarValues(0, 0, 0);
        }

        private void applyContent(String text, String tooltipText) {
            setText(text);
            setToolTipText(tooltipText);
        }

        private void applyFontAndColors(boolean isSelected, TsGridObs obs, Component resource) {
            setFont(resource.getFont());
            if (colorSchemeSupport != null) {
                Color plotColor = colorSchemeSupport.getPlotColor();
                Color lineColor = colorSchemeSupport.getLineColor(obs.getSeriesIndex());
                if (isSelected) {
                    setBackground(lineColor);
                    setForeground(plotColor);
                } else {
                    setBackground(plotColor);
                    setForeground(lineColor);
                }
            } else {
                setBackground(resource.getBackground());
                setForeground(resource.getForeground());
            }
        }
    }

    private record TsGridComboCellRenderer(ListCellRenderer<? super Ts> delegate,
                                           @Nullable SwingColorSchemeSupport colorSchemeSupport) implements ListCellRenderer<Ts> {

        @Override
        public Component getListCellRendererComponent(JList<? extends Ts> list, Ts value, int index, boolean isSelected, boolean cellHasFocus) {
            Component result = delegate.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (result instanceof JLabel label) {
                label.setText(value.getName());
                label.setIcon(DataSourceManager.get().getIcon(value.getMoniker(), BeanInfo.ICON_COLOR_16x16, false));
                if (colorSchemeSupport != null && index != -1) {
                    if (isSelected) {
                        label.setBackground(colorSchemeSupport.getPlotColor());
                    }
                    label.setForeground(colorSchemeSupport.getLineColor(index));
                }
            }
            return result;
        }
    }

    @lombok.AllArgsConstructor
    private static final class TsGridModelAdapter extends AbstractTableModel implements GridModel {

        private final TsGridData data;
        private final boolean transposed;
        private final IntUnaryOperator rowIndexer;
        private final IntUnaryOperator columnIndexer;

        @Override
        public int getRowCount() {
            return transposed
                    ? data.getColumnCount()
                    : data.getRowCount();
        }

        @Override
        public int getColumnCount() {
            return transposed
                    ? data.getRowCount()
                    : data.getColumnCount();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return transposed
                    ? data.getObs(rowIndexer.applyAsInt(columnIndex), columnIndexer.applyAsInt(rowIndex))
                    : data.getObs(rowIndexer.applyAsInt(rowIndex), columnIndexer.applyAsInt(columnIndex));
        }

        @Override
        public String getRowName(int rowIndex) {
            return transposed
                    ? data.getColumnName(columnIndexer.applyAsInt(rowIndex))
                    : data.getRowName(rowIndexer.applyAsInt(rowIndex));
        }

        @Override
        public String getColumnName(int columnIndex) {
            return transposed
                    ? data.getRowName(rowIndexer.applyAsInt(columnIndex))
                    : data.getColumnName(columnIndexer.applyAsInt(columnIndex));
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return TsGridObs.class;
        }

        @NonNull
        public ObsIndex toObsIndex(@NonNull CellIndex index) {
            if (CellIndex.NULL.equals(index)) {
                return ObsIndex.NULL;
            }
            TsGridObs obs = transposed
                    ? data.getObs(rowIndexer.applyAsInt(index.getColumn()), columnIndexer.applyAsInt(index.getRow()))
                    : data.getObs(rowIndexer.applyAsInt(index.getRow()), columnIndexer.applyAsInt(index.getColumn()));
            if (TsDataTable.ValueStatus.PRESENT.equals(obs.getStatus())) {
                return ObsIndex.valueOf(obs.getSeriesIndex(), obs.getIndex());
            }
            return ObsIndex.NULL;
        }

        @NonNull
        public CellIndex toCellIndex(@NonNull ObsIndex index) {
            return transposed
                    ? CellIndex.valueOf(rowIndexer.applyAsInt(data.getColumnIndex(index)), columnIndexer.applyAsInt(data.getRowIndex(index)))
                    : CellIndex.valueOf(rowIndexer.applyAsInt(data.getRowIndex(index)), columnIndexer.applyAsInt(data.getColumnIndex(index)));
        }
    }

    private static TsFeatureHelper getTsFeatureHelper(JTsGrid target) {
        return TsFeatureHelper.of(target.getTsCollection().getItems());
    }

    private static DoubleSummaryStatistics getDoubleSummaryStatistics(JTsGrid target) {
        return target.getMode() == JTsGrid.Mode.MULTIPLETS
                ? target.getTsCollection().stream().flatMapToDouble(o -> o.getData().getValues().stream()).summaryStatistics()
                : target.getTsCollection().get(target.getSingleTsIndex()).getData().getValues().stream().summaryStatistics();
    }
}
