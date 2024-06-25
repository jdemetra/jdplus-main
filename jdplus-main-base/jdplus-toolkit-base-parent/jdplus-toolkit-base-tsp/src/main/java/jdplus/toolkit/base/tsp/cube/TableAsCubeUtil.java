/*
 * Copyright 2017 National Bank of Belgium
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

import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.timeseries.util.ObsGathering;
import lombok.NonNull;

import java.util.Locale;
import java.util.stream.Collector;

/**
 * @author Philippe Charles
 * @since 2.2.0
 */
@lombok.experimental.UtilityClass
public class TableAsCubeUtil {

    @NonNull
    public String getDisplayName(@NonNull String db, @NonNull String table, @NonNull String value, @NonNull ObsGathering obsGathering) {
        return String.format(Locale.ROOT, "%s ~ %s \u00BB %s %s", db, table, value, toString(obsGathering));
    }

    @NonNull
    public String getDisplayName(@NonNull CubeId id, @NonNull Collector<? super String, ?, String> joiner) {
        return id.isVoid() ? "All" : (id.isRoot() ? "" : id.getDimensionValueStream().collect(joiner));
    }

    @NonNull
    public String getDisplayNodeName(@NonNull CubeId id) {
        return id.isVoid() ? "All" : (id.isRoot() ? "" : id.getDimensionValue(id.getLevel() - 1));
    }

    private String toString(ObsGathering gathering) {
        return TsUnit.UNDEFINED.equals(gathering.getUnit()) ? "" : ("(" + gathering.getUnit() + "/" + gathering.getAggregationType() + ")");
    }
}
