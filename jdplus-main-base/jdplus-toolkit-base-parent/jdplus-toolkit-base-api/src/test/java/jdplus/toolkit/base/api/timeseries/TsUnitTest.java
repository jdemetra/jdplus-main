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
import java.time.temporal.ChronoUnit;
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
        assertThat(of(1, WEEKS)).returns(1L, TsUnit::getAmount).returns(WEEKS, TsUnit::getChronoUnit);
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
        assertThatNullPointerException().isThrownBy(() -> P1Y.ratioOf(null));

        // easy ratio
        assertThat(P1Y.ratioOf(parse("P10Y"))).isEqualTo(10);
        assertThat(P1Y.ratioOf(P1Y)).isEqualTo(1);
        assertThat(P6M.ratioOf(P1Y)).isEqualTo(2);
        assertThat(P3M.ratioOf(P1Y)).isEqualTo(4);
        assertThat(P1M.ratioOf(P1Y)).isEqualTo(12);
        assertThat(P1M.ratioOf(P3M)).isEqualTo(3);

        // no ratio
        assertThat(P1Y.ratioOf(P1M)).isEqualTo(NO_RATIO);
        assertThat(P1Y.ratioOf(P3M)).isEqualTo(NO_RATIO);
        assertThat(P6M.ratioOf(P3M)).isEqualTo(NO_RATIO);

        // difficult ratio
        assertThat(PT1M.ratioOf(P1Y)).isEqualTo(NO_STRICT_RATIO);
        assertThat(P1D.ratioOf(P1Y)).isEqualTo(NO_STRICT_RATIO);
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
        assertThat(P1Y).hasToString("P1Y").isSameAs(of(1, YEARS));
        assertThat(P6M).hasToString("P6M").isSameAs(of(6, MONTHS));
        assertThat(P4M).hasToString("P4M").isSameAs(of(4, MONTHS));
        assertThat(P3M).hasToString("P3M").isSameAs(of(3, MONTHS));
        assertThat(P2M).hasToString("P2M").isSameAs(of(2, MONTHS));
        assertThat(P1M).hasToString("P1M").isSameAs(of(1, MONTHS));
        assertThat(P1W).hasToString("P1W").isSameAs(of(1, WEEKS));
        assertThat(P7D).hasToString("P7D").isSameAs(of(7, DAYS));
        assertThat(P1D).hasToString("P1D").isSameAs(of(1, DAYS));
        assertThat(PT1H).hasToString("PT1H").isSameAs(of(1, HOURS));
        assertThat(PT1M).hasToString("PT1M").isSameAs(of(1, MINUTES));
        assertThat(PT1S).hasToString("PT1S").isSameAs(of(1, SECONDS));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testDeprecatedConstants() {
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

    @Test
    public void testPrecision() {
        assertThat(P1M.getPrecision()).isEqualTo(MONTHS);
        assertThat(P2M.getPrecision()).isEqualTo(MONTHS);
        assertThat(P1Y.getPrecision()).isEqualTo(YEARS);
        assertThat(P0D.getPrecision()).isNull();
    }

    private static final TsUnit P0D = parse("P0D");
    private static final TsUnit P14M = parse("P14M");
    private static final TsUnit P7M = parse("P7M");
    private static final TsUnit P12M = parse("P12M");
    private static final TsUnit P2Y = parse("P2Y");
    private static final TsUnit P26M = parse("P26M");
    private static final TsUnit P2D = parse("P2D");
    private static final TsUnit P10D = parse("P10D");
    private static final TsUnit P11D = parse("P11D");
}
