/*
 * Copyright 2023 National Bank of Belgium
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
package jdplus.toolkit.base.core.regsarima.regular;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.arima.SarimaSpec;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.api.data.Doubles;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.information.GenericExplorable;
import jdplus.toolkit.base.core.regsarima.RegSarimaComputer;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import jdplus.toolkit.base.api.timeseries.regression.MissingValueEstimation;
import jdplus.toolkit.base.api.data.ParametersEstimation;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.stats.ProbabilityType;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.regression.ITsVariable;
import jdplus.toolkit.base.api.timeseries.regression.TrendConstant;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.api.timeseries.regression.RegressionItem;
import jdplus.toolkit.base.api.timeseries.regression.ResidualsType;
import jdplus.toolkit.base.api.dictionaries.ResidualsDictionaries;
import jdplus.toolkit.base.api.util.Arrays2;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsResiduals;
import jdplus.toolkit.base.api.timeseries.calendars.DayClustering;
import jdplus.toolkit.base.api.timeseries.regression.GenericTradingDaysVariable;
import jdplus.toolkit.base.api.timeseries.regression.HolidaysCorrectedTradingDays;
import jdplus.toolkit.base.api.timeseries.regression.ITradingDaysVariable;
import jdplus.toolkit.base.api.timeseries.regression.ModellingUtility;
import jdplus.toolkit.base.core.arima.estimation.IArimaMapping;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.dstats.F;
import jdplus.toolkit.base.core.dstats.LogNormal;
import jdplus.toolkit.base.core.dstats.T;
import jdplus.toolkit.base.core.stats.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.toolkit.base.core.stats.likelihood.LogLikelihoodFunction;
import jdplus.toolkit.base.core.math.functions.IFunction;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.QuadraticForm;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.modelling.GeneralLinearModel;
import jdplus.toolkit.base.core.modelling.LightweightLinearModel;
import jdplus.toolkit.base.core.modelling.Residuals;
import jdplus.toolkit.base.core.modelling.regression.RegressionDesc;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regarima.RegArimaForecasts;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.regarima.RegArimaUtility;
import jdplus.toolkit.base.core.regarima.RegArmaModel;
import jdplus.toolkit.base.core.regarima.estimation.RegArmaFunction;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.sarima.estimation.SarimaFixedMapping;
import jdplus.toolkit.base.core.sarima.estimation.SarimaMapping;
import jdplus.toolkit.base.core.ssf.arima.ExactArimaForecasts;
import jdplus.toolkit.base.core.stats.likelihood.DefaultLikelihoodEvaluation;
import jdplus.toolkit.base.core.stats.tests.NiidTests;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder
public class RegSarimaModel implements GeneralLinearModel<SarimaSpec>, GenericExplorable {

    private static final MissingValueEstimation[] NOMISSING = new MissingValueEstimation[0];

    public static RegSarimaModel of(ModelDescription model, RegSarimaComputer processor) {
        return RegSarimaModel.of(model, processor.process(model.regarima(), model.mapping()), ProcessingLog.dummy());
    }

    public static RegSarimaModel of(ModelDescription description, RegArimaEstimation<SarimaModel> estimation, ProcessingLog log) {

        SarimaSpec arima = description.getArimaSpec();
        int free = arima.freeParametersCount();
        RegArimaModel<SarimaModel> model = estimation.getModel();
        ConcentratedLikelihoodWithMissing ll = estimation.getConcentratedLikelihood();
        LikelihoodStatistics statistics = estimation.statistics();

        List<Variable> vars = description.variables().sequential().collect(Collectors.toList());
        int nvars = (int) vars.size();
        if (description.isMean()) {
            ++nvars;
        }
        Variable[] variables = new Variable[nvars];
        DoubleSeq coeffs = estimation.getConcentratedLikelihood().coefficients();
        DoubleSeqCursor cursor = coeffs.cursor();
        FastMatrix varcoeffs = estimation.getConcentratedLikelihood().unscaledCovariance();
        DoubleSeqCursor diag = varcoeffs.diagonal().cursor();
        int df = ll.degreesOfFreedom() - free;
        double vscale = ll.ssq() / df;
        T tstat = new T(df);

        int k = 0, pos = 0;

        List<RegressionDesc> regressionDesc = new ArrayList<>();
        if (description.isMean()) {
            ITsVariable cur = new TrendConstant(arima.getD(), arima.getBd());
            double c = cursor.getAndNext(), e = Math.sqrt(diag.getAndNext() * vscale);
            regressionDesc.add(new RegressionDesc("const", cur, 0, pos++, c, e, 2 * tstat.getProbability(Math.abs(c / e), ProbabilityType.Upper)));
            variables[k++] = Variable.variable("const", cur)
                    .withCoefficient(Parameter.estimated(c));
        }
        // fill the free coefficients
        TsDomain domain = description.getDomain();

        StatisticalTest tdf = null;
        RegressionDesc tdderived = null;
        for (Variable var : vars) {
            int nfree = var.freeCoefficientsCount();
            if (nfree == var.dim() && nfree > 1) {
                int startpos = pos;
                Parameter[] p = new Parameter[nfree];
                for (int j = 0; j < nfree; ++j) {
                    double c = cursor.getAndNext(), e = Math.sqrt(diag.getAndNext() * vscale);
                    if (e == 0) {
                        p[j] = Parameter.zero();
                        regressionDesc.add(new RegressionDesc(var.getCore().description(j, domain), var.getCore(), j, pos++, 0, 0, 0));
                    } else {
                        p[j] = Parameter.estimated(c);
                        regressionDesc.add(new RegressionDesc(var.getCore().description(j, domain), var.getCore(), j, pos++, c, e, 2 * tstat.getProbability(Math.abs(c / e), ProbabilityType.Upper)));
                    }
                }
                variables[k++] = var.withCoefficients(p);
                if (var.getCore() instanceof ITradingDaysVariable iTradingDaysVariable) {
                    DoubleSeq coef = coeffs.extract(startpos, nfree);
                    FastMatrix bvar = FastMatrix.of(varcoeffs.extract(startpos, nfree, startpos, nfree));
                    DataBlock w = weights(iTradingDaysVariable);
                    if (w != null) {
                        double c = -coef.dot(w);
                        double v = QuadraticForm.apply(bvar, w), e = Math.sqrt(v * vscale);
                        tdderived = new RegressionDesc("td-derived", var.getCore(), -1, -1, c, e, 2 * tstat.getProbability(Math.abs(c / e), ProbabilityType.Upper));
                    }
                    try {
                        SymmetricMatrix.lcholesky(bvar);
                        DataBlock r = DataBlock.of(coef);
                        LowerTriangularMatrix.solveLx(bvar, r);
                        double f = r.ssq() / (vscale * nfree);
                        F fdist = new F(nfree, df);
                        double pval = fdist.getProbability(f, ProbabilityType.Upper);
                        tdf = new StatisticalTest(f, pval, fdist.getDescription());
                    } catch (Exception ex) {
                    }
                }
            } else if (nfree > 0) {
                Parameter[] p = var.getCoefficients();
                for (int j = 0; j < p.length; ++j) {
                    if (p[j].isFree()) {
                        double c = cursor.getAndNext(), e = Math.sqrt(diag.getAndNext() * vscale);
                        p[j] = Parameter.estimated(c);
                        regressionDesc.add(new RegressionDesc(p.length > 1 ? var.getCore().description(j, domain) : var.getCore().description(domain),
                                var.getCore(), j, pos++, c, e, 2 * tstat.getProbability(Math.abs(c / e), ProbabilityType.Upper)));
                    }
                }
                variables[k++] = var.withCoefficients(p);
            } else {
                variables[k++] = var;
            }
        }

        LightweightLinearModel.Description desc = LightweightLinearModel.Description.<SarimaSpec>builder()
                .series(description.getSeries())
                .lengthOfPeriodTransformation(description.getPreadjustment())
                .logTransformation(description.isLogTransformation())
                .variables(variables)
                .stochasticComponent(arima)
                .build();

        LogLikelihoodFunction.Point<RegArimaModel<SarimaModel>, ConcentratedLikelihoodWithMissing> max = estimation.getMax();
        ParametersEstimation pestim;
        if (max == null) {
            pestim = new ParametersEstimation(Doubles.EMPTY, FastMatrix.EMPTY, Doubles.EMPTY, null);
        } else {
            pestim = new ParametersEstimation(max.getParameters(), max.asymptoticCovariance(), max.getScore(), "sarima (true signs)");
        }

        // complete for missings
        int nmissing = ll.nmissing();
        MissingValueEstimation[] missing = NOMISSING;
        if (nmissing > 0) {
            DoubleSeq y = model.getY();
            missing = new MissingValueEstimation[nmissing];
            DoubleSeqCursor cur = ll.missingCorrections().cursor();
            DoubleSeqCursor vcur = ll.missingUnscaledVariances().cursor();
            int[] pmissing = model.missing();
            for (int i = 0; i < nmissing; ++i) {
                double m = cur.getAndNext();
                double v = vcur.getAndNext();
                missing[i] = new MissingValueEstimation(pmissing[i], y.get(pmissing[i]) - m, Math.sqrt(v * vscale));
            }
        }
        DoubleSeq fullRes = RegArimaUtility.fullResiduals(model, ll);
        LightweightLinearModel.Estimation est = LightweightLinearModel.Estimation.builder()
                .domain(description.getEstimationDomain())
                .y(model.getY())
                .X(model.allVariables())
                .coefficients(ll.coefficients())
                .coefficientsCovariance(ll.covariance(free, true))
                .parameters(pestim)
                .statistics(statistics)
                .missing(missing)
                .logs(log.all())
                .build();

        int period = desc.getSeries().getAnnualFrequency();
        NiidTests niid = NiidTests.builder()
                .data(fullRes)
                .period(period)
                .hyperParametersCount(free)
                .build();
        TsPeriod start=description.getEstimationDomain().getEndPeriod().plus(-fullRes.length());
        TsResiduals residuals = TsResiduals.builder()
                .type(ResidualsType.QR_Transformed)
                .res(ll.e())
                .ssq(ll.ssq())
                .n(ll.dim())
                .df(ll.degreesOfFreedom())
                .dfc(df)
                .tsres(TsData.of(start, fullRes))
                .test(ResidualsDictionaries.MEAN, niid.meanTest())
                .test(ResidualsDictionaries.SKEW, niid.skewness())
                .test(ResidualsDictionaries.KURT, niid.kurtosis())
                .test(ResidualsDictionaries.DH, niid.normalityTest())
                .test(ResidualsDictionaries.LB, niid.ljungBox())
                .test(ResidualsDictionaries.BP, niid.boxPierce())
                .test(ResidualsDictionaries.SEASLB, niid.seasonalLjungBox())
                .test(ResidualsDictionaries.SEASBP, niid.seasonalBoxPierce())
                .test(ResidualsDictionaries.LB2, niid.ljungBoxOnSquare())
                .test(ResidualsDictionaries.BP2, niid.boxPierceOnSquare())
                .test(ResidualsDictionaries.NRUNS, niid.runsNumber())
                .test(ResidualsDictionaries.LRUNS, niid.runsLength())
                .test(ResidualsDictionaries.NUDRUNS, niid.upAndDownRunsNumbber())
                .test(ResidualsDictionaries.LUDRUNS, niid.upAndDownRunsLength())
                .build();

        // Fill TD stats
        return RegSarimaModel.builder()
                .description(desc)
                .estimation(est)
                .residuals(residuals)
                .details(Details.builder()
                        .regressionItems(regressionDesc)
                        .derivedTradingDay(tdderived)
                        .FTestonTradingDays(tdf)
                        .build())
                .build();
    }

    private final ConcurrentMap<String, Object> cache = new ConcurrentHashMap<>();

    @lombok.Singular
    private Map<String, Object> additionalResults;

    @lombok.Value
    @lombok.Builder
    public static class Details {

        List<RegressionDesc> regressionItems;

        RegressionDesc derivedTradingDay;
        StatisticalTest FTestonTradingDays;
    }

    Description<SarimaSpec> description;
    Estimation estimation;
    TsResiduals residuals;
    Details details;

    public int getAnnualFrequency() {
        return description.getSeries().getAnnualFrequency();
    }

    public SarimaOrders specification() {
        return description.getStochasticComponent().orders();
    }

    public SarimaModel arima() {
        return SarimaModel.builder(description.getStochasticComponent())
                .build();
    }

    public RegArimaModel<SarimaModel> regarima() {

        Matrix X = estimation.getX();
        boolean mean = isMeanEstimation();

        RegArimaModel.Builder builder = RegArimaModel.<SarimaModel>builder()
                .y(estimation.getY())
                .arima(arima())
                .meanCorrection(mean);

        int start = mean ? 1 : 0;
        for (int i = start; i < X.getColumnsCount(); ++i) {
            builder.addX(X.column(i));
        }
        return builder.build();
    }

    public TsData fullResiduals() {
        return residuals.getTsres();
    }

    public int freeArimaParametersCount() {
        return description.getStochasticComponent().freeParametersCount();
    }

    public IFunction likelihoodFunction() {
        RegArmaModel<SarimaModel> regarima = regarima().differencedModel();
        return RegArmaFunction.<SarimaModel>builder(regarima.getY())
                .likelihoodEvaluation(DefaultLikelihoodEvaluation.ml())
                .variables(regarima.getX())
                .mapping(mapping().stationaryMapping())
                .missingCount(regarima.getMissingCount())
                .build();
    }

    public IArimaMapping<SarimaModel> mapping() {
        SarimaSpec arima = description.getStochasticComponent();
        if (arima.hasFixedParameters()) {
            int n = arima.getP() + arima.getBp() + arima.getQ() + arima.getBq();
            double[] p = new double[n];
            boolean[] b = new boolean[n];
            int j = 0;
            Parameter[] P = arima.getPhi();
            for (int i = 0; i < P.length; ++i, ++j) {
                p[j] = P[i].getValue();
                b[j] = P[i].isFixed();
            }
            P = arima.getTheta();
            for (int i = 0; i < P.length; ++i, ++j) {
                p[j] = P[i].getValue();
                b[j] = P[i].isFixed();
            }
            P = arima.getBtheta();
            for (int i = 0; i < P.length; ++i, ++j) {
                p[j] = P[i].getValue();
                b[j] = P[i].isFixed();
            }
            return new SarimaFixedMapping(specification(), DoubleSeq.of(p), b);
        } else {
            return SarimaMapping.of(specification());
        }
    }

    public Forecasts forecasts(int nf) {
        if (nf < 0) {
            nf = (-nf) * getAnnualFrequency();
        }
        String key = "forecasts" + nf;
        Forecasts fcasts = (Forecasts) cache.get(key);
        if (fcasts == null) {
            fcasts = internalForecasts(nf);
            cache.put(key, fcasts);
        }
        return fcasts;
    }

    public Forecasts backcasts(int nb) {
        if (nb < 0) {
            nb = (-nb) * getAnnualFrequency();
        }
        String key = "backcasts" + nb;
        Forecasts bcasts = (Forecasts) cache.get(key);
        if (bcasts == null) {
            bcasts = internalBackcasts(nb);
            cache.put(key, bcasts);
        }
        return bcasts;
    }
    
    public TsData linearizedForecasts(int n){
        if (n < 0)
            n=-n*getAnnualFrequency();
        TsData lin = linearizedSeries();
        // FastArimaForecasts fcast = new FastArimaForecasts(model, false);
        ExactArimaForecasts fcast = new ExactArimaForecasts();
        fcast.prepare(arima(), isMeanCorrection());
        DoubleSeq f = fcast.forecasts(lin.getValues(), n);
        return TsData.of(lin.getEnd(), f);
    }

    public TsData linearizedBackcasts(int n){
        if (n < 0)
            n=-n*getAnnualFrequency();
        TsData lin = linearizedSeries();
        // FastArimaForecasts fcast = new FastArimaForecasts(model, false);
        ExactArimaForecasts fcast = new ExactArimaForecasts();
        fcast.prepare(arima(), isMeanCorrection());
        DoubleSeq f = fcast.backcasts(lin.getValues(), n);
        return TsData.of(lin.getStart().plus(-n), f);
    }

    private TsData regY() {
        TsData s = transformedSeries();
        TsData preadjust = this.preadjustmentEffect(s.getDomain(), v -> true);
        return TsData.subtract(s, preadjust);
    }

    private Forecasts internalForecasts(int nf) {
        TsDomain dom = this.getDescription().getDomain();
        if (nf == 0) {
            TsData empty = TsData.of(dom.getEndPeriod(), DoubleSeq.empty());
            return new Forecasts(empty, empty, empty, empty);
        }

        RegArimaForecasts.Result fcasts;
        DoubleSeq b = getEstimation().getCoefficients();
        LikelihoodStatistics ll = getEstimation().getStatistics();
        double sig2 = ll.getSsqErr() / (ll.getEffectiveObservationsCount() - ll.getEstimatedParametersCount());
        TsDomain xdom = dom.extend(0, nf);
        if (b.isEmpty()) {
            fcasts = RegArimaForecasts.calcForecast(arima(), regY().getValues(), nf, sig2);
        } else {
            FastMatrix matrix = regressionMatrix(xdom);
            fcasts = RegArimaForecasts.calcForecast(arima(),
                    regY().getValues(), matrix,
                    b, getEstimation().getCoefficientsCovariance(), sig2);
        }
        TsPeriod fstart = dom.getEndPeriod();
        double[] f = fcasts.getForecasts();
        double[] ef = fcasts.getForecastsStdev();

        TsData tf = TsData.ofInternal(fstart, f);
        tf = TsData.add(tf, preadjustmentEffect(xdom, v -> true));
        TsData fy = backTransform(tf, true);
        TsData efy;
        if (getDescription().isLogTransformation()) {
            double[] e = new double[nf];
            for (int i = 0; i < nf; ++i) {
                e[i] = LogNormal.stdev(f[i], ef[i]);
            }
            efy = TsData.ofInternal(fstart, e);
        } else {
            efy = TsData.ofInternal(fstart, ef);
        }
        return new Forecasts(TsData.ofInternal(fstart, f), TsData.ofInternal(fstart, ef), fy, efy);
    }

    private Forecasts internalBackcasts(int nb) {
        // we forecast to the past. Reverse everything
        TsDomain dom = getDescription().getDomain();
        if (nb == 0) {
            TsData empty = TsData.of(dom.getStartPeriod(), DoubleSeq.empty());
            return new Forecasts(empty, empty, empty, empty);
        }
        RegArimaForecasts.Result bcasts;
        DoubleSeq b = getEstimation().getCoefficients();
        LikelihoodStatistics ll = getEstimation().getStatistics();
        double sig2 = ll.getSsqErr() / (ll.getEffectiveObservationsCount() - ll.getEstimatedParametersCount());
        TsDomain xdom = dom.extend(nb, 0);
        if (b.isEmpty()) {
            bcasts = RegArimaForecasts.calcForecast(arima(), regY().getValues().reverse(), nb, sig2);
        } else {
            FastMatrix matrix = regressionMatrix(xdom);
            // reverse the matrix
            FastMatrix rmatrix = FastMatrix.make(matrix.getRowsCount(), matrix.getColumnsCount());
            DataBlockIterator iter = matrix.columnsIterator(), riter = rmatrix.columnsIterator();
            while (iter.hasNext()) {
                riter.next().copy(iter.next().reverse());
            }
            bcasts = RegArimaForecasts.calcForecast(arima(),
                    regY().getValues().reverse(), matrix,
                    b, getEstimation().getCoefficientsCovariance(), sig2);
        }
        TsPeriod bstart = dom.getStartPeriod().plus(-nb);
        double[] f = bcasts.getForecasts();
        double[] ef = bcasts.getForecastsStdev();
        Arrays2.reverse(f);
        Arrays2.reverse(ef);

        TsData tb = TsData.ofInternal(bstart, f);
        tb = TsData.add(tb, preadjustmentEffect(xdom, v -> true));
        TsData by = backTransform(tb, true);
        TsData eby;
        if (getDescription().isLogTransformation()) {
            double[] e = new double[nb];
            for (int i = 0; i < nb; ++i) {
                e[i] = LogNormal.stdev(f[i], ef[i]);
            }
            eby = TsData.ofInternal(bstart, e);
        } else {
            eby = TsData.ofInternal(bstart, ef);
        }
        return new Forecasts(TsData.ofInternal(bstart, f), TsData.ofInternal(bstart, ef), by, eby);
    }

    @lombok.Value
    public static class Forecasts {

        TsData rawForecasts, rawForecastsStdev;
        TsData forecasts, forecastsStdev;
    }

    public RegressionItem regressionItem(Predicate<ITsVariable> pred, int item) {
        List<RegressionDesc> items = details.getRegressionItems();
        int curitem = 0;
        for (RegressionDesc desc : items) {
            if (pred.test(desc.getCore())) {
                if (item == curitem) {
                    return new RegressionItem(desc.getCoef(), desc.getStderr(), desc.getPvalue(), desc.getCore().description(desc.getItem(), estimation.getDomain()));
                } else {
                    ++curitem;
                }
            }
        }
        return null;
    }

    public <T extends ITsVariable> int countVariables(Class<T> tclass, boolean fixed) {
        Variable[] variables = description.getVariables();
        if (fixed) {
            return Arrays.stream(variables).filter(var -> tclass.isInstance(var.getCore())).mapToInt(var -> var.fixedCoefficientsCount()).sum();
        } else {
            return Arrays.stream(variables).filter(var -> tclass.isInstance(var.getCore())).mapToInt(var -> var.freeCoefficientsCount()).sum();
        }
    }

    private static DataBlock weights(ITradingDaysVariable var) {
        if (var instanceof GenericTradingDaysVariable td) {
            return weights(td.getClustering());
        } else if (var instanceof HolidaysCorrectedTradingDays td) {
            return weights(td.getClustering());
        } else {
            return null;
        }
    }

    private static DataBlock weights(DayClustering td) {
        int n = td.getGroupsCount();
        double[] w = new double[n - 1];
        for (int i = 1; i < n; ++i) {
            w[i - 1] = td.getGroupCount(i);
        }
        return DataBlock.of(w);
    }

}
