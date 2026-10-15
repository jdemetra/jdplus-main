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
package jdplus.toolkit.base.core.ssf.akf;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.ssf.arima.SsfArima;
import jdplus.toolkit.base.core.ssf.basic.RegSsf;
import jdplus.toolkit.base.core.ssf.composite.CompositeSsf;
import jdplus.toolkit.base.core.ssf.sts.Noise;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import org.junit.jupiter.api.Test;
import tck.demetra.data.Data;

/**
 *
 * @author Jean Palate
 */
public class AkfToolkitTest {

    static final SarimaModel arima1, arima2;

    static {
        SarimaOrders spec = SarimaOrders.airline(12);
        spec.setP(1);
        arima1 = SarimaModel.builder(spec).phi(-.2).theta(1, -.6).btheta(1, -.8).build();
        arima2 = SarimaModel.builder(spec).phi(-.5).theta(1, .3).btheta(1, -.4).build();
    }

    public AkfToolkitTest() {}

    @Test
    public void testSmoothing() {
        SarimaOrders spec = SarimaOrders.m011(1);
        SarimaModel arima = SarimaModel.builder(spec)
                //                .theta(1, -.9)
                .differencing(2, 0)
                .build();
        CompositeSsf ssf = CompositeSsf.builder()
                .add(SsfArima.stateComponent(arima), SsfArima.defaultLoading())
                .add(Noise.of(100), Noise.defaultLoading())
                .build();
        double[] s = Data.NILE.clone();
        s[1] = Double.NaN;
        SsfData data = new SsfData(s);
        int n = s.length;
        FastMatrix X = FastMatrix.make(n, 2);
        X.set(3, 0, 1);
        X.set(0, 1, 1);
        Ssf xssf = RegSsf.ssf(ssf, X);
        DefaultSmoothingResults rslt =
                AkfToolkit.smooth(ssf, data, true, true, true, QAugmentation.DEFAULT_NOCOLLAPSING);
        System.out.println(rslt.getComponent(0));
        rslt = AkfToolkit.smooth(ssf, data, true, true, false, QAugmentation.QType.QR);
        System.out.println(rslt.getComponent(0));
    }
}
