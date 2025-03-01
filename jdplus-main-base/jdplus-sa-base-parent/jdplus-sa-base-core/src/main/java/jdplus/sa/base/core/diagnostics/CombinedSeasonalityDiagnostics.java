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

import jdplus.toolkit.base.api.processing.ProcQuality;
import java.util.Collections;
import java.util.List;
import jdplus.sa.base.core.tests.CombinedSeasonality;
import jdplus.toolkit.base.api.processing.Diagnostics;

/**
 *
 * @author PALATEJ
 */
public class CombinedSeasonalityDiagnostics implements Diagnostics {

    private CombinedSeasonality sa, saLast, irr, irrLast;
    private boolean strict;

    public static CombinedSeasonalityDiagnostics of(CombinedSeasonalityDiagnosticsConfiguration config, GenericSaTests data) {
        if (data == null) {
            return null;
        }
        try {
            CombinedSeasonalityDiagnostics diag = new CombinedSeasonalityDiagnostics();
            CombinedSeasonalityTests cs = data.combinedSeasonalityTests();
            if (cs == null) {
                return null;
            }
            if (config.isOnSa()) {
                diag.sa = cs.saTest(false);
            }
            if (config.isOnLastSa()) {
                diag.saLast = cs.saTest(true);
            }
            if (config.isOnI()) {
                diag.irr = cs.irrTest(false);
            }
            if (config.isOnLastI()) {
                diag.irrLast = cs.irrTest(true);
            }
            diag.strict = config.isStrict();
            return diag;
        } catch (Exception ex) {
            return null;
        }

    }

    @Override
    public String getName() {
        return CombinedSeasonalityDiagnosticsFactory.NAME;
    }

    @Override
    public List<String> getTests() {
        return CombinedSeasonalityDiagnosticsFactory.ALL;
    }

    @Override
    public ProcQuality getDiagnostic(String test) {
        switch (test) {
            case CombinedSeasonalityDiagnosticsFactory.SA -> {
                return quality(sa);
            }
            case CombinedSeasonalityDiagnosticsFactory.SA_LAST -> {
                return quality(saLast);
            }
            case CombinedSeasonalityDiagnosticsFactory.IRR -> {
                return quality(irr);
            }
            case CombinedSeasonalityDiagnosticsFactory.IRR_LAST -> {
                return quality(irrLast);
            }

            default ->
                throw new IllegalArgumentException(test);
        }
    }

    @Override
    public double getValue(String test) {
        return Double.NaN;
    }

    private ProcQuality quality(CombinedSeasonality q) {
        if (q == null) {
            return ProcQuality.Undefined;
        } else {
            return switch (q.getSummary()) {
                case None ->
                    ProcQuality.Good;
                case ProbablyNone ->
                    ProcQuality.Uncertain;
                default ->
                    strict ? ProcQuality.Severe : ProcQuality.Bad;
            };
        }
    }

    @Override
    public List<String> getWarnings() {
        return Collections.emptyList();
    }
}
