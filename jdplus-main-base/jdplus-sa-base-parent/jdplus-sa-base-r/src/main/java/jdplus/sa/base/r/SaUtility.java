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
package jdplus.sa.base.r;

import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.core.stats.DescriptiveStatistics;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class SaUtility {
    public double compareAnnualTotals(TsData y, TsData sa){
        
        DescriptiveStatistics ds = DescriptiveStatistics.of(y.getValues());
        double scale = ds.getRmse();
        TsData ya = y.aggregate(TsUnit.P1Y, AggregationType.Sum, true);
        TsData saa = sa.aggregate(TsUnit.P1Y, AggregationType.Sum, true);
        double maxAnnualDifference = 0;
        for (int k = 0; k < ya.length(); ++k) {
            double dcur = Math.abs(ya.getValue(k) - saa.getValue(k));
            if (dcur > maxAnnualDifference) {
                maxAnnualDifference = dcur;
            }
        }
        return maxAnnualDifference / (y.getAnnualFrequency() * scale);
    }
}
