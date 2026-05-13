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
package jdplus.tramoseats.base.api.tramo;

import lombok.NonNull;
import nbbrd.design.Development;
import nbbrd.design.RepresentableAsInt;
import nbbrd.design.StaticFactoryMethod;

/**
 * Frequency of an event. Only regular frequencies higher or equal to yearly
 * frequency are considered.
 *
 * @author Jean Palate
 */
@RepresentableAsInt
@Development(status = Development.Status.Release)
public enum TramoFrequency {
    /**
     * Undefined frequency. For legacy purposes
     */
    Undefined(-1),
    /**
     * Any frequency.
     */
    Any(0),
    /**
     * One event by year
     */
    Yearly(1),
    /**
     * One event every half-year
     */
    HalfYearly(2),
    /**
     * One event every four months
     */
    QuadriMonthly(3),
    /*
     * One event every quarter
     */
    Quarterly(4),
    /*
     * One event every two months
     */
    BiMonthly(6),
    /**
     * One event every month
     */
    Monthly(12);

    private static final TramoFrequency[] ENUMS = TramoFrequency.values();

    /**
     * Enum correspondence to an integer
     *
     * @param value Integer representation of the frequency
     * @return Enum representation of the frequency
     */
    @StaticFactoryMethod
    public static @NonNull
    TramoFrequency parse(int value) throws IllegalArgumentException {

        switch (value) {
            case -1 -> {
                return Undefined;
            }
            case 0 -> {
                return Any;
            }
            case 1 -> {
                return Yearly;
            }
            case 2 -> {
                return HalfYearly;
            }
            case 3 -> {
                return QuadriMonthly;
            }
            case 4 -> {
                return Quarterly;
            }
            case 6 -> {
                return BiMonthly;
            }
            case 12 -> {
                return Monthly;
            }
            default -> {
                throw new IllegalArgumentException("Cannot parse " + value);
            }
        }
    }

    private final int value;

    TramoFrequency(final int value) {
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

}
