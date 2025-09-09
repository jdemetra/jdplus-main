/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.toolkit.base.core.timeseries.calendars;

import jdplus.toolkit.base.api.timeseries.calendars.FixedDay;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 *
 * @author Jean Palate
 */
public class FixedDayInfoTest {

    public FixedDayInfoTest() {
    }

    @Test
    public void test1() {
        FixedDay fd = new FixedDay(7, 21);
        Iterable<HolidayInfo> iterable = HolidayInfo.iterable(fd, LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 1).plus(3, ChronoUnit.YEARS));
        Stream<HolidayInfo> stream = StreamSupport.stream(iterable.spliterator(), false);
        assertEquals(3, stream.count());
        stream = StreamSupport.stream(iterable.spliterator(), false);
        assertEquals(2018, stream.findFirst().orElseThrow().getDay().getYear());
    }

    @Test
    public void test2() {
        FixedDay fd = new FixedDay(7, 21);
        Iterable<HolidayInfo> iterable = HolidayInfo.iterable(fd, LocalDate.of(2017, 7, 21), LocalDate.of(2018, 1, 1).plus(3, ChronoUnit.YEARS));
        Stream<HolidayInfo> stream = StreamSupport.stream(iterable.spliterator(), false);
        assertEquals(4, stream.count());
        stream = StreamSupport.stream(iterable.spliterator(), false);
        assertEquals(2017, stream.findFirst().orElseThrow().getDay().getYear());
    }

    @Test
    public void test3() {
        FixedDay fd = new FixedDay(7, 21);
        Iterable<HolidayInfo> iterable = HolidayInfo.iterable(fd, LocalDate.of(2017, 7, 21), LocalDate.of(2017, 7, 22));
        Stream<HolidayInfo> stream = StreamSupport.stream(iterable.spliterator(), false);
        assertEquals(1, stream.count());
        stream = StreamSupport.stream(iterable.spliterator(), false);
        assertEquals(2017, stream.findFirst().orElseThrow().getDay().getYear());
    }

   @Test
    public void testEmpty() {
        FixedDay fd = new FixedDay(7, 21);
        Iterable<HolidayInfo> iterable = HolidayInfo.iterable(fd, LocalDate.of(2017, 7, 23), LocalDate.of(2018, 6, 22));
        Stream<HolidayInfo> stream = StreamSupport.stream(iterable.spliterator(), false);
       assertEquals(0, stream.count());
        stream = StreamSupport.stream(iterable.spliterator(), false);
       assertFalse(stream.findFirst().isPresent());
    }
}
