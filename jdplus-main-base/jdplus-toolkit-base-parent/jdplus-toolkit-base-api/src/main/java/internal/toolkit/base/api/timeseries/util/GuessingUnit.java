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
import jdplus.toolkit.base.api.timeseries.TsUnit;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
@lombok.Getter
public enum GuessingUnit {

    YEAR(TsUnit.P1Y, TsPeriod.DEFAULT_EPOCH, 2),
    HALF_YEAR(TsUnit.P6M, TsPeriod.DEFAULT_EPOCH, 6),
    QUADRI_MONTH(TsUnit.P4M, TsPeriod.DEFAULT_EPOCH, 4),
    QUARTER(TsUnit.P3M, TsPeriod.DEFAULT_EPOCH, 2),
    MONTH(TsUnit.P1M, TsPeriod.DEFAULT_EPOCH, 2),
    WEEK_MONDAY(TsUnit.P7D, TsPeriod.DEFAULT_EPOCH.plusDays(4), 3),
    DAY(TsUnit.P1D, TsPeriod.DEFAULT_EPOCH, 2),
    HOUR(TsUnit.PT1H, TsPeriod.DEFAULT_EPOCH, 2),
    MINUTE(TsUnit.PT1M, TsPeriod.DEFAULT_EPOCH, 2),
    SECOND(TsUnit.PT1S, TsPeriod.DEFAULT_EPOCH, 2);

    private final TsUnit tsUnit;
    private final LocalDateTime reference;
    private final int minimumObsCount;

    public TsPeriod atId(long id) {
        return TsPeriod.builder().unit(tsUnit).epoch(reference).id(id).build();
    }

    public TsPeriod atDate(LocalDateTime start) {
        return TsPeriod.builder().unit(tsUnit).epoch(reference).date(start).build();
    }
}
