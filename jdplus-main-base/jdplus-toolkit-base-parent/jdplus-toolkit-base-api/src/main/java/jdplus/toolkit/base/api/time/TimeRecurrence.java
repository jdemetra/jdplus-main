/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.toolkit.base.api.time;

import jdplus.toolkit.base.api.data.Seq;
import lombok.NonNull;

/**
 * Framework-level interface defining repeating intervals formed by a number of
 * repetitions and a time interval.
 *
 * @param <I>
 * @author Philippe Charles
 * @see <a href="https://en.wikipedia.org/wiki/ISO_8601#Repeating_intervals">Repeating
 * intervals in ISO_8601</a>
 */
@ISO_8601
public interface TimeRecurrence<I extends TimeInterval<?, ?>> extends TimeRecurrenceAccessor, Seq<I> {

    @NonNull
    I getInterval();
}
