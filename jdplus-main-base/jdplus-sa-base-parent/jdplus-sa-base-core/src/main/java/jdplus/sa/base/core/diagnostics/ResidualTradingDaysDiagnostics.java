/*
 * Copyright 2013-2014 National Bank of Belgium
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
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jdplus.toolkit.base.api.processing.Diagnostics;

/**
 *
 * @author Jean Palate
 */
public class ResidualTradingDaysDiagnostics implements Diagnostics {
    
    @lombok.Value
    public static class Input{
        ResidualTradingDaysTests tests;
        boolean td;
    }

    private StatisticalTest f_sa, f_i;
    private double sev, bad, unc;
    private boolean td;

    public static ResidualTradingDaysDiagnostics of(ResidualTradingDaysDiagnosticsConfiguration config, Input input) {
        if (input.getTests() == null)
            return null;
        try {
            ResidualTradingDaysTests tests = input.getTests();
            ResidualTradingDaysDiagnostics test = new ResidualTradingDaysDiagnostics();
            TsData sa = tests.getSa();
            TsData i = tests.getIrr();
            if (sa == null || i == null) {
                return null;
            }
            if (i.getAnnualFrequency() != 12 && config.isMonthlyOnly())
                return null;

            test.f_sa=tests.saTest(true);
            boolean isignif = SaDiagnosticsUtility.isSignificant(i.getValues(), sa.getValues());
            if (isignif) {
                test.f_i=tests.irrTest(true);
            }
            test.sev = config.getSevereThreshold();
            test.bad = config.getBadThreshold();
            test.unc = config.getUncertainThreshold();
            test.td=input.isTd();
            return test;
        } catch (Exception err) {
            return null;
        }
    }

    public StatisticalTest FTestOnSa(){
        return f_sa;
    }

    public StatisticalTest FTestOnI(){
        return f_i;
    }

    @Override
    public String getName() {
        return ResidualTradingDaysDiagnosticsFactory.NAME;
    }

    @Override
    public List<String> getTests() {
        ArrayList<String> tests = new ArrayList<>();
        if (f_sa != null) {
            tests.add(ResidualTradingDaysDiagnosticsFactory.FTEST_SA);
        }
        if (f_i != null) {
            tests.add(ResidualTradingDaysDiagnosticsFactory.FTEST_I);
        }
        return tests;
    }

    @Override
    public ProcQuality getDiagnostic(String test) {
        return switch (test) {
            case ResidualTradingDaysDiagnosticsFactory.FTEST_SA -> quality(f_sa);
            case ResidualTradingDaysDiagnosticsFactory.FTEST_I -> quality(f_i);
            default -> ProcQuality.Undefined;
        };
    }

    @Override
    public double getValue(String test) {

        return switch (test) {
            case ResidualTradingDaysDiagnosticsFactory.FTEST_SA -> pvalue(f_sa);
            case ResidualTradingDaysDiagnosticsFactory.FTEST_I -> pvalue(f_i);
            default -> Double.NaN;
        };
    }

    @Override
    public List<String> getWarnings() {
        return Collections.emptyList();
    }

    private ProcQuality quality(StatisticalTest test) {
        if (test == null) {
            return ProcQuality.Undefined;
        }
        double pval = test.getPvalue();
        if (!td && pval < sev) {
            return ProcQuality.Severe;
        } else if (pval < bad) {
            return ProcQuality.Bad;
        } else if (pval < unc) {
            return ProcQuality.Uncertain;
        } else {
            return ProcQuality.Good;
        }
    }

    private double pvalue(StatisticalTest test) {
        return test == null ? Double.NaN : test.getPvalue();
    }


}
