package internal.toolkit.base.api.timeseries.util;

import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import nbbrd.design.MightBePromoted;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static internal.toolkit.base.api.timeseries.util.TsDataCollector.*;
import static java.lang.Double.NaN;
import static java.time.DayOfWeek.MONDAY;
import static java.time.LocalDateTime.parse;
import static java.time.temporal.TemporalAdjusters.next;
import static jdplus.toolkit.base.api.data.AggregationType.*;
import static jdplus.toolkit.base.api.timeseries.TsPeriod.DEFAULT_EPOCH;
import static jdplus.toolkit.base.api.timeseries.TsUnit.*;
import static org.assertj.core.api.Assertions.assertThat;

public class TsDataCollectorTest {

    @Test
    public void testMakeWithAggregation() {
        var obs = new ByObjObsList.PreSorted<LocalDateTime>(TsPeriod::idAt, 32);

        obs.add(parse("2020-01-01T00:00:00"), NaN);
        obs.add(parse("2020-01-02T00:00:00"), 10);
        obs.add(parse("2020-01-10T00:00:00"), 1);
        obs.add(parse("2020-01-12T00:00:00"), 12);
        obs.add(parse("2020-01-15T00:00:00"), 8);
        obs.add(parse("2020-02-01T00:00:00"), NaN);
        obs.add(parse("2020-03-01T00:00:00"), 30);
        obs.add(parse("2020-03-02T00:00:00"), NaN);

        assertThat(makeWithAggregation(obs, DEFAULT_EPOCH, P1M, None))
                .isEqualTo(NO_AGGREGATION);

        assertThat(makeWithAggregation(obs, DEFAULT_EPOCH, P1M, Sum))
                .isEqualTo(dataOf("2020-01/P1M", 31.0, NaN, 30.0));

        assertThat(makeWithAggregation(obs, DEFAULT_EPOCH, P1M, Average))
                .isEqualTo(dataOf("2020-01/P1M", 7.75, NaN, 30.0));

        assertThat(makeWithAggregation(obs, DEFAULT_EPOCH, P1M, First))
                .isEqualTo(dataOf("2020-01/P1M", 10.0, NaN, 30.0));

        assertThat(makeWithAggregation(obs, DEFAULT_EPOCH, P1M, Last))
                .isEqualTo(dataOf("2020-01/P1M", 8.0, NaN, 30.0));

        assertThat(makeWithAggregation(obs, DEFAULT_EPOCH, P1M, Min))
                .isEqualTo(dataOf("2020-01/P1M", 1.0, NaN, 30.0));

        assertThat(makeWithAggregation(obs, DEFAULT_EPOCH, P1M, Max))
                .isEqualTo(dataOf("2020-01/P1M", 12.0, NaN, 30.0));

        assertThat(makeWithAggregation(obs, DEFAULT_EPOCH, P1M, UserDefined))
                .isEqualTo(NO_AGGREGATION);

        assertThat(makeWithAggregation(obs, DEFAULT_EPOCH, P3M, Sum))
                .isEqualTo(dataOf("2020-01/P3M", 61.0));

        obs.clear();
        obs.add(parse("2020-01-02T00:00:00"), 10);
        for (var aggregationType : SUPPORTED_TYPES) {
            assertThat(makeWithAggregation(obs, DEFAULT_EPOCH, P1M, aggregationType))
                    .isEqualTo(dataOf("2020-01/P1M", 10.0));
        }
        for (var aggregationType : UNSUPPORTED_TYPES) {
            assertThat(makeWithAggregation(obs, DEFAULT_EPOCH, P1M, aggregationType))
                    .isEqualTo(NO_AGGREGATION);
        }

        obs.clear();
        obs.add(parse("2020-01-02T00:00:00"), NaN);
        for (var aggregationType : SUPPORTED_TYPES) {
            assertThat(makeWithAggregation(obs, DEFAULT_EPOCH, P1M, aggregationType))
                    .isEqualTo(NO_DATA_AFTER_AGGREGATION);
        }
        for (var aggregationType : UNSUPPORTED_TYPES) {
            assertThat(makeWithAggregation(obs, DEFAULT_EPOCH, P1M, aggregationType))
                    .isEqualTo(NO_AGGREGATION);
        }

        obs.clear();
        for (var aggregationType : SUPPORTED_TYPES) {
            assertThat(makeWithAggregation(obs, DEFAULT_EPOCH, P1M, aggregationType))
                    .isEqualTo(NO_DATA);
        }
        for (var aggregationType : UNSUPPORTED_TYPES) {
            assertThat(makeWithAggregation(obs, DEFAULT_EPOCH, P1M, aggregationType))
                    .isEqualTo(NO_AGGREGATION);
        }


        obs.clear();
        obs.add(MAX_INT_PERIOD.start().minusSeconds(1), 0.0);
        obs.add(MAX_INT_PERIOD.start(), 1.1);
        for (var aggregationType : SUPPORTED_TYPES) {
            assertThat(makeWithAggregation(obs, DEFAULT_EPOCH, PT1S, aggregationType))
                    .describedAs("Periods up to Integer.MAX_VALUE are supported")
                    .isEqualTo(dataOf("2038-01-19T03:14:06/PT1S", 0.0, 1.1));
        }
        for (var aggregationType : UNSUPPORTED_TYPES) {
            assertThat(makeWithAggregation(obs, DEFAULT_EPOCH, P1M, aggregationType))
                    .isEqualTo(NO_AGGREGATION);
        }

        obs.clear();
        obs.add(MAX_INT_PERIOD.start(), 1.1);
        obs.add(MAX_INT_PERIOD.start().plusSeconds(1), 2.2);
        for (var aggregationType : SUPPORTED_TYPES) {
            assertThat(makeWithAggregation(obs, DEFAULT_EPOCH, PT1S, aggregationType))
                    .describedAs("Periods exceeding Integer.MAX_VALUE are not supported")
                    .isEqualTo(ARITHMETIC_EXCEPTION);
        }
        for (var aggregationType : UNSUPPORTED_TYPES) {
            assertThat(makeWithAggregation(obs, DEFAULT_EPOCH, P1M, aggregationType))
                    .isEqualTo(NO_AGGREGATION);
        }
    }

    @Test
    public void testMakeWithoutAggregation() {
        var obs = new ByObjObsList.PreSorted<LocalDateTime>(TsPeriod::idAt, 32);

        obs.clear();
        obs.add(parse("2020-01-01T00:00:00"), NaN);
        obs.add(parse("2020-01-02T00:00:00"), 10);
        obs.add(parse("2020-01-10T00:00:00"), 1);
        obs.add(parse("2020-01-12T00:00:00"), 12);
        obs.add(parse("2020-01-15T00:00:00"), 8);
        obs.add(parse("2020-02-01T00:00:00"), NaN);
        obs.add(parse("2020-03-01T00:00:00"), 30);
        obs.add(parse("2020-03-02T00:00:00"), NaN);
        assertThat(makeWithoutAggregation(obs, DEFAULT_EPOCH, P1M))
                .describedAs("No aggregation: multiple values for the same period are not allowed")
                .isEqualTo(DUPLICATION_WITHOUT_AGGREGATION);

        obs.clear();
        obs.add(parse("2020-01-02T00:00:00"), 10);
        assertThat(makeWithoutAggregation(obs, DEFAULT_EPOCH, P1M))
                .describedAs("Single observation")
                .isEqualTo(dataOf("2020-01/P1M", 10.0));

        obs.clear();
        obs.add(parse("2020-01-02T00:00:00"), NaN);
        assertThat(makeWithoutAggregation(obs, DEFAULT_EPOCH, P1M))
                .describedAs("Single observation with NaN value")
                .isEqualTo(dataOf("2020-01/P1M", NaN));

        obs.clear();
        obs.add(parse("2020-01-02T00:00:00"), 10);
        obs.add(parse("2020-02-01T00:00:00"), NaN);
        obs.add(parse("2020-04-01T00:00:00"), 30);
        assertThat(makeWithoutAggregation(obs, DEFAULT_EPOCH, P1M))
                .describedAs("Multiple observations with missing periods")
                .isEqualTo(dataOf("2020-01/P1M", 10.0, NaN, NaN, 30.0));

        obs.clear();
        assertThat(makeWithoutAggregation(obs, DEFAULT_EPOCH, P1M))
                .describedAs("No observations")
                .isEqualTo(NO_DATA);

        obs.clear();
        obs.add(MAX_INT_PERIOD.start().minusSeconds(1), 0.0);
        obs.add(MAX_INT_PERIOD.start(), 1.1);
        assertThat(makeWithoutAggregation(obs, DEFAULT_EPOCH, PT1S))
                .describedAs("Periods up to Integer.MAX_VALUE are supported")
                .isEqualTo(dataOf("2038-01-19T03:14:06/PT1S", 0.0, 1.1));

        obs.clear();
        obs.add(MAX_INT_PERIOD.start(), 1.1);
        obs.add(MAX_INT_PERIOD.start().plusSeconds(1), 2.2);
        assertThat(makeWithoutAggregation(obs, DEFAULT_EPOCH, PT1S))
                .describedAs("Periods exceeding Integer.MAX_VALUE are not supported")
                .isEqualTo(ARITHMETIC_EXCEPTION);
    }

    @Test
    public void testMakeFromUnknownUnit() {
        var obs = new ByObjObsList.PreSorted<LocalDateTime>(TsPeriod::idAt, 32);

        obs.clear();
        obs.add(parse("2020-01-01T00:00"), 1.1);
        assertThat(makeFromUnknownUnit(obs, DEFAULT_EPOCH))
                .describedAs("Requires at least two observations")
                .isEqualTo(GUESS_SINGLE);

        obs.clear();
        obs.add(parse("2020-01-01T00:00"), 1.1);
        obs.add(parse("2020-01-01T00:00"), 2.2);
        assertThat(makeFromUnknownUnit(obs, DEFAULT_EPOCH))
                .describedAs("Duplicated observations are not allowed")
                .isEqualTo(GUESS_DUPLICATION);

        obs.clear();
        obs.add(parse("2020-01-01T00:00"), 1.1);
        obs.add(parse("2021-01-01T00:00"), 2.2);
        assertThat(makeFromUnknownUnit(obs, DEFAULT_EPOCH))
                .describedAs("Yearly frequency")
                .isEqualTo(dataOf("2020/P1Y", 1.1, 2.2));

        obs.clear();
        obs.add(parse("2020-01-01T00:00"), 1.1);
        obs.add(parse("2020-08-01T00:00"), 2.2);
        assertThat(makeFromUnknownUnit(obs, DEFAULT_EPOCH))
                .describedAs("7-months frequency")
                .isEqualTo(dataOf("2020-01/P7M", 1.1, 2.2));

        obs.clear();
        obs.add(parse("2020-01-01T00:00"), 1.1);
        obs.add(parse("2020-07-01T00:00"), 2.2);
        obs.add(parse("2021-01-01T00:00"), 3.3);
        obs.add(parse("2021-07-01T00:00"), 4.4);
        obs.add(parse("2022-01-01T00:00"), 5.5);
        obs.add(parse("2022-07-01T00:00"), 6.6);
        assertThat(makeFromUnknownUnit(obs, DEFAULT_EPOCH))
                .describedAs("Semi-annual frequency")
                .isEqualTo(dataOf("2020-01/P6M", 1.1, 2.2, 3.3, 4.4, 5.5, 6.6));

        obs.clear();
        obs.add(parse("2020-01-01T00:00"), 1.1);
        obs.add(parse("2020-05-01T00:00"), 2.2);
        obs.add(parse("2020-09-01T00:00"), 3.3);
        obs.add(parse("2021-01-01T00:00"), 4.4);
        assertThat(makeFromUnknownUnit(obs, DEFAULT_EPOCH))
                .describedAs("Quadri-month frequency")
                .isEqualTo(dataOf("2020-01/P4M", 1.1, 2.2, 3.3, 4.4));

        obs.clear();
        obs.add(parse("2020-01-01T00:00"), 1.1);
        obs.add(parse("2020-04-01T00:00"), 2.2);
        assertThat(makeFromUnknownUnit(obs, DEFAULT_EPOCH))
                .describedAs("Quarterly frequency")
                .isEqualTo(dataOf("2020-01/P3M", 1.1, 2.2));

        obs.clear();
        obs.add(parse("2020-01-01T00:00"), 1.1);
        obs.add(parse("2020-03-01T00:00"), 2.2);
        assertThat(makeFromUnknownUnit(obs, DEFAULT_EPOCH))
                .describedAs("Bimonthly frequency")
                .isEqualTo(dataOf("2020-01/P2M", 1.1, 2.2));

        obs.clear();
        obs.add(DEFAULT_EPOCH.minusMonths(2), 0.0);
        obs.add(DEFAULT_EPOCH, 1.1);
        obs.add(DEFAULT_EPOCH.plusMonths(2), 2.2);
        assertThat(makeFromUnknownUnit(obs, DEFAULT_EPOCH))
                .describedAs("Bimonthly frequency across several years and around epoch")
                .isEqualTo(dataOf("1969-11/P2M", 0.0, 1.1, 2.2));

        obs.clear();
        obs.add(parse("2020-01-01T00:00"), 1.1);
        obs.add(parse("2020-02-01T00:00"), 2.2);
        assertThat(makeFromUnknownUnit(obs, DEFAULT_EPOCH))
                .describedAs("Monthly frequency")
                .isEqualTo(dataOf("2020-01/P1M", 1.1, 2.2));

        obs.clear();
        obs.add(parse("2020-01-01T00:00"), 1.1);
        obs.add(parse("2020-03-01T00:00"), 2.2);
        obs.add(parse("2020-04-01T00:00"), 3.3);
        assertThat(makeFromUnknownUnit(obs, DEFAULT_EPOCH))
                .describedAs("Non continuous series should fill with NaN")
                .isEqualTo(dataOf("2020-01/P1M", 1.1, NaN, 2.2, 3.3));

        obs.clear();
        obs.add(parse("2020-01-06T00:00"), 1.1);
        obs.add(parse("2020-01-13T00:00"), 2.2);
        obs.add(parse("2020-01-20T00:00"), 3.3);
        assertThat(makeFromUnknownUnit(obs, DEFAULT_EPOCH))
                .describedAs("Weekly frequency")
                .isEqualTo(TsData.of(periodOf(TsPeriod.DEFAULT_EPOCH.with(next(MONDAY)), P1W, LocalDate.parse("2020-01-06")), DoubleSeq.of(1.1, 2.2, 3.3)));

        obs.clear();
        obs.add(parse("2020-01-06T00:00"), 1.1);
        obs.add(parse("2020-01-20T00:00"), 2.2);
        obs.add(parse("2020-02-03T00:00"), 3.3);
        assertThat(makeFromUnknownUnit(obs, DEFAULT_EPOCH))
                .describedAs("2-weeks frequency")
                .isEqualTo(TsData.of(periodOf(TsPeriod.DEFAULT_EPOCH.with(next(MONDAY)), TsUnit.parse("P2W"), LocalDate.parse("2019-12-30")), DoubleSeq.of(1.1, 2.2, 3.3)));

        obs.clear();
        obs.add(parse("2020-01-01T00:00"), 1.1);
        obs.add(parse("2020-01-02T00:00"), 2.2);
        assertThat(makeFromUnknownUnit(obs, DEFAULT_EPOCH))
                .describedAs("Daily frequency")
                .isEqualTo(dataOf("2020-01-01/P1D", 1.1, 2.2));

        obs.clear();
        obs.add(parse("2020-01-01T00:00"), 1.1);
        obs.add(parse("2020-01-01T01:00"), 2.2);
        assertThat(makeFromUnknownUnit(obs, DEFAULT_EPOCH))
                .describedAs("Hourly frequency")
                .isEqualTo(dataOf("2020-01-01T00/PT1H", 1.1, 2.2));

        obs.clear();
        obs.add(parse("2020-01-01T00:00"), 1.1);
        obs.add(parse("2020-01-01T00:05"), 2.2);
        assertThat(makeFromUnknownUnit(obs, DEFAULT_EPOCH))
                .describedAs("5-minutes frequency")
                .isEqualTo(dataOf("2020-01-01T00:00/PT5M", 1.1, 2.2));

        obs.clear();
        obs.add(parse("2020-01-01T00:00"), 1.1);
        obs.add(parse("2020-01-01T00:01"), 2.2);
        assertThat(makeFromUnknownUnit(obs, DEFAULT_EPOCH))
                .describedAs("Minutely frequency")
                .isEqualTo(dataOf("2020-01-01T00:00/PT1M", 1.1, 2.2));

        obs.clear();
        obs.add(parse("2020-01-01T00:00:00"), 1.1);
        obs.add(parse("2020-01-01T00:00:01"), 2.2);
        assertThat(makeFromUnknownUnit(obs, DEFAULT_EPOCH))
                .describedAs("Secondly frequency")
                .isEqualTo(dataOf("2020-01-01T00:00:00/PT1S", 1.1, 2.2));

        obs.clear();
        obs.add(MAX_INT_PERIOD.start().minusSeconds(1), 0.0);
        obs.add(MAX_INT_PERIOD.start(), 1.1);
        assertThat(makeFromUnknownUnit(obs, DEFAULT_EPOCH))
                .describedAs("Periods up to Integer.MAX_VALUE are supported")
                .isEqualTo(dataOf("2038-01-19T03:14:06/PT1S", 0.0, 1.1));

        obs.clear();
        obs.add(MAX_INT_PERIOD.start(), 1.1);
        obs.add(MAX_INT_PERIOD.start().plusSeconds(1), 2.2);
        assertThat(makeFromUnknownUnit(obs, DEFAULT_EPOCH))
                .describedAs("Periods exceeding Integer.MAX_VALUE are not supported")
                .isEqualTo(ARITHMETIC_EXCEPTION);
    }

    private static final TsPeriod MAX_INT_PERIOD = TsPeriod.of(TsUnit.PT1S, Integer.MAX_VALUE);
    private static final AggregationType[] UNSUPPORTED_TYPES = {None, UserDefined};
    private static final AggregationType[] SUPPORTED_TYPES = {First, Last, Min, Max, Average, Sum};

    @MightBePromoted
    private static TsData dataOf(String start, double... values) {
        return TsData.of(TsPeriod.parse(start), DoubleSeq.of(values));
    }

    @MightBePromoted
    private static TsPeriod periodOf(LocalDateTime epoch, TsUnit unit, LocalDate date) {
        return TsPeriod.builder().epoch(epoch).unit(unit).date(date).build();
    }
}
