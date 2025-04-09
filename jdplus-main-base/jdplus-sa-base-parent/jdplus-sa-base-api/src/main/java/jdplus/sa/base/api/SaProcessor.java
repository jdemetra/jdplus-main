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
package jdplus.sa.base.api;

import jdplus.toolkit.base.api.design.Algorithm;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.api.information.Explorable;
import jdplus.toolkit.base.api.information.GenericExplorable;

/**
 * Generic seasonal adjustment processor
 *
 * @author PALATEJ
 * @param <R>
 */
@Algorithm
@FunctionalInterface
public interface SaProcessor<R extends Explorable> {
    GenericExplorable process(TsData series, ModellingContext context, ProcessingLog log);
}
