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
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author palatej
 */
@lombok.AllArgsConstructor
class CompositeCalendarCorrector implements HolidaysCorrectedTradingDays.HolidaysCorrector {

    final HolidaysCorrectedTradingDays.HolidaysCorrector[] correctors;
    final double[] weights;

    @Override
    public Matrix rawCorrection(TsDomain domain) {
        FastMatrix M = FastMatrix.of(correctors[0].rawCorrection(domain));
        M.mul(weights[0]);
        for (int i = 1; i < correctors.length; ++i) {
            FastMatrix cur = FastMatrix.of(correctors[i].rawCorrection(domain));
            M.addAY(weights[i], cur);
        }
        return M;
    }

    @Override
    public Matrix holidaysCorrection(TsDomain domain) {
        FastMatrix M = FastMatrix.of(correctors[0].holidaysCorrection(domain));
        M.mul(weights[0]);
        for (int i = 1; i < correctors.length; ++i) {
            FastMatrix cur = FastMatrix.of(correctors[i].holidaysCorrection(domain));
            M.addAY(weights[i], cur);
        }
        return M;
    }

    @Override
    public DoubleSeq longTermYearlyCorrection() {
        DataBlock all = DataBlock.make(7);
        all.setAY(weights[0], correctors[0].longTermYearlyCorrection());
        for (int i = 1; i < correctors.length; ++i) {
            all.addAY(weights[i], correctors[i].longTermYearlyCorrection());
        }
        return all;
    }

}
