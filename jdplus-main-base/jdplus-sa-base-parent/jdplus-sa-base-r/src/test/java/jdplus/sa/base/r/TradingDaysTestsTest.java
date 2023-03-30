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
package jdplus.sa.base.r;

import tck.demetra.data.Data;
import jdplus.toolkit.base.core.data.DataBlockStorage;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.composite.CompositeSsf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.core.timeseries.simplets.TsDataToolkit;
import static jdplus.toolkit.base.core.timeseries.simplets.TsDataToolkit.log;
import jdplus.toolkit.base.core.ucarima.ModelDecomposer;
import jdplus.toolkit.base.core.ucarima.SeasonalSelector;
import jdplus.toolkit.base.core.ucarima.TrendCycleSelector;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;
import jdplus.toolkit.base.core.ssf.arima.SsfUcarima;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jean Palate
 */
public class TradingDaysTestsTest {

    public TradingDaysTestsTest() {
    }

    @Test
    public void testTD() {
        TsData s = log(TsData.ofInternal(TsPeriod.monthly(1992, 1), Data.ABS_RETAIL));
        TimeVaryingRegression.Results regarima = TimeVaryingRegression.regarima(s, "TD7", "Default", 1e-7);
        TsData rtd = regarima.getData("tdeffect", TsData.class);

        UcarimaModel ucm = ucmAirline(regarima.getArima());
        ucm = ucm.simplify();
        CompositeSsf ssf = SsfUcarima.of(ucm);
        SsfData data = new SsfData(s.getValues());
        DataBlockStorage ds = DkToolkit.fastSmooth(ssf, data);
        int[] pos = ssf.componentsPosition();
        TsData i1 = TsData.ofInternal(s.getStart(), ds.item(pos[2]).toArray());
        
        data = new SsfData(TsDataToolkit.subtract(s, rtd).getValues());
        ds = DkToolkit.fastSmooth(ssf, data);
        TsData i2 = TsData.ofInternal(s.getStart(), ds.item(pos[2]).toArray());
       
//        System.out.println(TradingDaysTests.ftest(i1, true, 0));
//        System.out.println(TradingDaysTests.ftest(i1, false, 0));
//        System.out.println(TradingDaysTests.ftest(i1, true, 8));
//        System.out.println(TradingDaysTests.ftest(i1, false, 8));
//        System.out.println(TradingDaysTests.ftest(i2, true, 0));
//        System.out.println(TradingDaysTests.ftest(i2, false, 0));
//        System.out.println(TradingDaysTests.ftest(i2, true, 8));
//        System.out.println(TradingDaysTests.ftest(i2, false, 8));
    }

    public static UcarimaModel ucmAirline(double th, double bth) {
        SarimaOrders spec = SarimaOrders.airline(12);
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

    public static UcarimaModel ucmAirline(SarimaModel sarima) {

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
