/*
 * Copyright 2013-2014 National Bank of Belgium
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
package jdplus.toolkit.base.core.ssf.univariate;

import jdplus.toolkit.base.core.ssf.StateInfo;
import jdplus.toolkit.base.core.ssf.StateStorage;

/**
 * Contains at position t: R(t-1), N(t-1), u(t), M(t)
 * (R(n)=0, N(n)=0)
 * @author Jean Palate
 */
public class DefaultSmoothingResults extends StateStorage implements ISmoothingResults {

    protected DefaultSmoothingResults(final boolean cov) {
        super(StateInfo.Smoothed, cov);
    }
    
    @Override
    public void prepare(int dim, int start, int end) {
        super.prepare(dim, start, end);
    }

    @Override
    public void rescaleVariances(double factor) {
    }

    public static DefaultSmoothingResults full() {
        return new DefaultSmoothingResults(true);
    }

    public static DefaultSmoothingResults light() {
        return new DefaultSmoothingResults(false);
    }

}
