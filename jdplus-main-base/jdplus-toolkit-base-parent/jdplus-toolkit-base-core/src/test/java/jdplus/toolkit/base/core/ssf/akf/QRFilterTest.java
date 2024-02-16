/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.ssf.akf;

import jdplus.toolkit.base.core.ssf.arima.SsfArima;
import tck.demetra.data.Data;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.likelihood.DiffuseLikelihood;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class QRFilterTest {

    static final SarimaModel arima1, arima2;
    static final double[] data;

    static {
        SarimaOrders spec = SarimaOrders.airline(12);
        arima1 = SarimaModel.builder(spec).theta(1, -.6).btheta(1, -.8).build();
        arima2 = SarimaModel.builder(spec).theta(1, .3).btheta(1, -.4).build();
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
        DiffuseLikelihood ll2 = DkToolkit.likelihood(ssf, ssfData, true, true);
        DiffuseLikelihood ll3 = AkfToolkit.likelihoodComputer(true, true, true).compute(ssf, ssfData);
        assertEquals(ll1.logLikelihood(), ll2.logLikelihood(), 1e-6);
        assertEquals(ll1.logLikelihood(), ll3.logLikelihood(), 1e-6);
    }
}
