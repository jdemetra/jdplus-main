/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.base.core.x11;

import jdplus.x13.base.api.x11.MsrTable;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.x13.base.api.x11.CalendarSigmaOption;
import jdplus.x13.base.api.x11.SeasonalFilterOption;
import static jdplus.x13.base.core.x11.X11Kernel.table;
import jdplus.x13.base.core.x11.extremevaluecorrector.IExtremeValuesCorrector;
import jdplus.x13.base.core.x11.extremevaluecorrector.PeriodSpecificExtremeValuesCorrector;
import jdplus.x13.base.core.x11.filter.AutomaticHenderson;
import jdplus.x13.base.core.x11.filter.DefaultSeasonalNormalizer;
import jdplus.x13.base.core.x11.filter.MsrFilterSelection;
import jdplus.x13.base.core.x11.filter.MusgraveFilterFactory;
import jdplus.x13.base.core.x11.filter.X11SeasonalFilterProcessor;
import jdplus.x13.base.core.x11.filter.X11SeasonalFiltersFactory;
import jdplus.x13.base.core.x11.filter.endpoints.AsymmetricEndPoints;
import java.util.Arrays;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.linearfilters.IFiniteFilter;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import jdplus.x13.base.core.x11.filter.DummyFilter;
import jdplus.x13.base.core.x11.filter.X11TrendCycleFilterFactory;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Getter
public class X11DStep {

    private static final double EPS = 1e-9;

    private DoubleSeq d1, d2, d4, d5, d6, d7, d8, d9, d9g, d9bis, d9_g_bis, d10, d10bis, d11, d11bis, d12, d13;
    private int d9msriter;
    private SeasonalFilterOption d9filter;
    private boolean d9default;
    private MsrTable d9msr;
    private int d2drop, finalHendersonFilterLength;
    private double iCRatio;
    private SeasonalFilterOption[] seasFilter;
    private DoubleSeq refSeries;

    public void process(DoubleSeq refSeries, DoubleSeq input, X11Context context) {
        this.refSeries = refSeries;
        d1Step(context, input);
        d2Step(context);
        d4Step(context);
        d5Step(context);
        d6Step(context);
        d7Step(context);
        d8Step(context);
        d9Step(context);
        dFinalStep(context);
    }

    private void d1Step(X11Context context, DoubleSeq input) {
        d1 = d1(context, input);
    }

    protected DoubleSeq d1(X11Context context, DoubleSeq input) {
        return context.remove(this.refSeries, input);
    }

    private void d2Step(X11Context context) {
        SymmetricFilter filter = X11TrendCycleFilterFactory.makeTrendFilter(context.getPeriod());
        d2drop = filter.length() / 2;

        double[] x = table(d1.length() - 2 * d2drop, Double.NaN);
        DataBlock out = DataBlock.of(x, 0, x.length);
        filter.apply(d1, out);
        d2 = DoubleSeq.of(x);
    }

    private void d4Step(X11Context context) {
        d4 = context.remove(d1.drop(d2drop, d2drop), d2);
    }

    private void d5Step(X11Context context) {
        if (context.isSeasonal()) {
            X11SeasonalFilterProcessor processor = X11SeasonalFiltersFactory.filter(context.getPeriod(), context.getInitialSeasonalFilter());
            DoubleSeq d5a = processor.process(d4, context.getPosition(d2drop));
            d5 = DefaultSeasonalNormalizer.normalize(d5a, d2drop, context);
        } else {
            d5 = DummyFilter.filter(context.isMultiplicative(), d4);
        }
    }

    private void d6Step(X11Context context) {
        d6 = d6(context);
    }

    protected DoubleSeq d6(X11Context context) {
        return context.remove(d1, d5);
    }

    private void d7Step(X11Context context) {
        SymmetricFilter filter;
        if (context.isAutomaticHenderson()) {
            double icr = AutomaticHenderson.calcICR(context, d6);
            int filterLength = AutomaticHenderson.selectFilter(icr, context.getPeriod());
            filter = context.trendFilter(filterLength);
        } else {
            filter = context.trendFilter();
        }
        int ndrop = filter.length() / 2;

        double[] x = table(d6.length(), Double.NaN);
        DataBlock out = DataBlock.of(x, ndrop, x.length - ndrop);
        filter.apply(d6, out);

        // apply asymmetric filters
        double r = MusgraveFilterFactory.findR(filter.length(), context.getPeriod());
        IFiniteFilter[] asymmetricFilter = context.asymmetricTrendFilters(filter, r);
        AsymmetricEndPoints aep = new AsymmetricEndPoints(asymmetricFilter, 0);
        aep.process(d6, DataBlock.of(x));
        d7 = DoubleSeq.of(x);
        if (context.isMultiplicative()) {
            d7 = X11Context.makePositivity(d7);
        }
    }

    private void d8Step(X11Context context) {
        d8 = d8(context);
    }

    protected DoubleSeq d8(X11Context context) {
        return context.remove(refSeries, d7);
    }

    private void d9Step(X11Context context) {
        IExtremeValuesCorrector ecorr = context.getExtremeValuesCorrector();
        if (ecorr instanceof PeriodSpecificExtremeValuesCorrector && context.getCalendarSigma() != CalendarSigmaOption.Signif) {
            //compute corrections without backcast/forecast but keep the length
            d9 = ecorr.computeCorrections(d8, true);
            d9g = ecorr.applyCorrections(d8, d9);
            d9_g_bis = d9g;
        } else {
            d9bis = context.remove(d1, d7);
            DoubleSeq d9temp = DoubleSeq.onMapping(d9bis.length(), i -> Math.abs(d9bis.get(i) - d8.get(i)));
            d9 = DoubleSeq.onMapping(d9temp.length(), i -> d9temp.get(i) < EPS ? Double.NaN : d9bis.get(i));
            d9_g_bis = d9bis;
        }
    }

    private void dFinalStep(X11Context context) {
        if (context.isSeasonal()) {
            seasFilter = context.getFinalSeasonalFilter();
            if (context.isMSR()) {
                MsrFilterSelection msr = getMsrFilterSelection();
                SeasonalFilterOption msrFilter = msr.doMSR(d9_g_bis, context);
                d9msriter = msr.getIterCount();
                d9filter = msrFilter;
                Arrays.fill(seasFilter, msrFilter);
            }
            d9msr = X11Utility.defaultMsrTable(d9_g_bis.drop(context.getBackcastHorizon(), context.getForecastHorizon()), context.getPeriod(), context.getPosition(context.getBackcastHorizon()), context.getMode());
            X11SeasonalFilterProcessor processor = X11SeasonalFiltersFactory.filter(context.getPeriod(), seasFilter);
            d10bis = processor.process(d9_g_bis, context.getPosition(0));
            d10 = DefaultSeasonalNormalizer.normalize(d10bis, 0, context);
        } else {
            d10bis = DummyFilter.filter(context.isMultiplicative(), d9_g_bis);
            d10 = d10bis;
        }

        d11bis = d11bis(context);

        SymmetricFilter hfilter;
        iCRatio = AutomaticHenderson.calcICR(context, d11bis);
        if (context.isAutomaticHenderson()) {
            int filterLength = AutomaticHenderson.selectFilter(iCRatio, context.getPeriod());
            hfilter = context.trendFilter(filterLength);
        } else {
            hfilter = context.trendFilter();
        }
        finalHendersonFilterLength = hfilter.length();
        int ndrop = hfilter.length() / 2;

        double[] x = table(d11bis.length(), Double.NaN);
        DataBlock out = DataBlock.of(x, ndrop, x.length - ndrop);
        hfilter.apply(d11bis, out);

        // apply asymmetric filters
        double r = MusgraveFilterFactory.findR(hfilter.length(), context.getPeriod());
        IFiniteFilter[] asymmetricFilter = context.asymmetricTrendFilters(hfilter, r);
        AsymmetricEndPoints aep = new AsymmetricEndPoints(asymmetricFilter, 0);
        aep.process(d11bis, DataBlock.of(x));
        d12 = DoubleSeq.of(x);
        if (context.isMultiplicative()) {
            d12 = X11Context.makePositivity(d12);
        }

        d11 = d11(context);
        d13 = context.remove(d11, d12);

    }

    protected MsrFilterSelection getMsrFilterSelection() {
        return new MsrFilterSelection();
    }

    protected DoubleSeq d11(X11Context context) {
        return context.remove(refSeries, d10);
    }

    protected DoubleSeq d11bis(X11Context context) {
        return context.remove(d1, d10);
    }
}
