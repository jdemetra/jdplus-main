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
package jdplus.toolkit.base.api.timeseries;

import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.util.Collections2;
import jdplus.toolkit.base.api.util.function.BiIntPredicate;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.VisibleForTesting;
import nbbrd.design.NonNegative;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TsDataTable {

    public enum DistributionType {
        FIRST, LAST, MIDDLE
    }

    public enum ValueStatus {
        PRESENT, UNUSED, BEFORE, AFTER, EMPTY
    }

    @StaticFactoryMethod
    public static @NonNull <X> TsDataTable of(@NonNull Iterable<X> col, @NonNull Function<? super X, TsData> toData) {
        TsDomain domain = computeDomain(Collections2.streamOf(col).map(toData).filter(Objects::nonNull).map(TsData::getDomain).filter(o -> !o.isEmpty()).iterator());
        return new TsDataTable(domain, Collections2.streamOf(col).map(toData).toList());
    }

    @StaticFactoryMethod
    public static @NonNull TsDataTable of(@NonNull Iterable<TsData> col) {
        return of(col, Function.identity());
    }

    private final @NonNull TsDomain domain;

    private final @NonNull List<TsData> data;

    public @NonNull Cursor cursor(@NonNull DistributionType distribution) {
        Objects.requireNonNull(distribution);
        return cursor(i -> distribution);
    }

    public @NonNull Cursor cursor(@NonNull IntFunction<DistributionType> distribution) {
        Objects.requireNonNull(distribution);
        return new Cursor(getDistributors(data, distribution));
    }

    @lombok.RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public final class Cursor {

        private final List<BiIntPredicate> distributors;

        @lombok.Getter
        private int index = -1;

        @lombok.Getter
        private int windowLength = -1;

        @lombok.Getter
        private int windowIndex = -1;

        @lombok.NonNull
        @lombok.Getter
        private ValueStatus status = ValueStatus.EMPTY;

        @lombok.Getter
        private double value = Double.NaN;

        @NonNull
        public Cursor moveTo(int period, int series) {
            if (period <= -1 || period >= domain.getLength()) {
                throw new IndexOutOfBoundsException("period");
            }
            TsData ts = data.get(series);
            if (ts.isEmpty()) {
                index = -1;
                windowLength = -1;
                windowIndex = -1;
                status = ValueStatus.EMPTY;
                value = Double.NaN;
            } else {
                TsPeriod current = domain.getStartPeriod().plus(period);
                TsPeriod valuePeriod = current.withUnit(ts.getDomain().getTsUnit());
                index = ts.getDomain().position(valuePeriod);

                if (isInBounds(ts, index)) {
                    TsPeriod start = valuePeriod.withUnit(current.getUnit());
                    TsPeriod end = valuePeriod.next().withUnit(current.getUnit());

                    windowLength = start.until(end);
                    windowIndex = start.until(current);

                    if (distributors.get(series).test(windowIndex, windowLength)) {
                        status = ValueStatus.PRESENT;
                        value = ts.getValue(index);
                    } else {
                        status = ValueStatus.UNUSED;
                        value = Double.NaN;
                    }
                } else {
                    windowLength = -1;
                    windowIndex = -1;
                    status = index < 0 ? ValueStatus.BEFORE : ValueStatus.AFTER;
                    value = Double.NaN;
                }
            }

            return this;
        }

        private boolean isInBounds(TsData ts, int index) {
            return index >= 0 && index < ts.length();
        }

        @NonNegative
        public int getPeriodCount() {
            return domain.getLength();
        }

        @NonNegative
        public int getSeriesCount() {
            return data.size();
        }
    }

    private static List<BiIntPredicate> getDistributors(List<TsData> data, IntFunction<DistributionType> distribution) {
        return IntStream
                .range(0, data.size())
                .mapToObj(distribution)
                .map(TsDataTable::getDistributor)
                .collect(Collectors.toList());
    }

    private static BiIntPredicate getDistributor(DistributionType type) {
        return switch (type) {
            case FIRST -> (pos, size) -> pos % size == 0;
            case LAST -> (pos, size) -> pos % size == size - 1;
            case MIDDLE -> (pos, size) -> pos % size == size / 2;
        };
    }

    @VisibleForTesting
    static TsDomain computeDomain(Iterator<TsDomain> domains) {
        if (!domains.hasNext()) {
            return TsDomain.DEFAULT_EMPTY;
        }

        TsDomain o = domains.next();
        TsUnit lowestUnit = o.getTsUnit();
        LocalDateTime minDate = o.start();
        LocalDateTime maxDate = o.end();
        LocalDateTime epoch = o.getStartPeriod().getEpoch();

        while (domains.hasNext()) {
            o = domains.next();

            lowestUnit = TsUnit.gcd(lowestUnit, o.getTsUnit());
            if (minDate.isAfter(o.start())) {
                minDate = o.start();
            }
            if (maxDate.isBefore(o.end())) {
                maxDate = o.end();
            }
            LocalDateTime cepoch = o.getStartPeriod().getEpoch();
            if (!cepoch.equals(epoch)) {
                epoch = TsPeriod.DEFAULT_EPOCH;
            }
        }

        TsPeriod startPeriod = TsPeriod.make(epoch, lowestUnit, minDate);
        TsPeriod endPeriod = TsPeriod.make(epoch, lowestUnit, maxDate);
        // default epoch if we can't find a common epoch. Should be improved
        // FIXME
        return TsDomain.of(startPeriod, startPeriod.until(endPeriod));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Cursor cursor = this.cursor(DistributionType.LAST);
        int pos = 0;
        for (TsPeriod period : domain) {
            builder.append(period.getStartAsShortString());

            for (int i = 0; i < data.size(); ++i) {
                cursor.moveTo(pos, i);
                builder.append('\t');
                if (cursor.getStatus() == ValueStatus.PRESENT) {
                    builder.append(cursor.getValue());
                }
            }
            ++pos;
            builder.append(("\r\n"));
        }
        return builder.toString();
    }

    public Matrix toMatrix() {
        return toMatrix(DistributionType.LAST);
    }

    public Matrix toMatrix(DistributionType type) {

        int nr = domain.length(), nc = data.size();
        Cursor cursor = this.cursor(type);
        double[] m = new double[nr * nc];
        for (int row = 0, j0 = 0; row < nr; ++row, ++j0) {
            for (int col = 0, j = j0; col < nc; ++col, j += nr) {
                cursor.moveTo(row, col);
                if (cursor.getStatus() == ValueStatus.PRESENT) {
                    m[j] = cursor.getValue();
                } else {
                    m[j] = Double.NaN;
                }

            }
        }
        return Matrix.of(m, nr, nc);
    }
}
