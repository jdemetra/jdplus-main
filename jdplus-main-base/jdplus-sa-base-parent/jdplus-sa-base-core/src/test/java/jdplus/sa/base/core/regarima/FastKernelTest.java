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
package jdplus.sa.base.core.regarima;

import tck.demetra.data.Data;
import jdplus.toolkit.base.api.modelling.regular.CalendarSpec;
import jdplus.toolkit.base.api.modelling.regular.EasterSpec;
import jdplus.toolkit.base.api.modelling.regular.ModellingSpec;
import jdplus.toolkit.base.api.modelling.regular.RegressionSpec;
import jdplus.toolkit.base.api.modelling.regular.TradingDaysSpec;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.api.timeseries.calendars.TradingDaysType;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class FastKernelTest {
    
    public FastKernelTest() {
    }
    
    @Test
    public void testProd() {
        ModellingSpec spec = ModellingSpec.FULL;
        FastKernel kernel = FastKernel.of(spec, null);
        RegSarimaModel rslt = kernel.process(Data.TS_PROD, null);
        assertTrue(rslt != null);
    }
    
    public static void main(String[] args) {
        ModellingSpec spec=ModellingSpec.FULL;
        TradingDaysSpec tradingDays = TradingDaysSpec
                .td(TradingDaysType.TD2, LengthOfPeriodType.LeapYear, true, true);
                //.automatic(LengthOfPeriodType.LeapYear, TradingDaysSpec.AutoMethod.BIC, 0.01, true);
        
        CalendarSpec cspec=CalendarSpec.builder()
                .easter(EasterSpec.DEFAULT_USED)
                .tradingDays(tradingDays)
                .build(); 
        RegressionSpec rspec=RegressionSpec.builder()
                .checkMu(true)
                .calendar(cspec)
                .build();
        
        spec=spec.toBuilder().regression(rspec)
                .build();
        
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 1000; ++i) {
            FastKernel kernel = FastKernel.of(spec, null);
            RegSarimaModel rslt = kernel.process(Data.TS_ABS_RETAIL, null);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
    
}
