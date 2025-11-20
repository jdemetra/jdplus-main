/*
 * Copyright 2016 National Bank of Belgium
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

import internal.sql.base.api.ResultSetFunc;
import internal.sql.base.api.SelectBuilder;
import internal.sql.base.api.SqlTableAsCubeUtil;
import jdplus.toolkit.base.api.timeseries.util.ObsCharacteristics;
import jdplus.toolkit.base.api.timeseries.util.ObsGathering;
import jdplus.toolkit.base.api.timeseries.util.TsDataBuilder;
import jdplus.toolkit.base.api.util.Validatable;
import jdplus.toolkit.base.api.util.Validations;
import jdplus.toolkit.base.tsp.cube.CubeId;
import jdplus.toolkit.base.tsp.cube.TableAsCubeConnection;
import jdplus.toolkit.base.tsp.cube.TableAsCubeConnection.*;
import jdplus.toolkit.base.tsp.cube.TableAsCubeUtil;
import jdplus.toolkit.base.tsp.cube.TableDataParams;
import lombok.NonNull;
import nbbrd.design.NotThreadSafe;
import nbbrd.design.VisibleForTesting;
import nbbrd.sql.jdbc.SqlIdentifierQuoter;
import org.jspecify.annotations.Nullable;

import java.sql.*;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static internal.sql.base.api.ResultSetFunc.*;

/**
 * @author Philippe Charles
 */
@SuppressWarnings("RedundantThrows")
@NotThreadSafe
@lombok.Builder(buildMethodName = "buildWithoutValidation")
public final class SqlTableAsCubeResource implements TableAsCubeConnection.Resource<java.util.Date>, Validatable<SqlTableAsCubeResource> {

    @lombok.NonNull
    private final ConnectionSource source;

    @lombok.NonNull
    private final String table;

    @lombok.NonNull
    private final CubeId root;

    @lombok.NonNull
    private final TableDataParams tdp;

    @lombok.NonNull
    private final ObsGathering gathering;

    @lombok.NonNull
    private final String labelColumn;

    private final Consumer<String> onCall;

    @Override
    public Exception testConnection() {
        try (Connection ignore = source.open()) {
            return null;
        } catch (SQLException ex) {
            return ex;
        }
    }

    @Override
    public @NonNull CubeId getRoot() throws Exception {
        return root;
    }

    @Override
    public @NonNull AllSeriesCursor getAllSeriesCursor(@NonNull CubeId id) throws Exception {
        return new AllSeriesQuery(checkNode(id), table, labelColumn).call(source, onCall);
    }

    @Override
    public @NonNull AllSeriesWithDataCursor<java.util.Date> getAllSeriesWithDataCursor(@NonNull CubeId id) throws Exception {
        return new AllSeriesWithDataQuery(checkNode(id), table, labelColumn, tdp).call(source, onCall);
    }

    @Override
    public @NonNull SeriesCursor getSeriesCursor(@NonNull CubeId id) throws Exception {
        checkLeaf(id);
        if (labelColumn.isEmpty()) {
            return SqlTableAsCubeUtil.noLabelSeriesCursor();
        }
        return new SeriesQuery(id, table, labelColumn).call(source, onCall);
    }

    @Override
    public @NonNull SeriesWithDataCursor<java.util.Date> getSeriesWithDataCursor(@NonNull CubeId id) throws Exception {
        return new SeriesWithDataQuery(checkLeaf(id), table, labelColumn, tdp).call(source, onCall);
    }

    @Override
    public @NonNull ChildrenCursor getChildrenCursor(@NonNull CubeId id) throws Exception {
        return new ChildrenQuery(checkNode(id), table).call(source, onCall);
    }

    @Override
    public @NonNull TsDataBuilder<java.util.Date> newBuilder() {
        return TsDataBuilder.byCalendar(new GregorianCalendar(TimeZone.getDefault(), Locale.getDefault()), gathering, ObsCharacteristics.ORDERED);
    }

    @Override
    public @NonNull String getDisplayName() throws Exception {
        return TableAsCubeUtil.getDisplayName(source.getId(), table, tdp.getValueColumn(), gathering);
    }

    @Override
    public @NonNull String getDisplayName(@NonNull CubeId id) throws Exception {
        return TableAsCubeUtil.getDisplayName(id, LABEL_COLLECTOR);
    }

    @Override
    public @NonNull String getDisplayNodeName(@NonNull CubeId id) throws Exception {
        return TableAsCubeUtil.getDisplayNodeName(id);
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public @NonNull SqlTableAsCubeResource validate() throws IllegalArgumentException {
        Validations.notBlank(table, "table");
        return this;
    }

    public static final class Builder implements Validatable.Builder<SqlTableAsCubeResource> {
    }

    private static CubeId checkNode(CubeId id) {
        if (id.isSeries() || id.isVoid()) {
            throw new IllegalArgumentException(id.toString());
        }
        return id;
    }

    private static CubeId checkLeaf(CubeId id) {
        if (!id.isSeries() && !id.isVoid()) {
            throw new IllegalArgumentException(id.toString());
        }
        return id;
    }

    private static final Collector<? super String, ?, String> LABEL_COLLECTOR = Collectors.joining(", ");

    private static void closeAll(Exception root, AutoCloseable... items) {
        for (AutoCloseable o : items) {
            if (o != null) {
                try {
                    o.close();
                } catch (Exception ex) {
                    if (root == null) {
                        root = ex;
                    } else {
                        root.addSuppressed(ex);
                    }
                }
            }
        }
    }

    private static void close(SQLException ex, ResultSet rs, PreparedStatement stmt, Connection conn) {
        closeAll(ex, rs, stmt, conn);
    }

    private static AutoCloseable asCloseable(ResultSet rs, PreparedStatement stmt, Connection conn) {
        return () -> close(null, rs, stmt, conn);
    }

    private static String[] toSelect(CubeId ref) {
        String[] result = new String[ref.getDepth()];
        for (int i = 0; i < result.length; i++) {
            result[i] = ref.getDimensionId(ref.getLevel() + i);
        }
        return result;
    }

    private static String[] toFilter(CubeId ref) {
        String[] result = new String[ref.getLevel()];
        for (int i = 0; i < result.length; i++) {
            result[i] = ref.getDimensionId(i);
        }
        return result;
    }

    /**
     * A class that handles SQL queries from Jdbc.
     *
     * @author Philippe Charles
     */
    @VisibleForTesting
    sealed interface JdbcQuery<T extends AutoCloseable> permits AllSeriesQuery, AllSeriesWithDataQuery, SeriesQuery, SeriesWithDataQuery, ChildrenQuery {

        /**
         * Creates an SQL statement that may contain one or more '?' IN
         * parameter placeholders
         *
         * @return a SQL statement
         */
        @NonNull
        String getQueryString(@NonNull DatabaseMetaData metaData) throws SQLException;

        void setParameters(@NonNull PreparedStatement statement) throws SQLException;

        /**
         * Process the specified ResultSet in order to create the expected
         * result.
         *
         * @param rs the ResultSet to be processed
         * @return a non-null resource
         * @throws SQLException if something goes wrong on JDBC side
         */
        @NonNull
        T process(@NonNull ResultSet rs, @NonNull AutoCloseable closeable) throws SQLException;

        @NonNull
        default T call(@NonNull ConnectionSource supplier, @Nullable Consumer<String> onCall) throws SQLException {
            Connection conn = null;
            PreparedStatement cmd = null;
            ResultSet rs = null;
            try {
                conn = supplier.open();
                String queryString = getQueryString(conn.getMetaData());
                if (onCall != null) {
                    onCall.accept("Calling " + getClass().getSimpleName() + "' with '" + queryString + "'");
                }
                cmd = conn.prepareStatement(queryString);
                setParameters(cmd);
                rs = cmd.executeQuery();
                return process(rs, asCloseable(rs, cmd, conn));
            } catch (SQLException ex) {
                close(ex, rs, cmd, conn);
                throw ex;
            }
        }
    }

    @VisibleForTesting
    @lombok.AllArgsConstructor
    static final class AllSeriesQuery implements JdbcQuery<AllSeriesCursor> {

        private final CubeId ref;
        private final String tableName;
        private final String labelColumn;

        @Override
        public @NonNull String getQueryString(@NonNull DatabaseMetaData metaData) throws SQLException {
            return SelectBuilder.from(tableName)
                    .distinct(true)
                    .select(toSelect(ref)).select(labelColumn)
                    .filter(toFilter(ref))
                    .orderBy(toSelect(ref))
                    .withQuoter(SqlIdentifierQuoter.of(metaData))
                    .build();
        }

        @Override
        public void setParameters(@NonNull PreparedStatement statement) throws SQLException {
            for (int i = 0; i < ref.getLevel(); i++) {
                statement.setString(i + 1, ref.getDimensionValue(i));
            }
        }

        @Override
        public @NonNull AllSeriesCursor process(@NonNull ResultSet rs, @NonNull AutoCloseable closeable) {
            ResultSetFunc<String[]> toDimValues = onGetStringArray(1, ref.getDepth());
            ResultSetFunc<String> toLabel = !labelColumn.isEmpty() ? onGetString(2) : onNull();

            return SqlTableAsCubeUtil.allSeriesCursor(rs, closeable, toDimValues, toLabel);
        }
    }

    @VisibleForTesting
    @lombok.AllArgsConstructor
    static final class AllSeriesWithDataQuery implements JdbcQuery<AllSeriesWithDataCursor<java.util.Date>> {

        private final CubeId ref;
        private final String tableName;
        private final String labelColumn;
        private final TableDataParams tdp;

        @Override
        public @NonNull String getQueryString(@NonNull DatabaseMetaData metaData) throws SQLException {
            return SelectBuilder.from(tableName)
                    .select(toSelect(ref)).select(tdp.getPeriodColumn(), tdp.getValueColumn()).select(labelColumn)
                    .filter(toFilter(ref))
                    .orderBy(toSelect(ref)).orderBy(tdp.getPeriodColumn(), tdp.getVersionColumn())
                    .withQuoter(SqlIdentifierQuoter.of(metaData))
                    .build();
        }

        @Override
        public void setParameters(@NonNull PreparedStatement statement) throws SQLException {
            for (int i = 0; i < ref.getLevel(); i++) {
                statement.setString(i + 1, ref.getDimensionValue(i));
            }
        }

        @Override
        public @NonNull AllSeriesWithDataCursor<java.util.Date> process(@NonNull ResultSet rs, @NonNull AutoCloseable closeable) throws SQLException {
            // Beware that some jdbc drivers require to get the columns values
            // in the order of the query and only once.
            // So, call the following methods once per row and in this order.
            ResultSetMetaData metaData = rs.getMetaData();
            ResultSetFunc<String[]> toDimValues = onGetStringArray(1, ref.getDepth());
            ResultSetFunc<java.util.Date> toPeriod = onDate(metaData, ref.getDepth() + 1, tdp.getObsFormat().calendarParser());
            ResultSetFunc<Number> toValue = onNumber(metaData, ref.getDepth() + 2, tdp.getObsFormat().numberParser());
            ResultSetFunc<String> toLabel = !labelColumn.isEmpty() ? onGetString(ref.getDepth() + 3) : onNull();

            return SqlTableAsCubeUtil.allSeriesWithDataCursor(rs, closeable, toDimValues, toPeriod, toValue, toLabel);
        }
    }

    @VisibleForTesting
    @lombok.AllArgsConstructor
    static final class SeriesQuery implements JdbcQuery<SeriesCursor> {

        private final CubeId ref;
        private final String tableName;
        private final String labelColumn;

        @Override
        public @NonNull String getQueryString(@NonNull DatabaseMetaData metaData) throws SQLException {
            return SelectBuilder.from(tableName)
                    .distinct(true)
                    .select(labelColumn)
                    .filter(toFilter(ref))
                    .withQuoter(SqlIdentifierQuoter.of(metaData))
                    .build();
        }

        @Override
        public void setParameters(@NonNull PreparedStatement statement) throws SQLException {
            for (int i = 0; i < ref.getLevel(); i++) {
                statement.setString(i + 1, ref.getDimensionValue(i));
            }
        }

        @Override
        public @NonNull SeriesCursor process(@NonNull ResultSet rs, @NonNull AutoCloseable closeable) {
            ResultSetFunc<String> toLabel = !labelColumn.isEmpty() ? onGetString(1) : onNull();

            return SqlTableAsCubeUtil.seriesCursor(rs, closeable, toLabel);
        }
    }

    @VisibleForTesting
    @lombok.AllArgsConstructor
    static final class SeriesWithDataQuery implements JdbcQuery<SeriesWithDataCursor<java.util.Date>> {

        private final CubeId ref;
        private final String tableName;
        private final String labelColumn;
        private final TableDataParams tdp;

        @Override
        public @NonNull String getQueryString(@NonNull DatabaseMetaData metaData) throws SQLException {
            return SelectBuilder.from(tableName)
                    .select(tdp.getPeriodColumn(), tdp.getValueColumn()).select(labelColumn)
                    .filter(toFilter(ref))
                    .orderBy(tdp.getPeriodColumn(), tdp.getVersionColumn())
                    .withQuoter(SqlIdentifierQuoter.of(metaData))
                    .build();
        }

        @Override
        public void setParameters(@NonNull PreparedStatement statement) throws SQLException {
            for (int i = 0; i < ref.getLevel(); i++) {
                statement.setString(i + 1, ref.getDimensionValue(i));
            }
        }

        @Override
        public @NonNull SeriesWithDataCursor<java.util.Date> process(@NonNull ResultSet rs, @NonNull AutoCloseable closeable) throws SQLException {
            // Beware that some jdbc drivers require to get the columns values
            // in the order of the query and only once.
            // So, call the following methods once per row and in this order.
            ResultSetMetaData metaData = rs.getMetaData();
            ResultSetFunc<java.util.Date> toPeriod = onDate(metaData, 1, tdp.getObsFormat().calendarParser());
            ResultSetFunc<Number> toValue = onNumber(metaData, 2, tdp.getObsFormat().numberParser());
            ResultSetFunc<String> toLabel = !labelColumn.isEmpty() ? onGetString(3) : onNull();

            return SqlTableAsCubeUtil.seriesWithDataCursor(rs, closeable, toPeriod, toValue, toLabel);
        }
    }

    @VisibleForTesting
    @lombok.AllArgsConstructor
    static final class ChildrenQuery implements JdbcQuery<ChildrenCursor> {

        private final CubeId ref;
        private final String tableName;

        @Override
        public @NonNull String getQueryString(@NonNull DatabaseMetaData metaData) throws SQLException {
            String column = ref.getDimensionId(ref.getLevel());
            return SelectBuilder.from(tableName)
                    .distinct(true)
                    .select(column)
                    .filter(toFilter(ref))
                    .orderBy(column)
                    .withQuoter(SqlIdentifierQuoter.of(metaData))
                    .build();
        }

        @Override
        public void setParameters(@NonNull PreparedStatement statement) throws SQLException {
            for (int i = 0; i < ref.getLevel(); i++) {
                statement.setString(i + 1, ref.getDimensionValue(i));
            }
        }

        @Override
        public @NonNull ChildrenCursor process(@NonNull ResultSet rs, @NonNull AutoCloseable closeable) {
            ResultSetFunc<String> toChild = onGetString(1);

            return SqlTableAsCubeUtil.childrenCursor(rs, closeable, toChild);
        }
    }
}
