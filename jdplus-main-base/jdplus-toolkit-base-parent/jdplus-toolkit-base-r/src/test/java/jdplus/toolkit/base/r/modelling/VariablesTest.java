/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.r.modelling;

import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.calendars.Calendar;
import jdplus.toolkit.base.api.timeseries.calendars.EasterRelatedDay;
import jdplus.toolkit.base.api.timeseries.calendars.FixedDay;
import jdplus.toolkit.base.api.timeseries.calendars.Holiday;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class VariablesTest {

    public VariablesTest() {
    }

    @Test
    public void testLp() {
        double[] lp = Variables.leapYear(TsDomain.of(TsPeriod.monthly(2000, 1), 20), true);
        assertEquals(lp[1], .75, 1e-9);
        lp = Variables.leapYear(TsDomain.of(TsPeriod.monthly(2000, 1), 20), false);
        assertEquals(lp[1], 29 - (365.25 / 12), 1e-9);
    }

    @Test
    public void testHtd() {
        List<Holiday> holidays = new ArrayList<>();
        holidays.add(new FixedDay(7, 14));
        holidays.add(new FixedDay(5, 8));
        holidays.add(FixedDay.ALLSAINTSDAY);
        holidays.add(FixedDay.ARMISTICE);
        holidays.add(FixedDay.ASSUMPTION);
        holidays.add(FixedDay.CHRISTMAS);
        holidays.add(FixedDay.MAYDAY);
        holidays.add(FixedDay.NEWYEAR);
        holidays.add(EasterRelatedDay.ASCENSION);
        holidays.add(EasterRelatedDay.EASTERMONDAY);
        holidays.add(EasterRelatedDay.WHITMONDAY);
        Calendar calendar = new Calendar(holidays.toArray(Holiday[]::new), false);
        TsDomain mdom = TsDomain.of(TsPeriod.monthly(1980, 1), 60);
        Matrix td = Variables.htd(calendar, mdom, new int[]{1, 1, 1, 1, 2, 2, 0}, 7, true);
        for (int i = 0; i < td.getColumnsCount(); ++i) {
            assertTrue(td.column(i).count(z -> z != 0) > 0);
        }
        td = Variables.htd(calendar, mdom, new int[]{1, 1, 1, 1, 2, 2, 0}, 7, false);
        for (int i = 0; i < td.getColumnsCount(); ++i) {
            assertTrue(td.column(i).count(z -> z != 0) > 0);
        }
        calendar = new Calendar(holidays.toArray(Holiday[]::new), true);
        td = Variables.htd(calendar, mdom, new int[]{1, 1, 1, 1, 2, 2, 0}, 7, true);
        for (int i = 0; i < td.getColumnsCount(); ++i) {
            assertTrue(td.column(i).count(z -> z != 0) > 0);
        }
        td = Variables.htd(calendar, mdom, new int[]{1, 1, 1, 1, 2, 2, 0}, 7, false);
        for (int i = 0; i < td.getColumnsCount(); ++i) {
            assertTrue(td.column(i).count(z -> z != 0) > 0);
        }
    }
}
