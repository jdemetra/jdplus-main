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

import lombok.NonNull;
import nbbrd.design.Development;
import internal.toolkit.base.api.data.InternalSeqCursor;
import java.util.function.UnaryOperator;
import nbbrd.design.NonNegative;

/**
 * Describes a writable sequence of elements.
 *
 * @author Philippe Charles
 * @param <E>
 */
@Development(status = Development.Status.Release)
public interface Vector<E> extends Seq<E> {

    /**
     * Sets value at the specified index.
     *
     *
     * @param index the index of the value to be modified
     * @param value the specified value
     */
    void set(@NonNegative int index, E value) throws IndexOutOfBoundsException;

    default void apply(@NonNegative int index, UnaryOperator<E> fn) throws IndexOutOfBoundsException {
        set(index, fn.apply(get(index)));
    }

    @Override
    default @NonNull VectorCursor<E> cursor() {
        return new InternalSeqCursor.DefaultVectorCursor<>(this);
    }
}
