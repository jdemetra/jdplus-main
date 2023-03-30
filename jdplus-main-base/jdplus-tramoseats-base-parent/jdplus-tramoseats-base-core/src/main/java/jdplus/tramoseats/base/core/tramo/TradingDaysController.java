/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.core.tramo;

import jdplus.toolkit.base.core.stats.linearmodel.LeastSquaresResults;
import jdplus.toolkit.base.core.stats.linearmodel.LinearModel;
import jdplus.toolkit.base.core.stats.linearmodel.Ols;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.core.regsarima.regular.ModelDescription;
import jdplus.toolkit.base.core.regsarima.regular.ProcessingResult;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModelling;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.regression.ITradingDaysVariable;
import jdplus.toolkit.base.core.modelling.regression.Regression;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.api.timeseries.regression.ModellingUtility;

/**
 *
 * @author Jean Palate
 */
class TradingDaysController extends ModelController {

    private final ITradingDaysVariable td;
//    private final ILengthOfPeriodVariable lp;
    private double ptd = 0.01;

//    TradingDaysController(final ITradingDaysVariable td, ILengthOfPeriodVariable lp, double ptd) {
    TradingDaysController(final ITradingDaysVariable td, double ptd) {
        this.td = td;
//        this.lp = lp;
        this.ptd = ptd;
    }

    @Override
    ProcessingResult process(RegSarimaModelling modelling, TramoContext context) {
        // find td variables
        ModelDescription desc = modelling.getDescription();
        boolean hascal = desc.variables().anyMatch(var ->(ModellingUtility.isAutomaticallyIdentified(var)) && (ModellingUtility.isDaysRelated(var)));
        // nothing to do if td is prespecified
        if (hascal) {
            return ProcessingResult.Unchanged;
        }
        if (!needProcessing(modelling)) {
            return ProcessingResult.Unchanged;
        }
        ModelDescription nmodel = newModel(modelling);
        nmodel.removeVariable(var->ModellingUtility.isOutlier(var, true));
        // compute the corresponding airline model.
        RegSarimaModelling ncontext = RegSarimaModelling.of(nmodel);
        if (!estimate(ncontext, true)) {
            return ProcessingResult.Failed;
        }
        ModelComparator mcmp = ModelComparator.builder().build();
        int cmp = mcmp.compare(modelling, ncontext);
        if (cmp < 1) {
//            setReferenceModel(current);
            return ProcessingResult.Unchanged;
        } else {
//            setReferenceModel(ncurrent);
            transferInformation(ncontext, modelling);
            return ProcessingResult.Changed;
        }
    }

    private boolean needProcessing(RegSarimaModelling context) {
        DoubleSeq res = context.getEstimation().getConcentratedLikelihood().e();
        LinearModel.Builder builder = LinearModel.builder();
        builder.y(res);
        
        TsDomain domain = context.getDescription().getEstimationDomain();
        // drop the number of data corresponding to the number of regression variables 
        domain = domain.drop(domain.getLength() - res.length(), 0);
        if (td != null){
            FastMatrix mtd = Regression.matrix(domain, td);
            builder.addX(mtd);
        }
            
        LinearModel lm = builder.build();
        
        LeastSquaresResults lsr = Ols.compute(lm);
        if (lsr == null) {
            return false;
        }
        
        return lsr.Ftest().getPvalue()<ptd;
    }

    private ModelDescription newModel(RegSarimaModelling context) {
        ModelDescription ndesc = ModelDescription.copyOf(context.getDescription());
        ndesc.removeVariable(var -> ModellingUtility.isDaysRelated(var));
        ndesc.addVariable(Variable.variable("td", td, TramoModelBuilder.calendarAMI));
        return ndesc;
    }

}
