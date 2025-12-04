/*
 * Copyright 2017 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package internal.toolkit.base.api.timeseries.util;

import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.timeseries.util.ObsCharacteristics;
import jdplus.toolkit.base.api.timeseries.util.ObsGathering;
import jdplus.toolkit.base.api.timeseries.util.TsDataBuilder;
import lombok.NonNull;

import java.util.Arrays;

/**
 * @param <DATE>
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public abstract class ObsListDataBuilder<DATE, LIST extends ObsList> implements TsDataBuilder<DATE> {

    public static final int DEFAULT_INITIAL_CAPACITY = 32;

    protected final LIST obsList;
    protected final ObsGathering gathering;

    @Override
    public @NonNull TsDataBuilder<DATE> clear() {
        obsList.clear();
        return this;
    }

    @Override
    public @NonNull TsData build() {
        if (gathering.getUnit().equals(TsUnit.UNDEFINED)) {
            return TsDataCollector.makeFromUnknownUnit(
                    obsList,
                    gathering.getEpoch()
            );
        }
        if (gathering.getAggregationType() != AggregationType.None) {
            return TsDataCollector.makeWithAggregation(
                    obsList,
                    gathering.getEpoch(),
                    gathering.getUnit(),
                    gathering.getAggregationType(),
                    gathering.isAllowPartialAggregation()
            );
        }
        return TsDataCollector.makeWithoutAggregation(
                obsList,
                gathering.getEpoch(),
                gathering.getUnit()
        );
    }

    protected static boolean isOrdered(ObsCharacteristics[] characteristics) {
        return Arrays.binarySearch(characteristics, ObsCharacteristics.ORDERED) != -1;
    }

    public static boolean isValid(ObsGathering gathering) {
        return !(gathering.getUnit().equals(TsUnit.UNDEFINED) && gathering.getAggregationType() != AggregationType.None);
    }
}
