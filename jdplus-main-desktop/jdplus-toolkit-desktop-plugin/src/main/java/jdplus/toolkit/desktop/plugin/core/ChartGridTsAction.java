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
package jdplus.toolkit.desktop.plugin.core;

import jdplus.toolkit.desktop.plugin.TsManager;
import jdplus.toolkit.desktop.plugin.components.JTsGrid;
import jdplus.toolkit.desktop.plugin.components.parts.HasChart.LinesThickness;
import jdplus.toolkit.desktop.plugin.components.parts.HasTsCollection.TsUpdateMode;
import jdplus.toolkit.desktop.plugin.core.tools.JTsChartTopComponent;
import jdplus.toolkit.desktop.plugin.core.tools.JTsGridTopComponent;
import jdplus.toolkit.desktop.plugin.tsproviders.DataSourceManager;
import jdplus.toolkit.desktop.plugin.util.NbComponents;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsCollection;
import jdplus.toolkit.base.api.timeseries.TsInformationType;
import jdplus.toolkit.base.api.util.MultiLineNameUtil;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.util.HelpCtx;
import org.openide.windows.TopComponent;

import java.awt.*;
import java.beans.BeanInfo;
import jdplus.toolkit.desktop.plugin.TsActionOpenSpi;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider
public final class ChartGridTsAction implements TsActionOpenSpi {

    @Override
    public String getName() {
        return "ChartGridTsAction";
    }

    @Override
    public String getDisplayName() {
        return "Chart & grid";
    }

    @Override
    public void open(Ts ts) {
        String topComponentName = getTopComponentName(ts);
        NbComponents.findTopComponentByName(topComponentName)
                .orElseGet(() -> createComponent(topComponentName, ts))
                .requestActive();
    }

    private String getTopComponentName(Ts ts) {
        return getName() + ts.getMoniker();
    }

    private static TopComponent createComponent(String topComponentName, Ts ts) {
        MultiViewDescription[] descriptions = {new ChartTab(ts), new GridTab(ts)};
        TopComponent c = MultiViewFactory.createMultiView(descriptions, descriptions[0], null);
        c.setName(topComponentName);
        c.setIcon(DataSourceManager.get().getImage(ts.getMoniker(), BeanInfo.ICON_COLOR_16x16, false));
        applyText(ts.getName(), c);
        c.open();
        return c;
    }

    private static void applyText(String text, TopComponent c) {
        if (text.isEmpty()) {
            c.setDisplayName(" ");
            c.setToolTipText(null);
        } else if (text.startsWith("<html>")) {
            c.setDisplayName(text);
            c.setToolTipText(text);
        } else {
            c.setDisplayName(MultiLineNameUtil.lastWithMax(text, 40));
            c.setToolTipText(MultiLineNameUtil.toHtml(text));
        }
    }

    @lombok.AllArgsConstructor
    private static final class ChartTab implements MultiViewDescription {

        private final Ts ts;

        @Override
        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_NEVER;
        }

        @Override
        public String getDisplayName() {
            return "Chart";
        }

        @Override
        public Image getIcon() {
            return DataSourceManager.get().getImage(ts.getMoniker(), BeanInfo.ICON_COLOR_16x16, false);
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }

        @Override
        public String preferredID() {
            return "Chart";
        }

        @Override
        public MultiViewElement createElement() {
            TsCollection col = TsCollection.of(ts);
            JTsChartTopComponent result = new JTsChartTopComponent();
            result.getChart().setTsCollection(col);
            result.getChart().setTsUpdateMode(TsUpdateMode.None);
            result.getChart().setLegendVisible(true);
            result.getChart().setTitleVisible(false);
            result.getChart().setLinesThickness(LinesThickness.Thick);
            TsManager.get().loadAsync(col, TsInformationType.All, result.getChart()::replaceTsCollection);
            return result;
        }
    }

    @lombok.AllArgsConstructor
    private static final class GridTab implements MultiViewDescription {

        private final Ts ts;

        @Override
        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_NEVER;
        }

        @Override
        public String getDisplayName() {
            return "Grid";
        }

        @Override
        public Image getIcon() {
            return DataSourceManager.get().getImage(ts.getMoniker(), BeanInfo.ICON_COLOR_16x16, false);
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }

        @Override
        public String preferredID() {
            return "Grid";
        }

        @Override
        public MultiViewElement createElement() {
            TsCollection col = TsCollection.of(ts);
            JTsGridTopComponent result = new JTsGridTopComponent();
            result.getGrid().setTsCollection(col);
            result.getGrid().setTsUpdateMode(TsUpdateMode.None);
            result.getGrid().setMode(JTsGrid.Mode.SINGLETS);
            TsManager.get().loadAsync(col, TsInformationType.All, result.getGrid()::replaceTsCollection);
            return result;
        }
    }
}
