/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.toolkit.base.api.timeseries.regression;

import jdplus.toolkit.base.api.timeseries.TimeSeriesDomain;
import jdplus.toolkit.base.api.timeseries.calendars.Calendar;
import jdplus.toolkit.base.api.timeseries.calendars.CalendarDefinition;
import jdplus.toolkit.base.api.timeseries.calendars.Holiday;
import jdplus.toolkit.base.api.timeseries.calendars.HolidaysOption;
import jdplus.toolkit.base.api.timeseries.calendars.TradingDaysType;
import nbbrd.design.Development;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@Development(status = Development.Status.Release)
public class HolidaysVariable implements ITradingDaysVariable {

    public static final int[] NONWORKING_WE = new int[]{6, 7}, NONWORKING_SUNDAYS = new int[]{7};

    public static HolidaysVariable of(String name, HolidaysOption holidaysOption, int[] nonworking, boolean single, ModellingContext context) {
        CalendarDefinition cdef = context.getCalendars().get(name);
        if (!(cdef instanceof Calendar)) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        Calendar c = (Calendar) cdef;
        return new HolidaysVariable(c.getHolidays(), holidaysOption, single, nonworking);
    }

    private Holiday[] holidays;
    private HolidaysOption holidaysOption;
    private boolean single;
    private int[] nonworking;

    @Override
    public int dim() {
        return single ? 1 : holidays.length;
    }
    
    @Override
    public TradingDaysType getTradingDaysType(){
        return TradingDaysType.TDuser;
    }
    

    @Override
    public <D extends TimeSeriesDomain<?>> String description(D context) {
        return "holidays";
    }

    @Override
    public <D extends TimeSeriesDomain<?>> String description(int idx, D context) {
        return single ? "holidays" : holidays[idx].display();
    }

}
