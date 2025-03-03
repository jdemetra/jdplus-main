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
package jdplus.tramoseats.base.core.tramoseats;

import jdplus.toolkit.base.api.processing.ProcDiagnostic;
import jdplus.toolkit.base.api.processing.ProcQuality;
import jdplus.toolkit.base.api.processing.ProcessingStatus;
import jdplus.sa.base.api.HasSaEstimation;
import jdplus.sa.base.api.SaEstimation;
import jdplus.sa.base.api.SaSpecification;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.AbstractTsDocument;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsSpec;
import java.util.ArrayList;
import java.util.List;
import jdplus.toolkit.base.api.processing.DefaultProcessingLog;

/**
 *
 * @author PALATEJ
 */
public class TramoSeatsDocument extends AbstractTsDocument<TramoSeatsSpec, TramoSeatsResults> implements HasSaEstimation {

    private final ModellingContext context;

    public TramoSeatsDocument() {
        super(TramoSeatsSpec.RSAfull);
        context = ModellingContext.getActiveContext();
    }

    public TramoSeatsDocument(ModellingContext context) {
        super(TramoSeatsSpec.RSAfull);
        this.context = context;
    }

    public ModellingContext getContext() {
        return context;
    }

    @Override
    protected TramoSeatsResults internalProcess(TramoSeatsSpec spec, TsData data) {
        return TramoSeatsKernel.of(spec, context).process(data, new DefaultProcessingLog());
    }

    @Override
    public SaEstimation getEstimation() {
        if (getStatus() == ProcessingStatus.Unprocessed) {
            return null;
        }
        List<ProcDiagnostic> tests = new ArrayList<>();
        TramoSeatsResults result = getResult();
        SaSpecification pspec = null;
        ProcQuality quality = ProcQuality.Error;
        if (getStatus() == ProcessingStatus.Valid) {
            TramoSeatsFactory.getInstance().fillDiagnostics(tests, result);
            pspec = TramoSeatsFactory.getInstance().generateSpec(getSpecification(), result);
            quality = ProcDiagnostic.summary(tests);
        }
        return SaEstimation.builder()
                .results(result)
                .diagnostics(tests)
                .quality(quality)
                .pointSpec(pspec)
                .build();
    }
}
