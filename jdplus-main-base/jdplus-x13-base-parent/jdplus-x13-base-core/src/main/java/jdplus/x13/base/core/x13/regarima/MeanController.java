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
package jdplus.x13.base.core.x13.regarima;

import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regarima.RegArimaUtility;
import jdplus.toolkit.base.core.regsarima.regular.IRegressionModule;
import jdplus.toolkit.base.core.regsarima.regular.ModelDescription;
import jdplus.toolkit.base.core.regsarima.regular.ProcessingResult;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModelling;
import jdplus.toolkit.base.core.sarima.SarimaModel;

/**
 *
 * @author Jean Palate
 */
public class MeanController implements IRegressionModule {

    public static final String MEAN = "mean correction",
            MEAN_ADDED = "mean significant (added)",
            MEAN_REMOVED = "mean not significant (removed)";

    static final double CVAL0 = 1.96, CVAL1 = 1.6, CVALFINAL = 2.5;

    private final double cval;
    private final double eps = 1e-5;

    public MeanController(double cval) {
        this.cval = cval;
    }

    @Override
    public ProcessingResult test(RegSarimaModelling context) {

        ProcessingLog log = context.getLog();
        try {
            log.push(MEAN);
            ModelDescription desc = context.getDescription();
            RegArimaEstimation<SarimaModel> est = context.getEstimation();
            boolean mean = desc.isMean();
            if (!mean) {
                desc = ModelDescription.copyOf(desc);
                desc.setMean(true);
                est = null;
            }
            if (est == null) {
                est = desc.estimate(RegArimaUtility.processor(true, eps));
            }
            double t = est.getConcentratedLikelihood().tstat(0, 0, false);
            boolean nmean = Math.abs(t) > cval;
            if (nmean == mean) {
                return ProcessingResult.Unchanged;
            } else {
                log.info(nmean ? MEAN_ADDED : MEAN_REMOVED);
                context.getDescription().setMean(nmean);
                context.clearEstimation();
                return ProcessingResult.Changed;
            }
        } finally {
            log.pop();
        }
    }
}
