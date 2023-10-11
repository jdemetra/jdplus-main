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
package jdplus.toolkit.base.r.util;

import java.util.ArrayList;
import java.util.List;
import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.timeseries.TsFactory;
import jdplus.toolkit.base.api.timeseries.TsProvider;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.timeseries.util.ObsGathering;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class Providers {

    public void updateTsFactory(TsProvider... providers) {
        List<TsProvider> cproviders = new ArrayList<>(TsFactory.getDefault().getProviders().toList());
        for (TsProvider p : providers) {
            if (p != null) {
                cproviders.add(p);
            }
        }
        TsFactory.setDefault(TsFactory.of(cproviders));
    }

    public ObsGathering obsGathering(int period, String aggregationType, boolean allowPartialAggregation, boolean includeMissing) {
        if (period <= 0 && !includeMissing) {
            return ObsGathering.DEFAULT;
        }
        ObsGathering.Builder builder = ObsGathering.builder().includeMissingValues(includeMissing);
        if (period > 0) {
            builder.unit(TsUnit.ofAnnualFrequency(period))
                    .allowPartialAggregation(allowPartialAggregation)
                    .aggregationType(AggregationType.valueOf(aggregationType));
        }
        return builder.build();
    }

}
