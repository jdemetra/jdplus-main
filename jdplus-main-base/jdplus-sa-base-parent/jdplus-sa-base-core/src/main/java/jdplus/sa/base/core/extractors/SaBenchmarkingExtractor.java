/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.sa.base.core.extractors;

import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.sa.base.api.SaDictionaries;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.sa.base.core.SaBenchmarkingResults;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(InformationExtractor.class)
public class SaBenchmarkingExtractor extends InformationMapping<SaBenchmarkingResults> {

    @Override
    public Class getSourceClass() {
        return SaBenchmarkingResults.class;
    }


    public SaBenchmarkingExtractor() {

        set(SaDictionaries.ORIGINAL, TsData.class, source -> source.getSa());
        set(SaDictionaries.TARGET, TsData.class, source -> source.getTarget());
        set(SaDictionaries.BENCHMARKED, TsData.class, source -> source.getBenchmarkedSa());
    }
}
