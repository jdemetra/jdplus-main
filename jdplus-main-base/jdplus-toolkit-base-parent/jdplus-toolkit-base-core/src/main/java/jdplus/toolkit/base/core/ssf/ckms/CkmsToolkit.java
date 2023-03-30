/*
 * Copyright 2016 National Bank of Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package jdplus.toolkit.base.core.ssf.ckms;

import jdplus.toolkit.base.core.ssf.dk.DiffusePredictionErrorDecomposition;
import jdplus.toolkit.base.core.ssf.likelihood.DiffuseLikelihood;
import jdplus.toolkit.base.core.ssf.dk.sqrt.DiffuseSquareRootInitializer;
import jdplus.toolkit.base.core.ssf.univariate.ILikelihoodComputer;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.ISsfData;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class CkmsToolkit {

    public static ILikelihoodComputer<DiffuseLikelihood> likelihoodComputer(boolean scalingfactor) {
        return (ISsf ssf, ISsfData data) -> {
            DiffusePredictionErrorDecomposition decomp = new DiffusePredictionErrorDecomposition(false);
            CkmsDiffuseInitializer ff = new CkmsDiffuseInitializer(new DiffuseSquareRootInitializer(decomp));
            CkmsFilter ffilter = new CkmsFilter(ff);
            ffilter.process(ssf, data, decomp);
            return decomp.likelihood(scalingfactor);
        };
    }

}
