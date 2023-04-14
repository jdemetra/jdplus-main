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
package jdplus.tramoseats.base.core.seats;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import tck.demetra.data.Data;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.ucarima.ModelDecomposer;
import jdplus.toolkit.base.core.ucarima.SeasonalSelector;
import jdplus.toolkit.base.core.ucarima.TrendCycleSelector;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate
 */
public class BurmanEstimatesTest {

    public BurmanEstimatesTest() {
    }

    @Test
    public void testAirline() {
        UcarimaModel ucm = ucmAirline(-.6, -.8);
        ucm = ucm.simplify();
        BurmanEstimates burman = BurmanEstimates.builder()
                .mean(true)
                .backcastsCount(30)
                .forecastsCount(10)
                .data(Data.TS_PROD.getValues())
                .ucarimaModel(ucm)
                .build();
        DoubleSeq estimates = burman.estimates(0, true);
//        System.out.println(estimates);
        estimates = burman.estimates(1, true);
//        System.out.println(estimates);
        estimates = burman.estimates(2, true);
//        System.out.println(estimates);
    }

    @Test
    public void testAirlineC() {
        UcarimaModel ucm = ucmAirline(-.6, -.8);
        ucm = ucm.simplify();
        BurmanEstimatesC burman = BurmanEstimatesC.builder()
                .mean(true)
                .backcastsCount(30)
                .forecastsCount(10)
                .data(Data.TS_PROD.getValues())
                .ucarimaModel(ucm)
                .build();
        DoubleSeq estimates = burman.estimates(0, true);
//        System.out.println("burmanc");
//        System.out.println(estimates);
        estimates = burman.estimates(1, true);
//        System.out.println(estimates);
        estimates = burman.estimates(2, true);
//        System.out.println(estimates);
    }

    public static UcarimaModel ucmAirline(double th, double bth) {
        SarimaOrders spec = new SarimaOrders(12);
        spec.setP(1);
        spec.setQ(1);
        spec.setBp(1);
        spec.setBq(1);
        SarimaModel sarima = SarimaModel.builder(spec)
                .phi(1, -.9)
                .bphi(1, -.9)
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
