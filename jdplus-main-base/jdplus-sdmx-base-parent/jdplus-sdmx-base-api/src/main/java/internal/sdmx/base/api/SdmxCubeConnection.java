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
package internal.sdmx.base.api;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.timeseries.util.ObsGathering;
import jdplus.toolkit.base.api.timeseries.util.TsDataBuilder;
import jdplus.toolkit.base.tsp.cube.CubeConnection;
import jdplus.toolkit.base.tsp.cube.CubeId;
import jdplus.toolkit.base.tsp.cube.CubeSeries;
import jdplus.toolkit.base.tsp.cube.CubeSeriesWithData;
import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import sdmxdl.*;
import sdmxdl.ext.SdmxCubeUtil;

/**
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor
public final class SdmxCubeConnection implements CubeConnection {

    public static SdmxCubeConnection of(
            Connection connection,
            DatabaseRef databaseRef,
            FlowRef flowRef,
            List<String> dimensions,
            String labelAttribute,
            String sourceLabel,
            boolean displayCodes)
            throws IOException {
        MetaSet meta = connection.getMeta(databaseRef, flowRef);
        CubeId root = getOrLoadRoot(dimensions, meta.getStructure());
        return new SdmxCubeConnection(connection, databaseRef, meta, root, labelAttribute, sourceLabel, displayCodes);
    }

    private final Connection connection;
    private final DatabaseRef database;
    private final MetaSet meta;
    private final CubeId root;
    private final String labelAttribute;
    private final String sourceLabel;
    private final boolean displayCodes;

    @Override
    public @NonNull Optional<IOException> testConnection() {
        try {
            connection.testConnection();
            return Optional.empty();
        } catch (IOException ex) {
            return Optional.of(ex);
        }
    }

    @Override
    public @NonNull CubeId getRoot() {
        return root;
    }

    @Override
    public @NonNull Stream<CubeSeries> getAllSeries(@NonNull CubeId ref) throws IOException {
        KeyConverter converter = KeyConverter.of(meta.getStructure(), ref);
        return SdmxCubeUtil.getAllSeries(connection, database, meta.getFlow().getRef(), converter.toKey(ref))
                .map(series -> cubeSeriesOf(converter, series, labelAttribute));
    }

    @Override
    public @NonNull Stream<CubeSeriesWithData> getAllSeriesWithData(@NonNull CubeId ref) throws IOException {
        KeyConverter converter = KeyConverter.of(meta.getStructure(), ref);
        return SdmxCubeUtil.getAllSeriesWithData(
                        connection, database, meta.getFlow().getRef(), converter.toKey(ref))
                .map(series -> cubeSeriesWithDataOf(converter, series, labelAttribute));
    }

    @Override
    public @NonNull Optional<CubeSeries> getSeries(@NonNull CubeId id) throws IOException {
        KeyConverter converter = KeyConverter.of(meta.getStructure(), id);
        return SdmxCubeUtil.getSeries(connection, database, meta.getFlow().getRef(), converter.toKey(id))
                .map(series -> cubeSeriesOf(converter, series, labelAttribute));
    }

    @Override
    public @NonNull Optional<CubeSeriesWithData> getSeriesWithData(@NonNull CubeId ref) throws IOException {
        KeyConverter converter = KeyConverter.of(meta.getStructure(), ref);
        return SdmxCubeUtil.getSeriesWithData(
                        connection, database, meta.getFlow().getRef(), converter.toKey(ref))
                .map(series -> cubeSeriesWithDataOf(converter, series, labelAttribute));
    }

    @Override
    public @NonNull Stream<CubeId> getChildren(@NonNull CubeId ref) throws IOException {
        KeyConverter converter = KeyConverter.of(meta.getStructure(), ref);
        String dimensionId = ref.getDimensionId(ref.getLevel());
        int dimensionIndex = SdmxCubeUtil.getDimensionIndexById(meta.getStructure(), dimensionId)
                .orElseThrow(RuntimeException::new);
        return SdmxCubeUtil.getChildren(
                        connection, database, meta.getFlow().getRef(), converter.toKey(ref), dimensionIndex)
                .sorted()
                .map(ref::child);
    }

    @Override
    public @NonNull String getDisplayName() {
        return String.format(Locale.ROOT, "%s ~ %s", sourceLabel, meta.getFlow().getName());
    }

    @Override
    public @NonNull String getDisplayName(CubeId id) {
        if (id.isVoid()) {
            return "All";
        }
        return getKey(meta.getStructure(), id).toString();
    }

    @Override
    public @NonNull String getDisplayNodeName(CubeId id) {
        if (id.isVoid()) {
            return "All";
        }
        return displayCodes ? getDimensionCodeId(id) : getDimensionCodeLabel(id, meta.getStructure());
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }

    // <editor-fold defaultstate="collapsed" desc="Implementation details">
    private static CubeSeries cubeSeriesOf(KeyConverter converter, Series series, String labelAttribute) {
        return new CubeSeries(
                converter.fromKey(series.getKey()), series.getMeta().get(labelAttribute), series.getMeta());
    }

    private static CubeSeriesWithData cubeSeriesWithDataOf(
            KeyConverter converter, Series series, String labelAttribute) {
        return new CubeSeriesWithData(
                converter.fromKey(series.getKey()),
                series.getMeta().get(labelAttribute),
                series.getMeta(),
                getData(series));
    }

    private static String getDimensionCodeId(CubeId ref) {
        int index = ref.getLevel() - 1;
        return ref.getDimensionValue(index);
    }

    private static String getDimensionCodeLabel(CubeId ref, Structure dsd) {
        if (ref.isRoot()) {
            return "Invalid reference '" + dump(ref) + "'";
        }
        int index = ref.getLevel() - 1;
        Map<String, String> codes = SdmxCubeUtil.getDimensionById(dsd, ref.getDimensionId(index))
                .orElseThrow(RuntimeException::new)
                .getCodes();
        String codeId = ref.getDimensionValue(index);
        return codes.getOrDefault(codeId, codeId);
    }

    private static String dump(CubeId ref) {
        return IntStream.range(0, ref.getMaxLevel())
                .mapToObj(o -> ref.getDimensionId(o) + "=" + (o < ref.getLevel() ? ref.getDimensionValue(0) : "null"))
                .collect(Collectors.joining(", "));
    }

    @VisibleForTesting
    static Key getKey(Structure dsd, CubeId ref) {
        if (ref.isRoot()) {
            return Key.ALL;
        }
        String[] result = new String[ref.getMaxLevel()];
        for (int i = 0; i < result.length; i++) {
            String id = ref.getDimensionId(i);
            String value = i < ref.getLevel() ? ref.getDimensionValue(i) : "";
            int index = SdmxCubeUtil.getDimensionIndexById(dsd, id).orElseThrow(RuntimeException::new);
            result[index] = value;
        }
        return Key.of(result);
    }

    @lombok.RequiredArgsConstructor
    static final class KeyConverter {

        static KeyConverter of(Structure dsd, CubeId ref) {
            return new KeyConverter(dsd, new CubeIdBuilder(dsd, ref));
        }

        final Structure dsd;
        final CubeIdBuilder builder;

        public Key toKey(CubeId a) {
            return getKey(dsd, a);
        }

        public CubeId fromKey(Key b) {
            return builder.getId(b);
        }
    }

    private static final class CubeIdBuilder {

        private final CubeId ref;
        private final int[] indices;
        private final String[] dimValues;

        CubeIdBuilder(Structure dsd, CubeId ref) {
            this.ref = ref;
            this.indices = new int[ref.getDepth()];
            for (int i = 0; i < indices.length; i++) {
                indices[i] = SdmxCubeUtil.getDimensionIndexById(dsd, ref.getDimensionId(ref.getLevel() + i))
                        .orElseThrow(RuntimeException::new);
            }
            this.dimValues = new String[indices.length];
        }

        public CubeId getId(Key o) {
            for (int i = 0; i < indices.length; i++) {
                dimValues[i] = o.get(indices[i]);
            }
            return ref.child(dimValues);
        }
    }

    private static TsData getData(Series series) {
        switch (series.getObs().size()) {
            case 0:
                return TsData.empty("No data");
            case 1:
                Obs singleObs = series.getObs().first();
                return TsDataBuilder.byDateTime(getSingleGathering(singleObs))
                        .addAll(series.getObs().stream(), SdmxCubeConnection::toLocalDateTime, Obs::getValue)
                        .build();
            default:
                return TsDataBuilder.byDateTime(DEFAULT_GATHERING)
                        .addAll(series.getObs().stream(), SdmxCubeConnection::toLocalDateTime, Obs::getValue)
                        .build();
        }
    }

    private static LocalDateTime toLocalDateTime(Obs obs) {
        return obs.getPeriod().getStart();
    }

    private static ObsGathering getSingleGathering(Obs singleObs) {
        return ObsGathering.builder()
                .includeMissingValues(true)
                .unit(getTsUnit(singleObs))
                .aggregationType(AggregationType.None)
                .build();
    }

    private static final ObsGathering DEFAULT_GATHERING = ObsGathering.builder()
            .includeMissingValues(true)
            .unit(TsUnit.UNDEFINED)
            .aggregationType(AggregationType.None)
            .build();

    private static TsUnit getTsUnit(Obs obs) {
        try {
            return TsUnit.parse(obs.getPeriod().getDuration().toString());
        } catch (Exception ex) {
            return TsUnit.YEAR;
        }
    }

    private static CubeId getOrLoadRoot(List<String> dimensions, Structure dsd) {
        return dimensions.isEmpty() ? CubeId.root(loadDefaultDimIds(dsd)) : CubeId.root(dimensions);
    }

    private static List<String> loadDefaultDimIds(Structure dsd) {
        return dsd.getDimensions().stream().map(Dimension::getId).collect(Collectors.toList());
    }
    // </editor-fold>
}
