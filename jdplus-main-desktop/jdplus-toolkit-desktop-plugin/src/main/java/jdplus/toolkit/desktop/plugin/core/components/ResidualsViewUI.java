/*
 * Copyright 2018 National Bank of Belgium
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

import ec.util.chart.ColorScheme;
import ec.util.chart.swing.ChartCommand;
import ec.util.chart.swing.Charts;
import ec.util.chart.swing.SwingColorSchemeSupport;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsCollection;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsMoniker;
import jdplus.toolkit.desktop.plugin.components.ComponentBackendSpi;
import jdplus.toolkit.desktop.plugin.components.JResidualsView;
import jdplus.toolkit.desktop.plugin.components.JTsGrid;
import jdplus.toolkit.desktop.plugin.components.parts.*;
import jdplus.toolkit.desktop.plugin.components.parts.HasTsCollection.TsUpdateMode;
import jdplus.toolkit.desktop.plugin.jfreechart.TsCharts;
import jdplus.toolkit.desktop.plugin.jfreechart.TsXYDataset;
import jdplus.toolkit.desktop.plugin.util.NbComponents;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickMarkPosition;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.Range;
import org.jfree.ui.RectangleInsets;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.stream.DoubleStream;

import static jdplus.toolkit.desktop.plugin.components.parts.HasObsFormat.OBS_FORMAT_PROPERTY;
import static jdplus.toolkit.desktop.plugin.components.parts.HasTsData.TS_DATA_PROPERTY;

/**
 * @author Philippe Charles
 */
public final class ResidualsViewUI implements InternalUI<JResidualsView> {

    @DirectImpl
    @ServiceProvider
    public static final class Factory implements ComponentBackendSpi {

        @Override
        public boolean handles(Class<? extends JComponent> type) {
            return JResidualsView.class.equals(type);
        }

        @Override
        public void install(JComponent component) {
            new ResidualsViewUI().install((JResidualsView) component);
        }
    }

    private static final ColorScheme.KnownColor MAIN_COLOR = ColorScheme.KnownColor.BLUE;

    private final ChartPanel chartPanel;
    private final JTsGrid grid;
    private HasObsFormatResolver obsFormatResolver;
    private HasColorSchemeResolver colorSchemeResolver;

    public ResidualsViewUI() {
        this.chartPanel = Charts.newChartPanel(buildResidualViewChart());
        this.grid = new JTsGrid();
        this.grid.setTsUpdateMode(TsUpdateMode.None);
        this.grid.setMode(JTsGrid.Mode.SINGLETS);
    }

    @Override
    public void install(JResidualsView view) {
        this.obsFormatResolver = new HasObsFormatResolver(view, () -> onDataFormatChange(view));
        HasColorScheme todo = HasColorSchemeSupport.of((propertyName, oldValue, newValue) -> {
            //TODO
        });
        this.colorSchemeResolver = new HasColorSchemeResolver(todo, this::onColorSchemeChange);

        registerActions(view);

        onDataFormatChange(view);
        onColorSchemeChange();
        onComponentPopupMenuChange(view);

        enableProperties(view);

        JSplitPane splitPane = NbComponents.newJSplitPane(JSplitPane.VERTICAL_SPLIT, chartPanel, grid);
        splitPane.setDividerLocation(0.5);
        splitPane.setResizeWeight(.5);
        view.setLayout(new BorderLayout());
        view.add(splitPane, BorderLayout.CENTER);
    }

    private void registerActions(JResidualsView view) {
        HasObsFormatSupport.registerActions(view, view.getActionMap());
    }

    private void enableProperties(JResidualsView view) {
        view.addPropertyChangeListener(evt -> {
            switch (evt.getPropertyName()) {
                case OBS_FORMAT_PROPERTY:
                    onDataFormatChange(view);
                    break;
                case TS_DATA_PROPERTY:
                    onTsDataChange(view);
                case "componentPopupMenu":
                    onComponentPopupMenuChange(view);
                    break;
            }
        });
    }

    private void onTsDataChange(JResidualsView view) {
        TsData data = view.getTsData();
        Ts ts = Ts.builder().moniker(TsMoniker.of()).name("Residuals").data(data).build();
        chartPanel.getChart().getXYPlot().setDataset(TsXYDataset.of(Collections.singletonList(ts)));
        if (!data.isEmpty()) {
            Range rng = calcRange(data.getValues().toArray());
            ((NumberAxis) chartPanel.getChart().getXYPlot().getRangeAxis()).setTickUnit(new NumberTickUnit(calcTick(rng)), true, false);

            grid.setTsCollection(TsCollection.of(ts));
        } else {
            grid.setTsCollection(TsCollection.EMPTY);
        }
        onColorSchemeChange();
    }

    private void onDataFormatChange(JResidualsView view) {
        grid.setObsFormat(view.getObsFormat());
        TsData tsData = view.getTsData();
        if (tsData != null)
            ((DateAxis) chartPanel.getChart().getXYPlot().getDomainAxis())
                    .setDateFormatOverride(new InternalComponents.DateFormatAdapter(tsData::getDomain));
    }

    private void onColorSchemeChange() {
        SwingColorSchemeSupport themeSupport = colorSchemeResolver.resolve();

        XYPlot plot = chartPanel.getChart().getXYPlot();
        plot.setBackgroundPaint(themeSupport.getPlotColor());
        plot.setDomainGridlinePaint(themeSupport.getGridColor());
        plot.setRangeGridlinePaint(themeSupport.getGridColor());
        chartPanel.getChart().setBackgroundPaint(themeSupport.getBackColor());

        XYItemRenderer renderer = plot.getRenderer();
        renderer.setBasePaint(themeSupport.getAreaColor(MAIN_COLOR));
        renderer.setBaseOutlinePaint(themeSupport.getLineColor(MAIN_COLOR));
    }

    private void onComponentPopupMenuChange(JResidualsView view) {
        JPopupMenu popupMenu = view.getComponentPopupMenu();
        chartPanel.setComponentPopupMenu(popupMenu != null ? popupMenu : buildMenu().getPopupMenu());
    }

    private static JFreeChart buildResidualViewChart() {
        JFreeChart result = ChartFactory.createXYBarChart("Full residuals", "", false, "", Charts.emptyXYDataset(), PlotOrientation.VERTICAL, false, false, false);
        result.setPadding(TsCharts.CHART_PADDING);
        result.getTitle().setFont(TsCharts.CHART_TITLE_FONT);

        XYPlot plot = result.getXYPlot();

        DateAxis domainAxis = new DateAxis();
        domainAxis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);
        domainAxis.setLowerMargin(0);
        domainAxis.setUpperMargin(0);
        domainAxis.setTickLabelPaint(TsCharts.CHART_TICK_LABEL_COLOR);
        plot.setDomainAxis(domainAxis);

        NumberAxis rangeAxis = new NumberAxis();
        rangeAxis.setAutoRangeIncludesZero(false);
        rangeAxis.setTickLabelInsets(new RectangleInsets(10, 5, 10, 2));
        rangeAxis.setLowerMargin(0.02);
        rangeAxis.setUpperMargin(0.02);
        rangeAxis.setTickLabelPaint(TsCharts.CHART_TICK_LABEL_COLOR);
        plot.setRangeAxis(rangeAxis);

        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setShadowVisible(false);
        renderer.setDrawBarOutline(true);
        renderer.setAutoPopulateSeriesPaint(false);
        renderer.setAutoPopulateSeriesOutlinePaint(false);

        return result;
    }

    private JMenu buildMenu() {
        JMenu result = new JMenu();

        result.add(grid.getActionMap().get(HasTsCollection.COPY_ALL_ACTION)).setText("Copy series");

        JMenu export = new JMenu("Export image to");
        export.add(ChartCommand.printImage().toAction(chartPanel)).setText("Printer...");
        export.add(ChartCommand.copyImage().toAction(chartPanel)).setText("Clipboard");
        export.add(ChartCommand.saveImage().toAction(chartPanel)).setText("File...");
        result.add(export);

        return result;
    }

    private static Range calcRange(double[] values) {
        double min = Double.NEGATIVE_INFINITY, max = -Double.POSITIVE_INFINITY;

        final DoubleSummaryStatistics stats = DoubleStream.of(values).summaryStatistics();
        double smin = stats.getMin(), smax = stats.getMax();
        if (Double.isInfinite(min) || smin < min) {
            min = smin;
        }
        if (Double.isInfinite(max) || smax > max) {
            max = smax;
        }

        if (Double.isInfinite(max) || Double.isInfinite(min)) {
            return new Range(0, 1);
        }
        double length = max - min;
        if (length == 0) {
            return new Range(0, 1);
        } else {
            //double eps = length * .05;
            //return new Range(min - eps, max + eps);
            return new Range(min, max);
        }
    }

    private static double calcTick(Range rng) {
        double tick = 0;
        double avg = (rng.getUpperBound() - rng.getLowerBound()) / 6;
        if (avg > 1) {
            for (int i = 0; i < 10 && tick == 0; i++) {
                double power = Math.pow(10, i);
                if (avg > (0.01 * power) && avg <= (0.02 * power)) {
                    tick = (0.02 * power);
                } else if (avg > (0.02 * power) && avg <= (0.05 * power)) {
                    tick = (0.05 * power);
                } else if (avg > (0.05 * power) && avg <= (0.1 * power)) {
                    tick = (0.1 * power);
                }
            }
        } else {
            for (int i = 0; i < 10 && tick == 0; i++) {
                double power = Math.pow(.1, i);
                if (avg > (0.01 * power) && avg <= (0.02 * power)) {
                    tick = (0.02 * power);
                } else if (avg > (0.02 * power) && avg <= (0.05 * power)) {
                    tick = (0.05 * power);
                } else if (avg > (0.05 * power) && avg <= (0.1 * power)) {
                    tick = (0.1 * power);
                }
            }
        }
        if (tick == 0) {
            tick = avg;
        }
        return tick;
    }
}
