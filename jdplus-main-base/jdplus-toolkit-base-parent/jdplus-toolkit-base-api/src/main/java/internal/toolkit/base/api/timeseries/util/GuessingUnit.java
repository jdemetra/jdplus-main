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

import jdplus.toolkit.base.api.timeseries.TsUnit;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

    YEAR(ChronoUnit.YEARS, o -> o, 2),
    MONTH(ChronoUnit.MONTHS, o -> o, 2),
    WEEK_MONDAY(ChronoUnit.WEEKS, next(MONDAY), 3),
    DAY(ChronoUnit.DAYS, o -> o, 2),
    HOUR(ChronoUnit.HOURS, o -> o, 2),
    MINUTE(ChronoUnit.MINUTES, o -> o, 2),
    SECOND(ChronoUnit.SECONDS, o -> o, 2);

    private final ChronoUnit unit;
    private final TemporalAdjuster adjuster;
    private final int minimumObsCount;

    public TsUnit getTsUnit() {
        return TsUnit.of(1, unit);
    }

    TsUnit fillPeriodIds(ObsSeq obs, int[] ids, LocalDateTime epoch) throws ArithmeticException {
        if (obs.size() < minimumObsCount) {
            return null;
        }
        int minGap = Integer.MAX_VALUE;
        TsUnit baseUnit = TsUnit.of(1, unit);
        LocalDateTime adjustedEpoch = epoch.with(adjuster);
        ids[0] = obs.getIntPeriodIdAt(0, adjustedEpoch, baseUnit);
        for (int i = 1; i < ids.length; ++i) {
            ids[i] = obs.getIntPeriodIdAt(i, adjustedEpoch, baseUnit);
            int gap = ids[i] - ids[i - 1];
            if (gap == 0) {
                return null;
            }
            if (gap < minGap) {
                minGap = gap;
            }
        }
        if (minGap > 1) {
            for (int i = 0; i < ids.length; ++i) {
                ids[i] /= minGap;
            }
            return TsUnit.of(minGap, unit);
        }
        return baseUnit;
    }
}
