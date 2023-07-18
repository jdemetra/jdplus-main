/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or â€“ as soon they will be approved 
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

import static jdplus.toolkit.base.api.data.AggregationType.*;
import static jdplus.toolkit.base.api.timeseries.TsUnit.*;

import jdplus.toolkit.base.api.timeseries.*;
import nbbrd.design.Demo;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static jdplus.toolkit.base.api.timeseries.TsPeriod.DEFAULT_EPOCH;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class TsDataTest {

    @Demo
    public static void main(String[] args) {
        TsData ts = TsData.ofInternal(TsPeriod.yearly(2001), new double[]{3.14, 7});

        System.out.println("\n[Tests ...]");
        System.out.println(ts.toString());
        System.out.println(ts.getDomain());
        System.out.println(ts.getValues());

        System.out.println("\n[Test for]");
        for (int i = 0; i < ts.length(); i++) {
            System.out.println(ts.get(i));
        }

        System.out.println("\n[Test forEach]");
        ts.forEach(o -> System.out.println(o));

        System.out.println("\n[Test iterator]");
        for (TsObs o : ts) {
            System.out.println(o);
        }

        System.out.println("\n[Test stream]");
        ts.stream()
                .filter(o -> o.getPeriod().start().isAfter(LocalDate.of(2001, 1, 1).atStartOfDay()))
                .forEach(System.out::println);

        System.out.println("\n[Test forEach(k, v)]");
        ts.forEach((k, v) -> System.out.println(k + ":" + v + " "));

        System.out.println("\n[Test getPeriod / getValue]");
        for (int i = 0; i < ts.length(); i++) {
            System.out.println(ts.getPeriod(i) + " -> " + ts.getValue(i));
        }

        System.out.println("\n[Test ITimeSeries.OfDouble]");
        {
            TimeSeriesData<?, ?> y = ts;
            for (int i = 0; i < y.length(); i++) {
                System.out.println(y.getPeriod(i) + " -> " + y.getValue(i));
            }
        }
    }

    @Test
    @SuppressWarnings("null")
    public void testFactories() {
        TsPeriod start = TsPeriod.yearly(2001);
        double[] values = {1, 2, 3};
        String cause = "some text";

        TsData x;

        x = TsData.empty(start, cause);
        assertThat(x.getStart()).isEqualTo(start);
        assertThat(x.getValues().toArray()).isEmpty();
        assertThat(x.getEmptyCause()).isEqualTo(cause);

        assertThatNullPointerException().isThrownBy(() -> TsData.empty(null, cause));
        assertThatNullPointerException().isThrownBy(() -> TsData.empty(start, null));

        x = TsData.ofInternal(start, values);
        assertThat(x.getStart()).isEqualTo(start);
        assertThat(x.getValues().toArray()).containsExactly(values);
        assertThat(x.getEmptyCause()).isNull();

        assertThatNullPointerException().isThrownBy(() -> TsData.ofInternal(null, values));
        assertThatNullPointerException().isThrownBy(() -> TsData.of(start, (DoubleSeq)null));

        x = TsData.ofInternal(start, values);
        assertThat(x.getStart()).isEqualTo(start);
        assertThat(x.getValues().toArray()).containsExactly(values);
        assertThat(x.getEmptyCause()).isNull();

        assertThatNullPointerException().isThrownBy(() -> TsData.ofInternal(null, values));
        assertThatNullPointerException().isThrownBy(() -> TsData.of(start, (DoubleSeq) null));

        x = TsData.ofInternal(start, values);
        assertThat(x.getStart()).isEqualTo(start);
        assertThat(x.getValues().toArray()).containsExactly(values);
        assertThat(x.getEmptyCause()).isNull();

        assertThatNullPointerException().isThrownBy(() -> TsData.ofInternal(null, values));
        assertThatNullPointerException().isThrownBy(() -> TsData.ofInternal(start, (double[]) null));
    }

    @Test
    public void testEquals() {
        assertThat(TsData.empty(TsPeriod.yearly(2001), "abc"))
                .isEqualTo(TsData.empty(TsPeriod.yearly(2001), "abc"))
                .isNotEqualTo(TsData.empty(TsPeriod.yearly(2001), "xyz"));

        assertThat(TsData.ofInternal(TsPeriod.yearly(2001), new double[]{1, 2, 3}))
                .isEqualTo(TsData.ofInternal(TsPeriod.yearly(2001), new double[]{1, 2, 3}));
    }

    @Test
    public void testRandom() {
        TsData random = TsData.random(TsUnit.MONTH, 0);
        assertThat(random.getDomain().length() == random.getValues().length()).isTrue();
        assertThat(random.getValues().allMatch(x -> x >= 100)).isTrue();
    }

    @Test
    public void testAggregationNoRatio() {
        TsData ts = monthlyTs(DEFAULT_EPOCH.plusYears(40), 24);
        assertThatExceptionOfType(TsException.class).isThrownBy(() ->ts.aggregate(DAY, First, true));
    }

    @Test
    public void testAggregationNoChange() {
        TsData ts = monthlyTs(DEFAULT_EPOCH.plusYears(40), 24);
        assertThat(ts.aggregate(MONTH, First, true)).isEqualTo(ts);
    }

    @Test
    public void testAggregationCompleteAfterEpoch() {
        TsData ts = monthlyTs(DEFAULT_EPOCH.plusYears(40), 24);

        assertThat(ts.aggregate(YEAR, First, false)).containsExactly(y(2010, 1), y(2011, 13));
        assertThat(ts.aggregate(YEAR, First, true)).containsExactly(y(2010, 1), y(2011, 13));

        assertThat(ts.aggregate(YEAR, Last, false)).containsExactly(y(2010, 12), y(2011, 24));
        assertThat(ts.aggregate(YEAR, Last, true)).containsExactly(y(2010, 12), y(2011, 24));

        assertThat(ts.aggregate(QUARTER, First, false)).startsWith(q1(2010, 1), q2(2010, 4)).hasSize(8);
        assertThat(ts.aggregate(QUARTER, First, true)).startsWith(q1(2010, 1), q2(2010, 4)).hasSize(8);
    }

    @Test
    public void testAggregationIncompleteAfterEpoch() {
        TsData ts = monthlyTs(DEFAULT_EPOCH.plusYears(40).plusMonths(1), 24);

        assertThat(ts.aggregate(YEAR, First, false)).containsExactly(y(2010, 2), y(2011, 13), y(2012, 25));
        assertThat(ts.aggregate(YEAR, First, true)).containsExactly(y(2011, 13));

        assertThat(ts.aggregate(YEAR, Last, false)).containsExactly(y(2010, 12), y(2011, 24), y(2012, 25));
        assertThat(ts.aggregate(YEAR, Last, true)).containsExactly(y(2011, 24));

        assertThat(ts.aggregate(QUARTER, First, false)).startsWith(q1(2010, 2), q2(2010, 4)).hasSize(9);
        assertThat(ts.aggregate(QUARTER, First, true)).startsWith(q2(2010, 4), q3(2010, 7)).hasSize(7);

        TsData ts11 = monthlyTs(LocalDate.of(2010, 1, 1), 11);
        assertThat(ts11.aggregate(YEAR, First, false)).containsExactly(y(2010, 1));
        assertThat(ts11.aggregate(YEAR, First, true)).isEmpty();
    }

    @Test
    public void testAggregationCompleteBeforeEpoch() {
        TsData ts = monthlyTs(DEFAULT_EPOCH.minusYears(1), 24);

        assertThat(ts.aggregate(YEAR, First, false)).containsExactly(y(1969, 1), y(1970, 13));
        assertThat(ts.aggregate(YEAR, First, true)).containsExactly(y(1969, 1), y(1970, 13));

        assertThat(ts.aggregate(YEAR, Last, false)).containsExactly(y(1969, 12), y(1970, 24));
        assertThat(ts.aggregate(YEAR, Last, true)).containsExactly(y(1969, 12), y(1970, 24));

        assertThat(ts.aggregate(QUARTER, First, false)).startsWith(q1(1969, 1), q2(1969, 4)).hasSize(8);
        assertThat(ts.aggregate(QUARTER, First, true)).startsWith(q1(1969, 1), q2(1969, 4)).hasSize(8);
    }

    @Test
    public void testAggregationIncompleteBeforeEpoch() {
        TsData ts = monthlyTs(DEFAULT_EPOCH.minusYears(1).plusMonths(1), 24);

        assertThat(ts.aggregate(YEAR, First, false)).containsExactly(y(1969, 2), y(1970, 13), y(1971, 25));
        assertThat(ts.aggregate(YEAR, First, true)).containsExactly(y(1970, 13));

        assertThat(ts.aggregate(YEAR, Last, false)).containsExactly(y(1969, 12), y(1970, 24), y(1971, 25));
        assertThat(ts.aggregate(YEAR, Last, true)).containsExactly(y(1970, 24));

        assertThat(ts.aggregate(QUARTER, First, false)).startsWith(q1(1969, 2), q2(1969, 4)).hasSize(9);
        assertThat(ts.aggregate(QUARTER, First, true)).startsWith(q2(1969, 4), q3(1969, 7)).hasSize(7);

        TsData ts11 = monthlyTs(DEFAULT_EPOCH.minusYears(1), 11);
        assertThat(ts11.aggregate(YEAR, First, false)).containsExactly(y(1969, 1));
        assertThat(ts11.aggregate(YEAR, First, true)).isEmpty();
    }
    
    @Test
    public void testAggregationByPosition() {
        TsData ts = monthlyTs(DEFAULT_EPOCH.plusMonths(1), 61);
        assertThat(ts.aggregateByPosition(YEAR, 3)).hasSize(5);
        assertThat(ts.aggregateByPosition(YEAR, 0)).hasSize(5);
        assertThat(ts.aggregateByPosition(YEAR, 11)).hasSize(5);
        assertThat(ts.aggregateByPosition(YEAR, 1)).hasSize(6);
    }

    @Test
    public void testUpdateStartAndEndIntersecting() {
        TsData start = TsData.ofInternal(TsPeriod.monthly(1999, 1), new double[]{0, 1, 2});
        TsData end = TsData.ofInternal(TsPeriod.monthly(1999, 3), new double[]{3, 4, 5});
        TsData update = TsData.update(start, end);
        assertThat(update.equals(TsData.ofInternal(TsPeriod.monthly(1999, 1), new double[]{0, 1, 3, 4, 5})));
    }

    @Test
    public void testUpdateStartAndEndIntersecting2() {
        TsData start = TsData.ofInternal(TsPeriod.monthly(1999, 3), new double[]{0, 1, 2});
        TsData end = TsData.ofInternal(TsPeriod.monthly(1999, 1), new double[]{3, 4, 5});
        TsData update = TsData.update(start, end);
        assertThat(update.equals(TsData.ofInternal(TsPeriod.monthly(1999, 1), new double[]{3, 4, 5, 1, 2})));
    }

    @Test
    public void testUpdateEndContainedInStart() {
        TsData start = TsData.ofInternal(TsPeriod.monthly(1999, 1), new double[]{0, 1, 2, 3, 4});
        TsData end = TsData.ofInternal(TsPeriod.monthly(1999, 2), new double[]{3, 4, 5});
        TsData update = TsData.update(start, end);
        assertThat(update.equals(TsData.ofInternal(TsPeriod.monthly(1999, 1), new double[]{0, 3, 4, 5, 4})));
    }

    @Test
    public void testUpdateStartContainedInEnd() {
        TsData start = TsData.ofInternal(TsPeriod.monthly(1999, 2), new double[]{0, 1, 2});
        TsData end = TsData.ofInternal(TsPeriod.monthly(1999, 1), new double[]{3, 4, 5, 6, 7});
        TsData update = TsData.update(start, end);
        assertThat(update.equals(TsData.ofInternal(TsPeriod.monthly(1999, 1), new double[]{3, 4, 5, 6, 7})));
    }

    @Test
    public void testUpdateEndAfterStartConnecting() {
        TsData start = TsData.ofInternal(TsPeriod.monthly(1999, 1), new double[]{0, 1, 2});
        TsData end = TsData.ofInternal(TsPeriod.monthly(1999, 4), new double[]{3, 4, 5});
        TsData update = TsData.update(start, end);
        assertThat(update.equals(TsData.ofInternal(TsPeriod.monthly(1999, 1), new double[]{0, 1, 2, 3, 4, 5})));
    }

    @Test
    public void testUpdateStartAfterEndConnecting() {
        TsData start = TsData.ofInternal(TsPeriod.monthly(1999, 4), new double[]{0, 1, 2});
        TsData end = TsData.ofInternal(TsPeriod.monthly(1999, 1), new double[]{3, 4, 5});
        TsData update = TsData.update(start, end);
        assertThat(update.equals(TsData.ofInternal(TsPeriod.monthly(1999, 1), new double[]{3, 4, 5, 0, 1, 2})));
    }

    @Test
    public void testUpdateEndAfterStartSeparate() {
        TsData start = TsData.ofInternal(TsPeriod.monthly(1999, 1), new double[]{0, 1, 2});
        TsData end = TsData.ofInternal(TsPeriod.monthly(1999, 5), new double[]{3, 4, 5});
        TsData update = TsData.update(start, end);
        assertThat(update.equals(TsData.ofInternal(TsPeriod.monthly(1999, 1), new double[]{0, 1, 2, Double.NaN, 3, 4, 5})));
    }

    @Test
    public void testUpdateStartAfterEndSeparate() {
        TsData start = TsData.ofInternal(TsPeriod.monthly(1999, 5), new double[]{0, 1, 2});
        TsData end = TsData.ofInternal(TsPeriod.monthly(1999, 1), new double[]{3, 4, 5});
        TsData update = TsData.update(start, end);
        assertThat(update.equals(TsData.ofInternal(TsPeriod.monthly(1999, 1), new double[]{3, 4, 5, Double.NaN, 0, 1, 2})));
    }

    @Test
    public void testUpdatStartNull() {
        TsData start = null;
        TsData end = TsData.ofInternal(TsPeriod.monthly(1999, 5), new double[]{3, 4, 5});
        TsData update = TsData.update(start, end);
        assertThat(update.equals(TsData.ofInternal(TsPeriod.monthly(1999, 5), new double[]{3, 4, 5})));
    }

    @Test
    public void testUpdateEndNull() {
        TsData start = TsData.ofInternal(TsPeriod.monthly(1999, 1), new double[]{0, 1, 2});
        TsData end = null;
        TsData update = TsData.update(start, end);
        assertThat(update.equals(TsData.ofInternal(TsPeriod.monthly(1999, 1), new double[]{0, 1, 2})));
    }

    @Test
    public void testUpdateStartAndEndDifferentFrequency() {
        TsData start = TsData.ofInternal(TsPeriod.monthly(1999, 5), new double[]{0, 1, 2});
        TsData end = TsData.ofInternal(TsPeriod.quarterly(1999, 1), new double[]{0, 1, 2});
        assertThatThrownBy(() -> TsData.update(start, end)).isInstanceOf(TsException.class).hasMessageContaining("Incompatible frequencies");
    }
    
    private static TsData monthlyTs(LocalDateTime start, int count) {
        return TsData.of(TsPeriod.of(TsUnit.MONTH, start), DoubleSeq.onMapping(count, i -> i + start.getMonthValue()));
    }

    private static TsData monthlyTs(LocalDate start, int count) {
        return TsData.of(TsPeriod.of(TsUnit.MONTH, start), DoubleSeq.onMapping(count, i -> i + start.getMonthValue()));
    }

    private static TsObs y(int year, double val) {
        return TsObs.of(TsPeriod.yearly(year), val);
    }

    private static TsObs q1(int year, double val) {
        return TsObs.of(TsPeriod.quarterly(year, 1), val);
    }

    private static TsObs q2(int year, double val) {
        return TsObs.of(TsPeriod.quarterly(year, 2), val);
    }

    private static TsObs q3(int year, double val) {
        return TsObs.of(TsPeriod.quarterly(year, 3), val);
    }
}
