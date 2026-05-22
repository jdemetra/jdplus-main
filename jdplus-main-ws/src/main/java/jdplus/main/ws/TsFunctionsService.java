package jdplus.main.ws;

import io.quarkus.grpc.GrpcService;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/hello")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@GrpcService
@RegisterForReflection
public class TsFunctionsService implements TsFunctions {

    @RequestBody(
            content = @Content(
                    examples = @ExampleObject(
                            name = "Example 1",
                            value = """
                                    {
                                      "id": "Example 1",
                                      "series": {
                                        "start": {
                                          "frequency": "FREQ_YEARLY",
                                          "year": 2010,
                                          "pos": 0
                                        },
                                        "values": [ 3.0, 4.0 ]
                                      }
                                    }
                                    """
                    )
            )
    )
    @POST
    @Path("/normalize")
    @Override
    public Uni<ToolkitMessages.TsFunctionOutput> normalize(ToolkitMessages.TsFunctionInput request) {
        return Uni.createFrom().item(Helpers.normalize(request));
    }

    @RequestBody(
            content = @Content(
                    examples = @ExampleObject(
                            name = "Example 1",
                            value = """
                                    {
                                      "id": "Example 1",
                                      "series": {
                                        "start": {
                                          "frequency": "FREQ_YEARLY",
                                          "year": 2010,
                                          "pos": 0
                                        },
                                        "values": [ 0, 1, 2, 3, 4, 5, 6, 7 ]
                                      }
                                    }
                                    """
                    )
            )
    )
    @POST
    @Path("/statistics")
    @Override
    public Uni<ToolkitMessages.DescriptiveStatistics> statistics(ToolkitMessages.TsFunctionInput request) {
        return Uni.createFrom().item(Helpers.statistics(request));
    }

    @POST
    @Path("/pct")
    @Override
    public Uni<ToolkitMessages.TsFunctionOutput> pct(ToolkitMessages.PctInput request) {
        return Uni.createFrom().item(Helpers.pct(request));
    }

    @POST
    @Path("/delta")
    @Override
    public Uni<ToolkitMessages.TsFunctionOutput> delta(ToolkitMessages.DeltaInput request) {
        return Uni.createFrom().item(Helpers.delta(request));
    }

    @POST
    @Path("/aggregate")
    @Override
    public Uni<ToolkitMessages.TsFunctionOutput> aggregate(ToolkitMessages.AggregationInput request) {
        return Uni.createFrom().item(Helpers.aggregate(request));
    }

    @POST
    @Path("/hodrickPrescott")
    @Override
    public Uni<ToolkitMessages.HodrickPrescottOutput> hodrickPrescott(ToolkitMessages.HodrickPrescottInput request) {
        return Uni.createFrom().item(Helpers.hodrickPrescott(request));
    }

    @RequestBody(
            content = @Content(
                    examples = @ExampleObject(
                            name = "Example 1",
                            value = """
                                    {
                                      "id": "Example 1",
                                      "gathering": {
                                        "frequency": "FREQ_UNDEFINED",
                                        "aggregationType": "AGGREGATION_NONE",
                                        "allowPartialAggregation": false,
                                        "includeMissingValues": false
                                      },
                                      "observations": [
                                        {
                                          "date": { "year": 2010, "month": 1, "day": 1 },
                                          "value": 11
                                        },
                                        {
                                          "date": { "year": 2010, "month": 2, "day": 1 },
                                          "value": 22
                                        }
                                      ]
                                    }
                                    """
                    )
            )
    )
    @POST
    @Path("/buildTsData")
    @Override
    public Uni<ToolkitMessages.TsFunctionOutput> buildTsData(ToolkitMessages.BuildTsDataInput request) {
        return Uni.createFrom().item(Helpers.buildTsData(request));
    }

    @Override
    public Multi<ToolkitMessages.TsFunctionOutput> normalizeStream(Multi<ToolkitMessages.TsFunctionInput> request) {
        return request.onItem().transform(Helpers::normalize);
    }

    @Override
    public Multi<ToolkitMessages.DescriptiveStatistics> statisticsStream(Multi<ToolkitMessages.TsFunctionInput> request) {
        return request.onItem().transform(Helpers::statistics);
    }

    @Override
    public Multi<ToolkitMessages.TsFunctionOutput> pctStream(Multi<ToolkitMessages.PctInput> request) {
        return request.onItem().transform(Helpers::pct);
    }

    @Override
    public Multi<ToolkitMessages.TsFunctionOutput> deltaStream(Multi<ToolkitMessages.DeltaInput> request) {
        return request.onItem().transform(Helpers::delta);
    }

    @Override
    public Multi<ToolkitMessages.TsFunctionOutput> aggregateStream(Multi<ToolkitMessages.AggregationInput> request) {
        return request.onItem().transform(Helpers::aggregate);
    }

    @Override
    public Multi<ToolkitMessages.HodrickPrescottOutput> hodrickPrescottStream(Multi<ToolkitMessages.HodrickPrescottInput> request) {
        return request.onItem().transform(Helpers::hodrickPrescott);
    }

    @Override
    public Multi<ToolkitMessages.TsFunctionOutput> buildTsDataStream(Multi<ToolkitMessages.BuildTsDataInput> request) {
        return request.onItem().transform(Helpers::buildTsData);
    }

    @RequestBody(
            content = @Content(
                    examples = @ExampleObject(
                            name = "Example 1",
                            value = """
                                    {
                                      "id": "Example 1",
                                      "distributionType": "DIST_FIRST",
                                      "collection": [
                                        {
                                          "start": {
                                            "frequency": "FREQ_QUARTERLY",
                                            "year": 2010
                                          },
                                          "values": [
                                            1.1
                                          ]
                                        },
                                        {
                                          "start": {
                                            "frequency": "FREQ_MONTHLY",
                                            "year": 2010
                                          },
                                          "values": [
                                            2.1,
                                            2.2
                                          ]
                                        }
                                      ]
                                    }
                                    """
                    )
            )
    )
    @POST
    @Path("/buildTsDataTable")
    @Override
    public Uni<ToolkitMessages.BuildTsDataTableOutput> buildTsDataTable(ToolkitMessages.BuildTsDataTableInput request) {
        return Uni.createFrom().item(Helpers.buildTsDataTable(request));
    }
}
