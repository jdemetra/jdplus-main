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

    YEAR(ChronoUnit.YEARS, o -> o),
    MONTH(ChronoUnit.MONTHS, o -> o),
    WEEK_MONDAY(ChronoUnit.WEEKS, next(MONDAY)),
    DAY(ChronoUnit.DAYS, o -> o),
    HOUR(ChronoUnit.HOURS, o -> o),
    MINUTE(ChronoUnit.MINUTES, o -> o),
    SECOND(ChronoUnit.SECONDS, o -> o);

    private final ChronoUnit unit;
    private final TemporalAdjuster adjuster;

    public TsUnit getTsUnit(int unitAmount) {
        return TsUnit.of(unitAmount, unit);
    }

    public LocalDateTime getAdjustedEpoch(LocalDateTime epoch) {
        return epoch.with(adjuster);
    }

    private static final double MAGIC_RATIO_FOR_MIN_OBS = 1;

    public int getMinimumLengthForGuessing(int unitAmount) {
        // FIXME: kind of arbitrary function to ensure enough observations for guessing -> to be refined
        return Math.max(MINIMUM_OBS_COUNT, (int) (unitAmount * MAGIC_RATIO_FOR_MIN_OBS));
    }

    public int fillPeriodIds(ObsSeq obs, int[] ids, LocalDateTime epoch) throws ArithmeticException {
        if (obs.size() < getMinimumLengthForGuessing(1)) {
            return NO_UNIT_AMOUNT; // fail fast
        }
        int unitAmount = Integer.MAX_VALUE;
        TsUnit baseUnit = getTsUnit(1);
        LocalDateTime adjustedEpoch = getAdjustedEpoch(epoch);
        ids[0] = obs.getIntPeriodIdAt(0, adjustedEpoch, baseUnit);
        for (int i = 1; i < ids.length; ++i) {
            ids[i] = obs.getIntPeriodIdAt(i, adjustedEpoch, baseUnit);
            int distance = ids[i] - ids[i - 1];
            if (distance == 0) {
                return NO_UNIT_AMOUNT; // fail fast
            }
            if (distance < unitAmount) {
                unitAmount = distance;
            }
        }
        if (unitAmount > 1) {
            // rebase IDs
            final int first = ids[0];
            for (int i = 0; i < ids.length; ++i) {
                // DO NOT optimize this code!
                // This is done on purpose to avoid rounding issues
                ids[i] = (ids[i] - first) / unitAmount + first / unitAmount;
            }
        }
        return unitAmount;
    }

    public static final int NO_UNIT_AMOUNT = 0;
    public static final int MINIMUM_OBS_COUNT = 2;
}
