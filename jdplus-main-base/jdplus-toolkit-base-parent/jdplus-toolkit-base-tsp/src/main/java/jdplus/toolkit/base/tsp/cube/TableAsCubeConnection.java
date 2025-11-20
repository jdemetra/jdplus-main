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
package jdplus.toolkit.base.tsp.cube;

import internal.toolkit.base.tsp.cube.CubeRepository;
import jdplus.toolkit.base.api.timeseries.util.TsDataBuilder;
import lombok.NonNull;
import nbbrd.design.NotThreadSafe;
import nbbrd.io.AbstractIOIterator;
import nbbrd.io.WrappedIOException;
import nbbrd.io.function.IORunnable;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 * @since 2.2.0
 */
@NotThreadSafe
@lombok.AllArgsConstructor(staticName = "of")
public final class TableAsCubeConnection<DATE> implements CubeConnection {

    @NotThreadSafe
    public interface Resource<DATE> extends AutoCloseable {

        @Nullable
        Exception testConnection();

        @NonNull
        CubeId getRoot() throws Exception;

        @NonNull
        AllSeriesCursor getAllSeriesCursor(@NonNull CubeId id) throws Exception;

        @NonNull
        AllSeriesWithDataCursor<DATE> getAllSeriesWithDataCursor(@NonNull CubeId id) throws Exception;

        @NonNull
        SeriesCursor getSeriesCursor(@NonNull CubeId id) throws Exception;

        @NonNull
        SeriesWithDataCursor<DATE> getSeriesWithDataCursor(@NonNull CubeId id) throws Exception;

        @NonNull
        ChildrenCursor getChildrenCursor(@NonNull CubeId id) throws Exception;

        @NonNull
        String getDisplayName() throws Exception;

        @NonNull
        String getDisplayName(@NonNull CubeId id) throws Exception;

        @NonNull
        String getDisplayNodeName(@NonNull CubeId id) throws Exception;

        @NonNull
        TsDataBuilder<DATE> newBuilder();
    }

    @NotThreadSafe
    public interface TableCursor extends AutoCloseable {

        boolean nextRow() throws Exception;
    }

    @NotThreadSafe
    public interface WithLabel {

        @Nullable
        String getLabelOrNull() throws Exception;
    }

    @NotThreadSafe
    public interface WithData<DATE> {

        @Nullable
        DATE getPeriodOrNull() throws Exception;

        @Nullable
        Number getValueOrNull() throws Exception;
    }

    @NotThreadSafe
    public interface SeriesCursor extends TableCursor, WithLabel {
    }

    @NotThreadSafe
    public interface SeriesWithDataCursor<DATE> extends SeriesCursor, WithData<DATE> {
    }

    @NotThreadSafe
    public interface AllSeriesCursor extends TableCursor, WithLabel {

        @NonNull
        String[] getDimValues() throws Exception;
    }

    @NotThreadSafe
    public interface AllSeriesWithDataCursor<DATE> extends AllSeriesCursor, WithData<DATE> {
    }

    @NotThreadSafe
    public interface ChildrenCursor extends TableCursor {

        @NonNull
        String getChild() throws Exception;
    }

    @lombok.NonNull
    private final Resource<DATE> resource;

    @Override
    public @NonNull Optional<IOException> testConnection() {
        Exception result = resource.testConnection();
        return result != null ? Optional.of(WrappedIOException.wrap(result)) : Optional.empty();
    }

    @Override
    public @NonNull CubeId getRoot() throws IOException {
        try {
            return resource.getRoot();
        } catch (Exception ex) {
            throw WrappedIOException.wrap(ex);
        }
    }

    @Override
    public @NonNull Stream<CubeSeries> getAllSeries(@NonNull CubeId id) throws IOException {
        CubeRepository.checkNode(id);
        try {
            AllSeriesCursor cursor = resource.getAllSeriesCursor(id);
            return new AllSeriesIterator(id, cursor).asStream();
        } catch (Exception ex) {
            throw WrappedIOException.wrap(ex);
        }
    }

    @Override
    public @NonNull Stream<CubeSeriesWithData> getAllSeriesWithData(@NonNull CubeId id) throws IOException {
        CubeRepository.checkNode(id);
        try {
            AllSeriesWithDataCursor<DATE> cursor = resource.getAllSeriesWithDataCursor(id);
            return new AllSeriesWithDataIterator<>(id, cursor, resource.newBuilder()).asStream();
        } catch (Exception ex) {
            throw WrappedIOException.wrap(ex);
        }
    }

    @Override
    public @NonNull Optional<CubeSeries> getSeries(@NonNull CubeId id) throws IOException {
        CubeRepository.checkLeaf(id);
        try (SeriesCursor cursor = resource.getSeriesCursor(id)) {
            AbstractIOIterator<CubeSeries> result = new SeriesIterator(id, cursor);
            return result.hasNextWithIO() ? Optional.of(result.nextWithIO()) : Optional.empty();
        } catch (Exception ex) {
            throw WrappedIOException.wrap(ex);
        }
    }

    @Override
    public @NonNull Optional<CubeSeriesWithData> getSeriesWithData(@NonNull CubeId id) throws IOException {
        CubeRepository.checkLeaf(id);
        try (SeriesWithDataCursor<DATE> cursor = resource.getSeriesWithDataCursor(id)) {
            AbstractIOIterator<CubeSeriesWithData> result = new SeriesWithDataIterator<>(id, cursor, resource.newBuilder());
            return result.hasNextWithIO() ? Optional.of(result.nextWithIO()) : Optional.empty();
        } catch (Exception ex) {
            throw WrappedIOException.wrap(ex);
        }
    }

    @Override
    public @NonNull Stream<CubeId> getChildren(@NonNull CubeId id) throws IOException {
        CubeRepository.checkNode(id);
        try {
            ChildrenCursor cursor = resource.getChildrenCursor(id);
            return new ChildrenIterator(id, cursor).asStream();
        } catch (Exception ex) {
            throw WrappedIOException.wrap(ex);
        }
    }

    @Override
    public @NonNull String getDisplayName() throws IOException {
        try {
            return resource.getDisplayName();
        } catch (Exception ex) {
            throw WrappedIOException.wrap(ex);
        }
    }

    @Override
    public @NonNull String getDisplayName(@NonNull CubeId id) throws IOException {
        try {
            return resource.getDisplayName(id);
        } catch (Exception ex) {
            throw WrappedIOException.wrap(ex);
        }
    }

    @Override
    public @NonNull String getDisplayNodeName(@NonNull CubeId id) throws IOException {
        try {
            return resource.getDisplayNodeName(id);
        } catch (Exception ex) {
            throw WrappedIOException.wrap(ex);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            resource.close();
        } catch (Exception ex) {
            throw WrappedIOException.wrap(ex);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final Map<String, String> NO_META = Collections.emptyMap();

    private static abstract class AbstractTableIterator<T> extends AbstractIOIterator<T> {

        abstract protected TableCursor getTableCursor();

        @Override
        public @NonNull Stream<T> asStream() {
            return super.asStream().onClose(IORunnable.unchecked(this::close));
        }

        private void close() throws IOException {
            try {
                getTableCursor().close();
            } catch (Exception ex) {
                throw WrappedIOException.wrap(ex);
            }
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class AllSeriesIterator extends AbstractTableIterator<CubeSeries> {

        private final CubeId parentId;
        private final AllSeriesCursor cursor;

        @Override
        protected boolean moveNext() throws IOException {
            try {
                return cursor.nextRow();
            } catch (Exception ex) {
                throw WrappedIOException.wrap(ex);
            }
        }

        @Override
        protected CubeSeries get() throws IOException {
            try {
                return new CubeSeries(parentId.child(cursor.getDimValues()), cursor.getLabelOrNull(), NO_META);
            } catch (Exception ex) {
                throw WrappedIOException.wrap(ex);
            }
        }

        @Override
        protected TableCursor getTableCursor() {
            return cursor;
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class AllSeriesWithDataIterator<DATE> extends AbstractTableIterator<CubeSeriesWithData> {

        private final CubeId parentId;
        private final AllSeriesWithDataCursor<DATE> cursor;
        private final TsDataBuilder<DATE> data;

        private boolean first = true;
        private boolean t0 = false;
        private String[] currentId = null;
        private String currentLabel = null;

        @Override
        protected boolean moveNext() throws IOException {
            try {
                if (first) {
                    t0 = cursor.nextRow();
                    first = false;
                }
                if (t0) {
                    data.clear();
                    currentId = cursor.getDimValues();
                    currentLabel = cursor.getLabelOrNull();
                    boolean t1 = true;
                    while (t1) {
                        DATE period = cursor.getPeriodOrNull();
                        Number value = null;
                        boolean t2 = true;
                        while (t2) {
                            value = cursor.getValueOrNull();
                            t0 = cursor.nextRow();
                            t1 = t0 && Arrays.equals(currentId, cursor.getDimValues());
                            t2 = t1 && Objects.equals(period, cursor.getPeriodOrNull());
                        }
                        data.add(period, value);
                    }
                    return true;
                }
                currentId = null;
                currentLabel = null;
                return false;
            } catch (Exception ex) {
                throw WrappedIOException.wrap(ex);
            }
        }

        @Override
        protected CubeSeriesWithData get() {
            return new CubeSeriesWithData(parentId.child(currentId), currentLabel, NO_META, data.build());
        }

        @Override
        protected TableCursor getTableCursor() {
            return cursor;
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class SeriesIterator extends AbstractTableIterator<CubeSeries> {

        private final CubeId parentId;
        private final SeriesCursor cursor;

        private String currentLabel = null;

        @Override
        protected boolean moveNext() throws IOException {
            try {
                boolean t0 = cursor.nextRow();
                if (t0) {
                    currentLabel = cursor.getLabelOrNull();
                    return true;
                }
                currentLabel = null;
                return false;
            } catch (Exception ex) {
                throw WrappedIOException.wrap(ex);
            }
        }

        @Override
        protected CubeSeries get() {
            return new CubeSeries(parentId, currentLabel, NO_META);
        }

        @Override
        protected TableCursor getTableCursor() {
            return cursor;
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class SeriesWithDataIterator<DATE> extends AbstractTableIterator<CubeSeriesWithData> {

        private final CubeId parentId;
        private final SeriesWithDataCursor<DATE> cursor;
        private final TsDataBuilder<DATE> data;

        private String currentLabel = null;

        @Override
        protected boolean moveNext() throws IOException {
            try {
                boolean t0 = cursor.nextRow();
                if (t0) {
                    currentLabel = cursor.getLabelOrNull();
                    DATE latestPeriod = cursor.getPeriodOrNull();
                    while (t0) {
                        DATE period = latestPeriod;
                        Number value = null;
                        boolean t1 = true;
                        while (t1) {
                            value = cursor.getValueOrNull();
                            t0 = cursor.nextRow();
                            t1 = t0 && Objects.equals(period, latestPeriod = cursor.getPeriodOrNull());
                        }
                        data.add(period, value);
                    }
                    return true;
                }
                currentLabel = null;
                return false;
            } catch (Exception ex) {
                throw WrappedIOException.wrap(ex);
            }
        }

        @Override
        protected CubeSeriesWithData get() {
            return new CubeSeriesWithData(parentId, currentLabel, NO_META, data.build());
        }

        @Override
        protected TableCursor getTableCursor() {
            return cursor;
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class ChildrenIterator extends AbstractTableIterator<CubeId> {

        private final CubeId parentId;
        private final ChildrenCursor cursor;

        @Override
        protected boolean moveNext() throws IOException {
            try {
                return cursor.nextRow();
            } catch (Exception ex) {
                throw WrappedIOException.wrap(ex);
            }
        }

        @Override
        protected CubeId get() throws IOException {
            try {
                return parentId.child(cursor.getChild());
            } catch (Exception ex) {
                throw WrappedIOException.wrap(ex);
            }
        }

        @Override
        protected TableCursor getTableCursor() {
            return cursor;
        }
    }
    //</editor-fold>
}
