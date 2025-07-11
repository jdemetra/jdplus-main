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

import jdplus.toolkit.base.api.util.HasShortStringRepresentation;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import static jdplus.toolkit.base.api.timeseries.TsPeriod.*;
import static jdplus.toolkit.base.api.timeseries.TsPeriod.of;
import static jdplus.toolkit.base.api.timeseries.TsUnit.*;
import static jdplus.toolkit.base.api.timeseries.TsUnit.of;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Philippe Charles
 */
public class TsPeriodTest {

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testFactories() {
        assertThatThrownBy(() -> of(null, d2011_02_01_0000)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> of(P1M, (LocalDate) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> of(null, d2011_02_01)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> of(P1M, (LocalDateTime) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> of(null, 0)).isInstanceOf(NullPointerException.class);

        assertThat(monthly(2011, 2))
                .extracting("epoch", "unit", "id")
                .containsExactly(DEFAULT_EPOCH, P1M, 493L);

        assertThat(quarterly(2011, 2))
                .extracting("epoch", "unit", "id")
                .containsExactly(DEFAULT_EPOCH, P3M, 165L);
    }

    @Test
    public void testBuilder() {
        assertThat(builder().build()).isNotNull();

        assertThat(builder().unit(P1M).build())
                .isEqualTo(new TsPeriod(DEFAULT_EPOCH, P1M, 0))
                .extracting("epoch", "unit", "id")
                .containsExactly(DEFAULT_EPOCH, P1M, 0L);

        assertThat(builder().unit(P1M).epoch(someReference).build())
                .isEqualTo(new TsPeriod(someReference, P1M, -135L))
                .extracting("epoch", "unit", "id")
                .containsExactly(someReference, P1M, -135L);
        assertThat(builder().unit(of(2, ChronoUnit.DECADES)).epoch(someReference).plus(2).build())
                .isEqualTo(new TsPeriod(someReference, of(2, ChronoUnit.DECADES), 1))
                .extracting("epoch", "unit", "id")
                .containsExactly(someReference, of(2, ChronoUnit.DECADES), 1L);

        assertThat(TsPeriod.builder().unit(P1D).epoch(someReference).id(2).build())
                .isEqualTo(TsPeriod.monthly(1970, 1).withUnit(P1D).withEpoch(someReference).withId(2))
                .isNotEqualTo(TsPeriod.builder().unit(P1D).id(2).epoch(someReference).build());
    }

    @Test
    public void testEquals() {
        assertThat(of(P1Y, d2011_02_01))
                .isEqualTo(of(P1Y, d2011_02_01))
                .isNotEqualTo(of(P1Y, d2011_02_01).next())
                .isNotEqualTo(of(P1Y, d2011_02_01).withEpoch(someReference))
                .isNotEqualTo(of(P1Y, d2011_02_01).withUnit(PT1H))
                .isNotEqualTo(of(P1Y, d2011_02_01).withDate(d2011_02_01.plusYears(1).atStartOfDay()))
                .isEqualTo(of(P1Y, d2011_02_01).withDate(d2011_02_01_1337));
    }

    @Test
    public void testRange() {
        assertThat(monthly(2011, 2)).satisfies(o -> {
            assertThat(o.start()).isEqualTo(d2011_02_01_0000);
            assertThat(o.end()).isEqualTo(d2011_02_01_0000.plusMonths(1));
            assertThat(o.contains(d2011_02_01_0000)).isTrue();
            assertThat(o.contains(d2011_02_01_0000.plusDays(1))).isTrue();
            assertThat(o.contains(d2011_02_01_0000.plusMonths(1))).isFalse();
        });

        assertThat(minutely(2011, 2, 1, 13, 37)).satisfies(o -> {
            assertThat(o.start()).isEqualTo(d2011_02_01_1337);
            assertThat(o.end()).isEqualTo(d2011_02_01_1337.plusMinutes(1));
            assertThat(o.contains(d2011_02_01_1337)).isTrue();
            assertThat(o.contains(d2011_02_01_1337.plusSeconds(1))).isTrue();
            assertThat(o.contains(d2011_02_01_1337.plusMinutes(1))).isFalse();
        });
    }

    @Test
    public void testComparable() {
        assertThat(monthly(2011, 2))
                .isEqualByComparingTo(monthly(2011, 2))
                .isLessThan(monthly(2011, 3))
                .isGreaterThan(monthly(2011, 1));

        assertThat(monthly(2011, 2).withEpoch(someReference))
                .isEqualByComparingTo(monthly(2011, 2))
                .isLessThan(monthly(2011, 3))
                .isGreaterThan(monthly(2011, 1));

        assertThatThrownBy(() -> monthly(2011, 2).compareTo(yearly(2011)))
                .isInstanceOf(TsException.class)
                .hasMessage(TsException.INCOMPATIBLE_FREQ);
    }

    @Test
    public void testIsAfter() {
        assertThat(monthly(2011, 2).isAfter(monthly(2011, 3))).isFalse();
        assertThat(monthly(2011, 2).isAfter(monthly(2011, 2))).isFalse();
        assertThat(monthly(2011, 2).isAfter(monthly(2011, 1))).isTrue();

        assertThat(monthly(2011, 2).withEpoch(someReference).isAfter(monthly(2011, 3))).isFalse();
        assertThat(monthly(2011, 2).withEpoch(someReference).isAfter(monthly(2011, 2))).isFalse();
        assertThat(monthly(2011, 2).withEpoch(someReference).isAfter(monthly(2011, 1))).isTrue();

        assertThatThrownBy(() -> monthly(2011, 2).isAfter(yearly(2011)))
                .isInstanceOf(TsException.class)
                .hasMessage(TsException.INCOMPATIBLE_FREQ);
    }

    @Test
    public void testIsBefore() {
        assertThat(monthly(2011, 2).isBefore(monthly(2011, 3))).isTrue();
        assertThat(monthly(2011, 2).isBefore(monthly(2011, 2))).isFalse();
        assertThat(monthly(2011, 2).isBefore(monthly(2011, 1))).isFalse();

        assertThat(monthly(2011, 2).withEpoch(someReference).isBefore(monthly(2011, 3))).isTrue();
        assertThat(monthly(2011, 2).withEpoch(someReference).isBefore(monthly(2011, 2))).isFalse();
        assertThat(monthly(2011, 2).withEpoch(someReference).isBefore(monthly(2011, 1))).isFalse();

        assertThatThrownBy(() -> monthly(2011, 2).isBefore(yearly(2011)))
                .isInstanceOf(TsException.class)
                .hasMessage(TsException.INCOMPATIBLE_FREQ);
    }

    @Test
    public void testNext() {
        assertThat(of(P1Y, d2011_02_01).next()).isEqualTo(of(P1Y, d2011_02_01.plusYears(1)));
    }

    @Test
    public void testPlus() {
        assertThat(of(P1Y, d2011_02_01).plus(1)).isEqualTo(of(P1Y, d2011_02_01).next());
        assertThat(of(P1Y, d2011_02_01).plus(2)).isEqualTo(of(P1Y, d2011_02_01.plusYears(2)));
        assertThat(of(P1Y, d2011_02_01).plus(-1)).isEqualTo(of(P1Y, d2011_02_01.plusYears(-1)));
        assertThat(of(PT1H, d2011_02_01).plus(11)).isEqualTo(of(PT1H, d2011_02_01_0000.plusHours(11)));
    }

    @Test
    public void testWithFreq() {
        assertThatThrownBy(() -> of(P1Y, d2011_02_01).withUnit(null)).isInstanceOf(NullPointerException.class);
        assertThat(of(P1Y, d2011_02_01).withUnit(P1M)).isEqualTo(of(P1M, LocalDate.of(2011, 1, 1)));
        assertThat(of(P1M, d2011_02_01).withUnit(P1Y)).isEqualTo(of(P1Y, d2011_02_01));
    }

    @Test
    public void testWithReference() {
        assertThat(of(P1D, d2011_02_01).withEpoch(someReference)).isEqualTo(new TsPeriod(someReference, P1D, 10898));
        assertThat(of(P1D, d2011_02_01.plusDays(1)).withEpoch(someReference)).isEqualTo(new TsPeriod(someReference, P1D, 10899));
    }

    @Test
    public void testWithDate() {
        assertThatThrownBy(() -> of(P1Y, d2011_02_01).withDate((LocalDateTime) null)).isInstanceOf(NullPointerException.class);
        assertThat(of(P1D, d2011_02_01).withDate(d2011_02_01.plusDays(3).atStartOfDay())).isEqualTo(of(P1D, d2011_02_01.plusDays(3)));
    }

    @Test
    public void testToBuilder() {
        assertThat(of(P1D, d2011_02_01).toBuilder().build()).isEqualTo(of(P1D, d2011_02_01));
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testRepresentableAsString() {
        assertThatNullPointerException()
                .isThrownBy(() -> TsPeriod.parse(null));

        assertThatThrownBy(() -> TsPeriod.parse("hello"))
                .isInstanceOf(DateTimeParseException.class);

        assertThat(TsPeriod.parse("2011-02-01T00:00/P1M"))
                .hasToString("2011-02-01T00:00:00/P1M")
                .returns("2011-02/P1M", HasShortStringRepresentation::toShortString)
                .returns("2011-02", TsPeriod::getStartAsShortString)
                .isEqualTo(TsPeriod.parse("2011-02/P1M"))
                .isEqualTo(monthly(2011, 2));

        assertThat(TsPeriod.parse("2020-04-30T00:00/P7D"))
                .hasToString("2020-04-30T00:00:00/P7D")
                .returns("2020-04-30/P7D", HasShortStringRepresentation::toShortString)
                .returns("2020-04-30", TsPeriod::getStartAsShortString)
                .isEqualTo(TsPeriod.parse("2020-04-30/P7D"))
                .isEqualTo(weekly(2020, 4, 30));

        assertThat(TsPeriod.parse("2011-04-01T00:00/P3M"))
                .hasToString("2011-04-01T00:00:00/P3M")
                .returns("2011-04/P3M", HasShortStringRepresentation::toShortString)
                .returns("2011-04", TsPeriod::getStartAsShortString)
                .isEqualTo(TsPeriod.parse("2011-04/P3M"))
                .isEqualTo(quarterly(2011, 2));

        assertThat(TsPeriod.parse("2011-02-15T10:07/PT1M"))
                .hasToString("2011-02-15T10:07:00/PT1M")
                .returns("2011-02-15T10:07/PT1M", HasShortStringRepresentation::toShortString)
                .returns("2011-02-15T10:07", TsPeriod::getStartAsShortString)
                .isEqualTo(TsPeriod.parse("2011-02-15T10:07/PT1M"))
                .isEqualTo(minutely(2011, 2, 15, 10, 7));

        assertThat(TsPeriod.parse("2011/P1M"))
                .hasToString("2011-01-01T00:00:00/P1M")
                .returns("2011-01/P1M", HasShortStringRepresentation::toShortString)
                .returns("2011-01", TsPeriod::getStartAsShortString)
                .isEqualTo(TsPeriod.parse("2011-01/P1M"))
                .isEqualTo(monthly(2011, 1));

        assertThat(of(P1Y, d2011_02_01))
                .hasToString("2011-01-01T00:00:00/P1Y");

        assertThat(of(P1Y, d2011_02_01).withEpoch(someReference).next())
                .hasToString("2011-04-01T00:00:00/P1Y");

        assertThat(of(P1D, d2011_02_01))
                .hasToString("2011-02-01T00:00:00/P1D");

//        assertThat(TsPeriod.parse("P1M#2"))
//                .isEqualTo(TsPeriod.builder().unit(MONTH).id(2).build());
//
//        assertThat(TsPeriod.parse("P1M#-1"))
//                .isEqualTo(TsPeriod.builder().unit(MONTH).id(-1).build());
//
//        assertThat(TsPeriod.parse("P1M#2@" + someReference.toString()))
//                .isEqualTo(TsPeriod.builder().unit(MONTH).epoch(someReference).id(2).build());
    }

    @Test
    public void testUntil() {
        assertThat(yearly(2010).until(yearly(2012))).isEqualTo(2);
        assertThat(yearly(2010).until(yearly(2010))).isEqualTo(0);
        assertThat(yearly(2010).until(yearly(2009))).isEqualTo(-1);

        assertThatThrownBy(() -> monthly(2011, 2).until(yearly(2011)))
                .isInstanceOf(TsException.class)
                .hasMessage(TsException.INCOMPATIBLE_FREQ);
    }

    @Test
    public void testIdAt() {
        assertThat(idAt(DEFAULT_EPOCH, P1M, DEFAULT_EPOCH)).isEqualTo(0);
        assertThat(idAt(DEFAULT_EPOCH, P1M, DEFAULT_EPOCH.plusNanos(1))).isEqualTo(0);
        assertThat(idAt(DEFAULT_EPOCH, P1M, DEFAULT_EPOCH.plusMonths(1))).isEqualTo(1);
        assertThat(idAt(DEFAULT_EPOCH, P1M, DEFAULT_EPOCH.minusNanos(1))).isEqualTo(-1);
        assertThat(idAt(DEFAULT_EPOCH, P1M, DEFAULT_EPOCH.minusMonths(1))).isEqualTo(-1);

        assertThat(idAt(DEFAULT_EPOCH, P1Y, DEFAULT_EPOCH)).isEqualTo(0);
        assertThat(idAt(DEFAULT_EPOCH, P1Y, DEFAULT_EPOCH.plusNanos(1))).isEqualTo(0);
        assertThat(idAt(DEFAULT_EPOCH, P1Y, DEFAULT_EPOCH.plusYears(1))).isEqualTo(1);
        assertThat(idAt(DEFAULT_EPOCH, P1Y, DEFAULT_EPOCH.minusNanos(1))).isEqualTo(-1);
        assertThat(idAt(DEFAULT_EPOCH, P1Y, DEFAULT_EPOCH.minusYears(1))).isEqualTo(-1);

        assertThat(idAt(DEFAULT_EPOCH, P1D, DEFAULT_EPOCH)).isEqualTo(0);
        assertThat(idAt(DEFAULT_EPOCH.plusDays(4), P1D, DEFAULT_EPOCH.plusDays(4))).isEqualTo(0);
        assertThat(idAt(DEFAULT_EPOCH.plusDays(4), P1D, DEFAULT_EPOCH.plusDays(5))).isEqualTo(1);
    }

    @Test
    public void testDateAt() {
        assertThat(dateAt(DEFAULT_EPOCH, P1M, 0)).isEqualTo(DEFAULT_EPOCH);
        assertThat(dateAt(DEFAULT_EPOCH, P1M, 1)).isEqualTo(DEFAULT_EPOCH.plusMonths(1));
        assertThat(dateAt(DEFAULT_EPOCH, P1M, -1)).isEqualTo(DEFAULT_EPOCH.minusMonths(1));

        assertThat(dateAt(DEFAULT_EPOCH, P1Y, 0)).isEqualTo(DEFAULT_EPOCH);
        assertThat(dateAt(DEFAULT_EPOCH, P1Y, 1)).isEqualTo(DEFAULT_EPOCH.plusYears(1));
        assertThat(dateAt(DEFAULT_EPOCH, P1Y, -1)).isEqualTo(DEFAULT_EPOCH.minusYears(1));

        assertThat(dateAt(DEFAULT_EPOCH, P1D, 0)).isEqualTo(DEFAULT_EPOCH);
        assertThat(dateAt(DEFAULT_EPOCH.plusDays(4), P1D, 0)).isEqualTo(DEFAULT_EPOCH.plusDays(4));
        assertThat(dateAt(DEFAULT_EPOCH.plusDays(4), P1D, 1)).isEqualTo(DEFAULT_EPOCH.plusDays(5));
    }

    //    @Test
//    public void testGetPosition() {
//
//        assertThat(monthly(2010, 1).getPosition(YEAR)).isEqualTo(position(monthly(2010, 1), YEAR));
//        assertThat(monthly(2010, 2).getPosition(YEAR)).isEqualTo(1);
//        assertThat(monthly(2010, 12).getPosition(YEAR)).isEqualTo(11);
//
//        assertThat(monthly(2010, 1).getPosition(QUARTER)).isEqualTo(0);
//        assertThat(monthly(2010, 2).getPosition(QUARTER)).isEqualTo(1);
//        assertThat(monthly(2010, 3).getPosition(QUARTER)).isEqualTo(2);
//        assertThat(monthly(2010, 4).getPosition(QUARTER)).isEqualTo(0);
//
//        assertThat(monthly(2010, 1).getPosition(MONTH)).isEqualTo(0);
//        assertThat(monthly(2010, 2).getPosition(MONTH)).isEqualTo(0);
//
//        assertThat(quarterly(2010, 1).getPosition(YEAR)).isEqualTo(0);
//        assertThat(quarterly(2010, 2).getPosition(YEAR)).isEqualTo(1);
//        assertThat(quarterly(2010, 4).getPosition(YEAR)).isEqualTo(3);
//
//        assertThat(monthly(2010, 1).getPosition(HOUR)).isEqualTo(0);
//        assertThat(monthly(2010, 2).getPosition(HOUR)).isEqualTo(0);
//    }
//
//    private static int position(TsPeriod p, TsUnit lfreq) {
//        TsPeriod lp = p.withUnit(lfreq);
//        RegularDomain hdom = RegularDomain.splitOf(lp, p.getUnit(), true);
//        return hdom.indexOf(p);
//    }
//    @Test
//    public void stressTestPosition() {
//        TsPeriod p = TsPeriod.of(MONTH, d2011_02_01);
//        long t0 = System.currentTimeMillis();
//        for (int i = 0; i < 10000000; ++i) {
//            int pos = position(p, YEAR);
//        }
//        long t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
//        t0 = System.currentTimeMillis();
//        for (int i = 0; i < 10000000; ++i) {
//            int pos = p.getPosition(YEAR);
//        }
//        t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
//    }
    @Test
    public void testPoint() {
        LocalDateTime x = d2011_02_01_0000.plusSeconds(0);
        assertEquals(x, d2011_02_01_0000);
        LocalDate y = d2011_02_01.plusDays(0);
        assertEquals(y, d2011_02_01);
    }

    @Test
    public void testWeek() {
        TsPeriod w1 = TsPeriod.weekly(2020, 4, 30);
        TsPeriod w2 = TsPeriod.weekly(2020, 4, 20);
        assertNotEquals(w1, w2);
    }

    private final LocalDate d2011_02_01 = LocalDate.of(2011, 2, 1);
    private final LocalDateTime someReference = TsPeriod.DEFAULT_EPOCH.plusMonths(135);
    private final LocalDateTime d2011_02_01_0000 = d2011_02_01.atStartOfDay();
    private final LocalDateTime d2011_02_01_1337 = d2011_02_01.atTime(13, 37);
}
