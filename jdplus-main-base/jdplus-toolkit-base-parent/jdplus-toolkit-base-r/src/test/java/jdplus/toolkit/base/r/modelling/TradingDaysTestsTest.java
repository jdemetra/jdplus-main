/*
 * Copyright 2024 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.toolkit.base.r.modelling;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.core.modelling.regular.tests.TradingDaysTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import tck.demetra.data.Data;

/**
 *
 * @author Jean Palate
 */
public class TradingDaysTestsTest {

    public TradingDaysTestsTest() {
    }

    @Test
    public void testProd1() {
        TsData s = Data.TS_PROD.extract(0, 120);
        StatisticalTest ft = TradingDaysTests.fTest(s, "AIRLINE", 0);
//        System.out.println(ft.getDescription());
        // 120-13-2-6=99
        StatisticalTest maTest = TradingDaysTest.maTest(s, true);
//        System.out.println(maTest.getDescription());
        assertEquals(ft.getPvalue(), maTest.getPvalue(), 1e-9);
    }

    @Test
    public void testProd2() {
        TsData s = Data.TS_PROD.extract(0, 120);
        StatisticalTest ft = TradingDaysTests.fTest(s, "R011", 0);
//        System.out.println(ft.getDescription());
        // 120-1-1-6=112
        StatisticalTest maTest = TradingDaysTest.maTest(s, false);
//        System.out.println(maTest.getDescription());
        assertEquals(ft.getPvalue(), maTest.getPvalue(), 1e-9);
    }
    
    @Test
    public void testCH() {
        TsData s = Data.TS_ABS_RETAIL2.log();
        double[] q=TradingDaysTests.canovaHansen(s,new int[]{1, 12}, "Bartlett", -1);
//        System.out.println(DoubleSeq.of(q));
    }
    
}
