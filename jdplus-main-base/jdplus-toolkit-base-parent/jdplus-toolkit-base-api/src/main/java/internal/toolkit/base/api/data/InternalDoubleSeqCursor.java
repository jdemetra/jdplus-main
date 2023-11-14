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

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class InternalDoubleSeqCursor {

    public static class DefaultDoubleSeqCursor<T extends DoubleSeq> extends InternalBaseSeqCursor.DefaultBaseSeqCursor<T> implements DoubleSeqCursor {

        public DefaultDoubleSeqCursor(T data) {
            super(data);
        }

        @Override
        public double getAndNext() {
            return data.get(cursor++);
        }
    }

    public static class MultiDoubleSeqCursor extends InternalBaseSeqCursor.MultiBaseSeqCursor implements DoubleSeqCursor {

        protected final double[] values;

        public MultiDoubleSeqCursor(double[] values) {
            this.values = values;
        }

        @Override
        public double getAndNext() throws IndexOutOfBoundsException {
            return values[cursor++];
        }
    }

    public static class SubDoubleSeqCursor extends InternalBaseSeqCursor.SubBaseSeqCursor implements DoubleSeqCursor {

        protected final double[] values;

        public SubDoubleSeqCursor(double[] values, int begin) {
            super(begin);
            this.values = values;
        }

        @Override
        public double getAndNext() throws IndexOutOfBoundsException {
            return values[cursor++];
        }
    }
}
