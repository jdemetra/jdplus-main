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
package jdplus.toolkit.base.tsp.util;

import lombok.NonNull;
import nbbrd.design.NotThreadSafe;
import org.jspecify.annotations.Nullable;

/**
 * @author Philippe Charles
 */
@NotThreadSafe
public interface ShortLivedCache<K, V> {

    void put(@NonNull K key, @NonNull V value);

    @Nullable V get(@NonNull K key);
}
