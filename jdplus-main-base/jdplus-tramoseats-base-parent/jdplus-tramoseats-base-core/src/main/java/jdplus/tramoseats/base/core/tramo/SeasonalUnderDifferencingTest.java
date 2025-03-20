/*
 * Copyright 2013 National Bank of Belgium
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

import jdplus.sa.base.core.tests.SeasonalityTests;
import jdplus.toolkit.base.core.regsarima.regular.ModelDescription;
import jdplus.toolkit.base.core.regsarima.regular.ProcessingResult;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModelling;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.processing.ProcessingLog;

/**
 *
 * @author Jean Palate
 */
class SeasonalUnderDifferencingTest extends ModelController {
    
    private static final String SEAS_UNDERDIFF="Seasonal under-differencing has been fixed";

    private static final double DEF_SBOUND = .91;

    @Override
    ProcessingResult process(RegSarimaModelling modelling, TramoContext context) {
        ProcessingLog log = modelling.getLog();
        int period = modelling.getDescription().getAnnualFrequency();
        if (period == 1) {
            return ProcessingResult.Unprocessed;
        }
        if (fixSeasonalRoots(modelling)) {
            log.remark(SEAS_UNDERDIFF);
            return ProcessingResult.Changed;
        }
        // check seasonal quasi-unit roots
        if (!isUnderDiff(modelling, context)) {
            return ProcessingResult.Unchanged;
        }
        RegSarimaModelling scontext = buildNewModel(modelling);
        ModelComparator cmp = ModelComparator.builder()
                .build();
        if (cmp.compare(scontext, modelling) < 0) {
//            setReferenceModel(smodel);
            transferInformation(scontext, modelling);
            log.remark(SEAS_UNDERDIFF);
            return ProcessingResult.Changed;
        } else {
            return ProcessingResult.Unchanged;
        }
    }

    private boolean isUnderDiff(RegSarimaModelling modelling, TramoContext context) {
        DoubleSeq res = modelling.getEstimation().getConcentratedLikelihood().e();
        SeasonalityTests tests
                = SeasonalityTests.residualSeasonalityTest(res, modelling.getDescription().getAnnualFrequency());
        return tests.getScore() > 1 || (tests.getScore() == 1 && context.seasonal);
    }

    private RegSarimaModelling buildNewModel(RegSarimaModelling context) {
        ModelDescription ndesc = ModelDescription.copyOf(context.getDescription());
        SarimaOrders spec = ndesc.specification();
        spec.setBp(0);
        spec.setBd(1);
        spec.setBq(1);
        ndesc.setSpecification(spec);
        ndesc.setMean(false);
        RegSarimaModelling ncontext = RegSarimaModelling.of(ndesc);
        // estimate the new model
        if (!estimate(ncontext, false)) {
            return null;
        }
        return ncontext;
    }

    private boolean fixSeasonalRoots(RegSarimaModelling context) {
        ModelDescription description = context.getDescription();
        SarimaModel model = description.arima();
        SarimaOrders spec = model.orders();
        if (spec.getBd() != 0 || spec.getBp() != 1 || model.bphi(1) >= -DEF_SBOUND) {
            return false;
        }
        spec.setBp(0);
        spec.setBd(1);
        spec.setBq(1);
        ModelDescription ndesc = ModelDescription.copyOf(description);
        ndesc.setSpecification(spec);
        ndesc.setMean(false);
        RegSarimaModelling ncontext = RegSarimaModelling.of(ndesc);
        // estimate the new model
        if (!estimate(ncontext, false)) {
            return false;
        } else {
            transferInformation(ncontext, context);
            return true;
        }
    }
}
