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
import jdplus.toolkit.base.api.timeseries.regression.HolidaysCorrectedTradingDays;
import java.time.LocalDate;
import jdplus.toolkit.base.core.math.matrices.MatrixFactory;

/**
 *
 * @author palatej
 */
@lombok.AllArgsConstructor
class ChainedCalendarCorrector implements HolidaysCorrectedTradingDays.HolidaysCorrector {

    final HolidaysCorrectedTradingDays.HolidaysCorrector beg, end;
    final LocalDate breakDate;

    @Override
    public Matrix rawCorrection(TsDomain domain) {
        int n = domain.getLength();
        int pos = domain.indexOf(breakDate.atStartOfDay());
        if (pos > 0) {
            Matrix M1 = beg.rawCorrection(domain.range(0, pos));
            Matrix M2 = end.rawCorrection(domain.range(pos, n));
            return MatrixFactory.rowBind(M1, M2);
        } else if (pos >= -1) {
            return end.rawCorrection(domain);
        } else {
            return beg.rawCorrection(domain);
        }
    }

    @Override
    public Matrix holidaysCorrection(TsDomain domain) {
        int n = domain.getLength();
        int pos = domain.indexOf(breakDate.atStartOfDay());
        if (pos > 0) {
            Matrix M1 = beg.holidaysCorrection(domain.range(0, pos));
            Matrix M2 = end.holidaysCorrection(domain.range(pos, n));
            return MatrixFactory.rowBind(M1, M2);
        } else if (pos >= -1) {
            return end.holidaysCorrection(domain);
        } else {
            return beg.holidaysCorrection(domain);
        }
    }

    @Override
    public DoubleSeq longTermYearlyCorrection() {
        // no actual solution
        //prefer focusing on the recent part
        return end.longTermYearlyCorrection();
    }

}
