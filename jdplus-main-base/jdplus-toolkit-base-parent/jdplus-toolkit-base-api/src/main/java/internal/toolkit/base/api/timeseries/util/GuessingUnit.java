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
import java.time.temporal.TemporalAdjuster;

import static java.time.DayOfWeek.MONDAY;
import static java.time.temporal.TemporalAdjusters.next;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
@lombok.Getter
public enum GuessingUnit {

    YEAR(TsUnit.P1Y, o -> o, 2),
    HALF_YEAR(TsUnit.P6M, o -> o, 6),
    QUADRI_MONTH(TsUnit.P4M, o -> o, 4),
    QUARTER(TsUnit.P3M, o -> o, 2),
    MONTH(TsUnit.P1M, o -> o, 2),
    WEEK_MONDAY(TsUnit.P7D, next(MONDAY), 3),
    DAY(TsUnit.P1D, o -> o, 2),
    HOUR(TsUnit.PT1H, o -> o, 2),
    MINUTE(TsUnit.PT1M, o -> o, 2),
    SECOND(TsUnit.PT1S, o -> o, 2);

    private final TsUnit unit;
    private final TemporalAdjuster adjuster;
    private final int minimumObsCount;

    TsPeriod atId(long id, LocalDateTime epoch) {
        return TsPeriod.builder().unit(unit).epoch(epoch.with(adjuster)).id(id).build();
    }

    TsPeriod atDate(LocalDateTime start, LocalDateTime epoch) {
        return TsPeriod.builder().unit(unit).epoch(epoch.with(adjuster)).date(start).build();
    }

    boolean fillPeriodIds(ObsSeq obs, int[] ids, LocalDateTime epoch) throws ArithmeticException {
        if (obs.size() < minimumObsCount) {
            return false;
        }
        LocalDateTime adjustedEpoch = epoch.with(adjuster);
        ids[0] = obs.getIntPeriodIdAt(0, adjustedEpoch, unit);
        for (int i = 1; i < ids.length; ++i) {
            ids[i] = obs.getIntPeriodIdAt(i, adjustedEpoch, unit);
            if (ids[i] == ids[i - 1]) {
                return false;
            }
        }
        return true;
    }
}
