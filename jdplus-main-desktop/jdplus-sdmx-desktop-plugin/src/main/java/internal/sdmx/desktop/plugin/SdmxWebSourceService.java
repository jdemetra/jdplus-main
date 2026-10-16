/*
 * Copyright 2015 National Bank of Belgium
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
package internal.sdmx.desktop.plugin;

import ec.util.completion.swing.JAutoCompletion;
import javax.swing.text.JTextComponent;
import jdplus.sdmx.base.api.web.SdmxWebProvider;
import jdplus.toolkit.desktop.plugin.TsManager;
import jdplus.toolkit.desktop.plugin.completion.AutoCompletionSpi;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import sdmxdl.web.WebSource;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider
public final class SdmxWebSourceService implements AutoCompletionSpi {

    @Override
    public @NonNull String getPath() {
        return WebSource.class.getName();
    }

    @Override
    public @NonNull JAutoCompletion bind(@NonNull JTextComponent textComponent) {
        JAutoCompletion result = new JAutoCompletion(textComponent);
        result.setMinLength(0);
        TsManager.get()
                .getProvider(SdmxWebProvider.class)
                .map(SdmxAutoCompletion::onWebSource)
                .ifPresent(completion -> {
                    result.setSource(completion.getSource());
                    result.getList().setCellRenderer(completion.getRenderer());
                });
        return result;
    }
}
