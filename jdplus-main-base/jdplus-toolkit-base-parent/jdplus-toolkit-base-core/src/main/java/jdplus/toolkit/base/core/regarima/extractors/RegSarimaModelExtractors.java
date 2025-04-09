/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.regarima.extractors;

import jdplus.toolkit.base.api.arima.SarimaSpec;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.information.InformationDelegate;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.modelling.SeriesInfo;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.regression.IEasterVariable;
import jdplus.toolkit.base.api.timeseries.regression.ILengthOfPeriodVariable;
import jdplus.toolkit.base.api.timeseries.regression.IOutlier;
import jdplus.toolkit.base.api.timeseries.regression.ITradingDaysVariable;
import jdplus.toolkit.base.api.timeseries.regression.MissingValueEstimation;
import jdplus.toolkit.base.api.timeseries.regression.RegressionItem;
import jdplus.toolkit.base.api.timeseries.regression.TrendConstant;
import jdplus.toolkit.base.api.timeseries.regression.UserVariable;
import jdplus.toolkit.base.api.dictionaries.ArimaDictionaries;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import jdplus.toolkit.base.api.dictionaries.RegArimaDictionaries;
import jdplus.toolkit.base.api.dictionaries.RegressionDictionaries;
import jdplus.toolkit.base.api.dictionaries.UtilityDictionaries;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.regression.ModellingUtility;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.dstats.T;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.modelling.GeneralLinearModel;
import jdplus.toolkit.base.core.modelling.regression.RegressionDesc;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * Contains all the descriptors of a linear model, except additional information
 * and information related to the (generic) stochastic model
 *
 * @author palatej
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class RegSarimaModelExtractors {

    public final int NFCAST = -1, NBCAST = 0;

    @ServiceProvider(InformationExtractor.class)
    public static class Specific extends InformationMapping<RegSarimaModel> {

        private String arimaItem(String key) {
            return Dictionary.concatenate(RegArimaDictionaries.ARIMA, key);
        }

        private String regressionItem(String key) {
            return Dictionary.concatenate(RegArimaDictionaries.REGRESSION, key);
        }

//        private String residualsItem(String key) {
//            return Dictionary.concatenate(RegArimaDictionaries.RESIDUALS, key);
//        }
//
        private String advancedItem(String key) {
            return Dictionary.concatenate(RegArimaDictionaries.ADVANCED, key);
        }

        private String mlItem(String key) {
            return Dictionary.concatenate(RegArimaDictionaries.MAX, key);
        }

        private RegressionItem phi(RegSarimaModel model, int lag) {
            if (lag <= 0) {
                return null;
            }
            SarimaSpec arima = model.getDescription().getStochasticComponent();
            Parameter[] p = arima.getPhi();
            if (lag > p.length) {
                return null;
            }
            Parameter phi = p[lag - 1];
            if (phi.isFixed()) {
                return new RegressionItem(phi.getValue(), 0, Double.NaN, null);
            }
            int pos = 0;
            for (int i = 0; i < p.length; ++i) {
                if (lag == i + 1) {
                    break;
                }
                if (!p[i].isFixed()) {
                    ++pos;
                }
            }
            return pt(model, phi.getValue(), pos, null);
        }

        private RegressionItem bphi(RegSarimaModel model, int lag) {
            if (lag <= 0) {
                return null;
            }
            SarimaSpec arima = model.getDescription().getStochasticComponent();
            Parameter[] p = arima.getBphi();
            if (lag > p.length) {
                return null;
            }
            Parameter bphi = p[lag - 1];
            if (bphi.isFixed()) {
                return new RegressionItem(bphi.getValue(), 0, Double.NaN, null);
            }
            int pos = Parameter.freeParametersCount(arima.getPhi());
            for (int i = 0; i < p.length; ++i) {
                if (lag == i + 1) {
                    break;
                }
                if (!p[i].isFixed()) {
                    ++pos;
                }
            }
            return pt(model, bphi.getValue(), pos, null);
        }

        private RegressionItem theta(RegSarimaModel model, int lag) {
            if (lag <= 0) {
                return null;
            }
            SarimaSpec arima = model.getDescription().getStochasticComponent();
            Parameter[] p = arima.getTheta();
            if (lag > p.length) {
                return null;
            }
            Parameter theta = p[lag - 1];
            if (theta.isFixed()) {
                return new RegressionItem(theta.getValue(), 0, Double.NaN, null);
            }
            int pos = Parameter.freeParametersCount(arima.getPhi()) + Parameter.freeParametersCount(arima.getBphi());
            for (int i = 0; i < p.length; ++i) {
                if (lag == i + 1) {
                    break;
                }
                if (!p[i].isFixed()) {
                    ++pos;
                }
            }
            return pt(model, theta.getValue(), pos, null);
        }

        private RegressionItem btheta(RegSarimaModel model, int lag) {
            if (lag <= 0) {
                return null;
            }
            SarimaSpec arima = model.getDescription().getStochasticComponent();
            Parameter[] p = arima.getBtheta();
            if (lag > p.length) {
                return null;
            }
            Parameter btheta = p[lag - 1];
            if (btheta.isFixed()) {
                return new RegressionItem(btheta.getValue(), 0, Double.NaN, null);
            }
            int pos = Parameter.freeParametersCount(arima.getPhi())
                    + Parameter.freeParametersCount(arima.getBphi())
                    + Parameter.freeParametersCount(arima.getTheta());
            for (int i = 0; i < p.length; ++i) {
                if (lag == i + 1) {
                    break;
                }
                if (!p[i].isFixed()) {
                    ++pos;
                }
            }
            return pt(model, btheta.getValue(), pos, null);
        }

        RegressionItem pt(RegSarimaModel model, double val, int pos, String name) {
            GeneralLinearModel.Estimation estimation = model.getEstimation();
            LikelihoodStatistics ll = estimation.getStatistics();
            int nobs = ll.getEffectiveObservationsCount(), nparams = ll.getEstimatedParametersCount();
            int nhp = model.freeArimaParametersCount();
            double ndf = nobs - nparams;
            double vcorr = ndf / (ndf + nhp);
            T t = new T(nobs - nparams);
            double stde = Math.sqrt(estimation.getParameters().getCovariance().get(pos, pos) * vcorr);
            double tval = val / stde;
            double prob = 1 - t.getProbabilityForInterval(-tval, tval);
            return new RegressionItem(val, stde, prob, name);
        }

        public Specific() {
            // ARIMA related
            delegate(RegArimaDictionaries.ARIMA, SarimaSpec.class, source
                    -> source.getDescription().getStochasticComponent());

            setArray(arimaItem(ArimaDictionaries.PHI), 1, 12, RegressionItem.class, (source, i) -> {
                return phi(source, i);
            });
            setArray(arimaItem(ArimaDictionaries.BPHI), 1, 12, RegressionItem.class, (source, i) -> {
                return bphi(source, i);
            });
            setArray(arimaItem(ArimaDictionaries.THETA), 1, 12, RegressionItem.class, (source, i) -> {
                return theta(source, i);
            });
            setArray(arimaItem(ArimaDictionaries.BTHETA), 1, 12, RegressionItem.class, (source, i) -> {
                return btheta(source, i);
            });

            //*********************
            setArray(RegressionDictionaries.Y_F, NFCAST, TsData.class, (source, i) -> source.forecasts(i).getForecasts());
            setArray(RegressionDictionaries.Y_B, NBCAST, TsData.class, (source, i) -> source.backcasts(i).getForecasts());
            setArray(RegressionDictionaries.Y_EF, NFCAST, TsData.class, (source, i) -> source.forecasts(i).getForecastsStdev());
            setArray(RegressionDictionaries.Y_EB, NBCAST, TsData.class, (source, i) -> source.backcasts(i).getForecastsStdev());

            set(RegressionDictionaries.YC, TsData.class, source -> source.interpolatedSeries(false));
            setArray(RegressionDictionaries.YC_F, NFCAST, TsData.class, (source, i) -> source.forecasts(i).getForecasts());
            setArray(RegressionDictionaries.YC_B, NBCAST, TsData.class, (source, i) -> source.backcasts(i).getForecasts());

            set(RegressionDictionaries.YLIN, TsData.class, source -> {
                TsData lin = source.linearizedSeries();
                return source.backTransform(lin, false);
            });
            setArray(RegressionDictionaries.YLIN_B, NBCAST, TsData.class, (source, i) -> {
                TsData lin = source.linearizedBackcasts(i);
                return source.backTransform(lin, false);
            });
            setArray(RegressionDictionaries.YLIN_F, NFCAST, TsData.class, (source, i) -> {
                TsData lin = source.linearizedForecasts(i);
                return source.backTransform(lin, false);
            });
            set(RegressionDictionaries.L, TsData.class, source -> source.linearizedSeries());
            setArray(RegressionDictionaries.L_B, NFCAST, TsData.class, (source, i) -> source.linearizedBackcasts(i));
            setArray(RegressionDictionaries.L_F, NBCAST, TsData.class, (source, i) -> source.linearizedForecasts(i));
//            setArray(RegressionDictionaries.L_EF, NFCAST, TsData.class, (source, i) -> source.forecasts(i).getRawForecastsStdev());
//            setArray(RegressionDictionaries.L_EB, NBCAST, TsData.class, (source, i) -> source.backcasts(i).getRawForecastsStdev());
            set(RegressionDictionaries.YCAL, TsData.class, source -> {
                TsData y = source.getDescription().getSeries();
                TsData cal = source.getCalendarEffect(y.getDomain());
                return source.inv_op(y, cal);
            });
            setArray(RegressionDictionaries.YCAL_F, NFCAST, TsData.class,
                    (source, i) -> {
                        TsDomain fdom = source.forecastDomain(i);
                        TsData yf = source.forecasts(i).getForecasts();
                        TsData calf = source.getCalendarEffect(fdom);
                        return source.inv_op(yf, calf);
                    });
            setArray(RegressionDictionaries.YCAL_B, NBCAST, TsData.class,
                    (source, i) -> {
                        TsDomain fdom = source.backcastDomain(i);
                        TsData yf = source.backcasts(i).getForecasts();
                        TsData calf = source.getCalendarEffect(fdom);
                        return source.inv_op(yf, calf);
                    });
            set(RegressionDictionaries.YCAL, TsData.class, source -> {
                TsData y = source.getDescription().getSeries();
                TsData cal = source.getCalendarEffect(y.getDomain());
                return source.inv_op(y, cal);
            });

// All deterministic effects
            set(RegressionDictionaries.DET, TsData.class, (RegSarimaModel source) -> {
                TsData det = source.deterministicEffect(null, v -> true);
                return source.backTransform(det, true);
            });
            setArray(RegressionDictionaries.DET_F, NFCAST, TsData.class,
                    (source, i) -> {
                        TsData det = source.deterministicEffect(source.forecastDomain(i), v -> true);
                        return source.backTransform(det, true);
                    });
            setArray(RegressionDictionaries.DET_B, NBCAST, TsData.class,
                    (source, i) -> {
                        TsData det = source.deterministicEffect(source.backcastDomain(i), v -> true);
                        return source.backTransform(det, true);
                    });

// All calendar effects
            set(RegressionDictionaries.CAL, TsData.class, source -> source.getCalendarEffect(null));
            setArray(RegressionDictionaries.CAL_F, NFCAST, TsData.class,
                    (source, i) -> source.getCalendarEffect(source.forecastDomain(i)));
            setArray(RegressionDictionaries.CAL_B, NBCAST, TsData.class,
                    (source, i) -> source.getCalendarEffect(source.backcastDomain(i)));

// Trading days effects
            set(RegressionDictionaries.TDE, TsData.class, source -> source.getTradingDaysEffect(null));
            setArray(RegressionDictionaries.TDE_F, NFCAST, TsData.class,
                    (source, i) -> source.getTradingDaysEffect(source.forecastDomain(i)));
            setArray(RegressionDictionaries.TDE_B, NBCAST, TsData.class,
                    (source, i) -> source.getTradingDaysEffect(source.backcastDomain(i)));

// All moving holidays effects
            set(RegressionDictionaries.MHE, TsData.class, source -> source.getMovingHolidayEffect(null));
            setArray(RegressionDictionaries.MHE_F, NFCAST, TsData.class,
                    (source, i) -> source.getMovingHolidayEffect(source.forecastDomain(i)));
            setArray(RegressionDictionaries.MHE_B, NBCAST, TsData.class,
                    (source, i) -> source.getMovingHolidayEffect(source.backcastDomain(i)));

// Easter effect
            set(RegressionDictionaries.EE, TsData.class, source -> source.getEasterEffect(null));
            setArray(RegressionDictionaries.EE_F, NFCAST, TsData.class,
                    (source, i) -> source.getEasterEffect(source.forecastDomain(i)));
            setArray(RegressionDictionaries.EE_B, NBCAST, TsData.class,
                    (source, i) -> source.getEasterEffect(source.backcastDomain(i)));

// Other moving holidays effects
            set(RegressionDictionaries.OMHE, TsData.class, source -> source.getOtherMovingHolidayEffect(null));
            setArray(RegressionDictionaries.OMHE_F, NFCAST, TsData.class,
                    (source, i) -> source.getOtherMovingHolidayEffect(source.forecastDomain(i)));
            setArray(RegressionDictionaries.OMHE_B, NBCAST, TsData.class,
                    (source, i) -> source.getOtherMovingHolidayEffect(source.backcastDomain(i)));

// All Outliers effect
            set(RegressionDictionaries.OUT, TsData.class, source -> source.getOutliersEffect(null));
            setArray(RegressionDictionaries.OUT + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                    (source, i) -> source.getOutliersEffect(source.forecastDomain(i)));
            setArray(RegressionDictionaries.OUT + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                    (source, i) -> source.getOutliersEffect(source.backcastDomain(i)));

            set(regressionItem(RegressionDictionaries.MU), RegressionItem.class,
                    source -> source.regressionItem(v -> v instanceof TrendConstant, 0));
            set(regressionItem(RegressionDictionaries.LP), RegressionItem.class,
                    source -> source.regressionItem(v -> v instanceof ILengthOfPeriodVariable, 0));
            set(regressionItem(RegressionDictionaries.EASTER), RegressionItem.class,
                    source -> source.regressionItem(v -> v instanceof IEasterVariable, 0));
            setArray(regressionItem(RegressionDictionaries.OUTLIERS), 1, 31, RegressionItem.class,
                    (source, i) -> source.regressionItem(v -> v instanceof IOutlier, i - 1));
            setArray(regressionItem(RegressionDictionaries.TD), 1, 7, RegressionItem.class,
                    (source, i) -> source.regressionItem(v -> v instanceof ITradingDaysVariable, i - 1));
            setArray(regressionItem(RegressionDictionaries.USER), 1, 30, RegressionItem.class,
                    (source, i) -> source.regressionItem(v -> v instanceof UserVariable, i - 1));
            setArray(regressionItem(RegressionDictionaries.OUT), 1, 30, RegressionItem.class,
                    (source, i) -> source.regressionItem(v -> v instanceof IOutlier, i - 1));
            setArray(regressionItem(RegressionDictionaries.MISSING), 1, 100, MissingValueEstimation.class,
                    (source, i) -> {
                        MissingValueEstimation[] missing = source.getEstimation().getMissing();
                        return i <= 0 || i > missing.length ? null : missing[i - 1];
                    });

            set(RegressionDictionaries.FULL_RES, TsData.class, source -> source.fullResiduals());

            set(RegressionDictionaries.REG, TsData.class, (RegSarimaModel source) -> {
                TsData reg = source.deterministicEffect(null,
                        v -> !ModellingUtility.isCalendar(v) && !ModellingUtility.isOutlier(v));
                return source.backTransform(reg, false);
            });
            setArray(RegressionDictionaries.REG_F, NFCAST, TsData.class,
                    (source, i) -> {
                        TsData reg = source.deterministicEffect(source.forecastDomain(i),
                                v -> !ModellingUtility.isCalendar(v) && !ModellingUtility.isOutlier(v));
                        return source.backTransform(reg, false);
                    });
            setArray(RegressionDictionaries.REG_B, NBCAST, TsData.class,
                    (source, i) -> {
                        TsData reg = source.deterministicEffect(source.backcastDomain(i),
                                v -> !ModellingUtility.isCalendar(v) && !ModellingUtility.isOutlier(v));
                        return source.backTransform(reg, false);
                    });

            set(regressionItem(RegressionDictionaries.TDDERIVED), RegressionItem.class, source -> {
                RegressionDesc desc = source.getDetails().getDerivedTradingDay();
                if (desc == null) {
                    return null;
                }
                return new RegressionItem(desc.getCoef(), desc.getStderr(), desc.getPvalue(), desc.getName());
            }
            );

            set(regressionItem(RegressionDictionaries.TDF), StatisticalTest.class,
                    source -> source.getDetails().getFTestonTradingDays());

            set(advancedItem(RegressionDictionaries.COEFFDESC), String[].class,
                    source -> source.getDetails().getRegressionItems().stream()
                            .map(r -> r.getName())
                            .toArray(String[]::new));

            set(mlItem(UtilityDictionaries.PCORR), Matrix.class, source -> {
                FastMatrix cov = FastMatrix.of(source.getEstimation().getParameters().getCovariance());
                DataBlock diag = cov.diagonal();
                for (int i = 0; i < cov.getRowsCount(); ++i) {
                    double vi = diag.get(i);
                    for (int j = 0; j < i; ++j) {
                        double vj = diag.get(j);
                        if (vi != 0 && vj != 0) {
                            cov.mul(i, j, 1 / Math.sqrt(vi * vj));
                        }
                    }
                }
                SymmetricMatrix.fromLower(cov);
                diag.set(1);
                return cov;
            });

//            set(residualsItem(ResidualsDictionaries.SER), Double.class, (RegSarimaModel source) -> {
//                LikelihoodStatistics stats = source.getEstimation().getStatistics();
//                double ssqErr = stats.getSsqErr();
//                int ndf = stats.getEffectiveObservationsCount() - stats.getEstimatedParametersCount() - source.freeArimaParametersCount();
//                return Math.sqrt(ssqErr / ndf);
//            });
//            set(residualsItem(ResidualsDictionaries.SER_ML), Double.class, (RegSarimaModel source) -> {
//                LikelihoodStatistics stats = source.getEstimation().getStatistics();
//                double ssqErr = stats.getSsqErr();
//                int ndf = stats.getEffectiveObservationsCount();
//                return Math.sqrt(ssqErr / ndf);
//            });
//            set(residualsItem(ResidualsDictionaries.RES), double[].class, (RegSarimaModel source) -> {
//                RegSarimaModel.Details details = source.getDetails();
//                return details.getIndependentResiduals().toArray();
//            });
//            set(residualsItem(ResidualsDictionaries.TSRES), TsData.class, (RegSarimaModel source) -> source.fullResiduals());
        }

        @Override
        public Class<RegSarimaModel> getSourceClass() {
            return RegSarimaModel.class;
        }
    }

    @ServiceProvider(InformationExtractor.class)
    public static class GenericExtractor extends InformationDelegate<RegSarimaModel, GeneralLinearModel> {

        public GenericExtractor() {
            super(v -> v);
        }

        @Override
        public Class<GeneralLinearModel> getDelegateClass() {
            return GeneralLinearModel.class;
        }

        @Override
        public Class<RegSarimaModel> getSourceClass() {
            return RegSarimaModel.class;
        }

    }

}
