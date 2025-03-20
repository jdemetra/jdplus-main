/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.tramoseats.base.core.tramo.internal;

import nbbrd.design.BuilderPattern;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.regarima.RegArimaUtility;
import jdplus.toolkit.base.core.regsarima.regular.IArmaModule;
import jdplus.toolkit.base.core.regsarima.regular.ModelDescription;
import jdplus.toolkit.base.core.regsarima.regular.ProcessingResult;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModelling;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.arima.SarmaOrders;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.processing.ProcessingLog;

/**
 *
 * @author Jean Palate
 */
public class ArmaModule implements IArmaModule {

    public static final String ARMA = "arma selection",
            MODELS = "selected models", DEFAULT = "default model selected (not enough obs.)",
            FAILED = "arma selection failed";

    @lombok.Value
    public static class Info {

        private final ArmaModelSelector.FastBIC[] models;
        private final SarmaOrders selection;
    }

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(ArmaModule.class)
    public static class Builder {

        private boolean wn = false, seasonal = true;

        private Builder() {
        }

        public Builder acceptWhiteNoise(boolean ok) {
            this.wn = ok;
            return this;
        }

        public Builder seasonal(boolean seasonal) {
            this.seasonal = seasonal;
            return this;
        }

        public ArmaModule build() {
            return new ArmaModule(this);
        }
    }

    // returns the first inic value that can be estimated
    static int comespa(final int freq, final int n, final int inic, final int d, final int bd, final boolean seas) {
        for (int i = inic; i > 1; --i) {
            if (checkespa(freq, n, i, d, bd, seas)) {
                return i;
            }
        }
        return 0;
    }

    static boolean checkespa(final int freq, final int nz, final int inic, final int d, final int bd, final boolean seas) {
        SarimaOrders spec = checkmaxspec(freq, inic, d, bd, seas);
        if (TramoUtility.autlar(nz, spec) < 0) {
            return false;
        }
        int n = nz - spec.getP() - spec.getPeriod() * spec.getBp();
        spec.setP(0);
        spec.setBp(0);
        return TramoUtility.autlar(n, spec) >= 0;
    }

    static SarimaOrders calcmaxspec(final int freq, final int inic, final int d,
            final int bd, final boolean seas) {
        SarimaOrders spec = new SarimaOrders(freq);
        spec.setD(d);
        if (seas) {
            spec.setBd(bd);
        }
        switch (inic) {
            case 1 -> {
                spec.setP(1);
                spec.setQ(1);
                if (seas) {
                    spec.setBp(1);
                    spec.setBq(1);
                }
            }
            case 2 -> {
                spec.setP(2);
                spec.setQ(2);
                if (seas) {
                    spec.setBp(1);
                    spec.setBq(1);
                }
            }
            case 3 -> {
                spec.setP(3);
                spec.setQ(3);
                if (seas) {
                    spec.setBp(1);
                    spec.setBq(1);
                }
            }
            case 4 -> {
                spec.setP(3);
                spec.setQ(3);
                spec.setBp(2);
                spec.setBq(2);
            }
        }
//        if (inic <= 3 && bd == 1) {
//            spec.setBp(0);
//        }
        return spec;
    }

    static int maxInic(int period) {
        return switch (period) {
            case 2 ->
                1;
            case 3 ->
                2;
            default ->
                3;
        };
    }

    static SarimaOrders checkmaxspec(final int freq, final int inic, final int d,
            final int bd, final boolean seas) {
        SarimaOrders spec = new SarimaOrders(freq);
        spec.setD(d);
        if (seas) {
            spec.setBd(bd);
        }
        switch (inic) {
            case 1 -> {
                spec.setP(1);
                spec.setQ(1);
                if (seas) {
                    if (bd == 0) {
                        spec.setBp(1);
                    }
                    spec.setBq(1);
                }
            }
            case 2 -> {
                spec.setP(2);
                spec.setQ(2);
                if (seas) {
                    if (bd == 0) {
                        spec.setBp(1);
                    }
                    spec.setBq(1);
                }
            }
            case 3 -> {
                spec.setP(3);
                spec.setQ(3);
                if (seas) {
                    if (bd == 0) {
                        spec.setBp(1);
                    }
                    spec.setBq(1);
                }
            }
            case 4 -> {
                spec.setP(3);
                spec.setQ(3);
                if (seas) {
                    spec.setBp(2);
                    spec.setBq(2);
                }
            }
        }
//        if (inic <= 3 && bd == 1) {
//            spec.setBp(0);
//        }
        return spec;
    }

    private final boolean wn, seasonal;

    private ArmaModule(Builder builder) {
        this.wn = builder.wn;
        this.seasonal = builder.seasonal;
    }

    private ArmaModelSelector createModule(SarimaOrders maxspec) {
        return ArmaModelSelector.builder()
                .acceptWhiteNoise(wn)
                .maxP(maxspec.getP())
                .maxQ(maxspec.getQ())
                .maxBp(maxspec.getBp())
                .maxBq(maxspec.getBq())
                .build();

    }

    @Override
    public ProcessingResult process(RegSarimaModelling context) {
        ProcessingLog log = context.getLog();
        log.push(ARMA);
        try {
            ModelDescription desc = context.getDescription();
            SarimaOrders curspec = desc.specification();
            int inic = comespa(curspec.getPeriod(), desc.regarima().getObservationsCount(),
                    maxInic(curspec.getPeriod()), curspec.getD(), curspec.getBd(), seasonal);
            if (inic == 0) {
                log.remark(DEFAULT);
                if (!curspec.isAirline(seasonal)) {
                    curspec.setDefault(seasonal);
                    desc.setSpecification(curspec);
                    return ProcessingResult.Changed;
                } else {
                    return ProcessingResult.Unprocessed;
                }
            }
            SarimaOrders maxspec = calcmaxspec(desc.getAnnualFrequency(),
                    inic, curspec.getD(), curspec.getBd(), seasonal);
            DoubleSeq res = RegArimaUtility.olsResiduals(desc.regarima());
            ArmaModelSelector impl = createModule(maxspec);
            SarmaOrders nspec = impl.process(res, desc.getAnnualFrequency(), maxspec.getD(), maxspec.getBd(), seasonal);
            ArmaModelSelector.FastBIC[] models = impl.gePreferredModels();
            log.info(MODELS, new Info(models, nspec));
            if (nspec.equals(curspec.doStationary())) {
                return ProcessingResult.Unchanged;
            }
            curspec = SarimaOrders.of(nspec, curspec.getD(), curspec.getBd());
            desc.setSpecification(curspec);

            return ProcessingResult.Changed;
        } catch (RuntimeException ex) {
            log.remark(FAILED);
            return ProcessingResult.Failed;
        } finally {
            log.pop();
        }
    }

    public SarimaOrders process(RegArimaModel<SarimaModel> regarima, boolean seas) {
        SarimaOrders curSpec = regarima.arima().orders();
        int inic = comespa(curSpec.getPeriod(), regarima.getObservationsCount(), maxInic(curSpec.getPeriod()), curSpec.getD(), curSpec.getBd(), seas);
        if (inic == 0) {
            curSpec.setDefault(seas);
            return curSpec;
        }
        SarimaOrders maxspec = calcmaxspec(curSpec.getPeriod(), inic, curSpec.getD(), curSpec.getBd(), seas);
        DoubleSeq res = RegArimaUtility.olsResiduals(regarima);
        ArmaModelSelector impl = createModule(maxspec);
        SarmaOrders spec = impl.process(res, curSpec.getPeriod(), curSpec.getD(), curSpec.getBd(), curSpec.getPeriod() > 1);
        if (spec == null) {
            curSpec.setDefault(seas);
            return curSpec;
        } else {
            return SarimaOrders.of(spec, curSpec.getD(), curSpec.getBd());
        }
    }
}
