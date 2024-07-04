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
package internal.sql.base.api;

import jdplus.sql.base.api.ConnectionManager;
import jdplus.sql.base.api.HasSqlProperties;
import lombok.NonNull;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public final class SqlPropertiesSupport implements HasSqlProperties {

    private final Supplier<ConnectionManager> defaultManager;
    private final AtomicReference<ConnectionManager> manager;
    private final Runnable onManagerChange;

    @Override
    public @NonNull ConnectionManager getConnectionManager() {
        return manager.get();
    }

    @Override
    public void setConnectionManager(ConnectionManager manager) {
        ConnectionManager old = this.manager.get();
        if (this.manager.compareAndSet(old, manager != null ? manager : defaultManager.get())) {
            onManagerChange.run();
        }
    }
}
