/*
 * Copyright 2020 National Bank of Belgium
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
import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class CalendarPeriodTest {

    @Test
    public void testRepresentableAsString() {
        assertThat(CalendarPeriod.parse("2010-02-17/2010-03-17"))
                .hasToString("2010-02-17/2010-03-17")
                .returns("2010-02-17/03-17", HasShortStringRepresentation::toShortString)
                .isEqualTo(CalendarPeriod.parse("2010-02-17/03-17"))
                .returns(LocalDate.of(2010, 2, 17), CalendarPeriod::getStart)
                .returns(LocalDate.of(2010, 3, 17), CalendarPeriod::getEnd)
                .returns(Period.parse("P1M"), CalendarPeriod::getDuration);

        assertThat(CalendarPeriod.parse("2010-01-01/2010-02-01"))
                .hasToString("2010-01-01/2010-02-01")
                .returns("2010-01-01/02-01", HasShortStringRepresentation::toShortString)
                .isEqualTo(CalendarPeriod.parse("2010-01-01/02-01"))
                .returns(LocalDate.of(2010, 1, 1), CalendarPeriod::getStart)
                .returns(LocalDate.of(2010, 2, 1), CalendarPeriod::getEnd)
                .returns(Period.parse("P1M"), CalendarPeriod::getDuration);
    }
}
