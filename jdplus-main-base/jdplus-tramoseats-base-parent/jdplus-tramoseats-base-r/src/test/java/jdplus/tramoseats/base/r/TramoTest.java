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
package jdplus.tramoseats.base.r;

import tck.demetra.data.Data;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.tramoseats.base.core.tramo.TramoOutput;
import jdplus.tramoseats.base.api.tramo.TramoSpec;

import java.util.Map;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author palatej
 */
public class TramoTest {

    public TramoTest() {
    }

    @Test
    public void test() {
        RegSarimaModel rslt = Tramo.process(Data.TS_PROD, "TR5");
        Map<String, Class> dictionary = rslt.getDictionary();
//        dictionary.forEach((k, v)->System.out.println(k));
        assertTrue(rslt.getData("span.n", Integer.class) == Data.TS_PROD.length());
//        System.out.println(DoubleSeq.of(rslt.getData("sarima.parameters", double[].class)));

        SarimaModel model = rslt.getData("model", SarimaModel.class);
        String[] desc = rslt.getData("regression.description", String[].class);
//        Arrays.stream(desc).forEach(v->System.out.println(v));
        assertTrue(desc != null);
        StatisticalTest data = rslt.getData("residuals.doornikhansen", StatisticalTest.class);
        System.out.println(data.getPvalue());
    }

    @Test
    public void testFull() {
        TramoOutput rslt = Tramo.fullProcess(Data.TS_PROD, "TR5");
        byte[] bytes = Tramo.toBuffer(rslt);
        assertTrue(bytes != null);

        TramoOutput rslt2 = Tramo.fullProcess(Data.TS_PROD, rslt.getResultSpec(), null);
        byte[] bytes2 = Tramo.toBuffer(rslt2);
        assertTrue(bytes2 != null);

        byte[] sbytes = Tramo.toBuffer(rslt.getEstimationSpec());
        TramoSpec spec = Tramo.specOf(sbytes);

        assertTrue(spec != null);
    }

    @Test
    public void testShort() {
        double[] x = new double[]{
            2340456, 8944420, 7576600, 12100288,
            9460370, 9460370, 7790305, 11447244,
            7856177, 7641116, 7044036, 10595520,
            8574256, 7933196, 7658433, 10893388,
            8288369, 7885537, 4828187, 10993388
        };
        TsData X = TsData.ofInternal(TsPeriod.quarterly(2015, 1), x);
        TramoOutput rslt = Tramo.fullProcess(X, "TRfull");
        byte[] bytes = Tramo.toBuffer(rslt);
        assertTrue(bytes != null);

        TramoOutput rslt2 = Tramo.fullProcess(X, rslt.getResultSpec(), null);
        byte[] bytes2 = Tramo.toBuffer(rslt2);
        assertTrue(bytes2 != null);

        byte[] sbytes = Tramo.toBuffer(rslt.getEstimationSpec());
        TramoSpec spec = Tramo.specOf(sbytes);

        assertTrue(spec != null);
    }

    @Test
    public void testSpec() {
        byte[] bytes = Tramo.toBuffer(TramoSpec.TRfull);
        TramoSpec trf = Tramo.specOf(bytes);
        assertTrue(trf.equals(TramoSpec.TRfull));
    }

    @Test
    public void testForecast0() {
        Matrix terror = Tramo.forecast(Data.TS_PROD, TramoSpec.TR0, null, 12);
        assertTrue(terror != null);
//        System.out.println(terror);
    }

    @Test
    public void testForecast() {
        TsData s = TsData.ofInternal(TsPeriod.monthly(1992, 1), Data.RETAIL_BOOKSTORES).drop(0, 12);
        Matrix terror = Tramo.forecast(s, TramoSpec.TRfull, null, 12);
        assertTrue(terror != null);
        //       System.out.println(terror);

    }

    @Test
    public void testRefresh() {
        TramoOutput rslt = Tramo.fullProcess(Data.TS_PROD, "TR5");

        TramoSpec fspec = Tramo.refreshSpec(rslt.getResultSpec(), TramoSpec.TRfull, null, "Fixed");
        TramoSpec pspec = Tramo.refreshSpec(rslt.getResultSpec(), rslt.getEstimationSpec(), null, "FreeParameters");
        TramoSpec ospec = Tramo.refreshSpec(rslt.getResultSpec(), rslt.getEstimationSpec(), null, "Outliers");

        byte[] b = Tramo.toBuffer(fspec);
        TramoSpec fspec2 = Tramo.specOf(b);
    }

}
