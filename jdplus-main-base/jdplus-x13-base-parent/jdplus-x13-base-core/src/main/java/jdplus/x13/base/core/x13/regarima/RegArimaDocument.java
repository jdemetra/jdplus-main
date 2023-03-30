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
package jdplus.x13.base.core.x13.regarima;

import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.x13.base.api.regarima.RegArimaSpec;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.AbstractTsDocument;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;

/**
 *
 * @author PALATEJ
 */
public class RegArimaDocument extends AbstractTsDocument<RegArimaSpec, RegSarimaModel> {

    private final ModellingContext context;

    public RegArimaDocument() {
        super(RegArimaSpec.RG4);
        context = ModellingContext.getActiveContext();
    }

    public RegArimaDocument(ModellingContext context) {
        super(RegArimaSpec.RG4);
        this.context = context;
    }

    @Override
    protected RegSarimaModel internalProcess(RegArimaSpec spec, TsData data) {
        return RegArimaKernel.of(spec, context).process(data, ProcessingLog.dummy());
    }

}
