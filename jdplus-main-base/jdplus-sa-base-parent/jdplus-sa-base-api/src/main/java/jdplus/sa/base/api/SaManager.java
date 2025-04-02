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

import jdplus.toolkit.base.api.information.Explorable;
import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;
import jdplus.toolkit.base.api.processing.DefaultProcessingLog;
import jdplus.toolkit.base.api.processing.ProcDiagnostic;
import jdplus.toolkit.base.api.processing.ProcQuality;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import java.util.ArrayList;
import java.util.List;
import jdplus.toolkit.base.api.information.GenericExplorable;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class SaManager {
    
    public List<SaProcessingFactory> processors() {
        return SaProcessingFactoryLoader.get();
    }

    public List<SaOutputFactory> outputFactories() {
        return SaOutputFactoryLoader.get();
    }
    
    public void reload(){
        SaProcessingFactoryLoader.reload();
        SaOutputFactoryLoader.reload();
    }

    public Explorable process(TsData series, SaSpecification spec, ModellingContext context, ProcessingLog log) {
        List<SaProcessingFactory> all = processors();
        for (SaProcessingFactory fac : all) {
            SaSpecification dspec = fac.decode(spec);
            if (dspec != null) {
                return fac.processor(dspec).process(series, context, log);
            }
        }
        return null;
    }

    public SaEstimation process(SaDefinition def, ModellingContext context, boolean verbose) {
        List<SaProcessingFactory> all = processors();
        SaSpecification spec = def.activeSpecification();
        for (SaProcessingFactory fac : all) {
            SaSpecification dspec = fac.decode(spec);
            if (dspec != null) {
                ProcessingLog log = verbose ? new DefaultProcessingLog() : ProcessingLog.dummy();
                SaProcessor processor = fac.processor(dspec);
                GenericExplorable rslt = processor.process(def.getTs().getData(), context, log);
                if (rslt.isValid()) {
                    List<String> warnings = new ArrayList<>();
                    List<ProcDiagnostic> tests = new ArrayList<>();
                    fac.fillDiagnostics(tests, warnings, rslt);
                    SaSpecification pspec = fac.generateSpec(spec, rslt);
                    ProcQuality quality = ProcDiagnostic.summary(tests);
                    return SaEstimation.builder()
                            .results(rslt)
                            .diagnostics(tests)
                            .quality(quality)
                            .warnings(warnings)
                            .pointSpec(pspec)
                            .build();
                } else {
                    return SaEstimation.builder()
                            .results(rslt)
                            .quality(ProcQuality.Undefined)
                            .build();
                }
            }
        }
        return null;
    }

    public SaEstimation resetQuality(SaEstimation estimation) {
        Explorable rslt = estimation.getResults();
        if (rslt == null) {
            return estimation.withQuality(ProcQuality.Undefined);
        }
        List<SaProcessingFactory> all = processors();
        SaSpecification spec = estimation.getPointSpec();
        for (SaProcessingFactory fac : all) {
            SaSpecification dspec = fac.decode(spec);
            if (dspec != null) {
                List<String> warnings = new ArrayList<>();
                List<ProcDiagnostic> tests = new ArrayList<>();
                fac.fillDiagnostics(tests, warnings, rslt);
                return estimation.toBuilder()
                        .quality(ProcDiagnostic.summary(tests))
                        .warnings(warnings)
                        .build();
            }
        }
        return estimation.withQuality(ProcQuality.Undefined);
    }

    public <I extends SaSpecification> SaProcessingFactory factoryFor(SaSpecification spec) {
        List<SaProcessingFactory> all = processors();
        return all.stream().filter(p -> p.canHandle(spec)).findFirst().orElseThrow();
    }

    public <I extends SaSpecification> SaProcessingFactory factoryFor(AlgorithmDescriptor desc) {
        List<SaProcessingFactory> all = processors();
        return all.stream().filter(p -> p.descriptor().isCompatible(desc)).findFirst().orElseThrow();
    }
}
