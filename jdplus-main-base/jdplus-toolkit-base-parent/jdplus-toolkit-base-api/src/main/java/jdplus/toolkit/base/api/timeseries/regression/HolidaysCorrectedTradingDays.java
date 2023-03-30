/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.toolkit.base.api.timeseries.regression;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.calendars.DayClustering;
import jdplus.toolkit.base.api.timeseries.TimeSeriesDomain;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.timeseries.calendars.TradingDaysType;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.AllArgsConstructor
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public class HolidaysCorrectedTradingDays implements ITradingDaysVariable, ISystemVariable {

    public static interface HolidaysCorrector {

        Matrix rawCorrection(TsDomain domain);
        /**
         * Gets the corrections (in days) to be applied on normal calendars.For
         * each period, the sum of the correction should be 0.
         *
         * @param domain
         * @return The corrections for each period of the domain. The different
         * columns of the matrix correspond to Mondays...Sundays. The dimensions
         * of the matrix are (domain.length() x 7)
         */
        Matrix holidaysCorrection(TsDomain domain);
        
//        /**
//         * Gets the long term corrections to be applied on each period of the domain.
//         * The different columns correspond to Mondays...Sundays. The dimensions
//         * of the matrix are (domain.length() x 7)
//         * 
//         * @param domain
//         * @return 
//         */
//        Matrix longTermCorrection(TsDomain domain);

        /**
         * Gets the average annual corrections (in days) to be applied on
         * Mondays...Sundays The sum should be 0.
         * Only used for computing weights (experimental)
         *
         * @return An array of 7 elements
         */
        DoubleSeq longTermYearlyCorrection();
    }
    
    public static Builder builder(){
        return new Builder()
                .clustering(DayClustering.TD7)
                .contrast(true)
                .weighted(false);
    }

    @lombok.NonNull
    private HolidaysCorrector corrector;
    @lombok.NonNull
    private DayClustering clustering;
    private boolean contrast;
    private boolean weighted;

    @Override
    public int dim() {
        int n=clustering.getGroupsCount();
        return contrast ? n-1 : n; 
    }

    @Override
    public TradingDaysType getTradingDaysType() {
        return clustering.getType();
    }

    @Override
    public <D extends TimeSeriesDomain<?>> String description(D context) {
        return "Trading days";
    }

    @Override
    public <D extends TimeSeriesDomain<?>> String description(int idx, D context) {
        return GenericTradingDaysVariable.description(clustering, idx);
    }

}
