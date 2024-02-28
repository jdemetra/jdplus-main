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
package jdplus.toolkit.desktop.plugin.interchange;

import jdplus.toolkit.base.api.design.ExtensionPoint;
import jdplus.toolkit.desktop.plugin.NamedService;
import jdplus.toolkit.desktop.plugin.util.NetBeansServiceBackend;
import nbbrd.design.swing.OnEDT;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceSorter;
import lombok.NonNull;

import java.io.IOException;
import java.util.List;

/**
 * Service that performs import/export of configs.
 *
 * @author Philippe Charles
 * @since 1.5.1
 */
@ExtensionPoint
@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        backend = NetBeansServiceBackend.class
)
public interface InterchangeSpi extends NamedService {

    @ServiceSorter
    int getPosition();

    @OnEDT
    boolean canImport(@NonNull List<? extends Importable> importables);

    @OnEDT
    void performImport(@NonNull List<? extends Importable> importables) throws IOException, IllegalArgumentException;

    @OnEDT
    boolean canExport(@NonNull List<? extends Exportable> exportables);

    @OnEDT
    void performExport(@NonNull List<? extends Exportable> exportables) throws IOException;
}
