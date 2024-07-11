package _test;

import jdplus.toolkit.base.tsp.cube.CubeId;
import jdplus.toolkit.base.tsp.cube.TableAsCubeConnection;

import java.util.ArrayList;
import java.util.List;

public final class TableAsCubes {

    private TableAsCubes() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @FunctionalInterface
    public interface TableSupplier<T extends TableAsCubeConnection.TableCursor> {
        T get(CubeId cubeId) throws Exception;
    }

    @FunctionalInterface
    public interface RowFunction<T extends TableAsCubeConnection.TableCursor, X> {
        X apply(T cursor) throws Exception;
    }

    public static <CURSOR extends TableAsCubeConnection.TableCursor, T> List<T> toList(TableSupplier<CURSOR> supplier, RowFunction<CURSOR, T> function, CubeId cubeId) throws Exception {
        List<T> result = new ArrayList<>();
        try (var cursor = supplier.get(cubeId)) {
            while (cursor.nextRow()) {
                result.add(function.apply(cursor));
            }
        }
        return result;
    }

    public static List<String> getChildren(TableAsCubeConnection.Resource<?> x, CubeId cubeId) throws Exception {
        return toList(x::getChildrenCursor, TableAsCubeConnection.ChildrenCursor::getChild, cubeId);
    }

    @lombok.Value
    @lombok.Builder
    public static class AllSeries {

        String[] dimensions;
        String label;

        public static AllSeries of(TableAsCubeConnection.AllSeriesCursor cursor) throws Exception {
            return AllSeries
                    .builder()
                    .dimensions(cursor.getDimValues())
                    .label(cursor.getLabelOrNull())
                    .build();
        }

        public static List<AllSeries> of(TableAsCubeConnection.Resource<?> x, CubeId cubeId) throws Exception {
            return toList(x::getAllSeriesCursor, AllSeries::of, cubeId);
        }
    }

    @lombok.Value
    @lombok.Builder
    public static class AllSeriesWithData<DATE> {

        String[] dimensions;
        String label;
        Number value;
        DATE period;

        public static <DATE> AllSeriesWithData<DATE> of(TableAsCubeConnection.AllSeriesWithDataCursor<DATE> cursor) throws Exception {
            return AllSeriesWithData
                    .<DATE>builder()
                    .dimensions(cursor.getDimValues())
                    .label(cursor.getLabelOrNull())
                    .value(cursor.getValueOrNull())
                    .period(cursor.getPeriodOrNull())
                    .build();
        }

        public static <DATE> List<AllSeriesWithData<DATE>> of(TableAsCubeConnection.Resource<DATE> x, CubeId cubeId) throws Exception {
            return toList(x::getAllSeriesWithDataCursor, AllSeriesWithData::of, cubeId);
        }
    }

    @lombok.Value
    @lombok.Builder
    public static class Series {

        String label;

        public static Series of(TableAsCubeConnection.SeriesCursor cursor) throws Exception {
            return Series
                    .builder()
                    .label(cursor.getLabelOrNull())
                    .build();
        }

        public static List<Series> of(TableAsCubeConnection.Resource<?> x, CubeId cubeId) throws Exception {
            return toList(x::getSeriesCursor, Series::of, cubeId);
        }
    }

    @lombok.Value
    @lombok.Builder
    public static class SeriesWithData<DATE> {

        String label;
        Number value;
        DATE period;

        public static <DATE> SeriesWithData<DATE> of(TableAsCubeConnection.SeriesWithDataCursor<DATE> cursor) throws Exception {
            return SeriesWithData
                    .<DATE>builder()
                    .label(cursor.getLabelOrNull())
                    .value(cursor.getValueOrNull())
                    .period(cursor.getPeriodOrNull())
                    .build();
        }

        public static <DATE> List<SeriesWithData<DATE>> of(TableAsCubeConnection.Resource<DATE> x, CubeId cubeId) throws Exception {
            return toList(x::getSeriesWithDataCursor, SeriesWithData::of, cubeId);
        }
    }
}
