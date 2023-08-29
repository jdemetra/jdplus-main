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
package jdplus.toolkit.base.core.math.linearfilters;

/**
 *
 * @author PALATEJ
 */
public class QuasiSymmetricFiltering implements IQuasiSymmetricFiltering {

    private final SymmetricFilter cf;
    private final IFiniteFilter[] lf;
    private final IFiniteFilter[] rf;

    public QuasiSymmetricFiltering(SymmetricFilter cf, IFiniteFilter[] lf, IFiniteFilter[] rf) {
        this.cf = cf;
        this.lf = lf.clone();
        this.rf = rf.clone();
    }

    @Override
    public SymmetricFilter centralFilter() {
        return cf;
    }

    @Override
    public IFiniteFilter[] leftEndPointsFilters() {
        return lf.clone();
    }

    @Override
    public IFiniteFilter[] rightEndPointsFilters() {
        return rf.clone();
    }


}
