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

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class TimePeriodTest {

    @Test
    public void testRepresentableAsString() {
        assertThat(TimePeriod.parse("2010-02-17T11:03:00/2010-03-17T11:03:00"))
                .hasToString("2010-02-17T11:03:00/2010-03-17T11:03:00")
                .returns("2010-02-17T11:03:00/03-17T11:03:00", HasShortStringRepresentation::toShortString)
                .isEqualTo(TimePeriod.parse("2010-02-17T11:03:00/03-17T11:03:00"))
                .returns(LocalDateTime.of(2010, 2, 17, 11, 3), TimePeriod::getStart)
                .returns(LocalDateTime.of(2010, 3, 17, 11, 3), TimePeriod::getEnd);

        assertThat(TimePeriod.parse("2010-01-01T00:00:00/2010-02-01T00:00:00"))
                .hasToString("2010-01-01T00:00:00/2010-02-01T00:00:00")
                .returns("2010-01-01T00:00:00/02-01T00:00:00", HasShortStringRepresentation::toShortString)
                .isEqualTo(TimePeriod.parse("2010-01-01T00:00:00/02-01T00:00:00"))
                .returns(LocalDateTime.of(2010, 1, 1, 0, 0), TimePeriod::getStart)
                .returns(LocalDateTime.of(2010, 2, 1, 0, 0), TimePeriod::getEnd);
    }
}
