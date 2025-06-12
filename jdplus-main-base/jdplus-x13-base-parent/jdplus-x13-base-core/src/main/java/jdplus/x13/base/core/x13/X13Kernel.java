/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.x13.base.core.x13;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.x13.base.api.regarima.BasicSpec;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.api.timeseries.regression.ModellingUtility;
import jdplus.x13.base.api.x11.X11Spec;
import jdplus.x13.base.api.x13.X13Spec;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.sa.base.core.CholetteProcessor;
import jdplus.sa.base.core.PreliminaryChecks;
import jdplus.sa.base.core.SaBenchmarkingResults;
import jdplus.sa.base.core.modelling.RegArimaDecomposer;
import jdplus.sa.base.core.modelling.SaVariablesMapping;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.ssf.arima.ExactArimaForecasts;
import jdplus.x13.base.core.x11.X11Kernel;
import jdplus.x13.base.core.x11.X11Results;
import jdplus.x13.base.core.x11.X11Utility;
import jdplus.x13.base.core.x13.regarima.RegArimaKernel;

/**
 *
 * @author palatej
 */
@lombok.Value
public class X13Kernel {

    private static PreliminaryChecks.Tool of(X13Spec spec) {
        BasicSpec basic = spec.getRegArima().getBasic();
        return (s, logs) -> {
            TsData sc = s.select(basic.getSpan());
            if (basic.isPreliminaryCheck()) {
                PreliminaryChecks.testSeries(sc);
            }
            return sc;
        };
    }

    private PreliminaryChecks.Tool preliminary;
    private RegArimaKernel regarima;
    private SaVariablesMapping samapping;
    private X11Spec spec;
    private boolean preprop;
    private CholetteProcessor cholette;

    public static X13Kernel of(X13Spec spec, ModellingContext context) {
        PreliminaryChecks.Tool check = of(spec);
        boolean blPreprop = spec.getRegArima().getBasic().isPreprocessing();
//        boolean blPreprop = 
//                spec.getRegArima().getBasic().isPreprocessing() &&
//                (spec.getRegArima().getTransform().getFunction() != TransformationType.None ||
//                spec.getRegArima().getOutliers().isUsed() || spec.getRegArima().getRegression().isUsed());
        RegArimaKernel regarima = RegArimaKernel.of(spec.getRegArima(), context);
        SaVariablesMapping mapping = new SaVariablesMapping();
        // TO DO: fill maping with existing information in TramoSpec (section Regression)
        return new X13Kernel(check, regarima, mapping, spec.getX11(), blPreprop, CholetteProcessor.of(spec.getBenchmarking()));
    }

    public X13Results process(TsData s, ProcessingLog log) {
        if (log == null) {
            log = ProcessingLog.dummy();
        }
        try {
            // Step 0. Preliminary checks
            // sc is the series corresponding to the series span, after some verifications
            // null in case of problems
            TsData sc = preliminary.check(s, log);
            if (sc == null) {
                return X13Results.builder()
                        .log(log)
                        .build();
            }
            // Step 1. Preprocessing
            RegSarimaModel preprocessing;
            X13Preadjustment preadjustment;
            TsData alin;
            if (regarima != null) {
                // We reuse the full series because selection is integrated in the preprocessing step
                preprocessing = regarima.process(s, log);
                // Step 2. Link between regarima and x11
                int nb = spec.getBackcastHorizon();
                if (nb < 0) {
                    nb = -nb * s.getAnnualFrequency();
                }
                int nf = spec.getForecastHorizon();
                if (nf < 0) {
                    nf = -nf * s.getAnnualFrequency();
                }
                X13Preadjustment.Builder builder = X13Preadjustment.builder();
                alin = initialStep(preprocessing, nb, nf, builder);
                preadjustment = builder.build();
            } else {
                // we use here the series corresponding to the span
                preprocessing = null;
                preadjustment = X13Preadjustment.builder().a1(sc).build();
                alin = sc;
            }
            // Step 3. X11
            X11Kernel x11 = new X11Kernel();
            X11Spec nspec = updateSpec(spec, preprocessing);
            X11Results xr = x11.process(alin, nspec);
            X13Finals finals = finals(nspec.getMode(), preadjustment, xr);
            SaBenchmarkingResults bench = null;
            if (cholette != null) {
                bench = cholette.process(s, TsData.concatenate(finals.getD11final(), finals.getD11a()), preprocessing);
            }
            return X13Results.builder()
                    .preprocessing(preprocessing)
                    .preadjustment(preadjustment)
                    .decomposition(xr)
                    .finals(finals)
                    .benchmarking(bench)
                    .diagnostics(X13Diagnostics.of(preprocessing, preadjustment, xr, finals))
                    .log(log)
                    .build();
        } catch (Exception err) {
            log.error(err);
            return X13Results.builder()
                    .log(log)
                    .build();
        }
    }

    private TsData initialStep(RegSarimaModel model, int nb, int nf, X13Preadjustment.Builder astep) {
        boolean mul = model.getDescription().isLogTransformation();
        TsData series = model.interpolatedSeries(false);
        TsDomain sdomain = series.getDomain();
        TsDomain domain = sdomain.extend(nb, nf);
        // start of the backcasts/forecasts
        TsPeriod bstart = domain.getStartPeriod(), fstart = sdomain.getEndPeriod();

        // Gets all regression effects
        TsData mh = model.deterministicEffect(domain, v -> ModellingUtility.isMovingHoliday(v));
        TsData td = model.deterministicEffect(domain, v -> ModellingUtility.isDaysRelated(v));

        TsData pt = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.Trend, true, v -> ModellingUtility.isOutlier(v));
        TsData ps = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.Seasonal, true, v -> ModellingUtility.isOutlier(v));
        TsData pi = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.Irregular, true, v -> ModellingUtility.isOutlier(v));
        TsData ut = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.Trend, true, v -> ModellingUtility.isUser(v));
        TsData us = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.Seasonal, true, v -> ModellingUtility.isUser(v));
        TsData ui = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.Irregular, true, v -> ModellingUtility.isUser(v));
        TsData usa = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.SeasonallyAdjusted, true, v -> ModellingUtility.isUser(v));
        TsData user = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.Series, true, v -> ModellingUtility.isUser(v));
        TsData uu = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.Undefined, true, v -> ModellingUtility.isUser(v));
        TsData ucal = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.CalendarEffect, true, v -> ModellingUtility.isUser(v));
        pt = TsData.add(pt, ut);
        ps = TsData.add(ps, us);
        pi = TsData.add(pi, ui);
        TsData p = TsData.add(pt, ps, pi);
        TsData pall = TsData.add(pt, ps, pi);
        TsData u = TsData.add(ucal, usa, user);

        // linearized series. detlin are deterministic effects removed before the decomposition,
        // detall are all the deterministic effects
        TsData detlin = TsData.add(td, mh, p, u), detall = TsData.add(detlin, uu);
        // forecasts, backcasts
        TsData nbcasts = null, nfcasts = null;
        TsData s = model.interpolatedSeries(true);

        if (nb > 0 || nf > 0) {
            TsData lin = TsData.subtract(s, detall);
            SarimaModel arima = model.arima();
//            FastArimaForecasts fcasts = new FastArimaForecasts();
//            double mean = 0;
//            Optional<Variable> mu = Arrays.stream(model.getDescription().getVariables()).filter(v -> v.getCore() instanceof TrendConstant).findFirst();
//            if (mu.isPresent()) {
//                mean = mu.orElseThrow().getCoefficient(0).getValue();
//            }
            ExactArimaForecasts fcasts = new ExactArimaForecasts();
            boolean mean = model.isMeanCorrection();
            fcasts.prepare(arima, mean);

            if (nb > 0) {
                DoubleSeq tmp = fcasts.backcasts(lin.getValues(), nb);
                nbcasts = TsData.of(bstart, tmp);
                nbcasts = TsData.add(nbcasts, detall);
            }
            if (nf > 0) {
                DoubleSeq tmp = fcasts.forecasts(lin.getValues(), nf);
                nfcasts = TsData.of(fstart, tmp);
                nfcasts = TsData.add(nfcasts, detall);
            }
        }

        TsData a1a = nfcasts == null ? null : model.backTransform(nfcasts, true),
                a1b = nbcasts == null ? null : model.backTransform(nbcasts, true);

        astep.a1(series)
                .a1a(a1a)
                .a1b(a1b)
                .a6(model.backTransform(td, true))
                .a7(model.backTransform(mh, false))
                .a8(model.backTransform(pall, false))
                .a8t(model.backTransform(pt, false))
                .a8s(model.backTransform(ps, false))
                .a8i(model.backTransform(pi, false))
                .a9(model.backTransform(u, false))
                .a9u(model.backTransform(uu, false))
                .a9cal(model.backTransform(ucal, false))
                .a9sa(model.backTransform(usa, false))
                .a9ser(model.backTransform(user, false));

        series = TsData.concatenate(a1b, series, a1a);
        TsData x = model.backTransform(detlin, true);

        return (mul ? TsData.divide(series, x) : TsData.subtract(series, x));
    }

    private X11Spec updateSpec(X11Spec spec, RegSarimaModel model) {
        if (model == null) {
            return spec;
        }
        int nb = spec.getBackcastHorizon(), nf = spec.getForecastHorizon();
        int period = model.getAnnualFrequency();
        X11Spec.Builder builder = spec.toBuilder()
                .backcastHorizon(nb < 0 ? -nb * period : nb)
                .forecastHorizon(nf < 0 ? -nf * period : nf);

        if (!preprop) {
            builder.mode(spec.getMode() == DecompositionMode.Undefined ? DecompositionMode.Additive : spec.getMode());
            return builder.build();
        }
        if (spec.getMode() != DecompositionMode.PseudoAdditive) {
            boolean mul = model.getDescription().isLogTransformation();
            if (mul) {
                if (spec.getMode() != DecompositionMode.Multiplicative && spec.getMode() != DecompositionMode.LogAdditive) {
                    builder.mode(DecompositionMode.Multiplicative);
                }
            } else {
                builder.mode(DecompositionMode.Additive);
            }
        }
        return builder.build();
    }

    private TsData op(DecompositionMode mode, TsData l, TsData r) {
        if (mode != DecompositionMode.Multiplicative && mode != DecompositionMode.PseudoAdditive) {
            return TsData.subtract(l, r);
        } else {
            return TsData.divide(l, r);
        }
    }

    /**
     * Adds/multiplies two time series, following the decomposition mode.
     * (multiplies in the case of multiplicative decomposition)
     *
     * @param l The left operand
     * @param r The right operand
     *
     * @return A new time series is returned
     */
    private TsData invOp(DecompositionMode mode, TsData l, TsData r) {
        if (!mode.isMultiplicative() && mode != DecompositionMode.PseudoAdditive) {
            return TsData.add(l, r);
        } else {
            return TsData.multiply(l, r);
        }
    }

    private double mean(DecompositionMode mode) {
        if (!mode.isMultiplicative() && mode != DecompositionMode.PseudoAdditive) {
            return 0;
        } else {
            return 1;
        }
    }

    private TsData correct(TsData s, TsData weights, TsData rs) {
        DoubleSeq sc = X11Utility.correctSeries(s.getValues(), weights.getValues(), rs.getValues());
        return TsData.of(s.getStart(), sc.commit());
    }

    private TsData correct(TsData s, TsData weights, double mean) {
        DoubleSeq sc = X11Utility.correctSeries(s.getValues(), weights.getValues(), mean);
        return TsData.of(s.getStart(), sc.commit());
    }

//    private final TsData pseudoOp(TsData y, TsData t, TsData s) {
//        TsData sa = new TsData(y.getDomain());
//        int beg = t.getStart().minus(y.getStart()), end = t.getLength() + beg;
//        for (int i = 0; i < beg; ++i) {
//            double cur = s.get(i);
//            if (cur == 0) {
//                throw new X11Exception("Unexpected 0 in peudo-additive");
//            }
//            sa.set(i, y.get(i) / cur);
//        }
//        for (int i = beg; i < end; ++i) {
//            sa.set(i, y.get(i) - t.get(i - beg) * (s.get(i) - 1));
//        }
//        for (int i = end; i < sa.getLength(); ++i) {
//            double cur = s.get(i);
//            if (cur == 0) {
//                throw new X11Exception("Unexpected 0 in peudo-additive");
//            }
//            sa.set(i, y.get(i) / cur);
//        }
//        return sa;
//    }
    private X13Finals finals(DecompositionMode mode, X13Preadjustment astep, X11Results x11) {
        // add preadjustment
        TsData a1 = astep.getA1();
        TsData a1a = astep.getA1a();
        TsData a1b = astep.getA1b();
        TsData a8t = astep.getA8t();
        TsData a8i = astep.getA8i();
        TsData a8s = astep.getA8s();

        TsData d10 = x11.getD10();
        TsData d11 = x11.getD11();
        TsData d12 = x11.getD12();
        TsData d13 = x11.getD13();

        X13Finals.Builder decomp = X13Finals.builder();

        TsDomain bd = a1b == null ? null : a1b.getDomain();
        TsDomain fd = a1a == null ? null : a1a.getDomain();
        TsDomain d = a1.getDomain();
        // add ps to d10
//
        TsData a6 = astep.getA6(), a7 = astep.getA7();
        TsData a9cal = astep.getA9cal();
        TsData d18 = invOp(mode, a6, a7);
        d18 = invOp(mode, d18, a9cal);
        TsData d10c = invOp(mode, d10, a8s);
        TsData d16 = invOp(mode, d10c, d18);
        // add pt, pi to d11
        TsData d11c = invOp(mode, d11, a8t);
        d11c = invOp(mode, d11c, a8i);
        //   d11c = toolkit.getContext().invOp(d11c, a8s);
        TsData a9sa = astep.getA9sa();
        d11c = invOp(mode, d11c, a9sa);
        TsData d12c = invOp(mode, d12, a8t);
        TsData d13c = invOp(mode, d13, a8i);
        if (fd != null) {
            decomp.d11a(TsData.fitToDomain(d11c, fd));
            decomp.d12a(TsData.fitToDomain(d12c, fd));
            decomp.d16a(TsData.fitToDomain(d16, fd));
            decomp.d18a(TsData.fitToDomain(d18, fd));
        }
        if (bd != null) {
            decomp.d11b(TsData.fitToDomain(d11c, bd));
            decomp.d12b(TsData.fitToDomain(d12c, bd));
            decomp.d16b(TsData.fitToDomain(d16, bd));
            decomp.d18b(TsData.fitToDomain(d18, bd));
        }
        d11c = TsData.fitToDomain(d11c, d);
        d12c = TsData.fitToDomain(d12c, d);
        d16 = TsData.fitToDomain(d16, d);
        d18 = TsData.fitToDomain(d18, d);
        d13c = TsData.fitToDomain(d13c, d);
        decomp.d11final(d11c);
        decomp.d12final(d12c);
        decomp.d13final(d13c);
        decomp.d16(d16);
        decomp.d18(d18);

        // remove pre-specified outliers
        TsData a1c = op(mode, a1, a8i);
        d11c = op(mode, d11c, a8i);

        TsData c17 = TsData.fitToDomain(x11.getC17(), d);

        TsData tmp = op(mode, a1, d13c);
        TsData e1 = correct(a1c, c17, tmp);
        TsData e2 = correct(d11c, c17, d12);
        TsData e3 = correct(TsData.fitToDomain(d13, d), c17, mean(mode));
        TsData e11 = correct(d11c, c17, invOp(mode, d12, op(mode, a1c, e1)));

        decomp.e1(e1);
        decomp.e2(e2);
        decomp.e3(e3);
        decomp.e11(e11);

        return decomp.build();

    }

}
