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
package jdplus.toolkit.desktop.plugin;

import jdplus.toolkit.base.api.design.ExtensionPoint;
import jdplus.toolkit.desktop.plugin.util.NetBeansServiceBackend;
import jdplus.toolkit.base.api.timeseries.Ts;
import nbbrd.design.swing.OnEDT;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import lombok.NonNull;

/**
 *
 * @author Philippe Charles
 * @since 1.0.0
 */
@ExtensionPoint
@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        backend = NetBeansServiceBackend.class
)
public interface TsActionOpenSpi extends NamedService {

    @OnEDT
    void open(@NonNull Ts ts);
}
