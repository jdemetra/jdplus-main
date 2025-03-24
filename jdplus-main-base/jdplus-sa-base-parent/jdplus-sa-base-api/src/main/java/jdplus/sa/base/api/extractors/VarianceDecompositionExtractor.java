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
package jdplus.sa.base.api.extractors;

import jdplus.sa.base.api.SaDictionaries;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.sa.base.api.StationaryVarianceDecomposition;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(InformationExtractor.class)
public class VarianceDecompositionExtractor extends InformationMapping<StationaryVarianceDecomposition> {

    public VarianceDecompositionExtractor() {
        set(SaDictionaries.VAR_CYCLE, Double.class, source -> source.getC());
        set(SaDictionaries.VAR_SEASONAL, Double.class, source -> source.getS());
        set(SaDictionaries.VAR_IRREGULAR, Double.class, source -> source.getI());
        set(SaDictionaries.VAR_TDH, Double.class, source -> source.getCalendar());
        set(SaDictionaries.VAR_OTHERS, Double.class, source -> source.getP());
        set(SaDictionaries.VAR_TOTAL, Double.class, source -> source.total());
    }

    @Override
    public Class<StationaryVarianceDecomposition> getSourceClass() {
        return StationaryVarianceDecomposition.class;
    }

}
