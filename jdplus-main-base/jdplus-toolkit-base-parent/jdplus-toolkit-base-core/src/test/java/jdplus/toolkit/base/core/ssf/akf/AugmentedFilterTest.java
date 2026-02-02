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
import jdplus.toolkit.base.core.arima.StationaryTransformation;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.arima.SsfArima;
import jdplus.toolkit.base.core.ssf.composite.CompositeSsf;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.likelihood.DiffuseLikelihood;
import jdplus.toolkit.base.core.ssf.sts.Noise;
import jdplus.toolkit.base.core.ssf.sts.PeriodicComponent;
import jdplus.toolkit.base.core.ssf.univariate.OrdinaryFilter;
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
        QPredictionErrorDecomposition frslts = new QPredictionErrorDecomposition(new QAugmentation2(), true);
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
        int N = 10000, K = 100;
        for (int q = 1; q <= 3; ++q) {
            test1(N, K, 36, q, true);
        }
         for (int q = 1; q <= 3; ++q) {
            test2(N, K, 300, q, true);
        }
   }

    private static void test1(int N, int K, int p, int q, boolean print) {
        SarimaOrders spec = SarimaOrders.airline(p);
        spec.setP(1);
        SarimaModel m = SarimaModel.builder(spec).phi(-.2).theta(1, -.6).btheta(1, -.8).build();
        DataBlock A = DataBlock.make(N);
        Random rnd = new Random(0);
        A.apply(i -> rnd.nextDouble());

        Ssf ssf = Ssf.of(SsfArima.stateComponent(m), SsfArima.defaultLoading());
        SsfData ssfData = new SsfData(A);
        QAugmentation Q;
        Q = switch (q) {
            case 1 ->
                QAugmentation.byPartialTriangularization();
            case 2 ->
                QAugmentation.byFullTriangularization();
            default ->
                QAugmentation.normalEquation();
        };
        QPredictionErrorDecomposition frslts = new QPredictionErrorDecomposition(Q, false);
        AugmentedFilter filter = new AugmentedFilter(false);
        DiffuseLikelihood dl = null;
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            frslts.clear();
            frslts.prepare(ssf, A.length());
            filter.process(ssf, ssfData, frslts);
            dl = frslts.likelihood(true);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        if (print) {
            System.out.println(dl);
        }

        QRFilter filter2 = new QRFilter();
        t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            filter2.process(ssf, ssfData);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        if (print) {
            System.out.println(filter2.diffuseLikelihood(true, true));
        }

        t0 = System.currentTimeMillis();

        AugmentedFilterInitializer initializer = new AugmentedFilterInitializer(frslts);
        OrdinaryFilter ofilter = new OrdinaryFilter(initializer);
        for (int i = 0; i < K; ++i) {
            frslts.clear();
            frslts.prepare(ssf, A.length());
            ofilter.process(ssf, ssfData, frslts);
            dl = frslts.likelihood(true);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        if (print) {
            System.out.println(dl);
        }

        StationaryTransformation<SarimaModel> st = m.stationaryTransformation();
        DataBlock Ast = DataBlock.make(N - st.getUnitRoots().getDegree());
        st.getUnitRoots().apply(A, Ast);

        DiffuseLikelihood ll = DkToolkit.likelihoodComputer(true, true, true).compute(Ssf.of(SsfArima.stateComponent(st.getStationaryModel()), SsfArima.defaultLoading()), new SsfData(Ast));
        if (print) {
            System.out.println(ll);
        }
    }

    private static void test2(int N, int K, int p, int q, boolean print) {
        int[] cmps = new int[13];
        for (int i = 0; i < 13; ++i) {
            cmps[i] = i + 1;
        }
        StateComponent cmp1 = PeriodicComponent.stateComponent(p, cmps, 1);
        CompositeSsf ssf = CompositeSsf.builder()
                .add(cmp1, PeriodicComponent.defaultLoading(13))
                .add(Noise.of(1), Noise.defaultLoading())
                .build();
        DataBlock A = DataBlock.make(N);
        Random rnd = new Random(0);
        A.apply(i -> rnd.nextDouble());

        SsfData ssfData = new SsfData(A);
        QAugmentation Q;
        Q = switch (q) {
           case 1 ->
                QAugmentation.byPartialTriangularization();
            case 2 ->
                QAugmentation.byFullTriangularization();
            default ->
                QAugmentation.normalEquation();
        };
        QPredictionErrorDecomposition frslts = new QPredictionErrorDecomposition(Q, false);
        AugmentedFilter filter = new AugmentedFilter(false);
        DiffuseLikelihood dl = null;
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            frslts.clear();
            frslts.prepare(ssf, A.length());
            filter.process(ssf, ssfData, frslts);
            dl = frslts.likelihood(true);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        if (print) {
            System.out.println(dl);
        }

        QRFilter filter2 = new QRFilter();
        t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            filter2.process(ssf, ssfData);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        if (print) {
            System.out.println(filter2.diffuseLikelihood(true, true));
        }

        t0 = System.currentTimeMillis();

        AugmentedFilterInitializer initializer = new AugmentedFilterInitializer(frslts);
        OrdinaryFilter ofilter = new OrdinaryFilter(initializer);
        for (int i = 0; i < K; ++i) {
            frslts.clear();
            frslts.prepare(ssf, A.length());
            ofilter.process(ssf, ssfData, frslts);
            dl = frslts.likelihood(true);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        if (print) {
            System.out.println(dl);
        }

    }

}
