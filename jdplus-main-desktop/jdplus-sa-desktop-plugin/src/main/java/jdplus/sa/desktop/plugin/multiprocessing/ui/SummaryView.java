/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.desktop.plugin.multiprocessing.ui;

import jdplus.toolkit.desktop.plugin.util.NbComponents;
import jdplus.toolkit.desktop.plugin.components.JHtmlView;
import jdplus.toolkit.desktop.plugin.html.HtmlUtil;
import jdplus.toolkit.desktop.plugin.html.modelling.HtmlRegSarimaReport;
import jdplus.toolkit.base.api.information.Explorable;
import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;
import jdplus.sa.base.api.SaItem;
import ec.util.list.swing.JLists;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaReport;
import jdplus.sa.base.core.modelling.HasRegSarimaPreprocessing;

/**
 * @author Philippe Charles
 */
public class SummaryView extends AbstractSaProcessingTopComponent implements MultiViewElement {

    // main components
    private final JComponent visualRepresentation;
    private final JToolBar toolBarRepresentation;
    // data
    private Map<Integer, Map<AlgorithmDescriptor, RegSarimaReport>> reports;
    // subcomponents
    private final JComboBox<Map.Entry<Integer, AlgorithmDescriptor>> comboBox;
    private final JHtmlView reportTB_;

    public SummaryView(MultiProcessingController controller) {
        super(controller);
        this.reports = new HashMap<>();
        this.reportTB_ = new JHtmlView();

        this.comboBox = new JComboBox<>();
        comboBox.setRenderer(JLists.cellRendererOf((label, value) -> {
            if (value != null) {
                label.setText(TaggedTreeNode.freqName(value.getKey()) + " > " + value.getValue().getName());
            }
        }));
        comboBox.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED && event.getItem() != null) {
                Map.Entry<Integer, AlgorithmDescriptor> item = (Map.Entry<Integer, AlgorithmDescriptor>) event.getItem();
                HtmlRegSarimaReport report = new HtmlRegSarimaReport(item.getValue().getName(), reports.get(item.getKey()).get(item.getValue()));
                reportTB_.setHtml(HtmlUtil.toString(report));
            } else {
                reportTB_.setHtml("");
            }
        });

        toolBarRepresentation = NbComponents.newInnerToolbar();
        toolBarRepresentation.addSeparator();
        toolBarRepresentation.add(comboBox);

        visualRepresentation = reportTB_;

        setData(Collections.emptyMap());

        setLayout(new BorderLayout());
        add(toolBarRepresentation, BorderLayout.NORTH);
        add(visualRepresentation, BorderLayout.CENTER);
    }

    @Override
    protected void onSaProcessingStateChange() {
        super.onSaProcessingStateChange();
        if (controller.getSaProcessingState().isFinished()) {
            SaItem[] items = current();
            setData(createRegSarimaReports(items));
        } else {
            setData(Collections.emptyMap());
        }
    }

//    @Override
//    protected void onSaProcessingSaved(){
//        setData(Collections.emptyMap());
//    }
//    
    @Override
    public JComponent getVisualRepresentation() {
        return visualRepresentation;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return toolBarRepresentation;
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    @Override
    public void componentShowing() {
    }

    @Override
    public void componentHidden() {
    }

    @Override
    public void componentActivated() {
    }

    @Override
    public void componentDeactivated() {
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }
    // < MultiViewElement

    private static ComboBoxModel<Map.Entry<Integer, AlgorithmDescriptor>> asComboBoxModel(Map<Integer, Map<AlgorithmDescriptor, RegSarimaReport>> reports) {
        DefaultComboBoxModel<Map.Entry<Integer, AlgorithmDescriptor>> result = new DefaultComboBoxModel<>();
        for (Map.Entry<Integer, Map<AlgorithmDescriptor, RegSarimaReport>> item : reports.entrySet()) {
            for (AlgorithmDescriptor ritem : item.getValue().keySet()) {
                result.addElement(new HashMap.SimpleImmutableEntry<>(item.getKey(), ritem));
            }
        }
        return result;
    }

    public void setData(Map<Integer, Map<AlgorithmDescriptor, RegSarimaReport>> reports) {
        reportTB_.setHtml("");
        this.reports = reports;
        long count = reports.values().stream().mapToLong(r -> r.values().size()).sum();
        comboBox.setVisible(count > 1);
        comboBox.setModel(asComboBoxModel(reports));
        comboBox.setSelectedIndex(-1);
        if (!reports.isEmpty()) {
            comboBox.setSelectedIndex(0);
        }
    }

    public Map<Integer, Map<AlgorithmDescriptor, RegSarimaReport>> createRegSarimaReports(SaItem[] items) {
        Map<Integer, Map<AlgorithmDescriptor, RegSarimaReport>> reports = new HashMap<>();
        for (SaItem item : items) {
            if (item.isProcessed()) {
                Explorable rslt = item.getEstimation().getResults();
                if (rslt != null) {
                    int freq = item.getDefinition().getTs().getData().getAnnualFrequency();
                    Map<AlgorithmDescriptor, RegSarimaReport> cur = reports.get(freq);
                    if (cur == null) {
                        cur = new LinkedHashMap<>();
                        reports.put(freq, cur);
                    }
                    AlgorithmDescriptor method = item.getDefinition().getDomainSpec().getAlgorithmDescriptor();
                    RegSarimaReport report = cur.get(method);
                    if (report == null) {
                        report = new RegSarimaReport(freq);
                        cur.put(method, report);
                    }
                    if (rslt instanceof HasRegSarimaPreprocessing) {
                        report.add(((HasRegSarimaPreprocessing) rslt).getPreprocessing());
                    }
                }
            }
        }
        return reports;
    }

}
