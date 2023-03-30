/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.tramoseats.base.core.tramo;

import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.AbstractTsDocument;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.tramoseats.base.api.tramo.TramoSpec;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;

/**
 *
 * @author PALATEJ
 */
public class TramoDocument extends AbstractTsDocument<TramoSpec, RegSarimaModel> {

    private final ModellingContext context;

    public TramoDocument() {
        super(TramoSpec.TRfull);
        context = ModellingContext.getActiveContext();
    }

    public TramoDocument(ModellingContext context) {
        super(TramoSpec.TRfull);
        this.context = context;
    }

    @Override
    protected RegSarimaModel internalProcess(TramoSpec spec, TsData data) {
        return TramoKernel.of(spec, context).process(data, ProcessingLog.dummy());
    }

}
