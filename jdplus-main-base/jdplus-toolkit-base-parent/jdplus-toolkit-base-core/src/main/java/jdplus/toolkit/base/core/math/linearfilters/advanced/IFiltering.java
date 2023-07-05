/*
 * Copyright 2023 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.toolkit.base.core.math.linearfilters.advanced;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.linearfilters.IFiniteFilter;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface IFiltering {
    /**
     * Applies a filter on an input to produce an output.
     * The input and the output must have the same length
     * @param in
     * @return 
     */
    DoubleSeq process(DoubleSeq in);
    
    IFiniteFilter centralFilter();
    
    IFiniteFilter[] leftEndPointsFilters();
    IFiniteFilter[] rightEndPointsFilters();
}
