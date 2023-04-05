/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved
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
import jdplus.toolkit.desktop.plugin.components.parts.HasTs;
import jdplus.toolkit.base.api.timeseries.TsInformationType;
import jdplus.toolkit.base.api.timeseries.Ts;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import org.openide.windows.TopComponent;
import jdplus.toolkit.desktop.plugin.TsActionOpenSpi;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider
public final class TsViewsTsAction implements TsActionOpenSpi {

    @Override
    public String getName() {
        return "TsViewsTs";
    }

    @Override
    public String getDisplayName() {
        return "All ts views";
    }

    @Override
    public void open(Ts ts) {
       TsManager.get().loadAsync(ts, TsInformationType.Data, this::dispatch);
    }
    
    private void dispatch(Ts ts){
        TopComponent.getRegistry().getOpened().stream()
                .filter(HasTs.class::isInstance)
                .forEach(o -> ((HasTs) o).setTs(ts));
    }
}
