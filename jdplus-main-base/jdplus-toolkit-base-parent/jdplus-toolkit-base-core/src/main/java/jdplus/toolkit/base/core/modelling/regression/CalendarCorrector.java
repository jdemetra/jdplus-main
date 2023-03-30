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
package jdplus.toolkit.base.core.modelling.regression;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.calendars.Holiday;
import jdplus.toolkit.base.api.timeseries.regression.HolidaysCorrectedTradingDays;
import java.time.DayOfWeek;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.timeseries.calendars.HolidaysUtility;

/**
 *
 * @author palatej
 */
class CalendarCorrector implements HolidaysCorrectedTradingDays.HolidaysCorrector {
    
    final Holiday[] holidays;
    final boolean meanCorrection;
    final DayOfWeek hol;
    
    CalendarCorrector(final Holiday[] holidays, final boolean meanCorrection, final DayOfWeek hol) {
        this.holidays = holidays;
        this.meanCorrection=meanCorrection;
        this.hol = hol;
    }

   @Override
    public Matrix rawCorrection(TsDomain domain) {
        int phol = hol.getValue() - 1;
        Matrix C = HolidaysUtility.holidays(holidays, domain);
        FastMatrix Cc = FastMatrix.of(C);
        // we put in the hpos column the sum of all the other days
        // and we change the sign of the other days
        DataBlock chol = Cc.column(phol);
        chol.set(0);
        for (int i = 0; i < 7; ++i) {
            if (i != phol) {
                DataBlock cur = Cc.column(i);
                chol.add(cur);
                cur.chs();
            }
        }
        return Cc;
    }
    
    /**
     * C(i,t) if meanCorrection is false, C(i,t)-mean C(i) otherwise
     *
     * @param domain
     * @return
     */
    @Override
    public Matrix holidaysCorrection(TsDomain domain) {
        int phol = hol.getValue() - 1;
        Matrix C = HolidaysUtility.holidays(holidays, domain);
        FastMatrix Cc = FastMatrix.of(C);
        if (meanCorrection) {
            Matrix LT = HolidaysUtility.longTermMean(holidays, domain);
            for (int i=0; i<Cc.getColumnsCount(); ++i){
                Cc.column(i).sub(LT.column(i));
            }
        }
        // we put in the hpos column the sum of all the other days
        // and we change the sign of the other days
        DataBlock chol = Cc.column(phol);
        chol.set(0);
        for (int i = 0; i < 7; ++i) {
            if (i != phol) {
                DataBlock cur = Cc.column(i);
                chol.add(cur);
                cur.chs();
            }
        }
        return Cc;
    }
    
    @Override
    public DoubleSeq longTermYearlyCorrection() {
        if (holidays.length == 0) {
            return DoubleSeq.onMapping(7, i -> 0);
        }
        double[][] corr = HolidaysUtility.longTermMean(holidays, 1);
        double[] c = corr[0];
        double sum = 0;
        int ihol = hol.getValue() - 1;
        for (int i = 0; i < c.length; ++i) {
            if (i != ihol) {
                sum += c[i];
            }
        }
        for (int i = 0; i < c.length; ++i) {
            if (i != ihol) {
                c[i] = -c[i];
            } else {
                c[i] = sum;
            }
        }
        
        return DoubleSeq.of(c);
    }
    
}
