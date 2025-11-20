/*
 * Copyright 2018 National Bank of Belgium
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
package jdplus.sql.base.api;

import internal.sql.base.api.SqlPropertiesSupport;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * @author Philippe Charles
 */
public interface HasSqlProperties {

    @NonNull
    ConnectionManager getConnectionManager();

    void setConnectionManager(@Nullable ConnectionManager manager);

    @NonNull
    static HasSqlProperties of(@NonNull Supplier<ConnectionManager> defaultManager, @NonNull Runnable onManagerChange) {
        return new SqlPropertiesSupport(
                defaultManager,
                new AtomicReference<>(defaultManager.get()),
                onManagerChange);
    }
}
