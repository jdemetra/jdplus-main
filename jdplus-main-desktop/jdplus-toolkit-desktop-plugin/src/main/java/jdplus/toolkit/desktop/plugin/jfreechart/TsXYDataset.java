/*
 * Copyright 2018 National Bank of Belgium
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
package jdplus.toolkit.desktop.plugin.jfreechart;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.AbstractSeriesDataset;
import org.jfree.data.xy.IntervalXYDataset;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.StreamSupport;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TsXYDataset extends AbstractSeriesDataset implements IntervalXYDataset {

    @Deprecated
    @StaticFactoryMethod
    public static @NonNull TsXYDataset of(@NonNull List<Ts> list) {
        return ofTs(list);
    }

    @StaticFactoryMethod
    public static @NonNull TsXYDataset ofTs(@NonNull Iterable<Ts> list) {
        return new TsXYDataset(Series.allOf(list, ts -> ts.getMoniker().toString(), Ts::getData));
    }

    @StaticFactoryMethod
    public static <KEY extends Comparable<?>> @NonNull TsXYDataset ofTsData(@NonNull Map<KEY, TsData> map) {
        return new TsXYDataset(Series.allOf(map.entrySet(), Map.Entry::getKey, Map.Entry::getValue));
    }

    @lombok.NonNull
    private final Series[] delegate;

    @Override
    public Number getStartX(int series, int item) {
        return getStartXValue(series, item);
    }

    @Override
    public Number getEndX(int series, int item) {
        return getEndXValue(series, item);
    }

    @Override
    public Number getStartY(int series, int item) {
        return getEndYValue(series, item);
    }

    @Override
    public double getStartYValue(int series, int item) {
        return getYValue(series, item);
    }

    @Override
    public Number getEndY(int series, int item) {
        return getEndYValue(series, item);
    }

    @Override
    public double getEndYValue(int series, int item) {
        return getYValue(series, item);
    }

    @Override
    public DomainOrder getDomainOrder() {
        return DomainOrder.NONE;
    }

    @Override
    public Number getX(int series, int item) {
        return getXValue(series, item);
    }

    @Override
    public Number getY(int series, int item) {
        return getYValue(series, item);
    }

    @Override
    public int getSeriesCount() {
        return delegate.length;
    }

    @Override
    public Comparable<?> getSeriesKey(int series) {
        return delegate[series].key();
    }

    @Override
    public double getStartXValue(int series, int item) {
        return delegate[series].periods[item];
    }

    @Override
    public double getEndXValue(int series, int item) {
        // FIXME: TsPeriod#end() is upper bound excluded but IntervalXYDataset#getEndXValue(int,int) is upper bound included !
        return delegate[series].periods[item + 1];
    }

    @Override
    public int getItemCount(int series) {
        return delegate[series].values().length();
    }

    @Override
    public double getXValue(int series, int item) {
        // FIXME: use middle instead of start ?
        return delegate[series].periods[item];
    }

    @Override
    public double getYValue(int series, int item) {
        return delegate[series].values().get(item);
    }

    private record Series(Comparable<?> key, double[] periods, DoubleSeq values) {

        static <X> Series[] allOf(@NonNull Iterable<X> delegate, @NonNull Function<X, Comparable<?>> toKey, @NonNull Function<X, TsData> toData) {
            ZoneId zoneId = ZoneId.systemDefault();
            return StreamSupport.stream(delegate.spliterator(), false)
                    .map(item -> Series.of(toKey.apply(item), toData.apply(item), zoneId))
                    .toArray(Series[]::new);
        }

        static Series of(Comparable<?> key, TsData data, ZoneId zoneId) {
            return new Series(key, computePeriodsAsDoubles(data, zoneId), data.getValues());
        }

        private static double[] computePeriodsAsDoubles(TsData data, ZoneId zoneId) {
            TsUnit unit = data.getTsUnit();
            ZonedDateTime start = data.getStart().start().atZone(zoneId);
            double[] result = new double[data.length() + 1];
            for (int j = 0; j < result.length; j++) {
                result[j] = toEpochMilli(j, start, unit);
            }
            return result;
        }

        private static long toEpochMilli(int index, ZonedDateTime start, TsUnit unit) {
            return start
                    .plus(index * unit.getAmount(), unit.getChronoUnit())
                    .toInstant()
                    .toEpochMilli();
        }
    }
}
