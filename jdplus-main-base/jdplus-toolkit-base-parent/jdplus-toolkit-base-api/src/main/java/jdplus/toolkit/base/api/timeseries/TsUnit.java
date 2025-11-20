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

import jdplus.toolkit.base.api.time.ISO_8601;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.NonNegative;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.time.temporal.*;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.temporal.ChronoUnit.*;

/**
 * @author Philippe Charles
 */
@ISO_8601
@RepresentableAsString
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TsUnit implements TemporalAmount {

    @NonNegative
    long amount;

    @NonNull
    ChronoUnit chronoUnit;

    public boolean contains(TsUnit other) {
        return other.ratioOf(this) > 0;
    }

    /**
     * Returns the number of time this unit is contained in the given unit
     *
     * @param other
     * @return
     */
    public int ratioOf(@NonNull TsUnit other) {
        double x = 1D * other.chronoUnit.getDuration().getSeconds() / chronoUnit.getDuration().getSeconds() * other.amount / amount;
        if (x < 1) {
            return NO_RATIO;
        }
        if (((int) x) != x) {
            return NO_STRICT_RATIO;
        }
        return (int) x;
    }

    /**
     * Gets the number of periods in one year.
     *
     * @return The number of periods in 1 year or -1 if the unit is not
     * compatible with years
     * @see #NO_ANNUAL_FREQUENCY
     */
    public int getAnnualFrequency() {
        switch (chronoUnit) {
            case YEARS:
                if (amount == 1) {
                    return 1;
                }
                break;
            case MONTHS:
                int n = (int) amount;
                if (12 % n == 0) {
                    return 12 / n;
                }
                break;
        }
        return NO_ANNUAL_FREQUENCY;
    }

    public static final int NO_ANNUAL_FREQUENCY = -1;

    @Override
    public String toString() {
        return toISO8601();
    }

    private String toISO8601() {
        return switch (chronoUnit) {
            case FOREVER -> "";
            case HOURS, MINUTES, SECONDS -> Duration.of(amount, chronoUnit).toString();
            case WEEKS -> "P" + amount + "W";
            default -> Period.from(this).toString();
        };
    }

    public @Nullable ChronoUnit getPrecision() {
        return amount != 0 ? chronoUnit : null;
    }

    @Override
    public long get(TemporalUnit unit) {
        if (!this.chronoUnit.equals(unit)) {
            throw new UnsupportedTemporalTypeException(unit.toString());
        }
        return amount;
    }

    @Override
    public List<TemporalUnit> getUnits() {
        return Collections.singletonList(chronoUnit);
    }

    @Override
    public Temporal addTo(Temporal temporal) {
        return temporal.plus(amount, chronoUnit);
    }

    @Override
    public Temporal subtractFrom(Temporal temporal) {
        return temporal.minus(amount, chronoUnit);
    }

    public static final int NO_RATIO = -1;
    public static final int NO_STRICT_RATIO = 0;

    public static final TsUnit UNDEFINED = new TsUnit(1, FOREVER);

    public static final TsUnit P1Y = new TsUnit(1, YEARS);
    public static final TsUnit P6M = new TsUnit(6, MONTHS);
    public static final TsUnit P4M = new TsUnit(4, MONTHS);
    public static final TsUnit P3M = new TsUnit(3, MONTHS);
    public static final TsUnit P2M = new TsUnit(2, MONTHS);
    public static final TsUnit P1M = new TsUnit(1, MONTHS);
    public static final TsUnit P1W = new TsUnit(1, WEEKS);
    public static final TsUnit P7D = new TsUnit(7, DAYS);
    public static final TsUnit P1D = new TsUnit(1, DAYS);
    public static final TsUnit PT1H = new TsUnit(1, HOURS);
    public static final TsUnit PT1M = new TsUnit(1, MINUTES);
    public static final TsUnit PT1S = new TsUnit(1, SECONDS);

    private static final TsUnit P100Y = new TsUnit(100, YEARS);
    private static final TsUnit P10Y = new TsUnit(10, YEARS);

    @Deprecated
    public static final TsUnit CENTURY = P100Y;
    @Deprecated
    public static final TsUnit DECADE = P10Y;
    @Deprecated
    public static final TsUnit YEAR = P1Y;
    @Deprecated
    public static final TsUnit HALF_YEAR = P6M;
    @Deprecated
    public static final TsUnit QUARTER = P3M;
    @Deprecated
    public static final TsUnit MONTH = P1M;
    @Deprecated
    public static final TsUnit WEEK = P7D;
    @Deprecated
    public static final TsUnit DAY = P1D;
    @Deprecated
    public static final TsUnit HOUR = PT1H;
    @Deprecated
    public static final TsUnit MINUTE = PT1M;
    @Deprecated
    public static final TsUnit SECOND = PT1S;

    @SuppressWarnings({"ConstantValue", "DuplicateBranchesInSwitch"})
    @StaticFactoryMethod
    public static @NonNull TsUnit of(@NonNegative long amount, @NonNull ChronoUnit unit) throws UnsupportedTemporalTypeException {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        return switch (unit) {
            case FOREVER -> UNDEFINED;
            case ERAS -> throw new UnsupportedTemporalTypeException(unit.toString());
            case MILLENNIA -> new TsUnit(1000 * amount, YEARS);
            case CENTURIES -> amount == 1 ? P100Y
                    : new TsUnit(100 * amount, YEARS);
            case DECADES -> amount == 1 ? P10Y
                    : new TsUnit(10 * amount, YEARS);
            case YEARS -> amount == 1 ? P1Y : amount == 10 ? P10Y : amount == 100 ? P100Y
                    : new TsUnit(amount, YEARS);
            case MONTHS ->
                    amount == 1 ? P1M : amount == 2 ? P2M : amount == 3 ? P3M : amount == 4 ? P4M : amount == 6 ? P6M
                            : new TsUnit(amount, MONTHS);
            case WEEKS -> amount == 1 ? P1W
                    : new TsUnit(amount, WEEKS);
            case DAYS -> amount == 1 ? P1D : amount == 7 ? P7D
                    : new TsUnit(amount, DAYS);
            case HALF_DAYS -> new TsUnit(amount, ChronoUnit.HALF_DAYS);
            case HOURS -> amount == 1 ? PT1H
                    : new TsUnit(amount, HOURS);
            case MINUTES -> amount == 1 ? PT1M
                    : new TsUnit(amount, MINUTES);
            case SECONDS -> amount == 1 ? PT1S
                    : new TsUnit(amount, SECONDS);
            case MILLIS -> throw new UnsupportedTemporalTypeException(unit.toString());
            case MICROS -> throw new UnsupportedTemporalTypeException(unit.toString());
            case NANOS -> throw new UnsupportedTemporalTypeException(unit.toString());
        };
    }

    @StaticFactoryMethod
    public static @NonNull TsUnit ofAnnualFrequency(@NonNegative int freq) {
        return switch (freq) {
            case 1 -> P1Y;
            case 2 -> P6M;
            case 3 -> P4M;
            case 4 -> P3M;
            case 6 -> P2M;
            case 12 -> P1M;
            default -> throw new IllegalArgumentException("Illegal annual frequency: " + freq);
        };
    }

    @StaticFactoryMethod
    public static @NonNull TsUnit parse(@NonNull CharSequence text) throws DateTimeParseException {
        if (text.isEmpty()) {
            return UNDEFINED;
        }
        if (text.length() == 1) {
            throw new DateTimeParseException("Text cannot be parsed to a freq", text, 0);
        }
        if (text.charAt(0) != 'P') {
            throw new DateTimeParseException("Text cannot be parsed to a freq", text, 0);
        }
        return text.charAt(1) == 'T' ? parseTimePattern(text) : parseDatePattern(text);
    }

    /**
     * Computes the greatest common divisor of two units.
     *
     * @param a
     * @param b
     * @return
     */
    public static @NonNull TsUnit gcd(@NonNull TsUnit a, @NonNull TsUnit b) {
        if (a.equals(b)) {
            return a;
        }

        long amount = a.getAmount();
        ChronoUnit chronoUnit = a.getChronoUnit();

        if (b.getChronoUnit().compareTo(chronoUnit) < 0) {
            amount = getLowestAmount(amount, chronoUnit, b.getChronoUnit());
            chronoUnit = b.getChronoUnit();
        }
        amount = gcd(amount, b.getAmount());

        return of(amount, chronoUnit);
    }

    private static long gcd(long a, long b) {
        while (b > 0) {
            long temp = b;
            b = a % b; // % is the remainder
            a = temp;
        }
        return a;
    }

    private static TsUnit parseDatePattern(CharSequence text) {
        Matcher m = DATE_PATTERN.matcher(text);
        if (m.matches()) {
            int amount = Integer.parseInt(m.group(1));
            switch (m.group(2).charAt(0)) {
                case 'Y':
                    return of(amount, YEARS);
                case 'M':
                    return of(amount, MONTHS);
                case 'W':
                    return of(amount, WEEKS);
                case 'D':
                    return of(amount, DAYS);
            }
        }
        throw new DateTimeParseException("Text cannot be parsed to a freq", text, 0);
    }

    private static TsUnit parseTimePattern(CharSequence text) {
        Matcher m = TIME_PATTERN.matcher(text);
        if (m.matches()) {
            double amount = Double.parseDouble(m.group(1));
            switch (m.group(2).charAt(0)) {
                case 'H':
                    return of((long) amount, HOURS);
                case 'M':
                    return of((long) amount, MINUTES);
                case 'S':
                    // NOT supported for the moment:  milli, micro, nano
                    return of((long) amount, SECONDS);
            }
        }
        throw new DateTimeParseException("Text cannot be parsed to a freq", text, 0);
    }

    private static final Pattern DATE_PATTERN = Pattern.compile("P([0-9]+)([YMD])", Pattern.CASE_INSENSITIVE);
    private static final Pattern TIME_PATTERN = Pattern.compile("PT([0-9]+)([HMS])", Pattern.CASE_INSENSITIVE);

    private static long getLowestAmount(long lowestAmount, ChronoUnit oldUnit, ChronoUnit newUnit) {
        return oldUnit.compareTo(DAYS) > 0 && newUnit.compareTo(DAYS) <= 0
                ? 1
                : lowestAmount * CHRONO_UNIT_RATIOS_ON_SECONDS[oldUnit.ordinal()][newUnit.ordinal()];
    }

    private static final long[][] CHRONO_UNIT_RATIOS_ON_SECONDS = computeChronoUnitRatiosOnSeconds();

    private static long[][] computeChronoUnitRatiosOnSeconds() {
        ChronoUnit[] units = ChronoUnit.values();
        long[][] result = new long[units.length][units.length];
        for (ChronoUnit oldUnit : units) {
            for (ChronoUnit newUnit : units) {
                result[oldUnit.ordinal()][newUnit.ordinal()]
                        = hasSeconds(oldUnit) && hasSeconds(newUnit)
                        ? oldUnit.getDuration().dividedBy(newUnit.getDuration().getSeconds()).getSeconds()
                        : 0;
            }
        }
        return result;
    }

    private static boolean hasSeconds(ChronoUnit o) {
        return o.getDuration().getSeconds() > 0;
    }
}
