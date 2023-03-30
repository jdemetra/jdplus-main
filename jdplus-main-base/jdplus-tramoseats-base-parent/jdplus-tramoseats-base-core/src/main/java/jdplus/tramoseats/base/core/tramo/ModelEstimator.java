/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.core.tramo;

import jdplus.tramoseats.base.core.tramo.internal.OutliersDetectionModule;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.regsarima.regular.IModelEstimator;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModelling;
import jdplus.toolkit.base.core.regsarima.RegSarimaComputer;
import jdplus.toolkit.base.core.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.toolkit.base.api.timeseries.regression.ModellingUtility;

@Development(status = Development.Status.Beta)
class ModelEstimator implements IModelEstimator {

    private final OutliersDetectionModule outliers;
    private final double eps, va;

    ModelEstimator(double eps, double va, OutliersDetectionModule outliers) {
        this.eps = eps;
        this.va = va;
        this.outliers = outliers;
    }

    @Override
    public boolean estimate(RegSarimaModelling context) {
        context.getDescription().removeVariable(var -> ModellingUtility.isOutlier(var, true));
        if (outliers != null) {
            outliers.process(context, va);
        }
        if (!calc(context)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean calc(RegSarimaModelling context) {
        RegSarimaComputer processor = RegSarimaComputer.builder()
                .minimizer(LevenbergMarquardtMinimizer.builder())
                .precision(eps)                
//                .startingPoint(RegSarimaComputer.StartingPoint.Multiple)
                .build();
        context.estimate(processor);
        return true;
    }
}
