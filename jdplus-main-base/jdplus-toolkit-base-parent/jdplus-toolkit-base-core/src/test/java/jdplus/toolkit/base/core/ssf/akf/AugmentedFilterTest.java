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

import java.util.Random;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.ssf.arima.SsfArima;
import jdplus.toolkit.base.core.ssf.likelihood.DiffuseLikelihood;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Jean Palate
 */
public class AugmentedFilterTest {

    static final SarimaModel arima1, arima2;

    static {
        SarimaOrders spec = SarimaOrders.airline(12);
        spec.setP(1);
        arima1 = SarimaModel.builder(spec).phi(-.2).theta(1, -.6).btheta(1, -.8).build();
        arima2 = SarimaModel.builder(spec).phi(-.5).theta(1, .3).btheta(1, -.4).build();
    }

    public AugmentedFilterTest() {
    }

    @Test
    public void testLL() {
        DataBlock A = DataBlock.make(200);
        Random rnd = new Random(0);
        A.apply(i -> rnd.nextDouble());

        Ssf ssf = Ssf.of(SsfArima.stateComponent(arima1), SsfArima.defaultLoading());
        SsfData ssfData = new SsfData(A);
        QPredictionErrorDecomposition frslts = new QPredictionErrorDecomposition(true);
        AugmentedFilter filter = new AugmentedFilter();
        frslts.prepare(ssf, A.length());
        filter.process(ssf, ssfData, frslts);
        DiffuseLikelihood ll0 = frslts.likelihood(true);

        QRFilter filter2 = new QRFilter();
        filter2.process(ssf, ssfData);
        DiffuseLikelihood ll1 = filter2.diffuseLikelihood(true, true);
        DiffuseLikelihood ll2 = AkfToolkit.likelihoodComputer(true, true, true).compute(ssf, ssfData);
        assertEquals(ll0.logLikelihood(), ll1.logLikelihood(), 1e-10);
        assertEquals(ll0.logLikelihood(), ll2.logLikelihood(), 1e-10);
    }

    public static void main(String[] arg) {
        int N = 200, K = 10000;
        DataBlock A = DataBlock.make(N);
        Random rnd = new Random(0);
        A.apply(i -> rnd.nextDouble());

        Ssf ssf = Ssf.of(SsfArima.stateComponent(arima1), SsfArima.defaultLoading());
        SsfData ssfData = new SsfData(A);
        QPredictionErrorDecomposition frslts = new QPredictionErrorDecomposition(true);
        AugmentedFilter filter = new AugmentedFilter();
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            frslts.clear();
            frslts.prepare(ssf, A.length());
            filter.process(ssf, ssfData, frslts);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        System.out.println(frslts.likelihood(true));

        QRFilter filter2 = new QRFilter();
        t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            filter2.process(ssf, ssfData);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        System.out.println(filter2.diffuseLikelihood(true, true));

        DiffuseLikelihood dl = null;
        t0 = System.currentTimeMillis();

        for (int i = 0; i < K; ++i) {
            dl = AkfToolkit.likelihoodComputer(true, true, true).compute(ssf, ssfData);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        System.out.println(dl);
    }
}
