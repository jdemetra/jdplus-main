/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.sdmx.desktop.plugin.file;

import internal.sdmx.desktop.plugin.SdmxIcons;
import java.awt.*;
import java.util.List;
import java.util.Optional;
import javax.swing.*;
import jdplus.sdmx.base.api.file.SdmxFileBean;
import jdplus.sdmx.base.api.file.SdmxFileProvider;
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

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider(DataSourceProviderBuddy.class)
public final class SdmxFileProviderBuddy implements DataSourceProviderBuddy, Configurable, Persistable {

    private SdmxFileConfiguration configuration;

    public SdmxFileProviderBuddy() {
        this.configuration = new SdmxFileConfiguration();
        updateProvider();
    }

    private void updateProvider() {
        lookupProvider().ifPresent(provider -> {
            provider.setSdmxManager(configuration.toSdmxFileManager());
            provider.setLanguages(configuration.toLanguages());
        });
    }

    @Override
    public @lombok.NonNull String getProviderName() {
        return SdmxFileProvider.NAME;
    }

    @Override
    public Image getIconOrNull(int type, boolean opened) {
        return SdmxIcons.getDefaultIcon(16).getImage();
    }

    @Override
    public List<Sheet.Set> getSheetOfBeanOrNull(@NonNull Object bean) {
        return bean instanceof SdmxFileBean ? getSheetOrNull((SdmxFileBean) bean) : null;
    }

    @Override
    public void configure() {
        SdmxFileConfiguration editable = SdmxFileConfiguration.copyOf(configuration);
        PropertySheetDialogBuilder editor = new PropertySheetDialogBuilder()
                .title("Configure "
                        + lookupProvider().map(SdmxFileProvider::getDisplayName).orElse(""))
                .icon(SdmxIcons.getDefaultIcon(16));
        if (editor.editSheet(editable.toSheet())) {
            configuration = editable;
            updateProvider();
        }
    }

    @Override
    public @lombok.NonNull Config getConfig() {
        return SdmxFileConfiguration.PERSISTENCE.loadConfig(configuration);
    }

    @Override
    public void setConfig(@lombok.NonNull Config config) throws IllegalArgumentException {
        SdmxFileConfiguration.PERSISTENCE.storeConfig(configuration, config);
        updateProvider();
    }

    private List<Sheet.Set> getSheetOrNull(SdmxFileBean bean) {
        return lookupProvider()
                .map(provider -> SdmxFileBeanSupport.newSheet(bean, provider))
                .orElse(null);
    }

    private static Optional<SdmxFileProvider> lookupProvider() {
        return TsManager.get().getProvider(SdmxFileProvider.class);
    }
}
