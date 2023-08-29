/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.sa.base.core.movingtd;

import jdplus.sa.base.api.movingtd.TimeVaryingSpec;
import jdplus.sa.base.core.regarima.FastKernel;
import jdplus.toolkit.base.api.modelling.regular.CalendarSpec;
import jdplus.toolkit.base.api.modelling.regular.EasterSpec;
import jdplus.toolkit.base.api.modelling.regular.ModellingSpec;
import jdplus.toolkit.base.api.modelling.regular.RegressionSpec;
import jdplus.toolkit.base.api.modelling.regular.TradingDaysSpec;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.api.timeseries.calendars.TradingDaysType;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import tck.demetra.data.Data;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author palatej
 */
public class TimeVaryingEstimatorTest {

    public TimeVaryingEstimatorTest() {
    }

    @Test
    public void testProd() {
        TsData s = Data.TS_ABS_RETAIL;
        ModellingSpec spec = ModellingSpec.FULL;
        TradingDaysSpec tradingDays = TradingDaysSpec
                .td(TradingDaysType.TD7, LengthOfPeriodType.LeapYear, true, true);
        //.automatic(LengthOfPeriodType.LeapYear, TradingDaysSpec.AutoMethod.BIC, 0.01, true);

        CalendarSpec cspec = CalendarSpec.builder()
                .easter(EasterSpec.DEFAULT_USED)
                .tradingDays(tradingDays)
                .build();
        RegressionSpec rspec = RegressionSpec.builder()
                .checkMu(true)
                .calendar(cspec)
                .build();

        spec = spec.toBuilder().regression(rspec)
                .build();
        FastKernel kernel = FastKernel.of(spec, null);
        RegSarimaModel rslt = kernel.process(s, null);
        TimeVaryingSpec tdSpec = TimeVaryingSpec.builder()
                .onContrast(false)
                .reestimate(false)
                .build();
        TimeVaryingEstimator tde = new TimeVaryingEstimator(tdSpec);
        TimeVaryingCorrection q = tde.process(rslt);
//        System.out.println(q.getTdCoefficients());
        System.out.println(q.getTdEffect().getValues());
        System.out.println();
        tdSpec = TimeVaryingSpec.builder()
                .onContrast(true)
                .reestimate(false)
                .build();
        tde = new TimeVaryingEstimator(tdSpec);
        q = tde.process(rslt);
//        System.out.println(q.getTdCoefficients());
        System.out.println(q.getTdEffect().getValues());
    }

}
