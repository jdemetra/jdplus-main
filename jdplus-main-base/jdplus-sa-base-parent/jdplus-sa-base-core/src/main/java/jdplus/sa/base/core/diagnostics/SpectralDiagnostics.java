/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.sa.base.core.diagnostics;

import jdplus.toolkit.base.api.information.Explorable;
import jdplus.toolkit.base.api.processing.Diagnostics;
import jdplus.toolkit.base.api.processing.ProcQuality;
import jdplus.sa.base.api.SaDictionaries;
import jdplus.toolkit.base.api.timeseries.TimeSelector;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import java.util.Collections;
import java.util.List;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.toolkit.base.core.modelling.regular.tests.SpectralAnalysis;

/**
 *
 */
public class SpectralDiagnostics implements Diagnostics {

    private boolean sorig, ssa, sirr;
    private boolean tdsa, tdirr;
    private boolean strict;

    @lombok.Value
    public static class Input {

        /**
         *
         */
        DecompositionMode mode;
        TsData y, sa;
    }

    protected static SpectralDiagnostics of(SpectralDiagnosticsConfiguration config, Input input) {
        try {
            if (input == null) {
                return null;
            }
            SpectralDiagnostics diags = new SpectralDiagnostics();
            if (diags.test(input, config.getSensibility(), config.getLength(), config.isStrict())) {
                return diags;
            } else {
                return null;
            }

        } catch (Exception ex) {
            return null;
        }
    }

    private boolean test(Input input, double sens, int len, boolean strict) {
        this.strict = strict;
        try {
            boolean r = false;
            TsData s = input.getY();
            int sfreq = s.getAnnualFrequency();
            if (input.mode.isMultiplicative()) {
                s = s.log();
            }
            s = s.delta(1);
            if (len != 0) {
                TimeSelector sel = TimeSelector.last(len * sfreq);
                s = s.select(sel);
            }
            SpectralAnalysis diag = SpectralAnalysis.test(s)
                    .sensibility(sens)
                    .arLength(sfreq == 12 ? 30 : 3 * sfreq)
                    .build();

            if (diag != null) {
                sorig = diag.hasSeasonalPeaks();
                r = true;
            }

            s = input.getSa();
            if (input.mode.isMultiplicative()) {
                s = s.log();
            }
            int del = Math.max(1, sfreq / 4);
            s = s.delta(del);
            if (len != 0) {
                TimeSelector sel = TimeSelector.last(len * sfreq);
                s = s.select(sel);
            }
            diag = SpectralAnalysis.test(s)
                    .sensibility(sens)
                    .arLength(sfreq == 12 ? 30 : 3 * sfreq)
                    .build();
            if (diag != null) {
                r = true;
                ssa = diag.hasSeasonalPeaks();
                if (sfreq == 12) {
                    tdsa = diag.hasTradingDayPeaks();
                }
            }

            if (len != 0) {
                r = true;
                TimeSelector sel = TimeSelector.last(len * sfreq);
                s = s.select(sel);
            }
            diag = SpectralAnalysis.test(s)
                    .sensibility(sens)
                    .arLength(sfreq == 12 ? 30 : 3 * sfreq)
                    .build();
            if (diag != null) {
                r = true;
                sirr = diag.hasSeasonalPeaks();
                if (sfreq == 12) {
                    tdirr = diag.hasTradingDayPeaks();
                }
            }
            return r;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public String getName() {
        return SpectralDiagnosticsFactory.NAME;
    }

    @Override
    public List<String> getTests() {
        return SpectralDiagnosticsFactory.ALL;
    }

    @Override
    public ProcQuality getDiagnostic(String test) {
        if (test.equals(SpectralDiagnosticsFactory.SEAS)) {
            if (!sirr && !ssa) {
                return ProcQuality.Good;
            } else if (sirr && ssa) {
                return strict ? ProcQuality.Severe : ProcQuality.Bad;
            } else {
                return ProcQuality.Uncertain;
            }
        }
        if (test.equals(SpectralDiagnosticsFactory.TD)) {
            if (!tdirr && !tdsa) {
                return ProcQuality.Good;
            } else if (tdirr && tdsa) {
                return strict ? ProcQuality.Severe : ProcQuality.Bad;
            } else {
                return ProcQuality.Uncertain;
            }
        }
        return ProcQuality.Undefined;
    }

    @Override
    public double getValue(String test) {
        return Double.NaN;
    }

    @Override
    public List<String> getWarnings() {
        if (!sorig) {
            return Collections.singletonList("No seasonal peak in the original differenced series");
        } else {
            return Collections.emptyList();
        }
    }
}
