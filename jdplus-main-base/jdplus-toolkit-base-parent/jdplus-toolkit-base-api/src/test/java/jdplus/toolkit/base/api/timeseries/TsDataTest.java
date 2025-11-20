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

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Doubles;
import nbbrd.design.Demo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static jdplus.toolkit.base.api.data.AggregationType.*;
import static jdplus.toolkit.base.api.timeseries.TsUnit.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Jean Palate
 */
public class TsDataTest {

    @Demo
    public static void main(String[] args) {
        TsData ts = TsData.ofInternal(TsPeriod.yearly(2001), new double[]{3.14, 7});

        System.out.println("\n[Tests ...]");
        System.out.println(ts);
        System.out.println(ts.getDomain().toShortString());
        System.out.println(ts.getValues());

        System.out.println("\n[Test for loop]");
        for (int i = 0; i < ts.length(); i++) {
            System.out.println(ts.get(i).toShortString());
        }

        System.out.println("\n[Test #forEach()]");
        ts.forEach(x -> System.out.println(x.toShortString()));

        System.out.println("\n[Test #iterator()]");
        for (TsObs o : ts) {
            System.out.println(o.toShortString());
        }

        System.out.println("\n[Test #stream()]");
        ts.stream()
                .filter(o -> o.getPeriod().start().isAfter(LocalDate.of(2001, 1, 1).atStartOfDay()))
                .map(TsObs::toShortString)
                .forEach(System.out::println);

        System.out.println("\n[Test #forEach(k, v)]");
        ts.forEach((k, v) -> System.out.println(k.toShortString() + " -> " + v + " "));

        System.out.println("\n[Test #getPeriod() + #getValue()]");
        for (int i = 0; i < ts.length(); i++) {
            System.out.println(ts.getPeriod(i).toShortString() + " -> " + ts.getValue(i));
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
    @SuppressWarnings({"null", "DataFlowIssue"})
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
        assertThatNullPointerException().isThrownBy(() -> TsData.of(start, (DoubleSeq) null));

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
        TsData random = TsData.random(P1M, 0);
        assertThat(random.getDomain().length() == random.getValues().length()).isTrue();
        assertThat(random.getValues().allMatch(x -> x >= 100)).isTrue();
    }

    @Nested
    public class AggregationTest {

        @Test
        public void testNoRatio() {
            TsData x = dataOf("R24/2010-01/P1M");

            assertThatExceptionOfType(TsException.class)
                    .isThrownBy(() -> x.aggregate(P1D, First, true));
        }

        @Test
        public void testNoChange() {
            TsData x = dataOf("R24/2010-01/P1M");

            assertThatObject(x.aggregate(P1M, First, true))
                    .isEqualTo(x);
        }

        @Test
        public void testCompleteAfterEpoch() {
            TsData x = dataOf("R24/2010-01/P1M");

            assertThatObject(x.aggregate(P1Y, First, false))
                    .isEqualTo(x.aggregate(P1Y, First, true))
                    .returns("R2/2010/P1Y", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(1, 13), TsData::getValues);

            assertThatObject(x.aggregate(P1Y, Last, false))
                    .isEqualTo(x.aggregate(P1Y, Last, true))
                    .returns("R2/2010/P1Y", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(12, 24), TsData::getValues);

            assertThatObject(x.aggregate(P3M, First, false))
                    .isEqualTo(x.aggregate(P3M, First, true))
                    .returns("R8/2010-01/P3M", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(1, 4, 7, 10, 13, 16, 19, 22), TsData::getValues);
        }

        @Test
        public void testIncompleteAfterEpoch() {
            TsData x = dataOf("R24/2010-02/P1M");

            assertThatObject(x.aggregate(P1Y, First, false))
                    .returns("R3/2010/P1Y", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(2, 13, 25), TsData::getValues);

            assertThatObject(x.aggregate(P1Y, First, true))
                    .returns("R1/2011/P1Y", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(13), TsData::getValues);

            assertThatObject(x.aggregate(P1Y, Last, false))
                    .returns("R3/2010/P1Y", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(12, 24, 25), TsData::getValues);

            assertThatObject(x.aggregate(P1Y, Last, true))
                    .returns("R1/2011/P1Y", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(24), TsData::getValues);

            assertThatObject(x.aggregate(P3M, First, false))
                    .returns("R9/2010-01/P3M", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(2, 4, 7, 10, 13, 16, 19, 22, 25), TsData::getValues);

            assertThatObject(x.aggregate(P3M, First, true))
                    .returns("R7/2010-04/P3M", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(4, 7, 10, 13, 16, 19, 22), TsData::getValues);
        }

        @Test
        public void testPartialAfterEpoch() {
            TsData x = dataOf("R11/2010-01/P1M");

            assertThatObject(x.aggregate(P1Y, First, false))
                    .returns("R1/2010/P1Y", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(1), TsData::getValues);

            assertThatObject(x.aggregate(P1Y, First, true))
                    .returns("R0/2010/P1Y", TsDataTest::getDomainAsString)
                    .returns(Doubles.EMPTY, TsData::getValues);

            assertThatObject(TsData.ofInternal(TsPeriod.monthly(2010, 1), new double[]{1, 2, 3}).aggregate(P1Y, Sum, false))
                    .returns("R1/2010/P1Y", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(6), TsData::getValues);

            assertThatObject(TsData.ofInternal(TsPeriod.monthly(2010, 9), new double[]{1, 2, 3}).aggregate(P1Y, Sum, false))
                    .returns("R1/2010/P1Y", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(6), TsData::getValues);

            assertThatObject(TsData.ofInternal(TsPeriod.monthly(2010, 10), new double[]{1, 2, 3}).aggregate(P1Y, Sum, false))
                    .returns("R1/2010/P1Y", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(6), TsData::getValues);
        }

        @Test
        public void testCompleteBeforeEpoch() {
            TsData x = dataOf("R24/1969-01/P1M");

            assertThatObject(x.aggregate(P1Y, First, false))
                    .isEqualTo(x.aggregate(P1Y, First, true))
                    .returns("R2/1969/P1Y", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(1, 13), TsData::getValues);

            assertThatObject(x.aggregate(P1Y, Last, false))
                    .isEqualTo(x.aggregate(P1Y, Last, true))
                    .returns("R2/1969/P1Y", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(12, 24), TsData::getValues);

            assertThatObject(x.aggregate(P3M, First, false))
                    .isEqualTo(x.aggregate(P3M, First, true))
                    .returns("R8/1969-01/P3M", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(1, 4, 7, 10, 13, 16, 19, 22), TsData::getValues);
        }

        @Test
        public void testIncompleteBeforeEpoch() {
            TsData x = dataOf("R24/1969-02/P1M");

            assertThatObject(x.aggregate(P1Y, First, false))
                    .returns("R3/1969/P1Y", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(2, 13, 25), TsData::getValues);

            assertThatObject(x.aggregate(P1Y, First, true))
                    .returns("R1/1970/P1Y", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(13), TsData::getValues);

            assertThatObject(x.aggregate(P1Y, Last, false))
                    .returns("R3/1969/P1Y", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(12, 24, 25), TsData::getValues);

            assertThatObject(x.aggregate(P1Y, Last, true))
                    .returns("R1/1970/P1Y", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(24), TsData::getValues);

            assertThatObject(x.aggregate(P3M, First, false))
                    .returns("R9/1969-01/P3M", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(2, 4, 7, 10, 13, 16, 19, 22, 25), TsData::getValues);

            assertThatObject(x.aggregate(P3M, First, true))
                    .returns("R7/1969-04/P3M", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(4, 7, 10, 13, 16, 19, 22), TsData::getValues);
        }

        @Test
        public void testPartialBeforeEpoch() {
            TsData x = dataOf("R11/1969-01/P1M");

            assertThatObject(x.aggregate(P1Y, First, false))
                    .returns("R1/1969/P1Y", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(1), TsData::getValues);

            assertThatObject(x.aggregate(P1Y, First, true))
                    .returns("R0/1969/P1Y", TsDataTest::getDomainAsString)
                    .returns(Doubles.EMPTY, TsData::getValues);

            assertThatObject(TsData.ofInternal(TsPeriod.monthly(1969, 1), new double[]{1, 2, 3}).aggregate(P1Y, Sum, false))
                    .returns("R1/1969/P1Y", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(6), TsData::getValues);

            assertThatObject(TsData.ofInternal(TsPeriod.monthly(1969, 9), new double[]{1, 2, 3}).aggregate(P1Y, Sum, false))
                    .returns("R1/1969/P1Y", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(6), TsData::getValues);

            assertThatObject(TsData.ofInternal(TsPeriod.monthly(1969, 10), new double[]{1, 2, 3}).aggregate(P1Y, Sum, false))
                    .returns("R1/1969/P1Y", TsDataTest::getDomainAsString)
                    .returns(DoubleSeq.of(6), TsData::getValues);
        }

        @Test
        public void testByPosition() {
            TsData x = dataOf("R61/1970-02/P1M");
            assertThat(x.aggregateByPosition(P1Y, 3)).hasSize(5);
            assertThat(x.aggregateByPosition(P1Y, 0)).hasSize(5);
            assertThat(x.aggregateByPosition(P1Y, 11)).hasSize(5);
            assertThat(x.aggregateByPosition(P1Y, 1)).hasSize(6);
        }
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

    private static String getDomainAsString(TsData data) {
        return data.getDomain().toShortString();
    }

    private static TsData dataOf(String domain) {
        TsDomain value = TsDomain.parse(domain);
        return TsData.of(value.getStartPeriod(), DoubleSeq.onMapping(value.getLength(), i -> i + value.getStartPeriod().start().getMonthValue()));
    }
}
