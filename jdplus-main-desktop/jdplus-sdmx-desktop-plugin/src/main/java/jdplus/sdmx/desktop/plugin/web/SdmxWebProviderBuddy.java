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
package jdplus.sdmx.desktop.plugin.web;

import static ec.util.chart.impl.TangoColorScheme.DARK_ORANGE;
import static ec.util.chart.swing.SwingColorSchemeSupport.rgbToColor;

import ec.util.various.swing.FontAwesome;
import internal.sdmx.desktop.plugin.SdmxIcons;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import jdplus.sdmx.base.api.web.SdmxWebBean;
import jdplus.sdmx.base.api.web.SdmxWebProvider;
import jdplus.toolkit.base.api.timeseries.TsMoniker;
import jdplus.toolkit.base.tsp.DataSet;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.desktop.plugin.Config;
import jdplus.toolkit.desktop.plugin.Persistable;
import jdplus.toolkit.desktop.plugin.TsManager;
import jdplus.toolkit.desktop.plugin.actions.Configurable;
import jdplus.toolkit.desktop.plugin.properties.PropertySheetDialogBuilder;
import jdplus.toolkit.desktop.plugin.tsproviders.DataSourceProviderBuddy;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import sdmxdl.Feature;
import sdmxdl.SourceRequest;
import sdmxdl.web.WebSource;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider(DataSourceProviderBuddy.class)
public final class SdmxWebProviderBuddy implements DataSourceProviderBuddy, Configurable, Persistable {

    private static final String SOURCE = "DOTSTAT";

    private SdmxWebConfiguration configuration;

    public SdmxWebProviderBuddy() {
        this.configuration = new SdmxWebConfiguration();
        updateProvider();
    }

    private void updateProvider() {
        lookupProvider().ifPresent(provider -> {
            provider.setSdmxManager(configuration.toSdmxWebManager());
            provider.setLanguages(configuration.toLanguages());
            provider.setDisplayCodes(configuration.isDisplayCodes());
        });
    }

    @Override
    public @lombok.NonNull String getProviderName() {
        return SOURCE;
    }

    @Override
    public Image getIconOrNull(int type, boolean opened) {
        return SdmxIcons.getDefaultIcon(16).getImage();
    }

    @Override
    public Image getIconOrNull(@lombok.NonNull DataSource dataSource, int type, boolean opened) {
        Optional<SdmxWebProvider> lookupProvider = lookupProvider();
        if (lookupProvider.isPresent()) {
            SdmxWebProvider provider = lookupProvider.orElseThrow();
            SdmxWebBean bean = provider.decodeBean(dataSource);
            WebSource source = provider.getSdmxManager().getSources().get(bean.getSource());
            if (source != null) {
                Image result = getSourceIcon(provider, source);
                return supportsDataQueryDetail(provider, source)
                        ? result
                        : ImageUtilities.mergeImages(result, getWarningBadge(), 13, 8);
            }
        }
        return DataSourceProviderBuddy.super.getIconOrNull(dataSource, type, opened);
    }

    @Override
    public Image getIconOrNull(@lombok.NonNull TsMoniker moniker, int type, boolean opened) {
        Optional<SdmxWebProvider> lookupProvider = lookupProvider();
        if (lookupProvider.isPresent()) {
            SdmxWebProvider provider = lookupProvider.orElseThrow();
            Optional<DataSet> dataSet = provider.toDataSet(moniker);
            if (dataSet.isPresent()) {
                SdmxWebBean bean = provider.decodeBean(dataSet.orElseThrow().getDataSource());
                WebSource source = provider.getSdmxManager().getSources().get(bean.getSource());
                if (source != null) {
                    return getSourceIcon(provider, source);
                }
            }
        }
        return DataSourceProviderBuddy.super.getIconOrNull(moniker, type, opened);
    }

    @Override
    public List<Sheet.Set> getSheetOfBeanOrNull(@NonNull Object bean) {
        return bean instanceof SdmxWebBean ? getSheetOrNull((SdmxWebBean) bean) : null;
    }

    @Override
    public void configure() {
        SdmxWebConfiguration editable = SdmxWebConfiguration.copyOf(configuration);
        PropertySheetDialogBuilder editor = new PropertySheetDialogBuilder()
                .title("Configure "
                        + lookupProvider().map(SdmxWebProvider::getDisplayName).orElse(""))
                .icon(SdmxIcons.getDefaultIcon(16));
        if (editor.editSheet(editable.toSheet())) {
            configuration = editable;
            updateProvider();
        }
    }

    @Override
    public @lombok.NonNull Config getConfig() {
        return SdmxWebConfiguration.PERSISTENCE.loadConfig(configuration);
    }

    @Override
    public void setConfig(@lombok.NonNull Config config) throws IllegalArgumentException {
        SdmxWebConfiguration.PERSISTENCE.storeConfig(configuration, config);
        updateProvider();
    }

    private List<Sheet.Set> getSheetOrNull(SdmxWebBean bean) {
        return lookupProvider()
                .map(provider -> SdmxWebBeanSupport.newSheet(bean, provider))
                .orElse(null);
    }

    private static Image getSourceIcon(SdmxWebProvider provider, WebSource source) {
        return ImageUtilities.icon2Image(
                SdmxIcons.getFavicon(provider.getSdmxManager().getNetworking(), source, 16));
    }

    private static boolean supportsDataQueryDetail(SdmxWebProvider provider, WebSource source) {
        try {
            return provider.getSdmxManager()
                    .using(source)
                    .getSupportedFeatures(SourceRequest.builder()
                            .languages(provider.getLanguages())
                            .build())
                    .contains(Feature.DATA_QUERY_DETAIL);
        } catch (IOException ex) {
            return false;
        }
    }

    private static Image getWarningBadge() {
        return FontAwesome.FA_EXCLAMATION_TRIANGLE.getImage(rgbToColor(DARK_ORANGE), 8f);
    }

    private static Optional<SdmxWebProvider> lookupProvider() {
        return TsManager.get().getProvider(SdmxWebProvider.class);
    }
}
