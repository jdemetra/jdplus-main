/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved
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
package jdplus.toolkit.base.api.timeseries.calendars;

import jdplus.toolkit.base.api.timeseries.HasAnnualFrequency;
import jdplus.toolkit.base.api.timeseries.TsException;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import lombok.NonNull;
import nbbrd.design.Development;
import nbbrd.design.RepresentableAs;
import nbbrd.design.RepresentableAsInt;
import nbbrd.design.StaticFactoryMethod;

/**
 * Frequency of an event.
 * Only regular frequencies higher or equal to yearly frequency are considered.
 *
 * @author Jean Palate
 */
@RepresentableAsInt
@RepresentableAs(value = TsUnit.class, parseMethodName = "parseTsUnit")
@Development(status = Development.Status.Release)
public enum RegularFrequency {
    /**
     * Undefined frequency. To be used when the frequency of an event is
     * unknown.
     */
    Undefined(0),
    /**
     * One event by year
     */
    Yearly(1),
    /*
     * One event every half-year
     */
    /**
     *
     */
    HalfYearly(2),
    /*
     * One event every four months
     */
    /**
     *
     */
    QuadriMonthly(3),
    /*
     * One event every quarter
     */
    /**
     *
     */
    Quarterly(4),
    /*
     * One event every two months
     */
    /**
     *
     */
    BiMonthly(6),
    /*
     * One event every month
     */
    /**
     *
     */
    Monthly(12);

    private static final RegularFrequency[] ENUMS = RegularFrequency.values();

    /**
     * Enum correspondence to an integer
     *
     * @param value Integer representation of the frequency
     * @return Enum representation of the frequency
     */
    @StaticFactoryMethod
    public static @NonNull RegularFrequency parse(int value) throws IllegalArgumentException {
        if (value <= 0)
            return Undefined;
        if (12 % value == 0) {
            for (RegularFrequency anEnum : ENUMS) {
                if (value == anEnum.value) {
                    return anEnum;
                }
            }
        }
        throw new IllegalArgumentException("Cannot parse " + value);
    }

    @StaticFactoryMethod
    public static @NonNull RegularFrequency parse(@NonNull HasAnnualFrequency object) throws IllegalArgumentException {
        return parse(object.getAnnualFrequency());
    }

    private final int value;

    @Deprecated
    public static RegularFrequency[] all() {
        return RegularFrequency.values();
    }

    RegularFrequency(final int value) {
        this.value = value;
    }

    /**
     * Integer representation of the frequency
     *
     * @return The number of events by year
     */
    public int toInt() {
        return value;
    }

    /**
     * Checks that any period of the given frequency is strictly contained in
     * a period of this frequency
     *
     * @param other The other frequency to be checked
     * @return True if other is a multiple of this frequency,
     * false otherwise
     */
    public boolean contains(RegularFrequency other) {
        return other.value > value && other.value % value == 0;
    }

    public int ratio(RegularFrequency other) {
        if (value % other.value != 0) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
        return value / other.value;
    }

    public @NonNull TsUnit toTsUnit() {
        return switch (this) {
            case Yearly -> TsUnit.P1Y;
            case HalfYearly -> TsUnit.P6M;
            case QuadriMonthly -> TsUnit.P4M;
            case Quarterly -> TsUnit.P3M;
            case BiMonthly -> TsUnit.P2M;
            case Monthly -> TsUnit.P1M;
            case Undefined -> TsUnit.UNDEFINED;
        };
    }

    @StaticFactoryMethod
    public static @NonNull RegularFrequency parseTsUnit(@NonNull TsUnit unit) throws IllegalArgumentException {
        return parse(unit.getAnnualFrequency());
    }
}
