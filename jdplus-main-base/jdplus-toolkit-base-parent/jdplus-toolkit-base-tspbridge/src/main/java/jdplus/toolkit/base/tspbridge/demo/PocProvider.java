/*
 * Copyright 2015 National Bank of Belgium
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
package jdplus.toolkit.base.tspbridge.demo;

import jdplus.toolkit.base.api.timeseries.*;
import jdplus.toolkit.base.api.util.IntList;
import jdplus.toolkit.base.tsp.*;
import jdplus.toolkit.base.tsp.stream.DataSetTs;
import jdplus.toolkit.base.tsp.stream.HasTsStream;
import jdplus.toolkit.base.tsp.stream.TsStreamAsProvider;
import jdplus.toolkit.base.tsp.util.PropertyHandler;
import lombok.NonNull;
import nbbrd.design.BuilderPattern;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static jdplus.toolkit.base.api.timeseries.TsInformationType.*;
import static jdplus.toolkit.base.api.timeseries.TsUnit.*;
import static jdplus.toolkit.base.tsp.util.DataSourcePreconditions.checkProvider;

/**
 * @author Philippe Charles
 */
@lombok.extern.java.Log
public final class PocProvider implements DataSourceProvider {

    public static final String NAME = "poc";

    private static final PropertyHandler<DataType> TYPE_PARAM = PropertyHandler.onEnum("t", DataType.NORMAL);
    private static final PropertyHandler<Integer> INDEX_PARAM = PropertyHandler.onInteger("i", -1);

    @lombok.experimental.Delegate(types = HasDataHierarchy.class)
    private final PocDataSupport dataSupport;

    @lombok.experimental.Delegate
    private final HasDataSourceList listSupport;

    @lombok.experimental.Delegate
    private final HasDataDisplayName nameSupport;

    @lombok.experimental.Delegate
    private final HasDataMoniker monikerSupport;

    @lombok.experimental.Delegate(excludes = AutoCloseable.class)
    private final TsProvider tsSupport;

    private final Timer updater;

    public PocProvider() {
        this.dataSupport = new PocDataSupport();
        this.listSupport = HasDataSourceList.of(NAME, createDataSources());
        this.nameSupport = new PocDataDisplayName();
        this.monikerSupport = HasDataMoniker.usingUri(NAME);
        this.tsSupport = TsStreamAsProvider.of(NAME, dataSupport, monikerSupport, () -> {
        });

        this.updater = new Timer(true);
        updater.schedule(new TimerTask() {

            @Override
            public void run() {
                reload(DataType.UPDATING.getDataSource());
            }
        }, 1000, 1000);
    }

    @Override
    public @NonNull String getDisplayName() {
        return "Proof-of-concept";
    }

    @Override
    public void close() {
        updater.cancel();
        tsSupport.close();
    }

    private static List<DataSource> createDataSources() {
        return Stream.of(DataType.values()).map(DataType::getDataSource).toList();
    }

    private static final class PocDataDisplayName implements HasDataDisplayName {

        @Override
        public @NonNull String getDisplayName(@NonNull DataSource dataSource) throws IllegalArgumentException {
            checkProvider(NAME, dataSource);
            return switch (TYPE_PARAM.get(dataSource::getParameter)) {
                case NORMAL -> "Normal async";
                case FAILING_META -> "Failing on meta";
                case FAILING_DATA -> "Failing on data";
                case FAILING_DEF -> "Failing on definition";
                case UPDATING -> "Auto updating";
                case SLOW -> "Slow retrieval";
            };
        }

        @Override
        public @NonNull String getDisplayName(@NonNull DataSet dataSet) throws IllegalArgumentException {
            checkProvider(NAME, dataSet);
            return getDisplayName(dataSet.getDataSource()) + System.lineSeparator() + getDisplayNodeName(dataSet);
        }

        @Override
        public @NonNull String getDisplayNodeName(@NonNull DataSet dataSet) throws IllegalArgumentException {
            checkProvider(NAME, dataSet);
            TsDomain domain = TYPE_PARAM.get(dataSet.getDataSource()::getParameter).getDomain(INDEX_PARAM.get(dataSet::getParameter));
            return domain.getStartPeriod().getUnit() + "#" + domain.getLength();
        }
    }

    private static final class PocDataSupport implements HasTsStream, HasDataHierarchy {

        private final List<TsData> normalData;
        private final IntFunction<TsData> updatingData;
        private final IntFunction<TsData> slowData;

        public PocDataSupport() {
            normalData = createData(DataType.NORMAL);
            updatingData = shiftingValues(createData(DataType.UPDATING));
            slowData = createData(DataType.SLOW)::get;
        }

        @Override
        public @NonNull List<DataSet> children(@NonNull DataSource dataSource) throws IllegalArgumentException, IOException {
            checkProvider(NAME, dataSource);
            try (var cursor = cursorOf(dataSource, Definition)) {
                return cursor.map(DataSetTs::getId).toList();
            } catch (UncheckedIOException ex) {
                throw ex.getCause();
            }
        }

        @Override
        public @NonNull List<DataSet> children(@NonNull DataSet parent) throws IllegalArgumentException, IOException {
            checkProvider(NAME, parent);
            throw new IllegalArgumentException("Invalid hierarchy");
        }

        @Override
        public @NonNull Stream<DataSetTs> getData(@NonNull DataSource dataSource, @NonNull TsInformationType type) throws IllegalArgumentException, IOException {
            checkProvider(NAME, dataSource);
            return cursorOf(dataSource, type);
        }

        @Override
        public @NonNull Stream<DataSetTs> getData(@NonNull DataSet dataSet, @NonNull TsInformationType type) throws IllegalArgumentException, IOException {
            checkProvider(NAME, dataSet);
            if (!dataSet.getKind().equals(DataSet.Kind.SERIES)) {
                throw new IllegalArgumentException("Invalid hierarchy");
            }
            return cursorOf(dataSet.getDataSource(), type)
                    .filter(o -> o.getId().equals(dataSet));
        }

        private Stream<DataSetTs> cursorOf(DataSource source, TsInformationType type) throws IOException {
            DataType dt = TYPE_PARAM.get(source::getParameter);
            dt.sleep(type);
            return IntStream
                    .range(0, dt.getSeriesCount())
                    .mapToObj(seriesFunc(idFunc(source), dataFunc(dt, type), metaFunc(dt, type), labelFunc(dt)));
        }

        private static IntFunction<DataSetTs> seriesFunc(
                IntFunction<DataSet> toId,
                IntFunction<TsData> toData,
                IntFunction<Map<String, String>> toMeta,
                IntFunction<String> toLabel) {
            return seriesIndex -> new DataSetTs(toId.apply(seriesIndex), toLabel.apply(seriesIndex), toMeta.apply(seriesIndex), toData.apply(seriesIndex));
        }

        private static IntFunction<DataSet> idFunc(DataSource dataSource) {
            return seriesIndex -> {
                DataSet.Builder b = DataSet.builder(dataSource, DataSet.Kind.SERIES);
                INDEX_PARAM.set(b::parameter, seriesIndex);
                return b.build();
            };
        }

        private IntFunction<TsData> dataFunc(DataType dt, TsInformationType type) throws IOException {
            return switch (dt) {
                case NORMAL -> dataFunc(normalData::get, type);
                case FAILING_META -> failingDataFunc(MetaData);
                case FAILING_DATA -> failingDataFunc(Data);
                case FAILING_DEF -> failingDataFunc(Definition);
                case UPDATING -> dataFunc(updatingData, type);
                case SLOW -> dataFunc(slowData, type);
            };
        }

        private static IntFunction<TsData> dataFunc(IntFunction<TsData> delegate, TsInformationType type) {
            return type.encompass(Data) ? delegate : ignore -> DataSetTs.DATA_NOT_REQUESTED;
        }

        private static IntFunction<TsData> failingDataFunc(TsInformationType type) throws IOException {
            if (type.encompass(type)) {
                throw new IOException("Cannot load " + type);
            }
            return ignore -> DataSetTs.DATA_NOT_REQUESTED;
        }

        private static IntFunction<Map<String, String>> metaFunc(DataType dt, TsInformationType type) {
            return type.encompass(MetaData)
                    ? seriesIndex -> Map.of(
                    "Type", dt.name(),
                    "Index", String.valueOf(seriesIndex),
                    "Sleep meta", String.valueOf(dt.getSleepDuration(MetaData)),
                    "Sleep data", String.valueOf(dt.getSleepDuration(Data)))
                    : ignore -> Collections.emptyMap();
        }

        private static IntFunction<String> labelFunc(DataType dt) {
            return seriesIndex -> dt.name() + "#" + seriesIndex;
        }

        private static IntFunction<TsData> shiftingValues(List<TsData> list) {
            return seriesIndex -> {
                TsData result = shiftValues(list.get(seriesIndex));
                list.set(seriesIndex, result);
                return result;
            };
        }

        private static TsData shiftValues(TsData input) {
            if (!input.isEmpty()) {
                double[] values = input.getValues().toArray();
                double first = values[0];
                System.arraycopy(values, 1, values, 0, values.length - 1);
                values[values.length - 1] = first;
                return TsData.ofInternal(input.getStart(), values);
            }
            return input;
        }

        private static List<TsData> createData(DataType dataType) {
            return IntStream
                    .range(0, dataType.getSeriesCount())
                    .mapToObj(seriesIndex -> createData(dataType, seriesIndex))
                    .collect(Collectors.toList());
        }

        private static TsData createData(DataType dt, int seriesIndex) {
            return PocDomainBuilder.createData(dt.getDomain(seriesIndex));
        }
    }

    @lombok.RequiredArgsConstructor
    private enum DataType {

        NORMAL(new PocDomainBuilder()
                .units(P1Y, P6M, P3M, P1M, P1D)
                .sizes(0, 1, 24, 60, 120)
                .build()),
        FAILING_DATA(new PocDomainBuilder()
                .units(P1M)
                .sizes(60, 120)
                .build()),
        FAILING_META(new PocDomainBuilder()
                .units(P1M)
                .sizes(60, 120)
                .build()),
        FAILING_DEF(new PocDomainBuilder()
                .units(P1M)
                .sizes(60, 120)
                .build()),
        UPDATING(new PocDomainBuilder()
                .units(P1M)
                .sizes(0, 1, 24, 60, 120)
                .build()),
        SLOW(new PocDomainBuilder()
                .units(P1M)
                .sizes(60, 120)
                .build());

        private final TsDomain[] domains;

        @lombok.Getter(lazy = true)
        private final DataSource dataSource = initDataSource();

        public int getSeriesCount() {
            return domains.length;
        }

        public TsDomain getDomain(int seriesIndex) {
            return domains[seriesIndex];
        }

        public long getSleepDuration(TsInformationType type) {
            return switch (this) {
                case NORMAL, FAILING_META, FAILING_DATA, FAILING_DEF -> type.needsData() ? 2000 : 150;
                case UPDATING -> 0;
                case SLOW -> type.needsData() ? 5000 : type.equals(Definition) ? 0 : 1000;
            };
        }

        public void sleep(TsInformationType type) {
            try {
                TimeUnit.MILLISECONDS.sleep(getSleepDuration(type));
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }

        private DataSource initDataSource() {
            DataSource.Builder result = DataSource.builder(NAME, "");
            TYPE_PARAM.set(result::parameter, this);
            return result.build();
        }
    }

    @BuilderPattern(TsDomain[].class)
    private static final class PocDomainBuilder {

        private final List<TsUnit> units = new ArrayList<>();
        private final IntList sizes = new IntList();

        public PocDomainBuilder units(TsUnit... unitArray) {
            units.addAll(Arrays.asList(unitArray));
            return this;
        }

        public PocDomainBuilder sizes(int... sizeArray) {
            for (int size : sizeArray) {
                sizes.add(size);
            }
            return this;
        }

        public TsDomain[] build() {
            return units
                    .stream()
                    .flatMap(unit -> sizes.stream().mapToObj(size -> domainOf(unit, size)))
                    .toArray(TsDomain[]::new);
        }

        private static TsDomain domainOf(TsUnit unit, int size) {
            return TsDomain.of(TsPeriod.of(unit, 0), size);
        }

        public static TsData createData(TsDomain domain) {
            return TsData.ofInternal(domain.getStartPeriod(), createValues(new Random(0), domain.getLength()));
        }

        private static double[] createValues(Random rnd, int obsCount) {
            double[] data = new double[obsCount];
            double cur = rnd.nextDouble() + 100;
            for (int i = 0; i < obsCount; ++i) {
                cur = cur + rnd.nextDouble() - .5;
                data[i] = cur;
            }
            return data;
        }
    }
}
