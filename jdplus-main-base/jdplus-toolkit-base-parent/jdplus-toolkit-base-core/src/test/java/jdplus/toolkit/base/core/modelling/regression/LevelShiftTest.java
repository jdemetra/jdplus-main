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

import jdplus.toolkit.base.api.timeseries.regression.LevelShift;
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
public class LevelShiftTest {

    public LevelShiftTest() {
    }

    @Test
    public void testInside() {
        final int pos = 25;
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.P1D, LocalDate.now(Clock.systemDefaultZone())), 100);
        LevelShift ls = new LevelShift(days.get(pos).start(), true);
        DataBlock buffer = Regression.x(days, ls);
        assertEquals(-pos, buffer.sum(), 1e-9);
    }

    @Test
    public void testInside99() {
        final int pos = 99;
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.P1D, LocalDate.now(Clock.systemDefaultZone())), 100);
        LevelShift ls = new LevelShift(days.get(pos).start(), true);
        DataBlock buffer = Regression.x(days, ls);
        assertEquals(-pos, buffer.sum(), 1e-9);
    }

    @Test
    public void testInside0() {
        final int pos = 0;
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.P1D, LocalDate.now(Clock.systemDefaultZone())), 100);
        LevelShift ls = new LevelShift(days.get(pos).start(), true);
        DataBlock buffer = Regression.x(days, ls);
        assertEquals(-pos, buffer.sum(), 1e-9);
    }

    @Test
    public void testBefore() {
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.P1D, LocalDate.now(Clock.systemDefaultZone())), 100);
        for (int i = 1; i < 3; ++i) {
            LevelShift ls = new LevelShift(days.get(0).plus(-i).start(), true);
            DataBlock buffer = Regression.x(days, ls);
            assertEquals(0, buffer.sum(), 1e-9);
        }
    }

    @Test
    public void testAfter() {
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.P1D, LocalDate.now(Clock.systemDefaultZone())), 100);
        for (int i = 1; i < 3; ++i) {
            LevelShift ls = new LevelShift(days.get(99).plus(i).start(), true);
            DataBlock buffer = Regression.x(days, ls);
            assertEquals(100, -buffer.sum(), 1e-9);
        }
    }

    @Test
    public void testInside2() {
        final int pos = 25;
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.P1D, LocalDate.now(Clock.systemDefaultZone())), 100);
        LevelShift ls = new LevelShift(days.get(pos).start(), false);
        DataBlock buffer = Regression.x(days, ls);
        assertEquals(buffer.length() - pos, buffer.sum(), 1e-9);
    }

    @Test
    public void testInside299() {
        final int pos = 99;
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.P1D, LocalDate.now(Clock.systemDefaultZone())), 100);
        LevelShift ls = new LevelShift(days.get(pos).start(), false);
        DataBlock buffer = Regression.x(days, ls);
        assertEquals(buffer.length() - pos, buffer.sum(), 1e-9);
    }

    @Test
    public void testInside20() {
        final int pos = 0;
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.P1D, LocalDate.now(Clock.systemDefaultZone())), 100);
        LevelShift ls = new LevelShift(days.get(pos).start(), false);
        DataBlock buffer = Regression.x(days, ls);
        assertEquals(buffer.length() - pos, buffer.sum(), 1e-9);
    }

    @Test
    public void testBefore2() {
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.P1D, LocalDate.now(Clock.systemDefaultZone())), 100);
        for (int i = 1; i < 3; ++i) {
            LevelShift ls = new LevelShift(days.get(0).plus(-i).start(), false);
            DataBlock buffer = Regression.x(days, ls);
            assertEquals(buffer.length(), buffer.sum(), 1e-9);
        }
    }

    @Test
    public void testAfter2() {
        TsDomain days = TsDomain.of(TsPeriod.of(TsUnit.P1D, LocalDate.now(Clock.systemDefaultZone())), 100);
        for (int i = 1; i < 3; ++i) {
            LevelShift ls = new LevelShift(days.get(99).plus(i).start(), false);
            DataBlock buffer = Regression.x(days, ls);
            assertEquals(0, -buffer.sum(), 1e-9);
            buffer.set(0);
        }
    }

}
