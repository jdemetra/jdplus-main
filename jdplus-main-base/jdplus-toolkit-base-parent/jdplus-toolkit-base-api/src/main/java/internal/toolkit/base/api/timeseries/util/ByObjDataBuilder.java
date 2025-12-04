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

import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.util.ObsCharacteristics;
import jdplus.toolkit.base.api.timeseries.util.ObsGathering;
import jdplus.toolkit.base.api.timeseries.util.TsDataBuilder;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;

import java.time.LocalDateTime;

/**
 * @param <DATE>
 * @author Philippe Charles
 */
public final class ByObjDataBuilder<DATE> extends ObsListDataBuilder<DATE, ByObjObsList<DATE>> {

    @StaticFactoryMethod
    public static ByObjDataBuilder<LocalDateTime> fromDateTime(ObsGathering gathering, ObsCharacteristics[] characteristics, int initialCapacity) {
        return new ByObjDataBuilder<>(
                ByObjObsList.of(isOrdered(characteristics), TsPeriod::idAt, initialCapacity, LocalDateTime::compareTo),
                gathering
        );
    }

    private ByObjDataBuilder(
            ByObjObsList<DATE> obsList,
            ObsGathering gathering) {
        super(obsList, gathering);
    }

    @Override
    public @NonNull TsDataBuilder<DATE> add(DATE date, Number value) {
        if (date != null) {
            if (value != null) {
                obsList.add(date, value.doubleValue());
            } else if (gathering.isIncludeMissingValues()) {
                obsList.add(date, Double.NaN);
            }
        }
        return this;
    }
}
