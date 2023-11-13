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

import nbbrd.design.ThreadSafe;
import internal.sql.base.api.SqlTableAsCubeUtil;
import internal.sql.base.api.SelectBuilder;
import internal.sql.base.api.ResultSetFunc;
import nbbrd.design.VisibleForTesting;
import static internal.sql.base.api.ResultSetFunc.onDate;
import static internal.sql.base.api.ResultSetFunc.onGetString;
import static internal.sql.base.api.ResultSetFunc.onGetStringArray;
import static internal.sql.base.api.ResultSetFunc.onNull;
import static internal.sql.base.api.ResultSetFunc.onNumber;
import jdplus.toolkit.base.tsp.cube.CubeId;
import jdplus.toolkit.base.tsp.cube.TableAsCubeConnection;
import jdplus.toolkit.base.tsp.cube.TableAsCubeConnection.AllSeriesCursor;
import jdplus.toolkit.base.tsp.cube.TableAsCubeConnection.AllSeriesWithDataCursor;
import jdplus.toolkit.base.tsp.cube.TableAsCubeConnection.ChildrenCursor;
import jdplus.toolkit.base.tsp.cube.TableAsCubeConnection.SeriesWithDataCursor;
import jdplus.toolkit.base.tsp.cube.TableAsCubeUtil;
import jdplus.toolkit.base.tsp.cube.TableDataParams;
import jdplus.toolkit.base.api.timeseries.util.ObsCharacteristics;
import jdplus.toolkit.base.api.timeseries.util.ObsGathering;
import jdplus.toolkit.base.api.timeseries.util.TsDataBuilder;
import jdplus.toolkit.base.tsp.cube.TableAsCubeConnection.SeriesCursor;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import nbbrd.sql.jdbc.SqlConnectionSupplier;
import nbbrd.sql.jdbc.SqlIdentifierQuoter;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
@ThreadSafe
@lombok.AllArgsConstructor(staticName = "of")
public final class SqlTableAsCubeResource implements TableAsCubeConnection.Resource<java.util.Date> {

    @lombok.NonNull
    private final SqlConnectionSupplier supplier;

    @lombok.NonNull
    private final String db;

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

    @Override
    public Exception testConnection() {
        return null;
    }

    @Override
    public @NonNull CubeId getRoot() throws Exception {
        return root;
    }

    @Override
    public @NonNull AllSeriesCursor getAllSeriesCursor(@NonNull CubeId id) throws Exception {
        return new AllSeriesQuery(id, table, labelColumn).call(supplier, db);
    }

    @Override
    public @NonNull AllSeriesWithDataCursor<java.util.Date> getAllSeriesWithDataCursor(@NonNull CubeId id) throws Exception {
        return new AllSeriesWithDataQuery(id, table, labelColumn, tdp).call(supplier, db);
    }

    @Override
    public @NonNull SeriesCursor getSeriesCursor(@NonNull CubeId id) throws Exception {
        return new SeriesQuery(id, table, labelColumn).call(supplier, db);
    }

    @Override
    public @NonNull SeriesWithDataCursor<java.util.Date> getSeriesWithDataCursor(@NonNull CubeId id) throws Exception {
        return new SeriesWithDataQuery(id, table, labelColumn, tdp).call(supplier, db);
    }

    @Override
    public @NonNull ChildrenCursor getChildrenCursor(@NonNull CubeId id) throws Exception {
        return new ChildrenQuery(id, table).call(supplier, db);
    }

    @Override
    public @NonNull TsDataBuilder<java.util.Date> newBuilder() {
        return TsDataBuilder.byCalendar(new GregorianCalendar(TimeZone.getDefault(), Locale.getDefault()), gathering, ObsCharacteristics.ORDERED);
    }

    @Override
    public @NonNull String getDisplayName() throws Exception {
        return TableAsCubeUtil.getDisplayName(db, table, tdp.getValueColumn(), gathering);
    }

    @Override
    public @NonNull String getDisplayName(@NonNull CubeId id) throws Exception {
        return TableAsCubeUtil.getDisplayName(id, LABEL_COLLECTOR);
    }

    @Override
    public @NonNull String getDisplayNodeName(@NonNull CubeId id) throws Exception {
        return TableAsCubeUtil.getDisplayNodeName(id);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
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
     * An class that handles SQL queries from Jdbc.
     *
     * @author Philippe Charles
     */
    @VisibleForTesting
    interface JdbcQuery<T> {

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
         * @return
         * @throws SQLException
         */
        @Nullable
        T process(@NonNull ResultSet rs, @NonNull AutoCloseable closeable) throws SQLException;

        @Nullable
        default T call(@NonNull SqlConnectionSupplier supplier, @NonNull String connectionString) throws SQLException {
            Connection conn = null;
            PreparedStatement cmd = null;
            ResultSet rs = null;
            try {
                conn = supplier.getConnection(connectionString);
                String queryString = getQueryString(conn.getMetaData());
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
        public String getQueryString(DatabaseMetaData metaData) throws SQLException {
            return SelectBuilder.from(tableName)
                    .distinct(true)
                    .select(toSelect(ref)).select(labelColumn)
                    .filter(toFilter(ref))
                    .orderBy(toSelect(ref))
                    .withQuoter(SqlIdentifierQuoter.of(metaData))
                    .build();
        }

        @Override
        public void setParameters(PreparedStatement statement) throws SQLException {
            for (int i = 0; i < ref.getLevel(); i++) {
                statement.setString(i + 1, ref.getDimensionValue(i));
            }
        }

        @Override
        public AllSeriesCursor process(ResultSet rs, AutoCloseable closeable) throws SQLException {
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
        public String getQueryString(DatabaseMetaData metaData) throws SQLException {
            return SelectBuilder.from(tableName)
                    .select(toSelect(ref)).select(tdp.getPeriodColumn(), tdp.getValueColumn()).select(labelColumn)
                    .filter(toFilter(ref))
                    .orderBy(toSelect(ref)).orderBy(tdp.getPeriodColumn(), tdp.getVersionColumn())
                    .withQuoter(SqlIdentifierQuoter.of(metaData))
                    .build();
        }

        @Override
        public void setParameters(PreparedStatement statement) throws SQLException {
            for (int i = 0; i < ref.getLevel(); i++) {
                statement.setString(i + 1, ref.getDimensionValue(i));
            }
        }

        @Override
        public AllSeriesWithDataCursor<java.util.Date> process(ResultSet rs, AutoCloseable closeable) throws SQLException {
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
        public String getQueryString(DatabaseMetaData metaData) throws SQLException {
            return SelectBuilder.from(tableName)
                    .select(labelColumn)
                    .filter(toFilter(ref))
                    .withQuoter(SqlIdentifierQuoter.of(metaData))
                    .build();
        }

        @Override
        public void setParameters(PreparedStatement statement) throws SQLException {
            for (int i = 0; i < ref.getLevel(); i++) {
                statement.setString(i + 1, ref.getDimensionValue(i));
            }
        }

        @Override
        public SeriesCursor process(ResultSet rs, AutoCloseable closeable) throws SQLException {
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
        public String getQueryString(DatabaseMetaData metaData) throws SQLException {
            return SelectBuilder.from(tableName)
                    .select(tdp.getPeriodColumn(), tdp.getValueColumn()).select(labelColumn)
                    .filter(toFilter(ref))
                    .orderBy(tdp.getPeriodColumn(), tdp.getVersionColumn())
                    .withQuoter(SqlIdentifierQuoter.of(metaData))
                    .build();
        }

        @Override
        public void setParameters(PreparedStatement statement) throws SQLException {
            for (int i = 0; i < ref.getLevel(); i++) {
                statement.setString(i + 1, ref.getDimensionValue(i));
            }
        }

        @Override
        public SeriesWithDataCursor<java.util.Date> process(ResultSet rs, AutoCloseable closeable) throws SQLException {
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
        public String getQueryString(DatabaseMetaData metaData) throws SQLException {
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
        public void setParameters(PreparedStatement statement) throws SQLException {
            for (int i = 0; i < ref.getLevel(); i++) {
                statement.setString(i + 1, ref.getDimensionValue(i));
            }
        }

        @Override
        public ChildrenCursor process(ResultSet rs, AutoCloseable closeable) throws SQLException {
            ResultSetFunc<String> toChild = onGetString(1);

            return SqlTableAsCubeUtil.childrenCursor(rs, closeable, toChild);
        }
    }
    //</editor-fold>
}
