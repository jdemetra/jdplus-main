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
package jdplus.toolkit.base.r.timeseries;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.util.ObsCharacteristics;
import jdplus.toolkit.base.api.timeseries.util.ObsGathering;
import jdplus.toolkit.base.api.timeseries.util.TsDataBuilder;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class TsDataCollector {

    public TsData of(double[] values, String[] dates) {
        TsDataBuilder<LocalDate> cur = TsDataBuilder.byDate(ObsGathering.DEFAULT, ObsCharacteristics.ORDERED);
        for (int j = 0; j < dates.length; ++j) {
            double val = values[j];
            LocalDate jdate = LocalDate.parse(dates[j], DateTimeFormatter.ISO_DATE);
            cur.add(jdate, val);
        }
        return cur.build();
    }

}
