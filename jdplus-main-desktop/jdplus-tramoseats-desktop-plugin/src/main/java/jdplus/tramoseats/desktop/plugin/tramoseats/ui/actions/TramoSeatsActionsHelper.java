/*
 * Copyright 2022 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.tramoseats.desktop.plugin.tramoseats.ui.actions;

import jdplus.sa.desktop.plugin.util.ActionsHelper;
import jdplus.tramoseats.desktop.plugin.tramoseats.ui.TramoSeatsUI;
import jdplus.sa.base.api.SaProcessingFactory;
import jdplus.sa.base.api.SaSpecification;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsSpec;
import java.util.List;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(service = ActionsHelper.class, position=100)
public class TramoSeatsActionsHelper implements ActionsHelper {

    @Override
    public List<String> selectedSeries() {
        return TramoSeatsUI.get().getSelectedComponents();
    }

    @Override
    public List<String> selectedMatrixItems() {
        return TramoSeatsUI.get().getSelectedDiagnostics();
    }

    @Override
    public List<String> allSeries() {
        return TramoSeatsUI.get().allComponents();
    }

    @Override
    public List<String> allMatrixItems() {
        return TramoSeatsUI.get().allDiagnostics();
    }
    
    @Override
    public boolean match(SaSpecification spec) {
        return spec instanceof TramoSeatsSpec;
    }

    @Override
    public boolean match(SaProcessingFactory fac) {
        return fac instanceof TramoSeatsFactory;
    }

    @Override
    public int defaultSeriesParameter() {
        return TramoSeatsUI.get().getDefaultSeriesParameter();
    }

}
