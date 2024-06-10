/*
 * Copyright 2015 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or â€“ as soon they will be approved
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
package internal.spreadsheet.desktop.plugin;

import ec.util.spreadsheet.html.HtmlBookFactory;
import jdplus.toolkit.desktop.plugin.Config;
import jdplus.toolkit.desktop.plugin.ConfigEditor;
import jdplus.toolkit.desktop.plugin.DemetraIcons;
import jdplus.toolkit.desktop.plugin.Persistable;
import jdplus.toolkit.desktop.plugin.actions.Configurable;
import jdplus.toolkit.desktop.plugin.beans.BeanConfigurator;
import jdplus.toolkit.desktop.plugin.beans.BeanHandler;
import jdplus.toolkit.desktop.plugin.datatransfer.DataTransferSpi;
import lombok.NonNull;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

import java.awt.datatransfer.DataFlavor;

/**
 * @author Philippe Charles
 */
@ServiceProviders({
        @ServiceProvider(service = DataTransferSpi.class, position = HtmlDataTransfer.POSITION)
})
public final class HtmlDataTransfer implements DataTransferSpi, Configurable, Persistable, ConfigEditor {

    static final int POSITION = 1500;

    private final DataFlavor dataFlavor;
    @lombok.experimental.Delegate
    private final SpreadSheetDataTransferSupport support;
    private final BeanConfigurator<SpreadSheetDataTransferBean, HtmlDataTransfer> configurator;
    private SpreadSheetDataTransferBean config;

    public HtmlDataTransfer() {
        this.dataFlavor = createDataFlavor();
        this.support = new SpreadSheetDataTransferSupport(new HtmlBookFactory(), () -> config, SpreadSheetDataTransferSupport.RawDataType.TEXT);
        this.configurator = new BeanConfigurator<>(
                new HtmlBeanHandler(),
                new SpreadSheetDataTransferConverter("ec.tss.datatransfer.TssTransferHandler", "HTML", ""),
                new SpreadSheetDataTransferEditor("Configure HTML tables", ImageUtilities.icon2Image(DemetraIcons.CLIPBOARD_PASTE_DOCUMENT_TEXT_16))
        );
        this.config = new SpreadSheetDataTransferBean();
    }

    @Override
    public int getPosition() {
        return POSITION;
    }

    @Override
    public String getName() {
        return "HTML";
    }

    @Override
    public String getDisplayName() {
        return "HTML tables";
    }

    @Override
    public DataFlavor getDataFlavor() {
        return dataFlavor;
    }

    @Override
    public @NonNull Config getConfig() {
        return configurator.getConfig(this);
    }

    @Override
    public void setConfig(@NonNull Config config) {
        configurator.setConfig(this, config);
    }

    @Override
    public @NonNull Config editConfig(@NonNull Config config) {
        return configurator.editConfig(config);
    }

    @Override
    public void configure() {
        Configurable.configure(this, this);
    }

    private static DataFlavor createDataFlavor() {
        try {
            return new DataFlavor("text/html;class=java.lang.String");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final class HtmlBeanHandler implements BeanHandler<SpreadSheetDataTransferBean, HtmlDataTransfer> {

        @Override
        public SpreadSheetDataTransferBean load(HtmlDataTransfer resource) {
            return resource.config;
        }

        @Override
        public void store(HtmlDataTransfer resource, SpreadSheetDataTransferBean bean) {
            resource.config = bean;
        }
    }
}
