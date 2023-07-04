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
package jdplus.x13.base.r;

import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.core.timeseries.simplets.analysis.RevisionHistory;
import jdplus.toolkit.base.r.timeseries.Revisions;
import jdplus.x13.base.api.x13.X13Spec;
import jdplus.x13.base.core.x13.X13Kernel;
import jdplus.x13.base.core.x13.X13Results;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class X13RevisionHistory {

     public Revisions<X13Results> revisions(TsData series, X13Spec spec, ModellingContext context) {
        X13Kernel kernel = X13Kernel.of(spec, context);
        RevisionHistory<X13Results> rh = new RevisionHistory<>(series.getDomain(), d -> kernel.process(TsData.fitToDomain(series, d), ProcessingLog.dummy()));
        return new Revisions<>(rh);
    }
}
