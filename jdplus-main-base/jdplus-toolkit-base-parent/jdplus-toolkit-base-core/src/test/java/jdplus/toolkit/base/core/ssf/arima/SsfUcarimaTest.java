package jdplus.toolkit.base.core.ssf.arima;

/*
 * Copyright 2017 National Bank of Belgium
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


import tck.demetra.data.Data;
import jdplus.toolkit.base.core.data.DataBlockStorage;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.core.ssf.akf.AkfToolkit;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.composite.CompositeSsf;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import jdplus.toolkit.base.core.ucarima.ModelDecomposer;
import jdplus.toolkit.base.core.ucarima.SeasonalSelector;
import jdplus.toolkit.base.core.ucarima.TrendCycleSelector;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class SsfUcarimaTest {

    public SsfUcarimaTest() {
    }

    @Test
    public void testDkSmoother() {
        UcarimaModel ucm = ucmAirline(-.6, -.8);
        ucm = ucm.simplify();
        CompositeSsf ssf = SsfUcarima.of(ucm);
        SsfData data = new SsfData(Data.RETAIL_BOOKSTORES);
        DefaultSmoothingResults sd = AkfToolkit.smooth(ssf, data, true, true, false);
        DataBlockStorage ds = DkToolkit.fastSmooth(ssf, data);
        int[] pos = ssf.componentsPosition();
        for (int i = 0; i < 3; ++i) {
            System.out.println(sd.getComponent(pos[i]));
            System.out.println(ds.item(pos[i]));
 //           assertTrue(ds.item(pos[i]).distance(sd.getComponent(pos[i])) < 1e-9);
        }
//       System.out.println(sd.getComponentVariance(0));
    }

    public static UcarimaModel ucmAirline(double th, double bth) {
        SarimaOrders spec=SarimaOrders.airline(12);
        SarimaModel sarima = SarimaModel.builder(spec)
                .theta(1, th)
                .btheta(1, bth)
                .build();

        TrendCycleSelector tsel = new TrendCycleSelector();
        SeasonalSelector ssel = new SeasonalSelector(12);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(sarima);
        ucm = ucm.setVarianceMax(-1, false);
        return ucm;
    }
}
