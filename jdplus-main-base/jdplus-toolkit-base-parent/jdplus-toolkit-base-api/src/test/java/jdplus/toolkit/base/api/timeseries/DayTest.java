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

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class DayTest {

    @Test
    public void testRepresentableAsString() {
        assertThat(Day.parse("2010-02-17/P1D"))
                .hasToString("2010-02-17/P1D")
                .returns(LocalDate.of(2010, 2, 17), Day::getDay)
                .returns(Period.parse("P1D"), Day::getDuration);

        assertThat(Day.parse("2010-01-01/P1D"))
                .hasToString("2010-01-01/P1D")
                .returns(LocalDate.of(2010, 1, 1), Day::getDay)
                .returns(Period.parse("P1D"), Day::getDuration);
    }
}
