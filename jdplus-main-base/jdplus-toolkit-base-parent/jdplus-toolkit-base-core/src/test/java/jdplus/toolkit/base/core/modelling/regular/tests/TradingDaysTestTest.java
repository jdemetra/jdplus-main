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
package jdplus.toolkit.base.core.modelling.regular.tests;

import tck.demetra.data.Data;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class TradingDaysTestTest {

    public TradingDaysTestTest() {
    }

    @Test
    public void testProd() {
        TsData s = Data.TS_PROD.log();
        StatisticalTest olsTest = TradingDaysTest.olsTest(s, 1, 12);
        assertTrue(olsTest.getPvalue() < 1e-3);
    }

    @Test
    public void testProdMa() {
        TsData s = Data.TS_PROD.log();
        StatisticalTest maTest = TradingDaysTest.maTest(s, true);
        assertTrue(maTest.getPvalue() < 1e-3);
    }

}
