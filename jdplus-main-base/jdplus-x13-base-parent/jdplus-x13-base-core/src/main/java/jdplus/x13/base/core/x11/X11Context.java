/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.base.core.x11;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Doubles;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.x13.base.api.x11.CalendarSigmaOption;
import jdplus.x13.base.api.x11.SeasonalFilterOption;
import jdplus.x13.base.api.x11.SigmaVecOption;
import jdplus.x13.base.api.x11.X11Exception;
import jdplus.x13.base.api.x11.X11Spec;
import jdplus.x13.base.core.x11.extremevaluecorrector.Cochran;
import jdplus.x13.base.core.x11.extremevaluecorrector.DefaultExtremeValuesCorrector;
import jdplus.x13.base.core.x11.extremevaluecorrector.GroupSpecificExtremeValuesCorrector;
import jdplus.x13.base.core.x11.extremevaluecorrector.IExtremeValuesCorrector;
import jdplus.x13.base.core.x11.extremevaluecorrector.PeriodSpecificExtremeValuesCorrector;
import java.util.function.IntToDoubleFunction;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.analysis.DiscreteKernel;
import jdplus.toolkit.base.core.math.linearfilters.AsymmetricFiltersFactory;
import jdplus.toolkit.base.core.math.linearfilters.IFiniteFilter;
import jdplus.toolkit.base.core.math.linearfilters.LocalPolynomialFilters;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import jdplus.x13.base.api.x11.BiasCorrection;
import lombok.experimental.NonFinal;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder
public class X11Context {

    @lombok.NonNull
    DecompositionMode mode;
    int period;
    int trendFilterLength;
    int localPolynomialDegree;
    boolean seasonal;
    SeasonalFilterOption[] initialSeasonalFilter;
    SeasonalFilterOption[] finalSeasonalFilter;
    double lowerSigma, upperSigma;
    CalendarSigmaOption calendarSigma;
    SigmaVecOption[] sigmavecOptions;
    int forecastHorizon;
    int backcastHorizon;
    /**
     * First period of the series (including backcasts)
     */
    @lombok.Getter(lombok.AccessLevel.PRIVATE)
    int firstPeriod;
    /**
     * Excludefcast is true if the forecasts/backcasts should be excluded for
     * the calculation of the standard deviation of the extreme values
     */
    boolean excludefcast;
    BiasCorrection bias;

    @NonFinal
    IExtremeValuesCorrector extremeValuesCorrector;

    public static Builder builder() {
        Builder builder = new Builder();
        builder.mode = DecompositionMode.Multiplicative;
        builder.seasonal = true;
        builder.trendFilterLength = 13;
        builder.localPolynomialDegree = 3;
        builder.period = 1;
        builder.initialSeasonalFilter = new SeasonalFilterOption[]{SeasonalFilterOption.S3X3};
        builder.finalSeasonalFilter = new SeasonalFilterOption[]{SeasonalFilterOption.S3X5};
        builder.calendarSigma = CalendarSigmaOption.None;
        builder.lowerSigma = 1.5;
        builder.upperSigma = 2.5;
        builder.firstPeriod = 0;
        builder.bias = BiasCorrection.Legacy;
        return builder;
    }

    public static X11Context of(@lombok.NonNull X11Spec spec, @lombok.NonNull TsData data) {
        SeasonalFilterOption[] filters;
        if (!spec.isSeasonal()) {
            filters = null;
        } else {
            if (spec.getFilters().length == 1) {
                filters = new SeasonalFilterOption[data.getAnnualFrequency()];
                SeasonalFilterOption filter = spec.getFilters()[0];
                for (int i = 0; i < data.getAnnualFrequency(); i++) {
                    filters[i] = filter;
                }
            } else {
                filters = spec.getFilters();
            }
        }
        int p = data.getAnnualFrequency();
        int nb = spec.getBackcastHorizon(), nf = spec.getForecastHorizon();
        if (nb < 0) {
            nb = -nb * p;
        }
        if (nf < 0) {
            nf = -nf * p;
        }
        return builder().mode(spec.getMode())
                .seasonal(spec.isSeasonal())
                .trendFilterLength(spec.getHendersonFilterLength())
                .period(p)
                .firstPeriod(data.getStart().annualPosition()) 
                .lowerSigma(spec.getLowerSigma())
                .upperSigma(spec.getUpperSigma())
                .calendarSigma(spec.getCalendarSigma())
                .sigmavecOptions(spec.getSigmaVec())
                .excludefcast(spec.isExcludeForecast())
                .forecastHorizon(nf)
                .backcastHorizon(nb)
                .initialSeasonalFilter(filters)
                .finalSeasonalFilter(filters)
                .bias(spec.getBias())
                .build();
    }

    public boolean isAutomaticHenderson() {
        return trendFilterLength == 0;
    }

    public boolean isMultiplicative() {
        return mode == DecompositionMode.Multiplicative || mode == DecompositionMode.PseudoAdditive;
    }

    public boolean isLogAdd() {
        return mode == DecompositionMode.LogAdditive;
    }

    public boolean isPseudoAdd() {
        return mode == DecompositionMode.PseudoAdditive;
    }

    /**
     * position in the period of the idx-th data
     * @param idx
     * @return 
     */
    public int getPosition(int idx){
        return idx == 0 ? firstPeriod : (firstPeriod+idx)%period;
    }
    public DoubleSeq remove(DoubleSeq l, DoubleSeq r) {
        if (isMultiplicative()) {
            return DoubleSeq.onMapping(l.length(), i -> l.get(i) / r.get(i));
        }
        return DoubleSeq.onMapping(l.length(), i -> l.get(i) - r.get(i));
    }

    public DoubleSeq add(DoubleSeq l, DoubleSeq r) {
        if (isMultiplicative()) {
            return DoubleSeq.onMapping(l.length(), i -> l.get(i) * r.get(i));
        } else {
            return DoubleSeq.onMapping(l.length(), i -> l.get(i) + r.get(i));
        }
    }

    public void remove(DoubleSeq l, DoubleSeq r, DataBlock q) {
        if (isMultiplicative()) {
            q.set(l, r, (x, y) -> x / y);
        } else {
            q.set(l, r, (x, y) -> x - y);
        }
    }

    public void add(DoubleSeq l, DoubleSeq r, DataBlock q) {
        if (isMultiplicative()) {
            q.set(l, r, (x, y) -> x * y);
        } else {
            q.set(l, r, (x, y) -> x + y);
        }
    }

    public SymmetricFilter trendFilter() {
        return trendFilter(trendFilterLength);
    }

    public SymmetricFilter trendFilter(int filterLength) {
        int horizon = filterLength / 2;
        IntToDoubleFunction weights = DiscreteKernel.henderson(horizon);
        return LocalPolynomialFilters.of(horizon, localPolynomialDegree, weights);
    }

    private static final double SQRPI = Math.sqrt(Math.PI);

    public IFiniteFilter[] asymmetricTrendFilters(SymmetricFilter sfilter, double ic) {
        double d = 2 / (SQRPI * ic);
        int horizon = sfilter.getUpperBound();
        int u = 0;
        double[] c = new double[]{d};
        IFiniteFilter[] afilters = new IFiniteFilter[horizon];
        for (int i = 0; i < afilters.length; ++i) {
            afilters[horizon - i - 1] = AsymmetricFiltersFactory.mmsreFilter2(sfilter, i, u, c, null);
        }
        return afilters;
    }

    /**
     * Selects the extreme value corrector depending on the result of the
     * CochranTest if CalendarSimga is Signif, the other extreme value corrector
     * is used
     *
     * @param dsToTest
     *
     * @return
     */
    public IExtremeValuesCorrector selectExtremeValuesCorrector(DoubleSeq dsToTest) {
        if (calendarSigma == CalendarSigmaOption.Signif) {
            Cochran cochranTest = new Cochran(dsToTest, this);
            boolean testResult = cochranTest.getTestResult();
            if (!testResult) {
                extremeValuesCorrector = new PeriodSpecificExtremeValuesCorrector();
            } else {
                extremeValuesCorrector = new DefaultExtremeValuesCorrector();
            }
        }
        return getExtremeValuesCorrector();

    }

    public IExtremeValuesCorrector getExtremeValuesCorrector() {

        if (extremeValuesCorrector == null) {

            switch (calendarSigma) {
                case All:
                    extremeValuesCorrector = new PeriodSpecificExtremeValuesCorrector();
                    break;
                case Signif:
                    break;
                case Select:
                    extremeValuesCorrector = new GroupSpecificExtremeValuesCorrector(sigmavecOptions);
                    break;
                default:
                    extremeValuesCorrector = new DefaultExtremeValuesCorrector();
                    break;
            }
        }

        return extremeValuesCorrector;

    }

    /**
     * MSR calculation is just for all periods. In case of mixed filters and
     * MSR, the MSR defaults will be used.
     */
    public boolean isMSR() {
        for (SeasonalFilterOption option : finalSeasonalFilter) {
            if (!SeasonalFilterOption.Msr.equals(option)) {
                return false;
            }
        }
        return true;
    }

    public SeasonalFilterOption[] getInitialSeasonalFilter() {

        SeasonalFilterOption[] result = new SeasonalFilterOption[period];
        for (int i = 0; i < period; i++) {
            result[i] = initialSeasonalFilter[i];
            if (SeasonalFilterOption.Msr.equals(initialSeasonalFilter[i]) || SeasonalFilterOption.X11Default.equals(initialSeasonalFilter[i])) {
                result[i] = SeasonalFilterOption.S3X3;
            }
        }
        return result;
    }

    public SeasonalFilterOption[] getFinalSeasonalFilter() {
        if (finalSeasonalFilter == null) {
            return null;
        }
        SeasonalFilterOption[] result = new SeasonalFilterOption[period];
        for (int i = 0; i < period; i++) {
            result[i] = finalSeasonalFilter[i];
            if (SeasonalFilterOption.Msr.equals(finalSeasonalFilter[i]) || SeasonalFilterOption.X11Default.equals(finalSeasonalFilter[i])) {
                result[i] = SeasonalFilterOption.S3X5;
            }
        }
        return result;
    }

    /**
     * Replace negative values of a Double Sequence with either the mean of the
     * two nearest positive replacements before and after the value, or the
     * nearest value if it is on the ends of the series.
     *
     * @param in
     *
     * @return new DoubleSeq
     */
    public static DoubleSeq makePositivity(DoubleSeq in) {
        double[] stc = in.toArray();
        int n = in.length();
        for (int i = 0; i < n; ++i) {
            if (stc[i] <= 0) {
                int before = i - 1;
                while (before >= 0 && stc[before] <= 0) {
                    --before;
                }
                int after = i + 1;
                while (after < n && stc[after] <= 0) {
                    ++after;
                }
                double m;
                if (before < 0 && after >= n) {
                    throw new X11Exception("Negative series");
                }
                if (before >= 0 && after < n) {
                    m = (stc[before] + stc[after]) / 2;
                } else if (after >= n) {
                    m = stc[before];
                } else {
                    m = stc[after];
                }
                stc[i] = m;
            }
        }
        return Doubles.of(stc);
    }
}
