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
sealed interface ByObjObsList<DATE> extends ObsList permits ByObjObsList.Sortable, ByObjObsList.PreSorted {

    void add(DATE period, double value);

    DATE getPeriodAt(int index);

    PeriodIdFactory<DATE> getPeriodIdFactory();

    @Override
    default long getPeriodIdAt(int index, LocalDateTime epoch, TsUnit unit) {
        return getPeriodIdFactory().getPeriodIdOf(epoch, unit, getPeriodAt(index));
    }

    @FunctionalInterface
    interface PeriodIdFactory<DATE> {

        long getPeriodIdOf(LocalDateTime epoch, TsUnit unit, DATE value);
    }

    @StaticFactoryMethod
    static <DATE> ByObjObsList<DATE> of(boolean preSorted, PeriodIdFactory<DATE> periodIdFactory, int initialCapacity, Comparator<DATE> comparator) {
        return preSorted
                ? new PreSorted<>(periodIdFactory, initialCapacity)
                : new Sortable<>(periodIdFactory, initialCapacity, comparator);
    }

    final class PreSorted<DATE> implements ByObjObsList<DATE> {

        @lombok.Getter
        private final PeriodIdFactory<DATE> periodIdFactory;
        private Object[] periods;
        private double[] values;
        private int size;

        PreSorted(PeriodIdFactory<DATE> periodIdFactory, int initialCapacity) {
            this.periodIdFactory = periodIdFactory;
            this.periods = new Object[initialCapacity];
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
        public void add(DATE period, double value) {
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
        public void sortByPeriod() {
            // do nothing
        }

        @Override
        public DATE getPeriodAt(int index) {
            return (DATE) periods[index];
        }

        @Override
        public double getValueAt(int index) throws IndexOutOfBoundsException {
            return values[index];
        }

        @Override
        public double[] getValues() {
            return Arrays.copyOf(values, size);
        }
    }

    final class Sortable<DATE> implements ByObjObsList<DATE> {

        @lombok.Getter
        private final PeriodIdFactory<DATE> periodIdFactory;
        private final Comparator<DATE> comparator;
        private final List<Obs<DATE>> list;
        private boolean sorted;
        private DATE latestPeriod;

        Sortable(PeriodIdFactory<DATE> periodIdFactory, int initialCapacity, Comparator<DATE> comparator) {
            this.periodIdFactory = periodIdFactory;
            this.comparator = comparator;
            this.list = new ArrayList<>(initialCapacity);
            this.sorted = true;
            this.latestPeriod = null;
        }

        @Override
        public void clear() {
            list.clear();
            sorted = true;
            latestPeriod = null;
        }

        @Override
        public void add(DATE period, double value) {
            list.add(new Obs<>(period, value));
            sorted = sorted && (latestPeriod == null || comparator.compare(latestPeriod, period) <= 0);
            latestPeriod = period;
        }

        @Override
        public int length() {
            return list.size();
        }

        @Override
        public DATE getPeriodAt(int index) {
            return list.get(index).period;
        }

        @Override
        public double getValueAt(int index) {
            return list.get(index).value;
        }

        @Override
        public void sortByPeriod() {
            if (!sorted) {
                list.sort(Comparator.comparing(o -> o.period, comparator));
                sorted = true;
                latestPeriod = getPeriodAt(list.size() - 1);
            }
        }

        private record Obs<T>(T period, double value) {
        }
    }
}
