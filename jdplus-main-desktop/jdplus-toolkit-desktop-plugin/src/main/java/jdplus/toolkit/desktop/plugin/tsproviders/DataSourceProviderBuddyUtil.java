/*
 * Copyright 2013 National Bank of Belgium
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
package jdplus.toolkit.desktop.plugin.tsproviders;

import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.desktop.plugin.Config;
import lombok.NonNull;
import org.openide.ErrorManager;

import java.beans.IntrospectionException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Philippe Charles
 */
public final class DataSourceProviderBuddyUtil {

    private DataSourceProviderBuddyUtil() {
        // static class
    }

    @NonNull
    public static String getDataSourceDomain() {
        return DataSource.class.getName();
    }

    @NonNull
    public static DataSource getDataSource(@NonNull Config config) throws IllegalArgumentException {
        String uri = config.getParameter("uri");
        if (uri == null) {
            throw new IllegalArgumentException("Invalid config");
        }
        return DataSource.parse(uri);
    }

    public static Config getConfig(DataSource dataSource, String displayName) {
        return Config.builder(getDataSourceDomain(), displayName, "")
                .parameter("uri", dataSource.toString())
                .build();
    }

    @FunctionalInterface
    public interface IntrospectionFunc<X, Y> {

        Y apply(X x) throws IntrospectionException;
    }

    @NonNull
    public static <X, Y> Function<X, Y> usingErrorManager(IntrospectionFunc<X, Y> func, Supplier<Y> defaultValue) {
        return (X x) -> {
            try {
                return func.apply(x);
            } catch (IntrospectionException ex) {
                ErrorManager.getDefault().log(ex.getMessage());
                return defaultValue.get();
            }
        };
    }
}
