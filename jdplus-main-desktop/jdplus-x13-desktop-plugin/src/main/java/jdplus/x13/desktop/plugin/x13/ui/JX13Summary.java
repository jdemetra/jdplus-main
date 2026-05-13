/*
 * Copyright 2026 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.x13.desktop.plugin.x13.ui;

import jdplus.toolkit.desktop.plugin.TsDynamicProvider;
import jdplus.toolkit.desktop.plugin.components.JTsChart;
import jdplus.toolkit.desktop.plugin.components.parts.HasTsCollection.TsUpdateMode;
import jdplus.main.desktop.design.SwingComponent;
import jdplus.toolkit.desktop.plugin.interfaces.Disposable;
import jdplus.sa.desktop.plugin.processing.JSIView;
import jdplus.toolkit.desktop.plugin.ui.Disposables;
import jdplus.toolkit.desktop.plugin.ui.processing.TsViewToolkit;
import jdplus.toolkit.desktop.plugin.util.NbComponents;
import jdplus.toolkit.base.api.modelling.SeriesInfo;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.sa.base.api.SaDictionaries;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsCollection;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsFactory;
import jdplus.toolkit.base.api.timeseries.TsInformationType;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import jdplus.toolkit.base.api.util.MultiLineNameUtil;
import jdplus.x13.base.api.x11.X11Dictionaries;
import jdplus.x13.desktop.plugin.html.HtmlX13Summary;
import java.awt.*;
import java.util.Arrays;
import javax.swing.*;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.x13.base.core.x11.X11Results;
import jdplus.x13.base.core.x13.X13Document;
import jdplus.x13.base.core.x13.X13Results;

/**
 * @author Kristof Bayens
 */
@SwingComponent
public final class JX13Summary extends JComponent implements Disposable {

    private final Box document;
    private final JTsChart chart;
    private final JSIView siPanel;
    private X13Document doc;

    public JX13Summary() {
        setLayout(new BorderLayout());

        this.chart = new JTsChart();
        chart.setTsUpdateMode(TsUpdateMode.None);
        this.siPanel = new JSIView();

        JSplitPane split1 = NbComponents.newJSplitPane(JSplitPane.VERTICAL_SPLIT, chart, siPanel);
        split1.setDividerLocation(0.5);
        split1.setResizeWeight(.5);

        this.document = Box.createHorizontalBox();

        JSplitPane split2 = NbComponents.newJSplitPane(JSplitPane.HORIZONTAL_SPLIT, document, split1);
        split2.setDividerLocation(0.5);
        split2.setResizeWeight(.4);

        add(split2, BorderLayout.CENTER);
    }

    public void set(X13Document doc) {
        this.doc = doc;
        if (doc == null) {
            return;
        }
        X13Results results = doc.getResult();
        if (results == null) {
            return;
        }

        HtmlX13Summary summary = new HtmlX13Summary(MultiLineNameUtil.join(doc.getInput().getName()), results);
        Disposables.disposeAndRemoveAll(document).add(TsViewToolkit.getHtmlViewer(summary));

        String[] lowSeries = lowSeries(results.getPreprocessing() == null);
        chart.setTsCollection(
                Arrays.stream(lowSeries).map(s -> getMainSeries(s)).collect(TsCollection.toTsCollection())
        );

        X11Results x11 = doc.getResult().getDecomposition();
        if (x11 != null) {
            TsDomain dom = results.getDecomposition().getActualDomain();
            TsData si = TsData.fitToDomain(results.getDecomposition().getD8(), dom);
            TsData seas = TsData.fitToDomain(results.getDecomposition().getD10(), dom);

            siPanel.setSiData(seas, si);
        } else {
            siPanel.reset();
        }
    }

    private Ts getMainSeries(String str) {
        return TsFactory.getDefault().makeTs(TsDynamicProvider.monikerOf(doc, str), TsInformationType.All);
    }

    private static String generateId(String name, String id) {
        return TsDynamicProvider.CompositeTs.builder()
                .name(name)
                .back(id + SeriesInfo.B_SUFFIX)
                .now(id)
                .fore(id + SeriesInfo.F_SUFFIX)
                .build().toString();
    }

    private static String generateSimpleId(String name, String id) {
        return TsDynamicProvider.CompositeTs.builder()
                .name(name)
                .now(id)
                .build().toString();
    }

    public static String[] lowSeries(boolean x11) {
        if (x11) {
            return new String[]{
                generateSimpleId("Series", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, X11Dictionaries.B1)),
                generateSimpleId("Seasonally adjusted", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, X11Dictionaries.D11)),
                generateSimpleId("Trend", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, X11Dictionaries.D12))
            };
        } else {
            return new String[]{
                generateId("Series", SaDictionaries.Y),
                generateId("Seasonally adjusted", SaDictionaries.SA),
                generateId("Trend", SaDictionaries.T)
            };
        }
    }

    @Override
    public void dispose() {
        doc = null;
        Disposables.disposeAndRemoveAll(document);
    }
}
