/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.tramoseats.base.core.tramoseats.spi;

import jdplus.toolkit.base.api.processing.DefaultProcessingLog;
import jdplus.toolkit.base.api.processing.GenericResults;
import jdplus.toolkit.base.api.processing.ProcResults;
import jdplus.tramoseats.base.api.seats.Seats;
import jdplus.tramoseats.base.api.seats.SeatsSpec;
import java.util.List;
import jdplus.tramoseats.base.core.seats.SeatsKernel;
import jdplus.tramoseats.base.core.seats.SeatsResults;
import jdplus.tramoseats.base.core.seats.SeatsToolkit;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(Seats.Processor.class)
public class SeatsProcessor implements Seats.Processor{

    @Override
    public ProcResults process(SeatsSpec spec, List<String> items) {
        // TODO Handling of additional items
        SeatsToolkit toolkit=SeatsToolkit.of(spec.getDecompositionSpec());
        SeatsKernel kernel=new SeatsKernel(toolkit);
        DefaultProcessingLog log =new DefaultProcessingLog();
        SeatsResults rslts = kernel.process(spec.getModelSpec(), log);
        return GenericResults.of(rslts, items, log);
    }

}
