package jdplus.main.ws;

import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDataTable;
import jdplus.toolkit.base.api.timeseries.util.ObsGathering;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static jdplus.main.ws.Converters.*;
import static jdplus.main.ws.ToolkitMessages.ValueStatus.*;
import static java.lang.Double.NaN;
import static jdplus.toolkit.base.api.timeseries.TsPeriod.*;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class TsFunctionsServiceTest {

    @GrpcClient
    TsFunctions grpc;

    @Test
    public void testNormalize() {
        var input = ToolkitMessages.TsFunctionInput
                .newBuilder()
                .setId("Example 1")
                .setSeries(fromTsData(TsData.of(yearly(2010), DoubleSeq.of(3, 4))))
                .build();

        var output = ToolkitMessages.TsFunctionOutput
                .newBuilder()
                .setId("Example 1")
                .setSeries(fromTsData(TsData.of(yearly(2010), DoubleSeq.of(-1, 1))))
                .setStatus(Helpers.ok())
                .build();

        assertThat(grpc.normalize(input).await().atMost(Duration.ofSeconds(5)))
                .isEqualTo(output);
    }

    @Test
    public void testStatistics() {
        var input = ToolkitMessages.TsFunctionInput
                .newBuilder()
                .setId("Example 1")
                .setSeries(fromTsData(TsData.of(yearly(2010), DoubleSeq.of(0, 1, 2, 3, 4, 5, 6, 7))))
                .build();

        var output = ToolkitMessages.DescriptiveStatistics
                .newBuilder()
                .setId("Example 1")
                .setN(8)
                .setMax(7)
                .setAverage(3.5)
                .setStdev(2.29128784747792)
                .setQ25(1.75)
                .setQ50(3.5)
                .setQ75(5.25)
                .setStatus(Helpers.ok())
                .build();

        assertThat(grpc.statistics(input).await().atMost(Duration.ofSeconds(5)))
                .isEqualTo(output);
    }

    @Test
    public void testBuildTsDataSuccess() {
        var input = ToolkitMessages.BuildTsDataInput
                .newBuilder()
                .setId("Example 1")
                .setGathering(fromObsGathering(ObsGathering.DEFAULT))
                .addObservations(ToolkitMessages.BuildTsDataObs.newBuilder().setDate(fromLocalDate(LocalDate.of(2010, 1, 1))).setValue(11).build())
                .addObservations(ToolkitMessages.BuildTsDataObs.newBuilder().setDate(fromLocalDate(LocalDate.of(2010, 2, 1))).setValue(22).build())
                .build();

        var output = ToolkitMessages.TsFunctionOutput
                .newBuilder()
                .setId("Example 1")
                .setSeries(fromTsData(TsData.of(monthly(2010, 1), DoubleSeq.of(11, 22))))
                .setStatus(Helpers.ok())
                .build();

        assertThat(grpc.buildTsData(input).await().atMost(Duration.ofSeconds(5)))
                .isEqualTo(output);
    }

    @Test
    public void testBuildTsDataError() {
        var input = ToolkitMessages.BuildTsDataInput
                .newBuilder()
                .setId("abc")
                .setGathering(fromObsGathering(ObsGathering.DEFAULT))
                .addObservations(ToolkitMessages.BuildTsDataObs.newBuilder().setDate(fromLocalDate(LocalDate.of(2010, 1, 1))).setValue(1).build())
                .build();

        var output = ToolkitMessages.TsFunctionOutput
                .newBuilder()
                .setId("abc")
                .setSeries(fromTsData(TsData.empty("Cannot guess frequency with a single observation")))
                .setStatus(Helpers.ko("Cannot guess frequency with a single observation"))
                .build();

        assertThat(grpc.buildTsData(input).await().atMost(Duration.ofSeconds(5)))
                .isEqualTo(output);
    }

    @Test
    public void testBuildTsDataTable() {
        var input = ToolkitMessages.BuildTsDataTableInput
                .newBuilder()
                .setId("abc")
                .setDistributionType(fromDistributionType(TsDataTable.DistributionType.FIRST))
                .addCollection(fromTsData(TsData.of(quarterly(2010, 1), DoubleSeq.of(1.1))))
                .addCollection(fromTsData(TsData.of(monthly(2010, 1), DoubleSeq.of(2.1, 2.2))))
                .build();

        var output = ToolkitMessages.BuildTsDataTableOutput
                .newBuilder()
                .setId("abc")
                .setMatrix(ToolkitMessages.TsMatrix
                        .newBuilder()
                        .setStart(fromTsPeriod(parse("2010/P1M")))
                        .setValues(ToolkitMessages.Matrix
                                .newBuilder()
                                .setNrows(3)
                                .setNcols(2)
                                .addAllValues(Arrays.asList(1.1, NaN, NaN, 2.1, 2.2, NaN))
                                .build())
                        .build())
                .addAllStatuses(Arrays.asList(VS_PRESENT, VS_UNUSED, VS_UNUSED, VS_PRESENT, VS_PRESENT, VS_AFTER))
                .build();

        assertThat(grpc.buildTsDataTable(input).await().atMost(Duration.ofSeconds(5)))
                .isEqualTo(output);
    }

    @Test
    public void testBuildTsDataTableDaily() {
        var input = ToolkitMessages.BuildTsDataTableInput
                .newBuilder()
                .setId("abc")
                .setDistributionType(fromDistributionType(TsDataTable.DistributionType.FIRST))
                .addCollection(fromTsData(TsData.of(monthly(2010, 1), DoubleSeq.of(1.1))))
                .addCollection(fromTsData(TsData.of(daily(2010, 1, 1), DoubleSeq.of(2.1, 2.2))))
                .build();

        var output = ToolkitMessages.BuildTsDataTableOutput
                .newBuilder()
                .setId("abc")
                .setMatrix(ToolkitMessages.TsMatrix
                        .newBuilder()
                        .setStart(fromTsPeriod(parse("2010-01/P1D")))
                        .setValues(ToolkitMessages.Matrix
                                .newBuilder()
                                .setNrows(31)
                                .setNcols(2)
                                .addAllValues(Arrays.asList(
                                        1.1, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN,
                                        2.1, 2.2, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN))
                                .build())
                        .build())
                .addAllStatuses(Arrays.asList(
                        VS_PRESENT, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED, VS_UNUSED,
                        VS_PRESENT, VS_PRESENT, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER, VS_AFTER
                ))
                .build();

        assertThat(grpc.buildTsDataTable(input).await().atMost(Duration.ofSeconds(5)))
                .isEqualTo(output);
    }

    @Test
    public void testBuildTsDataTableEmpty() {
        var input = ToolkitMessages.BuildTsDataTableInput
                .newBuilder()
                .setId("abc")
                .setDistributionType(fromDistributionType(TsDataTable.DistributionType.FIRST))
                .build();

        var output = ToolkitMessages.BuildTsDataTableOutput
                .newBuilder()
                .setId("abc")
                .setMatrix(ToolkitMessages.TsMatrix
                        .newBuilder()
                        .setStart(fromTsPeriod(parse("1970/P1Y")))
                        .setValues(ToolkitMessages.Matrix
                                .newBuilder()
                                .setNrows(0)
                                .setNcols(0)
                                .build())
                        .build())
                .addAllStatuses(List.of())
                .build();

        assertThat(grpc.buildTsDataTable(input).await().atMost(Duration.ofSeconds(5)))
                .isEqualTo(output);
    }
}
