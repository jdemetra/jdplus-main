/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.core.tramo;

import jdplus.toolkit.base.api.timeseries.regression.Variable;
import java.util.Optional;
import jdplus.toolkit.base.core.stats.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.api.timeseries.regression.ModellingUtility;
import jdplus.toolkit.base.core.regsarima.regular.IRegressionModule;
import jdplus.toolkit.base.core.regsarima.regular.IRegressionTest;
import jdplus.toolkit.base.core.regsarima.regular.ModelDescription;
import jdplus.toolkit.base.core.regsarima.regular.ProcessingResult;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModelling;
import jdplus.toolkit.base.core.regsarima.regular.TRegressionTest;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import nbbrd.design.BuilderPattern;

/**
 * Remove non significant regression items. The model is not re-estimated
 *
 * @author palatej
 */
public class FastRegressionTest implements IRegressionModule {

    public static final double CVAL = 1.96;

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(FastRegressionTest.class)
    public static class Builder {

        private double tmean = CVAL, tvar = CVAL;
        private boolean testMean = true;

        public Builder meanThreshold(double tmean) {
            this.tmean = tmean;
            return this;
        }

        public Builder varThreshold(double tvar) {
            this.tvar = tvar;
            return this;
        }

        public Builder testMean(boolean test) {
            this.testMean = test;
            return this;
        }

        public FastRegressionTest build() {
            return new FastRegressionTest(this);
        }

    }

    private final IRegressionTest tdTest, wdTest, lpTest, mhTest, meanTest;

    private FastRegressionTest(Builder builder) {
        tdTest = new TRegressionTest(builder.tvar, builder.tvar);
        wdTest = new TRegressionTest(builder.tvar);
        lpTest = new TRegressionTest(builder.tvar);
        mhTest = new TRegressionTest(builder.tvar);
        meanTest = builder.testMean ? new TRegressionTest(builder.tmean) : null;
    }

    @Override
    public ProcessingResult test(final RegSarimaModelling context) {
        // estimate the model.
        ModelDescription currentModel = context.getDescription();
        // make a copy.
        ModelDescription tmpModel = ModelDescription.copyOf(currentModel);
        boolean changed = false;
        RegArimaModel<SarimaModel> regarima = tmpModel.regarima();
        ConcentratedLikelihoodWithMissing ll = context.getEstimation().getConcentratedLikelihood();

        int nhp = tmpModel.getArimaSpec().freeParametersCount();

        Optional<Variable> td = tmpModel.variables().filter(var -> ModellingUtility.isTradingDays(var) && ModellingUtility.isAutomaticallyIdentified(var)).findFirst();
        Optional<Variable> lp = tmpModel.variables().filter(var -> ModellingUtility.isLengthOfPeriod(var) && ModellingUtility.isAutomaticallyIdentified(var)).findFirst();
        Optional<Variable> easter = tmpModel.variables().filter(var -> ModellingUtility.isEaster(var) && ModellingUtility.isAutomaticallyIdentified(var)).findFirst();
        // td
        boolean removetd = false;
        if (td.isPresent()) {
            Variable variable = td.orElseThrow();
            int pos = tmpModel.findPosition(variable.getCore());
            int dim = variable.getCore().dim();
            IRegressionTest test = dim == 1 ? wdTest : tdTest;
            if (!test.accept(ll, nhp, pos, dim)) {
                removetd = true;
            }
        }
        if (removetd && lp.isPresent()) {
            Variable variable = lp.orElseThrow();
            int pos = tmpModel.findPosition(variable.getCore());
            if (lpTest.accept(ll, nhp, pos, 1)) {
                removetd = false;
            } else {
                currentModel.remove(variable.getCore());
                changed = true;
            }
        }

        if (removetd) {
            currentModel.remove(td.orElseThrow().getCore());
            if (lp.isPresent())
                currentModel.remove(lp.orElseThrow().getCore());
            changed = true;
        }

        if (easter.isPresent()) {
            Variable variable = easter.orElseThrow();
            int pos =  tmpModel.findPosition(variable.getCore());
            if (!mhTest.accept(ll, nhp, pos, 1)) {
                currentModel.remove(variable.getCore());
                changed = true;
            }
        }
        
        if (meanTest != null && regarima.isMean() && !meanTest.accept(ll, nhp, 0, 1)) {
            currentModel.setMean(false);
            changed = true;
        }

        if (changed) {
            context.set(currentModel, null);
        }

        return changed ? ProcessingResult.Changed : ProcessingResult.Unchanged;

    }

}
