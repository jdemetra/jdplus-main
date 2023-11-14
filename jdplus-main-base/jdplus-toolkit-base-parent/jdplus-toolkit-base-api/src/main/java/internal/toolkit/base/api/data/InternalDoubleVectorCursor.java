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

import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class InternalDoubleVectorCursor {

    public static class DefaultDoubleVectorCursor<T extends DoubleSeq.Mutable> extends InternalDoubleSeqCursor.DefaultDoubleSeqCursor<T> implements DoubleSeqCursor.OnMutable {

        public DefaultDoubleVectorCursor(T data) {
            super(data);
        }

        @Override
        public void setAndNext(double newValue) {
            data.set(cursor++, newValue);
        }

        @Override
        public void applyAndNext(DoubleUnaryOperator fn) {
            data.apply(cursor++, fn);
        }
    }

    public static class MultiDoubleVectorCursor extends InternalDoubleSeqCursor.MultiDoubleSeqCursor implements DoubleSeqCursor.OnMutable {

        public MultiDoubleVectorCursor(double[] values) {
            super(values);
        }

        @Override
        public void setAndNext(double newValue) throws IndexOutOfBoundsException {
            values[cursor++] = newValue;
        }

        @Override
        public void applyAndNext(DoubleUnaryOperator fn) throws IndexOutOfBoundsException {
            values[cursor] = fn.applyAsDouble(values[cursor]);
            cursor++;
        }
    }

    public static class SubDoubleVectorCursor extends InternalDoubleSeqCursor.SubDoubleSeqCursor implements DoubleSeqCursor.OnMutable {

        public SubDoubleVectorCursor(double[] values, int begin) {
            super(values, begin);
        }

        @Override
        public void setAndNext(double newValue) throws IndexOutOfBoundsException {
            values[cursor++] = newValue;
        }

        @Override
        public void applyAndNext(DoubleUnaryOperator fn) throws IndexOutOfBoundsException {
            values[cursor] = fn.applyAsDouble(values[cursor]);
            cursor++;
        }
    }
}
