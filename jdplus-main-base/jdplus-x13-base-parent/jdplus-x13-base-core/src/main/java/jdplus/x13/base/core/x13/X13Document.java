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
package jdplus.x13.base.core.x13;

import jdplus.toolkit.base.api.processing.ProcDiagnostic;
import jdplus.toolkit.base.api.processing.ProcQuality;
import jdplus.toolkit.base.api.processing.ProcessingStatus;
import jdplus.sa.base.api.HasSaEstimation;
import jdplus.sa.base.api.SaEstimation;
import jdplus.sa.base.api.SaSpecification;
import jdplus.toolkit.base.api.timeseries.AbstractTsDocument;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.x13.base.api.x13.X13Spec;
import java.util.ArrayList;
import java.util.List;
import jdplus.toolkit.base.api.processing.DefaultProcessingLog;

/**
 *
 * @author PALATEJ
 */
public class X13Document extends AbstractTsDocument<X13Spec, X13Results> implements HasSaEstimation {

    private final ModellingContext context;

    public X13Document() {
        super(X13Spec.RSA4);
        context = ModellingContext.getActiveContext();
    }

    public X13Document(ModellingContext context) {
        super(X13Spec.RSA4);
        this.context = context;
    }

    public ModellingContext getContext() {
        return context;
    }

    @Override
    protected X13Results internalProcess(X13Spec spec, TsData data) {
        return X13Kernel.of(spec, context).process(data, new DefaultProcessingLog());
    }

    @Override
    public SaEstimation getEstimation() {
        if (getStatus() == ProcessingStatus.Unprocessed) {
            return null;
        }
        List<ProcDiagnostic> tests = new ArrayList<>();
        X13Results result = getResult();
        SaSpecification pspec = null;
        ProcQuality quality = ProcQuality.Error;
        List<String> warnings=new ArrayList<>();
        if (getStatus() == ProcessingStatus.Valid) {
            X13Factory.getInstance().fillDiagnostics(tests, warnings, result);
            pspec = X13Factory.getInstance().generateSpec(getSpecification(), result);
            quality = ProcDiagnostic.summary(tests);
        }
        return SaEstimation.builder()
                .results(result)
                .diagnostics(tests)
                .quality(quality)
                .warnings(warnings)
                .pointSpec(pspec)
                .build();
    }
}
