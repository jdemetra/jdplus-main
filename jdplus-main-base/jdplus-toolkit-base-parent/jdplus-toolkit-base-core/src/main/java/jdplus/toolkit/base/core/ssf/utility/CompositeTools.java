/*
 * Copyright 2026 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.toolkit.base.core.ssf.utility;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.QuadraticForm;
import jdplus.toolkit.base.core.ssf.akf.AkfToolkit;
import jdplus.toolkit.base.core.ssf.akf.QAugmentation;
import jdplus.toolkit.base.core.ssf.composite.CompositeSsf;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import lombok.NonNull;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class CompositeTools {

    /**
     * Computes the smoothed components and eventually their variances
     * @param ssf The composite ssf (nc components)
     * @param data The data to be decomposed (length n)
     * @param cmps A Matrix that will contain the components (dim: n x nc)
     * @param vcmps A Matrix that will contain the variances of the components (dim: n x nc); null if we don't need them.
     */
    public void smoothedComponents(
            @NonNull CompositeSsf ssf, @NonNull DoubleSeq data, @NonNull FastMatrix cmps, FastMatrix vcmps) {

        int[] dim = ssf.componentsDimension();
        if (dim.length != cmps.getColumnsCount() || (vcmps != null && vcmps.getColumnsCount() != dim.length)) {
            throw new IllegalArgumentException();
        }
        int n = data.length();
        DefaultSmoothingResults srslts = AkfToolkit.smooth(
                ssf, new SsfData(data), vcmps != null, vcmps != null, true, QAugmentation.QType.NORMAL);
        DataBlock Z = DataBlock.make(ssf.getStateDim());

        for (int i = 0; i < n; ++i) {
            Z.set(0);
            ssf.loading().Z(i, Z);
            for (int j = 0, k = 0; j < 3; ++j) {
                DataBlock curZ = Z.extract(k, dim[j]);
                DataBlock curA = srslts.a(i).extract(k, dim[j]);
                cmps.set(i, j, curZ.dot(curA));
                if (vcmps != null) {
                    FastMatrix curP = srslts.P(i).extract(k, dim[j], k, dim[j]);
                    vcmps.set(i, j, QuadraticForm.apply(curP, curZ));
                }
                k += dim[j];
            }
        }
    }
}
