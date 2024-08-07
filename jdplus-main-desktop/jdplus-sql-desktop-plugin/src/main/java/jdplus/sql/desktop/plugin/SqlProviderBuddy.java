/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved
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
package jdplus.sql.desktop.plugin;

import ec.util.completion.AutoCompletionSource;
import ec.util.completion.ExtAutoCompletionSource;
import jdplus.sql.base.api.ConnectionManager;
import nbbrd.sql.jdbc.SqlColumn;
import nbbrd.sql.jdbc.SqlIdentifierQuoter;
import nbbrd.sql.jdbc.SqlTable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static ec.util.completion.AutoCompletionSource.Behavior.*;

/**
 * An abstract provider buddy that targets Jdbc providers.
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SqlProviderBuddy {

    private static boolean isValidConnectionString(Supplier<String> connectionString) {
        String value = connectionString.get();
        return value != null && !value.isEmpty();
    }

    private static boolean isValidTableName(Supplier<String> tableName) {
        String value = tableName.get();
        return value != null && !value.isEmpty();
    }

    public static AutoCompletionSource getTableSource(ConnectionManager manager, Supplier<String> connectionString, Supplier<String> tableName) {
        return ExtAutoCompletionSource
                .builder(o -> getJdbcTables(manager, connectionString.get()))
                .behavior(o -> isValidConnectionString(connectionString) ? ASYNC : NONE)
                .postProcessor(SqlProviderBuddy::getJdbcTables)
                .valueToString(SqlTable::getName)
                .cache(new ConcurrentHashMap<>(), o -> connectionString.get(), SYNC)
                .build();
    }

    public static AutoCompletionSource getColumnSource(ConnectionManager manager, Supplier<String> connectionString, Supplier<String> tableName) {
        return ExtAutoCompletionSource
                .builder(o -> getJdbcColumns(manager, connectionString.get(), tableName.get()))
                .behavior(o -> isValidConnectionString(connectionString) && isValidTableName(tableName) ? ASYNC : NONE)
                .postProcessor(SqlProviderBuddy::getJdbcColumns)
                .valueToString(SqlColumn::getName)
                .cache(new ConcurrentHashMap<>(), o -> connectionString.get() + "/" + tableName.get(), SYNC)
                .build();
    }

    private static List<SqlTable> getJdbcTables(ConnectionManager manager, String connectionString) throws SQLException {
        try (Connection c = manager.getSource(connectionString).open()) {
            return SqlTable.allOf(c.getMetaData(), c.getCatalog(), c.getSchema(), "%", new String[]{"TABLE", "VIEW"});
        }
    }

    private static List<SqlColumn> getJdbcColumns(ConnectionManager manager, String connectionString, String tableName) throws SQLException {
        try (Connection c = manager.getSource(connectionString).open()) {
            SqlIdentifierQuoter quoter = SqlIdentifierQuoter.of(c.getMetaData());
            try (Statement st = c.createStatement()) {
                try (ResultSet rs = st.executeQuery("select * from " + quoter.quote(tableName, false) + " where 1 = 0")) {
                    return SqlColumn.allOf(rs.getMetaData());
                }
            }
        }
    }

    private static List<SqlTable> getJdbcTables(List<SqlTable> values, String term) {
        Predicate<String> filter = ExtAutoCompletionSource.basicFilter(term);
        return values.stream()
                .filter(o -> filter.test(o.getName()) || filter.test(o.getSchema()) || filter.test(o.getCatalog()) || filter.test(o.getRemarks()))
                .sorted(Comparator.comparing(SqlTable::getName))
                .collect(Collectors.toList());
    }

    private static List<SqlColumn> getJdbcColumns(List<SqlColumn> values, String term) {
        Predicate<String> filter = ExtAutoCompletionSource.basicFilter(term);
        return values.stream()
                .filter(o -> filter.test(o.getName()) || filter.test(o.getLabel()) || filter.test(o.getTypeName()))
                .sorted(Comparator.comparing(SqlColumn::getName))
                .collect(Collectors.toList());
    }
}
