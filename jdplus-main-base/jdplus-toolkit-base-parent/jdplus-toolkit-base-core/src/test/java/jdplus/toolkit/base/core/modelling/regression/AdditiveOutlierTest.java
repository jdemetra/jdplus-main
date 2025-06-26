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

import jdplus.toolkit.base.api.timeseries.regression.AdditiveOutlier;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.timeseries.TsPeriod;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class AdditiveOutlierTest {

    public AdditiveOutlierTest() {
    }

    @Test
    public void testSimple() {
        final int pos = 25;
        TsDomain domain = TsDomain.of(TsPeriod.monthly(2000, 1), 100);
        AdditiveOutlier ao = new AdditiveOutlier(domain.get(pos).start());
        FastMatrix M = Regression.matrix(domain, ao);
        DataBlock buffer = M.column(0);
        assertTrue(buffer.indexOf(x -> x != 0) == pos);
        assertTrue(buffer.lastIndexOf(x -> x == 1) == pos);
//        System.out.println(ao.getDescription(domain));
    }

    @Test
    public void testWeek() {
        final int pos = 25;
        TsDomain weeks = TsDomain.of(TsPeriod.of(TsUnit.P7D, LocalDate.now(Clock.systemDefaultZone())), 100);
        AdditiveOutlier ao = new AdditiveOutlier(weeks.get(pos).start());
        DataBlock buffer = Regression.x(weeks, ao);
        assertTrue(buffer.indexOf(x -> x != 0) == pos);
        assertTrue(buffer.lastIndexOf(x -> x == 1) == pos);
//        System.out.println(ao.getDescription(weeks));
    }

    @Test
    public void testDay() {
        final int pos = 25;
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.P1D, LocalDate.now(Clock.systemDefaultZone())), 100);
        AdditiveOutlier ao = new AdditiveOutlier(days.get(pos).start());
        DataBlock buffer = Regression.x(days, ao);
        assertTrue(buffer.indexOf(x -> x != 0) == pos);
        assertTrue(buffer.lastIndexOf(x -> x == 1) == pos);
//        System.out.println(ao.getDescription(days));
    }

    @Test
    public void testInside() {
        final int pos = 25;
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.P1D, LocalDate.now(Clock.systemDefaultZone())), 100);
        AdditiveOutlier ao = new AdditiveOutlier(days.get(pos).start());
        DataBlock buffer = Regression.x(days, ao);
        assertEquals(1, buffer.sum(), 1e-9);
    }

    @Test
    public void testBefore() {
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.P1D, LocalDate.now(Clock.systemDefaultZone())), 100);
        for (int i = 1; i < 3; ++i) {
            AdditiveOutlier ao = new AdditiveOutlier(days.get(0).plus(-i).start());
            DataBlock buffer = Regression.x(days, ao);
            assertEquals(0, buffer.sum(), 1e-9);
        }
    }

    @Test
    public void testAfter() {
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.P1D, LocalDate.now(Clock.systemDefaultZone())), 100);
        for (int i = 1; i < 3; ++i) {
            AdditiveOutlier ao = new AdditiveOutlier(days.get(99).plus(i).start());
            DataBlock buffer = Regression.x(days, ao);
            assertEquals(0, buffer.sum(), 1e-9);
            buffer.set(0);
        }
    }

}
