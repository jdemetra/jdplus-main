package internal.toolkit.base.api.timeseries.util;

import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.timeseries.calendars.RegularFrequency;
import nbbrd.design.MightBePromoted;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;
import java.util.function.IntFunction;

import static internal.toolkit.base.api.timeseries.util.TsDataCollector.*;
import static java.lang.Double.NaN;
import static java.time.DayOfWeek.MONDAY;
import static java.time.LocalDateTime.parse;
import static java.time.temporal.TemporalAdjusters.next;
import static jdplus.toolkit.base.api.data.AggregationType.*;
import static jdplus.toolkit.base.api.data.DoubleSeq.onMapping;
import static jdplus.toolkit.base.api.time.TemporalFormatter.EXTENDED_CALENDAR_TIME;
import static jdplus.toolkit.base.api.timeseries.TsPeriod.DEFAULT_EPOCH;
import static jdplus.toolkit.base.api.timeseries.TsUnit.*;
import static jdplus.toolkit.base.api.timeseries.calendars.RegularFrequency.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatObject;
import static org.assertj.core.condition.AnyOf.anyOf;

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
    public void testMakeFromUnknownUnitErrors() {
        assertThatObject(makeFromUnknownUnit(listOfDates("2020"), DEFAULT_EPOCH))
                .describedAs("Requires at least two observations")
                .isEqualTo(GUESS_SINGLE);

        assertThatObject(makeFromUnknownUnit(listOfDates("2020", "2020"), DEFAULT_EPOCH))
                .describedAs("Duplicated observations are not allowed")
                .isEqualTo(GUESS_DUPLICATION);

        var max = MAX_INT_PERIOD.start();

        assertThatObject(makeFromUnknownUnit(listOfDates(max.minusSeconds(1), max), DEFAULT_EPOCH))
                .describedAs("Periods up to Integer.MAX_VALUE are supported")
                .isEqualTo(dataOf("2038-01-19T03:14:06/PT1S", 1, 2));

        assertThatObject(makeFromUnknownUnit(listOfDates(max, max.plusSeconds(1)), DEFAULT_EPOCH))
                .describedAs("Periods exceeding Integer.MAX_VALUE are not supported")
                .isEqualTo(ARITHMETIC_EXCEPTION);

        assertThatObject(makeFromUnknownUnit(listOfDates("2020-01", "2020-03", "2020-04"), DEFAULT_EPOCH))
                .describedAs("Non continuous series should fill with NaN")
                .isEqualTo(dataOf("2020-01/P1M", 1, NaN, 2, 3));
    }

    @Test
    public void testMakeFromUnknownUnitFromRegularFrequencies() {
        assertThatObject(makeFromUnknownUnit(listOfDomain("R2/2020/P1Y"), DEFAULT_EPOCH))
                .describedAs("Yearly frequency, 2 obs")
                .isEqualTo(dataOfDomain("R2/2020/P1Y"))
                .returns(Yearly, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R2/2020/P6M"), DEFAULT_EPOCH))
                .describedAs("Half-Yearly frequency, 2 obs & same year")
                .isEqualTo(dataOfDomain("R2/2020/P6M"))
                .returns(HalfYearly, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R2/1969-07/P6M"), DEFAULT_EPOCH))
                .describedAs("Half-Yearly frequency, 2 obs & different year around epoch -> Yearly")
                .isEqualTo(dataOfDomain("R2/1969/P1Y"))
                .returns(Yearly, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R3/1969-07/P6M"), DEFAULT_EPOCH))
                .describedAs("Half-Yearly frequency, 3 obs & different year around epoch")
                .isEqualTo(dataOfDomain("R3/1969-07/P6M"))
                .returns(HalfYearly, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R2/2020/P4M"), DEFAULT_EPOCH))
                .describedAs("Quadri-monthly frequency, 2 obs & same year")
                .isEqualTo(dataOfDomain("R2/2020/P4M"))
                .returns(QuadriMonthly, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R2/1969-09/P4M"), DEFAULT_EPOCH))
                .describedAs("Quadri-monthly frequency, 2 obs & different year around epoch -> Yearly")
                .isEqualTo(dataOfDomain("R2/1969/P1Y"))
                .returns(Yearly, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R3/1969-09/P4M"), DEFAULT_EPOCH))
                .describedAs("Quadri-monthly frequency, 3 obs & different year around epoch")
                .isEqualTo(dataOfDomain("R3/1969-09/P4M"))
                .returns(QuadriMonthly, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R2/2020/P3M"), DEFAULT_EPOCH))
                .describedAs("Quarterly frequency, 2 obs & same year")
                .isEqualTo(dataOfDomain("R2/2020/P3M"))
                .returns(Quarterly, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R2/1969-10/P3M"), DEFAULT_EPOCH))
                .describedAs("Quarterly frequency, 2 obs & different year around epoch -> Yearly")
                .isEqualTo(dataOfDomain("R2/1969/P1Y"))
                .returns(Yearly, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R3/1969-10/P3M"), DEFAULT_EPOCH))
                .describedAs("Quarterly frequency, 3 obs & different year around epoch")
                .isEqualTo(dataOfDomain("R3/1969-10/P3M"))
                .returns(Quarterly, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R2/2020/P2M"), DEFAULT_EPOCH))
                .describedAs("Bimonthly frequency, 2 obs & same year")
                .isEqualTo(dataOfDomain("R2/2020/P2M"))
                .returns(BiMonthly, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R2/1969-11/P2M"), DEFAULT_EPOCH))
                .describedAs("Bimonthly frequency, 2 obs & different year around epoch -> Yearly")
                .isEqualTo(dataOfDomain("R2/1969/P1Y"))
                .returns(Yearly, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R3/1969-11/P2M"), DEFAULT_EPOCH))
                .describedAs("Bimonthly frequency, 3 obs & different year around epoch")
                .isEqualTo(dataOfDomain("R3/1969-11/P2M"))
                .returns(BiMonthly, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R2/2020/P1M"), DEFAULT_EPOCH))
                .describedAs("Monthly frequency, 2 obs & same year")
                .isEqualTo(dataOfDomain("R2/2020/P1M"))
                .returns(Monthly, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R2/1969-12/P1M"), DEFAULT_EPOCH))
                .describedAs("Monthly frequency, 2 obs & different year around epoch -> Yearly")
                .isEqualTo(dataOfDomain("R2/1969/P1Y"))
                .returns(Yearly, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R3/1969-12/P1M"), DEFAULT_EPOCH))
                .describedAs("Monthly frequency, 3 obs & different year around epoch")
                .isEqualTo(dataOfDomain("R3/1969-12/P1M"))
                .returns(Monthly, RegularFrequency::parse);
    }

    @Test
    public void testMakeFromUnknownUnitFromHighFrequencies() {
        assertThatObject(makeFromUnknownUnit(listOfDomain("R2/2020/P1D"), DEFAULT_EPOCH))
                .describedAs("Daily frequency, 2 obs & same year")
                .isEqualTo(dataOfDomain("R2/2020/P1D"))
                .returns(Undefined, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R2/1969-12-31/P1D"), DEFAULT_EPOCH))
                .describedAs("Daily frequency, 2 obs & different year around epoch -> Yearly")
                .isEqualTo(dataOfDomain("R2/1969/P1Y"))
                .returns(Yearly, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R3/1969-12-31/P1D"), DEFAULT_EPOCH))
                .describedAs("Daily frequency, 3 obs & different year around epoch")
                .isEqualTo(dataOfDomain("R3/1969-12-31/P1D"))
                .returns(Undefined, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R2/2020/PT1H"), DEFAULT_EPOCH))
                .describedAs("Hourly frequency, 2 obs & same year")
                .isEqualTo(dataOfDomain("R2/2020/PT1H"))
                .returns(Undefined, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R2/1969-12-31T23/PT1H"), DEFAULT_EPOCH))
                .describedAs("Hourly frequency, 2 obs & different year around epoch -> Yearly")
                .isEqualTo(dataOfDomain("R2/1969/P1Y"))
                .returns(Yearly, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R3/1969-12-31T23/PT1H"), DEFAULT_EPOCH))
                .describedAs("Hourly frequency, 3 obs & different year around epoch")
                .isEqualTo(dataOfDomain("R3/1969-12-31T23/PT1H"))
                .returns(Undefined, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R2/2020/PT1M"), DEFAULT_EPOCH))
                .describedAs("Minutely frequency, 2 obs & same year")
                .isEqualTo(dataOfDomain("R2/2020/PT1M"))
                .returns(Undefined, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R2/1969-12-31T23:59/PT1M"), DEFAULT_EPOCH))
                .describedAs("Minutely frequency, 2 obs & different year around epoch -> Yearly")
                .isEqualTo(dataOfDomain("R2/1969/P1Y"))
                .returns(Yearly, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R3/1969-12-31T23:59/PT1M"), DEFAULT_EPOCH))
                .describedAs("Minutely frequency, 3 obs & different year around epoch")
                .isEqualTo(dataOfDomain("R3/1969-12-31T23:59/PT1M"))
                .returns(Undefined, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R2/2020/PT1S"), DEFAULT_EPOCH))
                .describedAs("Secondly frequency, 2 obs & same year")
                .isEqualTo(dataOfDomain("R2/2020/PT1S"))
                .returns(Undefined, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R2/1969-12-31T23:59:59/PT1S"), DEFAULT_EPOCH))
                .describedAs("Secondly frequency, 2 obs & different year around epoch -> Yearly")
                .isEqualTo(dataOfDomain("R2/1969/P1Y"))
                .returns(Yearly, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R3/1969-12-31T23:59:59/PT1S"), DEFAULT_EPOCH))
                .describedAs("Secondly frequency, 3 obs & different year around epoch")
                .isEqualTo(dataOfDomain("R3/1969-12-31T23:59:59/PT1S"))
                .returns(Undefined, RegularFrequency::parse);
    }

    @Test
    public void testMakeFromUnknownUnitFromExoticFrequencies() {
        assertThatObject(makeFromUnknownUnit(listOfDomain("R11/2020-01/P11M"), DEFAULT_EPOCH))
                .describedAs("11-months frequency")
                .isEqualTo(dataOfDomain("R11/2020-01/P11M"))
                .returns(Undefined, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R8/2020-01/P11M"), DEFAULT_EPOCH))
                .describedAs("11-months frequency without min obs")
                .isEqualTo(dataOfDomain("R8/2020-01/P11M"))
                .returns(Undefined, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R12/2020-01/P12M"), DEFAULT_EPOCH))
                .describedAs("12-months frequency as 1-year frequency")
                .isEqualTo(dataOfDomain("R12/2020-01/P1Y"))
                .returns(Yearly, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R13/2020-01/P13M"), DEFAULT_EPOCH))
                .describedAs("13-months frequency")
                .isEqualTo(dataOfDomain("R13/2020-01/P13M"))
                .returns(Undefined, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R13/2020-01/P14M"), DEFAULT_EPOCH))
                .describedAs("14-months frequency without min obs")
                .isEqualTo(dataOfDomain("R13/2020-01/P14M"))
                .returns(Undefined, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDates("2020-01", "2020-08"), DEFAULT_EPOCH))
                .describedAs("7-months frequency")
                .isEqualTo(dataOfDomain("R2/2020-01/P7M"))
                .returns(Undefined, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDates("2020-01-06", "2020-01-13", "2020-01-20"), DEFAULT_EPOCH))
                .describedAs("Weekly frequency")
                .isEqualTo(TsData.of(periodOf(DEFAULT_EPOCH.with(next(MONDAY)), P1W, dt("2020-01-06")), DoubleSeq.of(1, 2, 3)))
                .returns(Undefined, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDates("2020-01-06", "2020-01-20", "2020-02-03"), DEFAULT_EPOCH))
                .describedAs("2-weeks frequency")
                .isEqualTo(TsData.of(periodOf(DEFAULT_EPOCH.with(next(MONDAY)), P2W, dt("2019-12-30")), DoubleSeq.of(1, 2, 3)))
                .returns(Undefined, RegularFrequency::parse);

        assertThatObject(makeFromUnknownUnit(listOfDomain("R2/2020/PT5M"), DEFAULT_EPOCH))
                .describedAs("5-minutes frequency")
                .isEqualTo(dataOfDomain("R2/2020/PT5M"))
                .returns(Undefined, RegularFrequency::parse);
    }

    @ParameterizedTest
    @EnumSource(GuessingUnit.class)
    public void testMakeFromUnknownUnitDomain(GuessingUnit guess) {
        for (int unitAmount = 1; unitAmount < 1000; unitAmount++) {
            int size = guess.getMinimumLengthForGuessing(unitAmount);

            TsDomain expected = TsDomain.of(
                    TsPeriod
                            .builder()
                            .unit(guess.getTsUnit(unitAmount))
                            .epoch(guess.getAdjustedEpoch(DEFAULT_EPOCH))
                            .build(),
                    size);

            assertThatObject(makeFromUnknownUnit(listOfDomain(expected), DEFAULT_EPOCH).getDomain())
                    .describedAs("Testing guessing unit " + guess + " with multiplier " + unitAmount)
                    .is(anyOf(
                            new Condition<>(expected::equals, "matches domain " + expected),
                            new Condition<>(current -> isCoarserThan(current, expected), "is coarser than " + expected)
                    ));
        }
    }

    @MightBePromoted
    private static boolean isCoarserThan(TsDomain current, TsDomain other) {
        return current.size() == other.size()
                && current.getTsUnit().getEstimatedDurationRatio(other.getTsUnit()) <= 1;
    }

    // FIXME: extend period range by using relative positioning instead of integer-based positioning
    private static final TsPeriod MAX_INT_PERIOD = TsPeriod.of(TsUnit.PT1S, Integer.MAX_VALUE);
    private static final AggregationType[] UNSUPPORTED_TYPES = {None, UserDefined};
    private static final AggregationType[] SUPPORTED_TYPES = {First, Last, Min, Max, Average, Sum};

    @MightBePromoted
    private static TsData dataOf(String start, double... values) {
        DoubleSeq values1 = DoubleSeq.of(values);
        return TsData.of(TsPeriod.parse(start), values1);
    }

    @MightBePromoted
    private static TsPeriod periodOf(LocalDateTime epoch, TsUnit unit, LocalDateTime date) {
        return TsPeriod.builder().epoch(epoch).unit(unit).date(date).build();
    }

    private static TsData dataOfDomain(CharSequence domain) {
        return dataOfDomain(TsDomain.parse(domain));
    }

    private static TsData dataOfDomain(TsDomain domain) {
        return TsData.of(domain.getStartPeriod(), onMapping(domain.size(), TsDataCollectorTest::indexToValue));
    }

    private static ObsList listOfDomain(CharSequence domain) {
        return listOfDomain(TsDomain.parse(domain));
    }

    private static ObsList listOfDomain(TsDomain domain) {
        return listOf(domain.size(), i -> domain.get(i).start());
    }

    private static ObsList listOfDates(CharSequence... dates) {
        return listOf(dates.length, i -> dt(dates[i]));
    }

    private static ObsList listOfDates(LocalDateTime... dates) {
        return listOf(dates.length, i -> dates[i]);
    }

    private static ObsList listOf(int size, IntFunction<LocalDateTime> dateFunction) {
        var result = new ByObjObsList.PreSorted<LocalDateTime>(TsPeriod::idAt, 32);
        for (int i = 0; i < size; i++) result.add(dateFunction.apply(i), indexToValue(i));
        return result;
    }

    private static double indexToValue(int index) {
        return index + 1;
    }

    @MightBePromoted
    private static LocalDateTime dt(CharSequence obs) {
        return EXTENDED_CALENDAR_TIME.parse(obs, LocalDateTime::from);
    }

    private static final TsUnit P2W = TsUnit.parse("P2W");
}
