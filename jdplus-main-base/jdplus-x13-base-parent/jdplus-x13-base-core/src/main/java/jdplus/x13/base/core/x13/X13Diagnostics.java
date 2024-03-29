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
package jdplus.x13.base.core.x13;

import jdplus.sa.base.api.StationaryVarianceDecomposition;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.x13.base.core.x11.X11Results;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.sa.base.core.diagnostics.GenericSaTests;
import jdplus.toolkit.base.api.timeseries.TsDomain;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class X13Diagnostics {

    private StationaryVarianceDecomposition varianceDecomposition;
    private GenericSaTests genericDiagnostics;
    private Mstatistics mstatistics;

    public static X13Diagnostics of(RegSarimaModel preprocessing, X13Preadjustment preadj, X11Results xrslts, X13Finals finals) {
        Mstatistics mstats = Mstatistics.of(preadj, xrslts, finals);
        boolean mul = xrslts.getMode().isMultiplicative();
        TsDomain dom = xrslts.getActualDomain();
        TsData sa = TsData.fitToDomain(xrslts.getD11(), dom);
        TsData i = TsData.fitToDomain(xrslts.getD13(), dom);
        TsData t = TsData.fitToDomain(xrslts.getD12(), dom);
        TsData si = TsData.fitToDomain(xrslts.getD8(), dom);
        TsData y = TsData.fitToDomain(xrslts.getB1(), dom);
        TsData lsa = mul ? sa.log() : sa;
        TsData li = mul ? i.log() : i;
        TsData lin = preprocessing != null ? preprocessing.linearizedSeries() : mul ? preadj.getA1().log() : preadj.getA1();

        GenericSaTests gsadiags = GenericSaTests.builder()
                .mul(mul)
                .regarima(preprocessing)
                .lin(lin)
                .res(preprocessing == null ? null : preprocessing.fullResiduals())
                .y(y)
                .sa(sa)
                .irr(i)
                .si(si)
                .lsa(lsa)
                .lirr(li)
                .build();
        return new X13Diagnostics(varDecomposition(mstats), gsadiags, mstats);
    }

    private static StationaryVarianceDecomposition varDecomposition(Mstatistics m) {
        return StationaryVarianceDecomposition.builder()
                .C(m.getVarC())
                .S(m.getVarS())
                .I(m.getVarI())
                .Calendar(m.getVarTD())
                .P(m.getVarP())
                .trendType(StationaryVarianceDecomposition.TrendType.Linear)
                .build();

    }
}
