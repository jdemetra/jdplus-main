/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.core.tramoseats;

import jdplus.toolkit.base.api.modelling.ComponentInformation;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.SeriesDecomposition;
import jdplus.sa.base.api.StationaryVarianceDecomposition;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.sa.base.core.StationaryVarianceComputer;
import jdplus.sa.base.core.diagnostics.GenericSaTests;
import jdplus.tramoseats.base.core.seats.SeatsResults;
import jdplus.tramoseats.base.core.seats.SeatsTests;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class TramoSeatsDiagnostics {

    private StationaryVarianceDecomposition varianceDecomposition;
    private GenericSaTests genericDiagnostics;
    private SeatsTests specificDiagnostics;

    public static TramoSeatsDiagnostics of(RegSarimaModel preprocessing, SeatsResults srslts, SeriesDecomposition finals){
//        DefaultSaDiagnostics.Builder sadiags = DefaultSaDiagnostics.builder()
//                .varianceDecomposition(varDecomposition(preprocessing, srslts));
        boolean mul = preprocessing.getDescription().isLogTransformation();
        TsData sa = srslts.getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);
        TsData i = srslts.getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value);
        TsData s = srslts.getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Value);
        TsData si = mul ? TsData.multiply(s, i) : TsData.add(s, i);
        TsData lsa = srslts.getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);
        TsData li = srslts.getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value);
        TsData lin=preprocessing.linearizedSeries();
        TsData y=preprocessing.backTransform(lin, false);
        
        GenericSaTests gsadiags = GenericSaTests.builder()
                .mul(mul)
                .regarima(preprocessing)
                .lin(lin)
                .res(preprocessing.fullResiduals())
                .y(y)
                .sa(sa)
                .irr(i)
                .si(si)
                .lsa(lsa)
                .lirr(li)
                .build();
        
        SeatsTests st=new SeatsTests(srslts);
                
        return new TramoSeatsDiagnostics(varDecomposition(preprocessing, srslts), gsadiags, st);
    }

    private static StationaryVarianceDecomposition varDecomposition(RegSarimaModel preprocessing, SeatsResults srslts) {
        StationaryVarianceComputer var = new StationaryVarianceComputer(StationaryVarianceComputer.HP);
        boolean mul = preprocessing.getDescription().isLogTransformation();
        TsData y = preprocessing.interpolatedSeries(false),
                t = srslts.getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.Value),
                seas = srslts.getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Value),
                irr = srslts.getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value),
                cal = preprocessing.getCalendarEffect(y.getDomain());

        TsData others;
        if (mul) {
            TsData all = TsData.multiply(t, seas, irr, cal);
            others = TsData.divide(y, all);
        } else {
            TsData all = TsData.add(t, seas, irr, cal);
            others = TsData.subtract(y, all);
        }
        return var.build(y, t, seas, irr, cal, others, mul);
    }
}
