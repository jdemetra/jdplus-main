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
package jdplus.tramoseats.base.r;

import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.core.timeseries.simplets.analysis.RevisionHistory;
import jdplus.toolkit.base.r.timeseries.Revisions;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsSpec;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsKernel;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsResults;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class TramoSeatsRevisionHistory {


    public Revisions<TramoSeatsResults> revisions(TsData series, TramoSeatsSpec spec, ModellingContext context) {
        TramoSeatsKernel kernel = TramoSeatsKernel.of(spec, context);
        RevisionHistory<TramoSeatsResults> rh = new RevisionHistory<>(series.getDomain(), d -> kernel.process(TsData.fitToDomain(series, d), ProcessingLog.dummy()));
        return new Revisions<>(rh);
    }

}
