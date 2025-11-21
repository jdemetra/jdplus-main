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
import nbbrd.design.StaticFactoryMethod;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Philippe Charles
 */
sealed interface ByLongObsList extends ObsList permits ByLongObsList.Sortable, ByLongObsList.PreSorted {

    void add(long period, double value);

    long getPeriodAt(int index);

    PeriodIdFactory getPeriodIdFactory();

    @Override
    default long getPeriodIdAt(int index, LocalDateTime epoch, TsUnit unit) {
        return getPeriodIdFactory().getPeriodIdOf(epoch, unit, getPeriodAt(index));
    }

    @FunctionalInterface
    interface PeriodIdFactory {

        long getPeriodIdOf(LocalDateTime epoch, TsUnit unit, long value);
    }

    @StaticFactoryMethod
    static ByLongObsList of(boolean preSorted, PeriodIdFactory periodIdFactory, int initialCapacity) {
        return preSorted
                ? new PreSorted(periodIdFactory, initialCapacity)
                : new Sortable(periodIdFactory, initialCapacity);
    }

    final class Sortable implements ByLongObsList {

        @lombok.Getter
        private final PeriodIdFactory periodIdFactory;
        private final List<LongObs> list;
        private boolean sorted;
        private long latestPeriod;

        Sortable(PeriodIdFactory periodIdFactory, int initialCapacity) {
            this.periodIdFactory = periodIdFactory;
            this.list = new ArrayList<>(initialCapacity);
            this.sorted = true;
            this.latestPeriod = Long.MIN_VALUE;
        }

        @Override
        public void clear() {
            list.clear();
            sorted = true;
            latestPeriod = Long.MIN_VALUE;
        }

        @Override
        public void add(long period, double value) {
            list.add(new LongObs(period, value));
            sorted = sorted && latestPeriod <= period;
            latestPeriod = period;
        }

        @Override
        public int length() {
            return list.size();
        }

        @Override
        public long getPeriodAt(int index) {
            return list.get(index).period;
        }

        @Override
        public double getValueAt(int index) {
            return list.get(index).value;
        }

        @Override
        public void sortByPeriod() {
            if (!sorted) {
                list.sort(Comparator.comparingLong(obs -> obs.period));
                sorted = true;
                latestPeriod = getPeriodAt(list.size() - 1);
            }
        }

        private record LongObs(long period, double value) {
        }
    }

    final class PreSorted implements ByLongObsList {

        @lombok.Getter
        private final PeriodIdFactory periodIdFactory;
        private long[] periods;
        private double[] values;
        private int size;

        PreSorted(PeriodIdFactory periodIdFactory, int initialCapacity) {
            this.periodIdFactory = periodIdFactory;
            this.periods = new long[initialCapacity];
            this.values = new double[initialCapacity];
            this.size = 0;
        }

        private void grow() {
            int oldCapacity = periods.length;
            int newCapacity = Math.min(oldCapacity * 2, Integer.MAX_VALUE);
            periods = Arrays.copyOf(periods, newCapacity);
            values = Arrays.copyOf(values, newCapacity);
        }

        @Override
        public void clear() {
            size = 0;
        }

        @Override
        public void add(long period, double value) {
            if (size + 1 == periods.length) {
                grow();
            }
            periods[size] = period;
            values[size] = value;
            size++;
        }

        @Override
        public int length() {
            return size;
        }

        @Override
        public long getPeriodAt(int index) {
            return periods[index];
        }

        @Override
        public double getValueAt(int index) {
            return values[index];
        }

        @Override
        public void sortByPeriod() {
            // do nothing
        }

        @Override
        public double[] getValues() {
            return Arrays.copyOf(values, size);
        }
    }
}
