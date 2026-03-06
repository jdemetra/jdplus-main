/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.ssf.akf;

import java.util.Random;
import jdplus.toolkit.base.core.ssf.arima.SsfArima;
import tck.demetra.data.Data;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoublesMath;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.likelihood.DiffuseLikelihood;
import jdplus.toolkit.base.core.ssf.likelihood.MarginalLikelihood;
import jdplus.toolkit.base.core.ssf.likelihood.ProfileLikelihood;
import jdplus.toolkit.base.core.ssf.univariate.DefaultFilteringResults;
import jdplus.toolkit.base.core.ssf.univariate.OrdinaryFilter;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class QRFilterTest {

    static final SarimaModel arima1, arima2;
    static final double[] data;

    static {
        SarimaOrders spec = SarimaOrders.airline(12);
        spec.setP(1);
        arima1 = SarimaModel.builder(spec).phi(-.2).theta(1, -.6).btheta(1, -.8).build();
        arima2 = SarimaModel.builder(spec).phi(-.5).theta(1, .3).btheta(1, -.4).build();
        data = Data.PROD.clone();
        data[data.length - 1] = Double.NaN;
        data[17] = Double.NaN;
        data[8] = Double.NaN;
    }

    public QRFilterTest() {
    }

    @Test
    public void testDiffuse() {
        Ssf ssf = Ssf.of(SsfArima.stateComponent(arima1), SsfArima.defaultLoading());
        SsfData ssfData = new SsfData(data);

        QRFilter filter = new QRFilter();
        filter.process(ssf, ssfData);
        DiffuseLikelihood ll1 = filter.diffuseLikelihood(true, true);
        MarginalLikelihood mll = filter.marginalLikelihood(true, true);
        ProfileLikelihood pll = filter.profileLikelihood();
        DiffuseLikelihood ll2 = DkToolkit.likelihood(ssf, ssfData, true, true);
        DiffuseLikelihood ll3 = AkfToolkit.likelihoodComputer(true, true, true).compute(ssf, ssfData);
        assertEquals(ll1.logLikelihood(), ll2.logLikelihood(), 1e-6);
        assertEquals(ll1.logLikelihood(), ll3.logLikelihood(), 1e-6);
    }

    @Test
    public void testLF() {

        DataBlock A = DataBlock.make(100);
        DataBlock B = DataBlock.make(100);
        Random rnd = new Random(0);
        A.apply(i -> rnd.nextDouble());
        B.apply(i -> rnd.nextDouble());

        DataBlock C = A.deepClone();
        C.addAY(3.5, B);

        Ssf ssf = Ssf.of(SsfArima.stateComponent(arima1), SsfArima.defaultLoading());
        QRFilter filter = new QRFilter();
        filter.process(ssf, new SsfData(A));
        DoubleSeq AL = filter.diffuseLikelihood(false, true).e();
        filter.process(ssf, new SsfData(B));
        DoubleSeq BL = filter.diffuseLikelihood(false, true).e();
        filter.process(ssf, new SsfData(C));
        DoubleSeq CL = filter.diffuseLikelihood(false, true).e();
//        System.out.println(AL);
//        System.out.println(BL);
//        System.out.println(CL);

        DoubleSeq cl = DoublesMath.add(AL, BL.times(3.5));
//        System.out.println(cl);
        assertTrue(cl.distance(CL) < 1e-13);

        SarimaModel sarima1 = arima1.stationaryTransformation().getStationaryModel();
        ssf = Ssf.of(SsfArima.stateComponent(sarima1), SsfArima.defaultLoading());
        OrdinaryFilter of = new OrdinaryFilter();
        DefaultFilteringResults r = DefaultFilteringResults.light();
        r.prepare(ssf, 0, A.length());
        of.process(ssf, new SsfData(A), r);
        AL = r.errors();
        r = DefaultFilteringResults.light();
        r.prepare(ssf, 0, A.length());
        of.process(ssf, new SsfData(B), r);
        BL = r.errors();
        r = DefaultFilteringResults.light();
        r.prepare(ssf, 0, A.length());
        of.process(ssf, new SsfData(C), r);
        CL = r.errors();
//        System.out.println(AL);
//        System.out.println(BL);
//        System.out.println(CL);

        cl = DoublesMath.add(AL, BL.times(3.5));
//        System.out.println(cl);
        assertTrue(cl.distance(CL) < 1e-13);
    }
}
