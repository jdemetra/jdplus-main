/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.modelling.extractors;

import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.regression.AdditiveOutlier;
import jdplus.toolkit.base.api.timeseries.regression.IEasterVariable;
import jdplus.toolkit.base.api.timeseries.regression.ILengthOfPeriodVariable;
import jdplus.toolkit.base.api.timeseries.regression.IMovingHolidayVariable;
import jdplus.toolkit.base.api.timeseries.regression.IOutlier;
import jdplus.toolkit.base.api.timeseries.regression.ITradingDaysVariable;
import jdplus.toolkit.base.api.timeseries.regression.ITsVariable;
import jdplus.toolkit.base.api.timeseries.regression.InterventionVariable;
import jdplus.toolkit.base.api.timeseries.regression.LevelShift;
import jdplus.toolkit.base.api.timeseries.regression.PeriodicOutlier;
import jdplus.toolkit.base.api.timeseries.regression.Ramp;
import jdplus.toolkit.base.api.timeseries.regression.TransitoryChange;
import jdplus.toolkit.base.api.timeseries.regression.TrendConstant;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import java.util.Arrays;
import java.util.function.Predicate;
import nbbrd.service.ServiceProvider;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.timeseries.regression.EasterVariable;
import jdplus.toolkit.base.api.timeseries.regression.IUserVariable;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import jdplus.toolkit.base.api.dictionaries.RegArimaDictionaries;
import jdplus.toolkit.base.api.dictionaries.RegressionDictionaries;
import jdplus.toolkit.base.api.dictionaries.UtilityDictionaries;
import java.util.Optional;
import jdplus.toolkit.base.api.timeseries.TsResiduals;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import jdplus.toolkit.base.core.modelling.GeneralLinearModel;

/**
 *
 * Contains all the descriptors of a linear model, except information related to the stochastic model
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class LinearModelExtractors {
    

    @ServiceProvider(InformationExtractor.class)
    public static class Default extends InformationMapping<GeneralLinearModel> {

        public static final int IMEAN = 0, ITD = 10, ILP = 11, IEASTER = 12,
                AO = 20, LS = 21, TC = 22, SO = 23, IOUTLIER = 29,
                IIV = 30, IRAMP = 40, IOTHER = 50;


        private String regressionItem(String key){
            return Dictionary.concatenate(RegArimaDictionaries.REGRESSION, key);
        }

        private String advancedItem(String key){
            return Dictionary.concatenate(RegArimaDictionaries.ADVANCED, key);
        }

        private String mlItem(String key){
            return Dictionary.concatenate(RegArimaDictionaries.MAX, key);
        }

        public Default() {
            set(RegressionDictionaries.Y, TsData.class, source -> source.getDescription().getSeries());
            set(RegressionDictionaries.PERIOD, Integer.class, source -> source.getDescription().getSeries().getAnnualFrequency());
            set(RegressionDictionaries.SPAN_START, TsPeriod.class, source -> source.getDescription().getSeries().getStart());
            set(RegressionDictionaries.SPAN_END, TsPeriod.class, source -> source.getDescription().getSeries().getDomain().getLastPeriod());
            set(RegressionDictionaries.SPAN_N, Integer.class, source -> source.getDescription().getSeries().length());
            set(RegressionDictionaries.SPAN_MISSING, Integer.class, source -> source.getEstimation().getMissing().length);

            set(RegressionDictionaries.LOG, Integer.class, source -> source.getDescription().isLogTransformation()? 1 : 0);
            set(RegressionDictionaries.ADJUST, String.class, source -> source.getDescription().getLengthOfPeriodTransformation().name());

            set(regressionItem(RegressionDictionaries.ESPAN_START), TsPeriod.class, source -> source.getEstimation().getDomain().getStartPeriod());
            set(regressionItem(RegressionDictionaries.ESPAN_END), TsPeriod.class, source -> source.getEstimation().getDomain().getLastPeriod());
            set(regressionItem(RegressionDictionaries.ESPAN_N), Integer.class, source -> source.getEstimation().getDomain().length());
            set(regressionItem(RegressionDictionaries.ESPAN_MISSING), Integer.class, source -> source.getEstimation().getMissing().length);
           
            set(regressionItem(RegressionDictionaries.MEAN), Integer.class, source -> source.isMeanCorrection() ? 1 : 0);
            set(regressionItem(RegressionDictionaries.NLP), Integer.class, source -> count( source, var->var instanceof ILengthOfPeriodVariable));
            set(regressionItem(RegressionDictionaries.NTD), Integer.class, source -> count( source, var->var instanceof ITradingDaysVariable));
            set(regressionItem(RegressionDictionaries.NMH), Integer.class, source -> count( source, var->var instanceof IMovingHolidayVariable));
            set(regressionItem(RegressionDictionaries.NOUT), Integer.class, source -> count( source, var->var instanceof IOutlier));
            set(regressionItem(RegressionDictionaries.NAO), Integer.class, source -> count( source, var->var instanceof AdditiveOutlier));
            set(regressionItem(RegressionDictionaries.NLS), Integer.class, source -> count( source, var->var instanceof LevelShift));
            set(regressionItem(RegressionDictionaries.NTC), Integer.class, source -> count( source, var->var instanceof TransitoryChange));
            set(regressionItem(RegressionDictionaries.NSO), Integer.class, source -> count( source, var->var instanceof PeriodicOutlier));
            set(regressionItem(RegressionDictionaries.NUSERS), Integer.class, source -> count( source, var->var instanceof IUserVariable));
            set(regressionItem(RegressionDictionaries.LEASTER), Integer.class, source ->{
                Variable[] variables = source.getDescription().getVariables();
                Optional<Variable> found = Arrays.stream(variables).filter(var->var.getCore() instanceof EasterVariable).findAny();
                if (found.isPresent()){
                    EasterVariable ev=(EasterVariable) found.orElseThrow().getCore();
                    return ev.getDuration();
                }else
                return 0;
            });

            set(regressionItem(RegressionDictionaries.COEFFDESC), String[].class, source -> {
                TsDomain domain = source.getDescription().getSeries().getDomain();
                Variable[] vars = source.getDescription().getVariables();
                if (vars.length == 0) {
                    return null;
                }
                int n = Arrays.stream(vars).mapToInt(var -> var.dim()).sum();
                String[] nvars = new String[n];
                for (int i = 0, j = 0; i < vars.length; ++i) {
                    int m = vars[i].dim();
                    if (m == 1) {
                        nvars[j++] = vars[i].getCore().description(domain);
                    } else {
                        for (int k = 0; k < m; ++k) {
                            nvars[j++] = vars[i].getCore().description(k, domain);
                        }
                    }
                }
                return nvars;
            });
            
            set(regressionItem(RegressionDictionaries.REGTYPE), int[].class, (GeneralLinearModel source) -> {
                Variable[] vars = source.getDescription().getVariables();
                if (vars.length == 0) {
                    return null;
                }
                int n = Arrays.stream(vars).mapToInt(var -> var.dim()).sum();
                int[] tvars = new int[n];
                for (int i = 0, j = 0; i < vars.length; ++i) {
                    int m = vars[i].dim();
                    int type = type(vars[i].getCore());
                    for (int k = 0; k < m; ++k) {
                        tvars[j++] = type;
                    }
                }
                return tvars;
            });
 
            set(advancedItem(RegressionDictionaries.COEFF), double[].class, source -> source.getEstimation().getCoefficients().toArray());
            set(advancedItem(RegressionDictionaries.COVAR), Matrix.class, source -> source.getEstimation().getCoefficientsCovariance());
            set(advancedItem(RegressionDictionaries.COVAR_ML), Matrix.class, source
                    -> mul(source.getEstimation().getCoefficientsCovariance(), mlcorrection(source.getEstimation().getStatistics())));
            set(mlItem(UtilityDictionaries.P), double[].class, source -> source.getEstimation().getParameters().getValues().toArray());
            set(mlItem(UtilityDictionaries.PCOVAR), Matrix.class, source -> source.getEstimation().getParameters().getCovariance());
            set(mlItem(UtilityDictionaries.PCOVAR_ML), Matrix.class, source
                    -> mul(source.getEstimation().getParameters().getCovariance(), mlcorrection(source.getEstimation().getStatistics())));
            set(mlItem(UtilityDictionaries.SCORE), double[].class, source -> source.getEstimation().getParameters().getScores().toArray());
            delegate(RegArimaDictionaries.LIKELIHOOD, LikelihoodStatistics.class, source -> source.getEstimation().getStatistics());
            delegate(RegArimaDictionaries.RESIDUALS, TsResiduals.class, source -> source.getResiduals());
        }

        private double mlcorrection(LikelihoodStatistics ll) {
            double n = ll.getEffectiveObservationsCount();
            return (n - ll.getEstimatedParametersCount()) / n;
        }

        private Matrix mul(Matrix m, double c) {
            double[] cm = m.toArray();
            for (int i = 0; i < cm.length; ++i) {
                cm[i] *= c;
            }
            return Matrix.of(cm, m.getRowsCount(), m.getColumnsCount());
        }

        private int type(ITsVariable var) {
            if (var instanceof TrendConstant) {
                return IMEAN;
            }
            if (var instanceof ITradingDaysVariable) {
                return ITD;
            }
            if (var instanceof ILengthOfPeriodVariable) {
                return ILP;
            }
            if (var instanceof IEasterVariable) {
                return IEASTER;
            }
            if (var instanceof IOutlier iOutlier) {
                return switch (iOutlier.getCode()) {
                    case AdditiveOutlier.CODE -> AO;
                    case LevelShift.CODE -> LS;
                    case TransitoryChange.CODE -> TC;
                    case PeriodicOutlier.CODE, PeriodicOutlier.PO -> SO;
                    default -> IOUTLIER;
                };
            }
            if (var instanceof InterventionVariable) {
                return IIV;
            }
            if (var instanceof Ramp) {
                return IRAMP;
            }
            return IOTHER;
        }

        @Override
        public Class getSourceClass() {
            return GeneralLinearModel.class;
        }

        @Override
        public int getPriority() {
            return 1;
        }

        private int countVar(GeneralLinearModel source, Predicate<Variable> pred) {
            Variable[] variables = source.getDescription().getVariables();
            int n = 0;
            for (int i = 0; i < variables.length; ++i) {
                if (pred.test(variables[i])) {
                    n += variables[i].dim();
                }
            }
            return n;

        }

        private int count(GeneralLinearModel source, Predicate<ITsVariable> pred) {
            Variable[] variables = source.getDescription().getVariables();
            int n = 0;
            for (int i = 0; i < variables.length; ++i) {
                ITsVariable core = variables[i].getCore();
                if (pred.test(core)) {
                    n += core.dim();
                }
            }
            return n;
        }

    }

}
