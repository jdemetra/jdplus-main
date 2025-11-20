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
package internal.spreadsheet.base.api;

import jdplus.spreadsheet.base.api.SpreadSheetBean;
import jdplus.toolkit.base.api.timeseries.util.ObsGathering;
import jdplus.toolkit.base.tsp.DataSet;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.base.tsp.legacy.LegacyHandler;
import jdplus.toolkit.base.tsp.util.ObsFormat;
import jdplus.toolkit.base.tsp.util.PropertyHandler;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author Philippe Charles
 */
public interface SpreadSheetParam extends DataSource.Converter<SpreadSheetBean> {

    @NonNull String getVersion();

    DataSet.@NonNull Converter<String> getSheetParam();

    DataSet.@NonNull Converter<String> getSeriesParam();

    final class V1 implements SpreadSheetParam {

        @lombok.experimental.Delegate
        private final DataSource.Converter<SpreadSheetBean> converter =
                SpreadSheetBeanHandler
                        .builder()
                        .file(PropertyHandler.onFile("file", Path.of("").toFile()))
                        .format(LegacyHandler.onObsFormat("locale", "datePattern", "numberPattern", ObsFormat.getSystemDefault()))
                        .gathering(LegacyHandler.onObsGathering("frequency", "aggregationType", "cleanMissing", ObsGathering.DEFAULT))
                        .build()
                        .asDataSourceConverter();

        @lombok.Getter
        private final String version = "20111201";

        @lombok.Getter
        private final DataSet.Converter<String> sheetParam = PropertyHandler.onString("sheetName", "").asDataSetConverter();

        @lombok.Getter
        private final DataSet.Converter<String> seriesParam = PropertyHandler.onString("seriesName", "").asDataSetConverter();
    }

    @lombok.Builder(toBuilder = true)
    final class SpreadSheetBeanHandler implements PropertyHandler<SpreadSheetBean> {

        @lombok.NonNull
        private final PropertyHandler<File> file;

        @lombok.NonNull
        private final PropertyHandler<ObsFormat> format;

        @lombok.NonNull
        private final PropertyHandler<ObsGathering> gathering;

        @Override
        public @NonNull SpreadSheetBean get(@NonNull Function<? super String, ? extends CharSequence> properties) {
            SpreadSheetBean result = new SpreadSheetBean();
            result.setFile(file.get(properties));
            result.setFormat(format.get(properties));
            result.setGathering(gathering.get(properties));
            return result;
        }

        @Override
        public void set(@NonNull BiConsumer<? super String, ? super String> properties, @Nullable SpreadSheetBean value) {
            if (value != null) {
                file.set(properties, value.getFile());
                // FIXME: NPE bug in jtss
                if (value.getFormat() != null) {
                    format.set(properties, value.getFormat());
                }
                if (value.getGathering() != null) {
                    gathering.set(properties, value.getGathering());
                }
            }
        }
    }
}
