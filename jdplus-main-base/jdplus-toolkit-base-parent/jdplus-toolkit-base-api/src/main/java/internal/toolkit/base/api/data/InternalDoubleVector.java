/*
 * Copyright 2019 National Bank of Belgium
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
package internal.toolkit.base.api.data;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.api.data.Doubles;
import jdplus.toolkit.base.api.util.function.IntDoubleConsumer;
import lombok.NonNull;

import java.util.function.IntToDoubleFunction;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class InternalDoubleVector {

    public static class MultiDoubleVector extends InternalDoubleSeq.MultiDoubleSeq implements DoubleSeq.Mutable {

        public static final MultiDoubleVector EMPTY = new MultiDoubleVector(Doubles.EMPTYARRAY);

        public MultiDoubleVector(double[] values) {
            super(values);
        }

        @Override
        public void set(int index, double value) throws IndexOutOfBoundsException {
            values[index] = value;
        }

        @Override
        public DoubleSeqCursor.@NonNull OnMutable cursor() {
            return new InternalDoubleVectorCursor.MultiDoubleVectorCursor(values);
        }
    }

    public static class SubDoubleVector extends InternalDoubleSeq.SubDoubleSeq implements DoubleSeq.Mutable {

        public SubDoubleVector(double[] values, int begin, int length) {
            super(values, begin, length);
        }

        @Override
        public void set(int index, double value) throws IndexOutOfBoundsException {
            values[begin + index] = value;
        }

        @Override
        public DoubleSeqCursor.@NonNull OnMutable cursor() {
            return new InternalDoubleVectorCursor.SubDoubleVectorCursor(values, begin);
        }
    }

    public static class MappingDoubleVector extends InternalDoubleSeq.MappingDoubleSeq implements DoubleSeq.Mutable {

        private final IntDoubleConsumer setter;

        public MappingDoubleVector(int length, IntToDoubleFunction getter, IntDoubleConsumer setter) {
            super(length, getter);
            this.setter = setter;
        }

        @Override
        public void set(int index, double value) throws IndexOutOfBoundsException {
            setter.accept(index, value);
        }

        @Override
        public DoubleSeqCursor.@NonNull OnMutable cursor() {
            return new InternalDoubleVectorCursor.DefaultDoubleVectorCursor<>(this);
        }

        @Override
        public String toString() {
            return DoubleSeq.format(this);
        }
    }
}
