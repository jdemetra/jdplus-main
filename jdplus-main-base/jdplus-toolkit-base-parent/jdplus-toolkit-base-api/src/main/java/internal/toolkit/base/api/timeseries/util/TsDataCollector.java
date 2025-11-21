/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved
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
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import lombok.NonNull;
import nbbrd.design.Development;
import nbbrd.design.StaticFactoryMethod;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.function.IntToDoubleFunction;

/**
 * A TsDataCollector collects time observations (identified by pairs of
 * date-double) to create simple time series. Time series can be created
 * following different aggregation mode or in an automatic way. See the "make"
 * method for further information
 *
 * @author Jean Palate. National Bank of Belgium
 */
@Development(status = Development.Status.Alpha)
public final class TsDataCollector {

    private TsDataCollector() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final TsData NO_DATA = TsData.empty("No data available");
    public static final TsData GUESS_SINGLE = TsData.empty("Cannot guess frequency with a single observation");
    public static final TsData GUESS_DUPLICATION = TsData.empty("Cannot guess frequency with duplicated periods");
    public static final TsData DUPLICATION_WITHOUT_AGGREGATION = TsData.empty("Duplicated observations without aggregation");
    public static final TsData ARITHMETIC_EXCEPTION = TsData.empty("Periods exceeding Integer.MAX_VALUE are not supported");
    public static final TsData NO_AGGREGATION = TsData.empty("No valid aggregation specified");
    public static final TsData NO_DATA_AFTER_AGGREGATION = TsData.empty("No data to aggregate");

    public static boolean isSupportedAggregationType(AggregationType aggregationType) {
        return !aggregationType.equals(AggregationType.None)
                && !aggregationType.equals(AggregationType.UserDefined);
    }

    @StaticFactoryMethod(TsData.class)
    public static @NonNull TsData makeWithAggregation(ObsSeq obs, LocalDateTime epoch, TsUnit unit, AggregationType aggregationType, boolean allowPartialAggregation) {
        TsData result = TsDataCollector.makeFromUnknownUnit(obs, epoch);
        if (!result.isEmpty() && unit.contains(result.getTsUnit())) {
            return result.aggregate(unit, aggregationType, !allowPartialAggregation);
        } else {
            return TsDataCollector.makeWithAggregation(obs, epoch, unit, aggregationType);
        }
    }

    @StaticFactoryMethod(TsData.class)
    public static @NonNull TsData makeWithAggregation(ObsSeq obs, LocalDateTime epoch, TsUnit unit, AggregationType aggregationType) {
        if (!isSupportedAggregationType(aggregationType)) {
            return NO_AGGREGATION;
        }

        int n = obs.size();
        if (n == 0) {
            return NO_DATA;
        }
        obs.sortByPeriod();

        double[] vals = new double[n];
        int[] ids = new int[n];
        int ncur = -1;

        try {
            switch (aggregationType) {
                case Average: {
                    int avn = 0;
                    for (int i = 0; i < n; ++i) {
                        double value = obs.getValueAt(i);
                        if (Double.isFinite(value)) {
                            int periodId = obs.getIntPeriodIdAt(i, epoch, unit);
                            if (isNewPeriod(ncur, periodId, ids)) {
                                if (ncur >= 0) {
                                    vals[ncur] /= avn;
                                }
                                vals[++ncur] = value;
                                ids[ncur] = periodId;
                                avn = 1;
                            } else {
                                vals[ncur] += value;
                                ++avn;
                            }
                        }
                    }
                    // correction pour le dernier cas
                    if (ncur >= 0) {
                        vals[ncur] /= avn;
                    }
                    break;
                }
                case Sum: {
                    for (int i = 0; i < n; ++i) {
                        double value = obs.getValueAt(i);
                        if (Double.isFinite(value)) {
                            int periodId = obs.getIntPeriodIdAt(i, epoch, unit);
                            if (isNewPeriod(ncur, periodId, ids)) {
                                vals[++ncur] = value;
                                ids[ncur] = periodId;
                            } else {
                                vals[ncur] += value;
                            }
                        }
                    }
                    break;
                }
                case First: {
                    for (int i = 0; i < n; ++i) {
                        double value = obs.getValueAt(i);
                        if (Double.isFinite(value)) {
                            int periodId = obs.getIntPeriodIdAt(i, epoch, unit);
                            if (isNewPeriod(ncur, periodId, ids)) {
                                vals[++ncur] = value;
                                ids[ncur] = periodId;
                            }
                        }
                    }
                    break;
                }
                case Last: {
                    for (int i = 0; i < n; ++i) {
                        double value = obs.getValueAt(i);
                        if (Double.isFinite(value)) {
                            int periodId = obs.getIntPeriodIdAt(i, epoch, unit);
                            if (isNewPeriod(ncur, periodId, ids)) {
                                ids[++ncur] = periodId;
                            }
                            vals[ncur] = value;
                        }
                    }
                    break;
                }
                case Max: {
                    for (int i = 0; i < n; ++i) {
                        double value = obs.getValueAt(i);
                        if (Double.isFinite(value)) {
                            int periodId = obs.getIntPeriodIdAt(i, epoch, unit);
                            if (isNewPeriod(ncur, periodId, ids)) {
                                vals[++ncur] = value;
                                ids[ncur] = periodId;
                            } else {
                                if (value > vals[ncur]) {
                                    vals[ncur] = value;
                                }
                            }
                        }
                    }
                    break;
                }
                case Min: {
                    for (int i = 0; i < n; ++i) {
                        double value = obs.getValueAt(i);
                        if (Double.isFinite(value)) {
                            int periodId = obs.getIntPeriodIdAt(i, epoch, unit);
                            if (isNewPeriod(ncur, periodId, ids)) {
                                vals[++ncur] = value;
                                ids[ncur] = periodId;
                            } else {
                                if (value < vals[ncur]) {
                                    vals[ncur] = value;
                                }
                            }
                        }
                    }
                    break;
                }
            }
        } catch (ArithmeticException ex) {
            return ARITHMETIC_EXCEPTION;
        }

        if (ncur < 0) {
            return NO_DATA_AFTER_AGGREGATION;
        }

        final int size = ncur + 1;

        final int firstId = ids[0];
        final int lastId = ids[size - 1];

        final TsPeriod start = TsPeriod.of(unit, firstId);

        // check if the series is continuous and complete.
        final int expectedSize = lastId - firstId + 1;
        if (expectedSize == size) {
            return TsData.ofInternal(start, size == n ? vals : Arrays.copyOf(vals, size));
        } else {
            return TsData.ofInternal(start, expand(size, expectedSize, ids, index -> vals[index]));
        }
    }

    private static boolean isNewPeriod(int ncur, int periodId, int[] ids) {
        return ncur < 0 || periodId != ids[ncur];
    }

    @StaticFactoryMethod(TsData.class)
    public static @NonNull TsData makeWithoutAggregation(ObsSeq obs, LocalDateTime epoch, TsUnit unit) {
        final int size = obs.size();
        if (size == 0) {
            return NO_DATA;
        }

        obs.sortByPeriod();

        final int[] ids = new int[size];
        try {
            ids[0] = obs.getIntPeriodIdAt(0, epoch, unit);
            for (int i = 1; i < size; i++) {
                ids[i] = obs.getIntPeriodIdAt(i, epoch, unit);
                if (ids[i] == ids[i - 1]) {
                    return DUPLICATION_WITHOUT_AGGREGATION;
                }
            }
        } catch (ArithmeticException ex) {
            return ARITHMETIC_EXCEPTION;
        }

        final int firstId = ids[0];
        final int lastId = ids[size - 1];

        final TsPeriod start = TsPeriod.of(unit, firstId);

        // check if the series is continuous and complete.
        final int expectedSize = lastId - firstId + 1;
        if (expectedSize == size) {
            return TsData.ofInternal(start, obs.getValues());
        } else {
            return TsData.ofInternal(start, expand(size, expectedSize, ids, obs::getValueAt));
        }
    }

    @StaticFactoryMethod(TsData.class)
    public static @NonNull TsData makeFromUnknownUnit(ObsSeq obs, LocalDateTime epoch) {
        final int size = obs.size();
        switch (size) {
            case 0:
                return NO_DATA;
            case 1:
                return GUESS_SINGLE;
        }

        obs.sortByPeriod();

        final int[] ids = new int[size];

        try {
            for (var guess : GuessingUnit.values()) {
                if (guess.fillPeriodIds(obs, ids, epoch)) {
                    final int firstId = ids[0];
                    final int lastId = ids[size - 1];

                    final TsPeriod start = guess.atId(firstId, epoch);

                    // check if the series is continuous and complete.
                    final int expectedSize = lastId - firstId + 1;
                    if (expectedSize == size) {
                        return TsData.ofInternal(start, obs.getValues());
                    } else {
                        return TsData.ofInternal(start, expand(size, expectedSize, ids, obs::getValueAt));
                    }
                }
            }
            return GUESS_DUPLICATION;
        } catch (ArithmeticException ex) {
            return ARITHMETIC_EXCEPTION;
        }
    }

    private static double[] expand(int currentSize, int expectedSize, int[] ids, IntToDoubleFunction valueFunc) {
        double[] safeArray = new double[expectedSize];
        Arrays.fill(safeArray, Double.NaN);
        safeArray[0] = valueFunc.applyAsDouble(0);
        for (int j = 1; j < currentSize; ++j) {
            safeArray[ids[j] - ids[0]] = valueFunc.applyAsDouble(j);
        }
        return safeArray;
    }
}
