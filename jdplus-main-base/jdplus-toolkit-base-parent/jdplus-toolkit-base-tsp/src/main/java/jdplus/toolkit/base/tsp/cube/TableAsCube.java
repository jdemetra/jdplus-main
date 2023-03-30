package jdplus.toolkit.base.tsp.cube;

import jdplus.toolkit.base.api.timeseries.util.ObsGathering;
import jdplus.toolkit.base.tsp.util.ObsFormat;
import nbbrd.design.LombokWorkaround;

import java.util.List;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class TableAsCube {

    @lombok.NonNull
    @lombok.Singular
    List<String> dimensions;

    @lombok.NonNull
    String timeDimension;

    @lombok.NonNull
    String measure;

    @lombok.NonNull
    String version;

    @lombok.NonNull
    String label;

    @lombok.NonNull
    ObsFormat format;

    @lombok.NonNull
    ObsGathering gathering;

    @LombokWorkaround
    public static TableAsCube.Builder builder() {
        return new Builder()
                .timeDimension("")
                .measure("")
                .version("")
                .label("")
                .format(ObsFormat.DEFAULT)
                .gathering(ObsGathering.DEFAULT);
    }
}
