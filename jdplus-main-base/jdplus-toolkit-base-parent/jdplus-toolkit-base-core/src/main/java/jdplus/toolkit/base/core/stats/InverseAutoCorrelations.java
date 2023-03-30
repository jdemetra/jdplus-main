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
package jdplus.toolkit.base.core.stats;

import jdplus.toolkit.base.api.data.DoubleSeq;
import java.util.function.IntToDoubleFunction;
import jdplus.toolkit.base.core.data.analysis.AutoRegressiveSpectrum;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class InverseAutoCorrelations {

    public IntToDoubleFunction sampleInverseAutoCorrelationsFunction(DoubleSeq data, int nar){
        AutoRegressiveSpectrum ar=new AutoRegressiveSpectrum(AutoRegressiveSpectrum.Method.Durbin);
        ar.process(data, Math.min(nar, data.length()/2));
        DoubleSeq all = ar.getCoefficients();
        double[] par=new double[all.length()+1];
        all.copyTo(par, 1);
        par[0]=1;
        for (int i=1; i<par.length; ++i){
            par[i]=-par[i];
        }
        SymmetricFilter sfilter = SymmetricFilter.convolutionOf(Polynomial.of(par), 1);
        Polynomial p = sfilter.coefficientsAsPolynomial();
        return i->{
            if (i<0)
                i=-i;
            if (i>p.degree())
                return 0;
            return p.get(i)/p.get(0);
        };
    }
}
