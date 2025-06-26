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

import org.junit.jupiter.api.Test;

import java.time.format.DateTimeParseException;
import java.time.temporal.UnsupportedTemporalTypeException;

import static java.time.temporal.ChronoUnit.*;
import static jdplus.toolkit.base.api.timeseries.TsUnit.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Philippe Charles
 */
public class TsUnitTest {

    @SuppressWarnings({"null", "DataFlowIssue"})
    @Test
    public void testFactoryOf() {
        assertThatIllegalArgumentException().isThrownBy(() -> of(-1, MONTHS));
        assertThatNullPointerException().isThrownBy(() -> of(1, null));

        assertThat(of(1, FOREVER)).returns(1L, TsUnit::getAmount).returns(FOREVER, TsUnit::getChronoUnit);
        assertThatExceptionOfType(UnsupportedTemporalTypeException.class).isThrownBy(() -> of(1, ERAS));
        assertThat(of(1, MILLENNIA)).returns(1000L, TsUnit::getAmount).returns(YEARS, TsUnit::getChronoUnit);
        assertThat(of(1, CENTURIES)).returns(100L, TsUnit::getAmount).returns(YEARS, TsUnit::getChronoUnit);
        assertThat(of(1, DECADES)).returns(10L, TsUnit::getAmount).returns(YEARS, TsUnit::getChronoUnit);
        assertThat(of(1, YEARS)).returns(1L, TsUnit::getAmount).returns(YEARS, TsUnit::getChronoUnit);
        assertThat(of(1, MONTHS)).returns(1L, TsUnit::getAmount).returns(MONTHS, TsUnit::getChronoUnit);
        assertThat(of(1, WEEKS)).returns(7L, TsUnit::getAmount).returns(DAYS, TsUnit::getChronoUnit);
        assertThat(of(1, DAYS)).returns(1L, TsUnit::getAmount).returns(DAYS, TsUnit::getChronoUnit);
        assertThat(of(1, HALF_DAYS)).returns(1L, TsUnit::getAmount).returns(HALF_DAYS, TsUnit::getChronoUnit);
        assertThat(of(1, HOURS)).returns(1L, TsUnit::getAmount).returns(HOURS, TsUnit::getChronoUnit);
        assertThat(of(1, MINUTES)).returns(1L, TsUnit::getAmount).returns(MINUTES, TsUnit::getChronoUnit);
        assertThat(of(1, SECONDS)).returns(1L, TsUnit::getAmount).returns(SECONDS, TsUnit::getChronoUnit);
        assertThatExceptionOfType(UnsupportedTemporalTypeException.class).isThrownBy(() -> of(1, MILLIS));
        assertThatExceptionOfType(UnsupportedTemporalTypeException.class).isThrownBy(() -> of(1, MICROS));
        assertThatExceptionOfType(UnsupportedTemporalTypeException.class).isThrownBy(() -> of(1, NANOS));
    }

    @SuppressWarnings({"DataFlowIssue", "ResultOfMethodCallIgnored"})
    @Test
    public void testFactoryOfAnnualFrequency() {
        assertThatIllegalArgumentException().isThrownBy(() -> ofAnnualFrequency(-1));
        assertThatIllegalArgumentException().isThrownBy(() -> ofAnnualFrequency(0));
        assertThat(ofAnnualFrequency(1)).returns(1L, TsUnit::getAmount).returns(YEARS, TsUnit::getChronoUnit);
        assertThat(ofAnnualFrequency(2)).returns(6L, TsUnit::getAmount).returns(MONTHS, TsUnit::getChronoUnit);
        assertThat(ofAnnualFrequency(3)).returns(4L, TsUnit::getAmount).returns(MONTHS, TsUnit::getChronoUnit);
        assertThat(ofAnnualFrequency(4)).returns(3L, TsUnit::getAmount).returns(MONTHS, TsUnit::getChronoUnit);
        assertThatIllegalArgumentException().isThrownBy(() -> ofAnnualFrequency(5));
        assertThat(ofAnnualFrequency(6)).returns(2L, TsUnit::getAmount).returns(MONTHS, TsUnit::getChronoUnit);
        assertThatIllegalArgumentException().isThrownBy(() -> ofAnnualFrequency(7));
        assertThatIllegalArgumentException().isThrownBy(() -> ofAnnualFrequency(8));
        assertThatIllegalArgumentException().isThrownBy(() -> ofAnnualFrequency(9));
        assertThatIllegalArgumentException().isThrownBy(() -> ofAnnualFrequency(10));
        assertThatIllegalArgumentException().isThrownBy(() -> ofAnnualFrequency(11));
        assertThat(ofAnnualFrequency(12)).returns(1L, TsUnit::getAmount).returns(MONTHS, TsUnit::getChronoUnit);
        assertThatIllegalArgumentException().isThrownBy(() -> ofAnnualFrequency(13));
        assertThatIllegalArgumentException().isThrownBy(() -> ofAnnualFrequency(24));
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testRatioOf() {
        assertThatNullPointerException().isThrownBy(() -> YEAR.ratioOf(null));

        // easy ratio
        assertThat(YEAR.ratioOf(CENTURY)).isEqualTo(100);
        assertThat(YEAR.ratioOf(DECADE)).isEqualTo(10);
        assertThat(YEAR.ratioOf(YEAR)).isEqualTo(1);
        assertThat(HALF_YEAR.ratioOf(YEAR)).isEqualTo(2);
        assertThat(QUARTER.ratioOf(YEAR)).isEqualTo(4);
        assertThat(MONTH.ratioOf(YEAR)).isEqualTo(12);
        assertThat(MONTH.ratioOf(QUARTER)).isEqualTo(3);

        // no ratio
        assertThat(YEAR.ratioOf(MONTH)).isEqualTo(NO_RATIO);
        assertThat(YEAR.ratioOf(QUARTER)).isEqualTo(NO_RATIO);
        assertThat(HALF_YEAR.ratioOf(QUARTER)).isEqualTo(NO_RATIO);

        // difficult ratio
        assertThat(MINUTE.ratioOf(YEAR)).isEqualTo(NO_STRICT_RATIO);
        assertThat(DAY.ratioOf(YEAR)).isEqualTo(NO_STRICT_RATIO);
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testRepresentableAsString() {
        assertThatNullPointerException().isThrownBy(() -> parse(null));
        assertThatThrownBy(() -> parse("hello")).isInstanceOf(DateTimeParseException.class);

        var inputs = new String[]{"",
                "P1000Y", "P100Y", "P10Y", "P1Y", "P6M", "P3M", "P1M", "P7D",
                "P1D", "PT1H", "PT1M", "PT1S"
        };

        for (String input : inputs) {
            assertThat(parse(input)).hasToString(input);
        }
    }

    @Test
    public void testConstants() {
        assertThat(UNDEFINED).hasToString("").isSameAs(of(1, FOREVER));
        assertThat(CENTURY).hasToString("P100Y").isSameAs(of(100, YEARS));
        assertThat(DECADE).hasToString("P10Y").isSameAs(of(10, YEARS));
        assertThat(YEAR).hasToString("P1Y").isSameAs(of(1, YEARS));
        assertThat(HALF_YEAR).hasToString("P6M").isSameAs(of(6, MONTHS));
        assertThat(QUARTER).hasToString("P3M").isSameAs(of(3, MONTHS));
        assertThat(MONTH).hasToString("P1M").isSameAs(of(1, MONTHS));
        assertThat(WEEK).hasToString("P7D").isSameAs(of(7, DAYS));
        assertThat(DAY).hasToString("P1D").isSameAs(of(1, DAYS));
        assertThat(HOUR).hasToString("PT1H").isSameAs(of(1, HOURS));
        assertThat(MINUTE).hasToString("PT1M").isSameAs(of(1, MINUTES));
        assertThat(SECOND).hasToString("PT1S").isSameAs(of(1, SECONDS));
    }

    @Test
    public void testGcd() {
        assertThat(gcd(P14M, P14M))
                .as("same chrono, same amount")
                .hasToString("P14M");

        assertThat(gcd(P14M, P7M))
                .as("same chrono, compatible amount")
                .hasToString("P7M");

        assertThat(gcd(P14M, P12M))
                .as("same chrono, incompatible amount")
                .hasToString("P2M");

        assertThat(gcd(P2Y, P2M))
                .as("compatible chrono, same amount")
                .hasToString("P2M");

        assertThat(gcd(P2Y, P12M))
                .as("compatible chrono, compatible amount")
                .hasToString("P12M");

        assertThat(gcd(P2Y, P26M))
                .as("compatible chrono, incompatible amount")
                .hasToString("P2M");

        assertThat(gcd(P2M, P2D))
                .as("incompatible chrono, same amount")
                .hasToString("P1D");

        assertThat(gcd(P2M, P10D))
                .as("incompatible chrono, compatible amount")
                .hasToString("P1D");

        assertThat(gcd(P2M, P11D))
                .as("incompatible chrono, incompatible amount")
                .hasToString("P1D");
    }

    private static final TsUnit P14M = parse("P14M");
    private static final TsUnit P7M = parse("P7M");
    private static final TsUnit P12M = parse("P12M");
    private static final TsUnit P2Y = parse("P2Y");
    private static final TsUnit P2M = parse("P2M");
    private static final TsUnit P26M = parse("P26M");
    private static final TsUnit P2D = parse("P2D");
    private static final TsUnit P10D = parse("P10D");
    private static final TsUnit P11D = parse("P11D");
}
