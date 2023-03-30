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

import jdplus.toolkit.base.api.information.InformationExtractors;
import jdplus.toolkit.base.api.processing.DefaultProcessingLog;
import jdplus.toolkit.base.api.processing.GenericResults;
import jdplus.toolkit.base.api.processing.ProcResults;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.tramoseats.base.api.tramoseats.TramoSeats;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsSpec;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsKernel;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsResults;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(TramoSeats.Processor.class)
public class TramoSeatsProcessor implements TramoSeats.Processor {

    @Override
    public ProcResults process(TsData series, TramoSeatsSpec spec, ModellingContext context, List<String> items) {
        TramoSeatsKernel tramoseats = TramoSeatsKernel.of(spec, context);
        DefaultProcessingLog log = new DefaultProcessingLog();
        TramoSeatsResults rslt = tramoseats.process(series, log);
        return GenericResults.of(rslt, items, log);
    }

    @Override
    public Map<String, Class> outputDictionary(boolean compact) {
        Map<String, Class> dic = new LinkedHashMap<>();
        InformationExtractors.fillDictionary(TramoSeatsResults.class, null, dic, compact);
        return dic;
    }

}
