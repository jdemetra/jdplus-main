/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.r.calendar;

import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.regression.EasterVariable;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.modelling.regression.Regression;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class CalendarsTest {
    
    public CalendarsTest() {
    }

    @Test
    public void testEaster() {
        int dur=10;
        EasterVariable easter=EasterVariable.builder()
                .duration(dur)
                .meanCorrection(EasterVariable.Correction.None)
                .build();
        
        DataBlock x = Regression.x(TsDomain.of(TsPeriod.monthly(1980, 1), 480), easter);
        assertEquals(x.sum(), 40, 1e-9);
    }
    

    public static void main(String[] arg){
        String[] easters = Calendars.easter(1900, 2050, false);
        for (int i=0; i<easters.length; ++i)
            System.out.println(easters[i]);
    }
    
}
