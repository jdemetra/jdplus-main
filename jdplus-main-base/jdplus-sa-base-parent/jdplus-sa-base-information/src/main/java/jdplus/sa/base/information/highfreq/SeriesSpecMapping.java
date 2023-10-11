/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.sa.base.information.highfreq;

import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.api.modelling.highfreq.SeriesSpec;
import jdplus.toolkit.base.api.timeseries.TimeSelector;
import jdplus.toolkit.base.api.modelling.highfreq.DataCleaning;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class SeriesSpecMapping {

    final String SPAN = "span", CLEANING = "cleaning";


    public InformationSet write(SeriesSpec spec, boolean verbose) {
        if (!verbose && spec.isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (verbose || spec.getSpan().getType() != TimeSelector.SelectionType.All) {
            info.add(SPAN, spec.getSpan());
        }
         if (verbose || spec.getCleaning() != DataCleaning.NONE) {
            info.add(CLEANING, spec.getCleaning().name());
        }
        return info;
    }

    public SeriesSpec read(InformationSet info) {

        if (info == null) {
            return SeriesSpec.DEFAULT;
        }

        SeriesSpec.Builder builder = SeriesSpec.builder();
        TimeSelector span = info.get(SPAN, TimeSelector.class);
        if (span != null) {
            builder.span(span);
        }
        String cleaning = info.get(CLEANING, String.class);
        if (cleaning != null) {
            DataCleaning dc=DataCleaning.valueOf(cleaning);
            builder.cleaning(dc);
        }
        return builder.build();
    }

}
