/*
 * Copyright 2021 National Bank of Belgium
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
package jdplus.toolkit.base.api.data;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.Development;
import nbbrd.design.Immutable;
import nbbrd.design.Internal;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.NonNegative;

import java.util.function.IntToDoubleFunction;
import java.util.stream.DoubleStream;

/**
 * An immutable sequence of doubles.
 *
 * @author Philippe Charles
 */
@Immutable
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
@lombok.EqualsAndHashCode
@Development(status = Development.Status.Release)
public final class Doubles implements DoubleSeq {

    public static final Doubles EMPTY = ofInternal(new double[0]);
    public static final Doubles ZERO = of(0.0);
    public static final Doubles ONE = of(1.0);

    @StaticFactoryMethod
    @NonNull
    public static Doubles of(@NonNegative int length, @NonNull IntToDoubleFunction generator) {
        double[] values = new double[length];
        for (int i = 0; i < values.length; i++) {
            values[i] = generator.applyAsDouble(i);
        }
        return new Doubles(values);
    }

    @StaticFactoryMethod
    @NonNull
    public static Doubles of(@NonNull DoubleStream stream) {
        return new Doubles(stream.toArray());
    }

    @StaticFactoryMethod
    @NonNull
    public static Doubles of(@NonNull DoubleSeq seq) {
        return seq instanceof Doubles ? (Doubles) seq : new Doubles(seq.toArray());
    }

    @StaticFactoryMethod
    @NonNull
    public static Doubles of(double value) {
        return new Doubles(new double[]{value});
    }

    @StaticFactoryMethod
    @NonNull
    public static Doubles of(double @NonNull [] values) {
        return new Doubles(values.clone());
    }

    @Internal
    @NonNull
    public static Doubles ofInternal(double @NonNull [] safeArray) {
        return new Doubles(safeArray);
    }

    private final double[] values;

    @Override
    public double get(int index) throws IndexOutOfBoundsException {
        return values[index];
    }

    @Override
    public int length() {
        return values.length;
    }

    @Override
    public String toString() {
        return DoubleSeq.format(this);
    }
}
