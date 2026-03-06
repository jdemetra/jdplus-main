/*
 * Copyright 2025 JDemetra+.
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
package jdplus.toolkit.base.core.ssf.arima;

import java.util.Random;
import jdplus.toolkit.base.api.arima.SarmaOrders;
import jdplus.toolkit.base.core.arima.ArimaModel;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixNorms;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.likelihood.DiffuseLikelihood;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import jdplus.toolkit.base.core.ssf.utility.DynamicsCoherence;
import jdplus.toolkit.base.core.ssf.utility.StationaryInitialization;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Jean Palate
 */
public class SsfArma2Test {

    public SsfArma2Test() {
    }

    @Test
    public void testDynamics() {

        for (int p = 0; p <= 3; ++p) {
            for (int q = 0; q <= 3; ++q) {
                for (int bp = 0; bp <= 1; ++bp) {
                    for (int bq = 0; bq <= 1; ++bq) {
                        SarmaOrders spec = new SarmaOrders(12);
                        spec.setP(p);
                        spec.setBp(bp);
                        spec.setQ(q);
                        spec.setBq(bq);
                        SarimaModel arima = SarimaModel.builder(spec).setDefault().build();
                        StateComponent cmp = SsfArma2.stateComponent(arima);
                        DynamicsCoherence.check(cmp.dynamics(), cmp.dim());
                    }
                }
            }
        }
    }

    @Test
    public void testInitialisation() {

        for (int p = 0; p <= 3; ++p) {
            for (int q = 0; q <= 3; ++q) {
                for (int bp = 0; bp <= 1; ++bp) {
                    for (int bq = 0; bq <= 1; ++bq) {
                        SarmaOrders spec = new SarmaOrders(12);
                        spec.setP(p);
                        spec.setBp(bp);
                        spec.setQ(q);
                        spec.setBq(bq);
                        SarimaModel arima = SarimaModel.builder(spec).setDefault().build();
                        StateComponent cmp = SsfArma2.stateComponent(arima);
                        FastMatrix M = StationaryInitialization.of(cmp.dynamics(), cmp.dim());
                        FastMatrix Q = FastMatrix.square(cmp.dim());
                        cmp.initialization().Pf0(Q);
                        M.sub(Q);
                        double w = MatrixNorms.frobeniusNorm(M);
                        assertTrue(w < 1e-6);
                    }
                }
            }
        }
    }

    @Test
    public void testLikelihood() {
        int p = 1;
        int bp = 1;
        int q = 3;
        int bq = 1;
        SarmaOrders spec = new SarmaOrders(12);
        spec.setP(p);
        spec.setBp(bp);
        spec.setQ(q);
        spec.setBq(bq);
        SarimaModel arima = SarimaModel.builder(spec).setDefault().build();
        StateComponent cmp2 = SsfArma2.stateComponent(arima);
        Ssf ssf2 = Ssf.of(cmp2, SsfArma2.defaultLoading());
        DataBlock x = DataBlock.make(240);
        Random rnd = new Random();
        x.set(() -> rnd.nextDouble(-1, 1));

        DiffuseLikelihood ll2 = DkToolkit.likelihood(ssf2, new SsfData(x), true, true);

        Ssf ssf1 = SsfArima.ssf(arima);
        DiffuseLikelihood ll1 = DkToolkit.likelihood(ssf1, new SsfData(x), true, true);
        assertEquals(ll1.logLikelihood(), ll2.logLikelihood(), 1e-6);
//        System.out.println(ll1.logLikelihood());
//        System.out.println(ll2.logLikelihood());

        ArimaModel model = ArimaModel.of(arima).scaleVariance(10);
        cmp2 = SsfArma2.stateComponent(model);
        ssf2 = Ssf.of(cmp2, SsfArma2.defaultLoading());

        ll2 = DkToolkit.likelihood(ssf2, new SsfData(x), true, true);

        ssf1 = SsfArima.ssf(model);
        ll1 = DkToolkit.likelihood(ssf1, new SsfData(x), true, true);
        assertEquals(ll1.logLikelihood(), ll2.logLikelihood(), 1e-6);
//        System.out.println(ll1.logLikelihood());
//        System.out.println(ll2.logLikelihood());

    }

    public static void main(String[] arg) {
        int p = 3;
        int bp = 0;
        int q = 1;
        int bq = 1;
        int N = 300;
        int K = 10000;
        SarmaOrders spec = new SarmaOrders(12);
        spec.setP(p);
        spec.setBp(bp);
        spec.setQ(q);
        spec.setBq(bq);
        SarimaModel arima = SarimaModel.builder(spec).setDefault(-.2, -.9).build();
        StateComponent cmp2 = SsfArma2.stateComponent(arima);
        Ssf ssf1 = SsfArima.ssf(arima);
        Ssf ssf2 = Ssf.of(cmp2, SsfArma2.defaultLoading());
        DataBlock x = DataBlock.make(N);
        Random rnd = new Random();
        x.set(() -> rnd.nextDouble(-1, 1));
        DiffuseLikelihood ll1 = null, ll2 = null;
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            ll1 = DkToolkit.likelihood(ssf1, new SsfData(x), true, true);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            ll2 = DkToolkit.likelihood(ssf2, new SsfData(x), true, true);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        System.out.println(ll1.logLikelihood());
        System.out.println(ll2.logLikelihood());
    }
}
