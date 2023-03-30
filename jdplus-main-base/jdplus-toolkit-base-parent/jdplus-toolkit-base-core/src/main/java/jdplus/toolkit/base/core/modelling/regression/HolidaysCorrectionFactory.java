/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.modelling.regression;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import nbbrd.design.Development;
import jdplus.toolkit.base.api.timeseries.regression.HolidaysCorrectedTradingDays;
import jdplus.toolkit.base.api.timeseries.regression.HolidaysCorrectedTradingDays.HolidaysCorrector;
import jdplus.toolkit.base.api.timeseries.TimeSeriesDomain;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.calendars.Calendar;
import jdplus.toolkit.base.api.timeseries.calendars.CalendarManager;
import jdplus.toolkit.base.api.timeseries.calendars.ChainedCalendar;
import jdplus.toolkit.base.api.timeseries.calendars.CompositeCalendar;
import jdplus.toolkit.base.api.util.WeightedItem;
import java.time.LocalDate;
import jdplus.toolkit.base.api.timeseries.calendars.CalendarDefinition;
import jdplus.toolkit.base.api.timeseries.TimeSeriesInterval;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.timeseries.calendars.DayClustering;
import java.time.DayOfWeek;
import jdplus.toolkit.base.core.data.DataBlock;

/**
 * The trading days are computed as the sum of the "normal" calendar and the
 * corrections implied by the holidays (holidays are assimilated to a given day)
 * TD(i,t) = D(i, t) + C(i, t) To remove the systematic seasonal component, we
 * must compute the long term averages of each period (Jan..., Q1...) TDc(i,t) =
 * D(i,t) - mean D(i) + C(i,t) - mean C(i)
 *
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
public class HolidaysCorrectionFactory implements RegressionVariableFactory<HolidaysCorrectedTradingDays> {

    public static HolidaysCorrectionFactory FACTORY = new HolidaysCorrectionFactory();

    /**
     *
     * @param name Name of the calendar
     * @param mgr
     * @param hol The day to which the holidays correspond (holidays are
     * considered as a "hol")
     * @return
     */
    public static HolidaysCorrector corrector(String name, CalendarManager mgr, DayOfWeek hol) {
        CalendarDefinition cur = mgr.get(name);
        if (cur == null) {
            return null;
        }
        return corrector(cur, mgr, hol);
    }

    public static HolidaysCorrector corrector(CalendarDefinition cur, CalendarManager mgr, DayOfWeek hol) {
        if (cur instanceof Calendar calendar) {
            return corrector(calendar, hol);
        } else if (cur instanceof ChainedCalendar ccur) {
            HolidaysCorrector beg = corrector(ccur.getFirst(), mgr, hol);
            HolidaysCorrector end = corrector(ccur.getSecond(), mgr, hol);
            if (beg == null || end == null) {
                return null;
            }
            return corrector(beg, end, ccur.getBreakDate());
        } else if (cur instanceof CompositeCalendar ccur) {
            WeightedItem<String>[] calendars = ccur.getCalendars();
            HolidaysCorrector[] corr = new HolidaysCorrector[calendars.length];
            double[] weights = new double[calendars.length];
            for (int i = 0; i < calendars.length; ++i) {
                corr[i] = corrector(calendars[i].getItem(), mgr, hol);
                if (corr[i] == null) {
                    return null;
                }
                weights[i] = calendars[i].getWeight();
            }
            return corrector(corr, weights);
        } else {
            return null;
        }
    }

    /**
     * Usual corrections: the holidays are considered as the specified day
     *
     * @param calendar
     * @param hol
     * @return
     */
    public static HolidaysCorrector corrector(final Calendar calendar, DayOfWeek hol) {
        return new CalendarCorrector(calendar.getHolidays(), calendar.isMeanCorrection(), hol);
    }

    public static HolidaysCorrector corrector(final HolidaysCorrector beg, final HolidaysCorrector end, final LocalDate breakDate) {
        return new ChainedCalendarCorrector(beg, end, breakDate);
    }

    public static HolidaysCorrector corrector(final HolidaysCorrector[] correctors, double[] weights) {
        return new CompositeCalendarCorrector(correctors, weights);
    }

    private HolidaysCorrectionFactory() {
    }

    private static final double AVG = 1.0 / 7.0;

    private boolean fillRaw(HolidaysCorrector corrector, DayClustering clustering, TsPeriod start, FastMatrix buffer) {
        int n = buffer.getRowsCount();
        TsDomain domain = TsDomain.of(start, n);
        FastMatrix days = FastMatrix.make(n, 7);
        GenericTradingDaysFactory.fillTradingDaysMatrix(start, false, days);
        Matrix corr = corrector.rawCorrection(domain);
        for (int i = 0; i < 7; ++i) {
            days.column(i).apply(corr.column(i), (a, b) -> a + b);
        }
        GenericTradingDaysFactory.fillNoContrasts(clustering, days, buffer);
        return true;
    }


    @Override
    public boolean fill(HolidaysCorrectedTradingDays var, TsPeriod start, FastMatrix buffer) {
        return var.isContrast()
                ? fill(var.getCorrector(), var.getClustering(), var.isWeighted(), start, buffer)
                : fillRaw(var.getCorrector(), var.getClustering(), start, buffer);
 
    }

    private boolean fill(HolidaysCorrector corrector, DayClustering clustering, boolean weighted, TsPeriod start, FastMatrix buffer) {
        int n = buffer.getRowsCount();
        TsDomain domain = TsDomain.of(start, n);
        FastMatrix days = FastMatrix.make(n, 7);
        GenericTradingDaysFactory.fillTradingDaysMatrix(start, false, days);
        Matrix corr = corrector.holidaysCorrection(domain);
        for (int i = 0; i < 7; ++i) {
            DataBlock col = days.column(i);
            col.add(corr.column(i));
        }
        double[] weights = null;
        if (weighted) {
            DoubleSeq dc = corrector.longTermYearlyCorrection();
            weights = dc.toArray();
            for (int j = 0; j < weights.length; ++j) {
                weights[j] = AVG + weights[j] / 365.25;
            }
        }
        GenericTradingDaysFactory.fillContrasts(clustering, days, buffer, weights);
        return true;
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>> boolean fill(HolidaysCorrectedTradingDays var, D domain, FastMatrix buffer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
