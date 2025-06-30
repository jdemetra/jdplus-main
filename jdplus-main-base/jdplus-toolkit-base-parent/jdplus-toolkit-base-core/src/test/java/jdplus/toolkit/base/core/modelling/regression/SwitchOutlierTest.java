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
package jdplus.toolkit.base.core.modelling.regression;

import jdplus.toolkit.base.api.timeseries.regression.SwitchOutlier;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.timeseries.TsPeriod;

import java.time.Clock;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class SwitchOutlierTest {

    public SwitchOutlierTest() {
    }

    @Test
    public void testData() {
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.P1D, LocalDate.now(Clock.systemDefaultZone())), 20);
        for (int i = -10; i < 30; ++i) {
            SwitchOutlier wo = new SwitchOutlier(days.get(0).plus(i).start());
            DataBlock buffer = Regression.x(days, wo);
            assertTrue(buffer.sum() <= 1.0001);
        }
    }

}
