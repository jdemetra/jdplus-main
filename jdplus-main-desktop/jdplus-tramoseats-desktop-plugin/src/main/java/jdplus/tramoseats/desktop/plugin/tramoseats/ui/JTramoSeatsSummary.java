/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.desktop.plugin.tramoseats.ui;

import jdplus.toolkit.desktop.plugin.TsDynamicProvider;
import jdplus.toolkit.desktop.plugin.components.JTsChart;
import jdplus.toolkit.desktop.plugin.components.parts.HasTsCollection.TsUpdateMode;
import jdplus.main.desktop.design.SwingComponent;
import jdplus.toolkit.desktop.plugin.interfaces.Disposable;
import jdplus.sa.desktop.plugin.processing.JSIView;
import jdplus.toolkit.desktop.plugin.ui.Disposables;
import jdplus.toolkit.desktop.plugin.ui.processing.TsViewToolkit;
import jdplus.sa.base.api.ComponentDescriptor;
import jdplus.toolkit.desktop.plugin.util.NbComponents;
import jdplus.toolkit.base.api.modelling.SeriesInfo;
import jdplus.sa.base.api.SaDictionaries;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsCollection;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsFactory;
import jdplus.toolkit.base.api.timeseries.TsInformationType;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import jdplus.tramoseats.desktop.plugin.html.HtmlTramoSeatsSummary;
import jdplus.toolkit.base.api.util.MultiLineNameUtil;
import java.awt.*;
import java.util.Arrays;
import javax.swing.*;
import jdplus.toolkit.base.core.arima.ArimaModel;
import jdplus.tramoseats.base.core.seats.SeatsResults;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsDocument;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;

/**
 * @author Kristof Bayens
 */
@SwingComponent
public final class JTramoSeatsSummary extends JComponent implements Disposable{

    public static final ComponentDescriptor[] components;

    static {
        components = new ComponentDescriptor[5];
        components[0] = new ComponentDescriptor("sa", 1, false, true);
        components[1] = new ComponentDescriptor("trend", 0, true, true);
        components[2] = new ComponentDescriptor("seasonal", 1, true, false);
        components[3] = new ComponentDescriptor("transitory", 2, true, false);
        components[4] = new ComponentDescriptor("irregular", 3, true, false);
    }

    protected String[] getComponentsName(UcarimaModel ucm) {
        int n = ucm.getComponentsCount();
        String[] c = new String[n + 1];
        for (int i = 0; i < n; ++i) {
            c[i] = components[i].getName();
        }
        c[n] = components[4].getName();
        return c;
    }

    protected ArimaModel[] getComponents(UcarimaModel ucm) {
        int n = ucm.getComponentsCount();
        ArimaModel[] models = new ArimaModel[n + 1];
        for (int i = 0; i < n; ++i) {
            models[i] = components[i].isSignal() ? ucm.getComponent(components[i].getComponent())
                    : ucm.getComplement(components[i].getComponent());
        }
        models[n] = ucm.getComponent(components[4].getComponent());

        return models;
    }

    private final Box document_;
    private final JTsChart chart_;
    private final JSIView siPanel_;
    private TramoSeatsDocument doc_;

    public JTramoSeatsSummary() {
        setLayout(new BorderLayout());

        chart_ = new JTsChart();
        chart_.setTsUpdateMode(TsUpdateMode.None);
        siPanel_ = new JSIView();

        JSplitPane split1 = NbComponents.newJSplitPane(JSplitPane.VERTICAL_SPLIT, chart_, siPanel_);
        split1.setDividerLocation(0.5);
        split1.setResizeWeight(.5);

        document_ = Box.createHorizontalBox();

        JSplitPane split2 = NbComponents.newJSplitPane(JSplitPane.HORIZONTAL_SPLIT, document_, split1);
        split2.setDividerLocation(0.5);
        split2.setResizeWeight(.4);

        add(split2, BorderLayout.CENTER);
    }

    public void set(TramoSeatsDocument doc) {
        doc_ = doc;
        chart_.setTsCollection(TsCollection.EMPTY);
        siPanel_.reset();
        if (doc_ == null || doc_.getResult() == null) {
            return;
        }

        SeatsResults seats = doc_.getResult().getDecomposition();
        HtmlTramoSeatsSummary document;
        if (seats == null) {
            document = new HtmlTramoSeatsSummary(MultiLineNameUtil.join(doc_.getInput().getName()), doc_.getResult(), null, null);
        } else {
            UcarimaModel ucm = seats.getUcarimaModel();
            document = new HtmlTramoSeatsSummary(MultiLineNameUtil.join(doc_.getInput().getName()), doc_.getResult(), getComponentsName(ucm), getComponents(ucm));
        }
        Disposables.disposeAndRemoveAll(document_).add(TsViewToolkit.getHtmlViewer(document));

        String[] lowSeries = lowSeries();
        TsCollection ncoll = Arrays.stream(lowSeries).map(s->getMainSeries(s)).collect(TsCollection.toTsCollection());
        chart_.setTsCollection(ncoll);

        if (seats != null) {
            TsData seas = doc_.getResult().getData(Dictionary.concatenate(SaDictionaries.DECOMPOSITION, SaDictionaries.S_CMP), TsData.class);
            TsData irr = doc_.getResult().getData(Dictionary.concatenate(SaDictionaries.DECOMPOSITION, SaDictionaries.I_CMP), TsData.class);
            siPanel_.setData(seas, irr, doc_.getResult().getFinals().getMode());
        } 
    }

    private Ts getMainSeries(String str) {
        return TsFactory.getDefault().makeTs(TsDynamicProvider.monikerOf(doc_, str), TsInformationType.All);
    }

    @Override
    public void dispose() {
        doc_ = null;
        Disposables.disposeAndRemoveAll(document_);
    }
    
    private static String generateId(String name, String id){
        return TsDynamicProvider.CompositeTs.builder()
                .name(name)
                .back(id+SeriesInfo.B_SUFFIX)
                .now(id)
                .fore(id+SeriesInfo.F_SUFFIX)
                .build().toString();
    }
    
    public static String[] lowSeries(){
        return new String[]{
            generateId("Series", SaDictionaries.Y),
            generateId("Seasonally adjusted", SaDictionaries.SA),
            generateId("Trend", SaDictionaries.T)
        };
    }
}
