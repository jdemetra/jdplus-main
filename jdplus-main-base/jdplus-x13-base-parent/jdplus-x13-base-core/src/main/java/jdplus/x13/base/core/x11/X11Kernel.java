/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.base.core.x11;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.x13.base.api.x11.X11Exception;
import jdplus.x13.base.api.x11.X11Spec;
import jdplus.x13.base.core.x11.filter.MusgraveFilterFactory;
import jdplus.x13.base.core.x11.filter.endpoints.AsymmetricEndPoints;
import jdplus.x13.base.core.x11.pseudoadd.X11BStepPseudoAdd;
import jdplus.x13.base.core.x11.pseudoadd.X11CStepPseudoAdd;
import jdplus.x13.base.core.x11.pseudoadd.X11DStepPseudoAdd;
import java.util.Arrays;
import jdplus.toolkit.base.api.data.DoublesMath;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.linearfilters.FiniteFilter;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import jdplus.x13.base.api.x11.BiasCorrection;
import jdplus.x13.base.core.x11.filter.X11TrendCycleFilterFactory;
import jdplus.x13.base.core.x11.filter.endpoints.CopyEndPoints;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Getter
public class X11Kernel {

    private X11BStep bstep;
    private X11CStep cstep;
    private X11DStep dstep;
    private TsData input;
    private X11Context context;

    public static double[] table(int n, double value) {
        double[] x = new double[n];
        Arrays.fill(x, value);
        return x;
    }

    /**
     *
     * @param timeSeries Time series including forecasts/backcasts
     * @param spec
     * @return
     */
    public X11Results process(@lombok.NonNull TsData timeSeries, @lombok.NonNull X11Spec spec) {
        clear();
        check(timeSeries, spec);

        input = timeSeries;
        DoubleSeq data = input.getValues();
        context = X11Context.of(spec, input);

        if (context.isPseudoAdd()) {
            bstep = new X11BStepPseudoAdd();
            bstep.process(data, context);
            cstep = new X11CStepPseudoAdd(bstep.getB7(), bstep.getB13());
            cstep.process(data, bstep.getB20(), context);
            dstep = new X11DStepPseudoAdd(cstep.getC7(), cstep.getC13(), cstep.getC20());
            dstep.process(data, cstep.getC20(), context);
        } else {
            if (context.isLogAdd()) {
                data = data.log();
            }
            bstep = new X11BStep();
            bstep.process(data, context);
            cstep = new X11CStep();
            cstep.process(data, bstep.getB20(), context);
            dstep = new X11DStep();
            dstep.process(data, cstep.getC20(), context);
        }
        return buildResults(timeSeries.getStart(), spec);
    }

    private void check(TsData timeSeries, X11Spec spec) throws X11Exception, IllegalArgumentException {
        int frequency = timeSeries.getAnnualFrequency();
        if (frequency == -1) {
            throw new IllegalArgumentException("Frequency of the time series must be compatible with years");
        }
        if (timeSeries.getValues().length() < 3 * frequency) {
            throw new X11Exception(X11Exception.ERR_LENGTH);
        }
        if (!timeSeries.getValues().allMatch(Double::isFinite)) {
            throw new X11Exception(X11Exception.ERR_MISSING);
        }
        if ((spec.getMode() == DecompositionMode.Multiplicative || spec.getMode() == DecompositionMode.LogAdditive)
                && timeSeries.getValues().anyMatch(x -> x <= 0)) {
            throw new X11Exception(X11Exception.ERR_NEG);
        }
    }

    private void clear() {
        bstep = null;
        cstep = null;
        dstep = null;
        input = null;
        context = null;
    }

    private X11Results buildResults(TsPeriod start, X11Spec spec) {
        int nb = spec.getBackcastHorizon() >= 0 ? spec.getBackcastHorizon() : -spec.getBackcastHorizon() * start.annualFrequency();
        int nf = spec.getForecastHorizon() >= 0 ? spec.getForecastHorizon() : -spec.getForecastHorizon() * start.annualFrequency();

        // bias correction for s // sa // t // i
        X11Results.Builder builder = X11Results.builder()
                .nbackcasts(nb)
                .nforecasts(nf)
                //B-Tables
                .b1(input)
                .b2(TsData.of(start.plus(bstep.getB2drop()), prepare(bstep.getB2())))
                .b3(TsData.of(start.plus(bstep.getB2drop()), prepare(bstep.getB3())))
                .b4(TsData.of(start.plus(bstep.getB2drop()), prepare(bstep.getB4())))
                .b5(TsData.of(start, prepare(bstep.getB5())))
                .b6(TsData.of(start, prepare(bstep.getB6())))
                .b7(TsData.of(start, prepare(bstep.getB7())))
                .b8(TsData.of(start, prepare(bstep.getB8())))
                .b9(TsData.of(start, prepare(bstep.getB9())))
                .b10(TsData.of(start, prepare(bstep.getB10())))
                .b11(TsData.of(start, prepare(bstep.getB11())))
                .b13(TsData.of(start, prepare(bstep.getB13())))
                .b17(TsData.of(start, bstep.getB17()))
                .b20(TsData.of(start, prepare(bstep.getB20())))
                //C-Tables
                .c1(TsData.of(start, prepare(cstep.getC1())))
                .c2(TsData.of(start.plus(cstep.getC2drop()), prepare(cstep.getC2())))
                .c4(TsData.of(start.plus(cstep.getC2drop()), prepare(cstep.getC4())))
                .c5(TsData.of(start, prepare(cstep.getC5())))
                .c6(TsData.of(start, prepare(cstep.getC6())))
                .c7(TsData.of(start, prepare(cstep.getC7())))
                .c9(TsData.of(start, prepare(cstep.getC9())))
                .c10(TsData.of(start, prepare(cstep.getC10())))
                .c11(TsData.of(start, prepare(cstep.getC11())))
                .c13(TsData.of(start, prepare(cstep.getC13())))
                .c17(TsData.of(start, cstep.getC17()))
                .c20(TsData.of(start, prepare(cstep.getC20())))
                //D-Tables
                .d1(TsData.of(start, prepare(dstep.getD1())))
                .d2(TsData.of(start.plus(dstep.getD2drop()), prepare(dstep.getD2())))
                .d4(TsData.of(start.plus(dstep.getD2drop()), prepare(dstep.getD4())))
                .d5(TsData.of(start, prepare(dstep.getD5())))
                .d6(TsData.of(start, prepare(dstep.getD6())))
                .d7(TsData.of(start, prepare(dstep.getD7())))
                .d8(TsData.of(start, prepare(dstep.getD8())))
                .d9(TsData.of(start, prepare(dstep.getD9())))
                //msr
                .d9Msr(dstep.getD9msr())
                .d9default(dstep.isD9default())
                .d9filter(dstep.getD9filter())
                // trend selection
                .iCRatio(dstep.getICRatio())
                .finalHendersonFilterLength(dstep.getFinalHendersonFilterLength())
                .finalSeasonalFilter(dstep.getSeasFilter())
                .mode(spec.getMode());

        return finalResults(builder, start, spec).build();
    }

    // fill D10, d11, d12, d13
    private X11Results.Builder finalResults(X11Results.Builder builder, TsPeriod start, X11Spec spec) {
        if (spec.getMode() != DecompositionMode.LogAdditive || !spec.isSeasonal() || spec.getBias() == BiasCorrection.None) {
            builder.d10(TsData.of(start, prepare(dstep.getD10())))
                    .d11(TsData.of(start, prepare(dstep.getD11())))
                    .d12(TsData.of(start, prepare(dstep.getD12())))
                    .d13(TsData.of(start, prepare(dstep.getD13())));
        } else {
            biasCorrection(builder, start);
        }
        return builder;
    }

    private DoubleSeq prepare(final DoubleSeq in) {
        return context.isLogAdd() ? in.exp() : in;
    }

    // input are in logs
    private void biasCorrection(X11Results.Builder builder, TsPeriod start) {
        switch (context.getBias()) {
            case Legacy ->
                legacyBiasCorrection(builder, start);
            case Smooth ->
                smoothBiasCorrection(builder, start);
            case Ratio ->
                ratioBiasCorrection(builder, start);
        }
    }

    private void fill(X11Results.Builder builder, TsPeriod start, DoubleSeq d10, DoubleSeq d12) {
        d12 = X11Context.makePositivity(d12);
        DoubleSeq d11 = DoublesMath.divide(input.getValues(), d10);
        DoubleSeq d13 = DoublesMath.divide(d11, d12);
        builder.d10(TsData.of(start, d10))
                .d11(TsData.of(start, d11))
                .d12(TsData.of(start, d12))
                .d13(TsData.of(start, d13));
    }

    private void smoothBiasCorrection(X11Results.Builder builder, TsPeriod start) {
        DoubleSeq d13 = dstep.getD13(), d10 = dstep.getD10(), d12 = dstep.getD12();
        double issq = d13.ssq();
        double sig = Math.exp(issq / (2 * d13.length()));
        int ifreq = context.getPeriod();
        SymmetricFilter filter = X11TrendCycleFilterFactory.makeTrendFilter(ifreq);
        int ndrop = filter.length() / 2;
        double[] x = new double[d10.length()];
        DataBlock out = DataBlock.of(x, ndrop, x.length - ndrop);
        d10 = d10.exp();
        filter.apply(d10, out);
        CopyEndPoints cp = new CopyEndPoints(ndrop);
        DataBlock X = DataBlock.of(x);
        cp.process(d10, X);
        d12 = d12.fn(X, (a, b) -> Math.exp(a) * b * sig).commit();

        fill(builder, start, d10, d12);
    }

    private void ratioBiasCorrection(X11Results.Builder builder, TsPeriod start) {
        DoubleSeq d10 = dstep.getD10().exp();
        DoubleSeq d12 = dstep.getD12().exp();
        DoubleSeq d13 = dstep.getD13().exp();

        int ifreq = context.getPeriod();
        int s0 = start.annualPosition(), ny = (d10.length() - s0) / ifreq;

        double sbias = d10.range(s0, s0 + ny * ifreq).average(), ibias = d13.average();
        d10 = d10.fn(x -> x / sbias);
        double tbias = sbias * ibias;
        d12 = d12.fn(x -> x * tbias);
        fill(builder, start, d10, d12);
    }

    private void legacyBiasCorrection(X11Results.Builder builder, TsPeriod start) {
        DoubleSeq d13 = cstep.getC13(), d10 = dstep.getD10(), d12 = dstep.getD12();
        double issq = d13.ssq();
        double sig = Math.exp(issq / (2 * d13.length()));
        int ifreq = context.getPeriod();
        int length = (ifreq == 2) ? 5 : 2 * ifreq - 1;
        double[] x = table(d10.length(), Double.NaN);
        int ndrop = length / 2;
        DataBlock out = DataBlock.of(x, ndrop, x.length - ndrop);
        d10 = d10.exp();
        SymmetricFilter smoother = context.trendFilter(length);
        smoother.apply(d10, out);
        FiniteFilter[] musgraveFilters = MusgraveFilterFactory.makeFilters(smoother, 4.5);
        AsymmetricEndPoints aepFilter = new AsymmetricEndPoints(musgraveFilters, 0);
        DataBlock hs = out.extend(ndrop, ndrop);
        aepFilter.process(d10, hs);
        d12 = d12.fn(hs, (a, b) -> Math.exp(a) * b * sig).commit();

        fill(builder, start, d10, d12);
    }

//    private TsData smoothBiasCorrection(DoubleSeq t, DoubleSeq s, DoubleSeq i) {
//        double issq = i.ssq();
//        double sig = Math.exp(issq / (2 * i.length()));
//        int ifreq = context.getPeriod();
//        int length = (ifreq == 2) ? 5 : 2 * ifreq - 1;
//        s = prepare(s);
//        TsData hs=new DefaultNormalizingStrategie().process(s, null, ifreq);
//        hs.applyOnFinite(x -> x * sig);
//        return t.times(hs);
//    }
//
//    private TsData ratioBiasCorrection(DoubleSeq t, DoubleSeq s, DoubleSeq i) {
//        // average of s, i on complete years
//        double sbias=s.fullYears().average(), ibias=i.average();
//        s.apply(x->x/sbias);
//        return t.times(sbias*ibias);
//    }
}
