/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.ssf.implementations;

import jdplus.toolkit.base.core.ssf.basic.Coefficients;
import jdplus.toolkit.base.core.ssf.basic.Loading;
import jdplus.toolkit.base.core.ssf.composite.CompositeSsf;
import jdplus.toolkit.base.core.ssf.basic.RegSsf;
import jdplus.toolkit.base.core.ssf.arima.SsfArima;
import tck.demetra.data.Data;
import jdplus.toolkit.base.core.modelling.regression.Regression;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.likelihood.DiffuseLikelihood;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.calendars.DayClustering;
import jdplus.toolkit.base.api.timeseries.calendars.GenericTradingDays;
import jdplus.toolkit.base.api.timeseries.regression.GenericTradingDaysVariable;

import static org.junit.jupiter.api.Assertions.*;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import org.junit.jupiter.api.Test;

/**
 *
 * @author palatej
 */
public class RegSsfTest {

    public RegSsfTest() {
    }

    @Test
    public void testDirectComposite() {
        SarimaOrders spec=SarimaOrders.airline(12);
        SarimaModel airline = SarimaModel.builder(spec)
                .theta(1, -.6)
                .btheta(1, -.8)
                .build();
        StateComponent cmp1 = SsfArima.stateComponent(airline);
        TsData s = Data.TS_PROD;
        SsfData y = new SsfData(s.getValues());
        GenericTradingDays td = GenericTradingDays.contrasts(DayClustering.TD7);
        FastMatrix X = Regression.matrix(s.getDomain(), new GenericTradingDaysVariable(td));
        ISsf rssf1 = Ssf.of(RegSsf.of(cmp1, X), RegSsf.defaultLoading(cmp1.dim(), SsfArima.defaultLoading(), X));
        CompositeSsf rssf2 = CompositeSsf.builder()
                .add(cmp1, Loading.fromPosition(0))
                .add(Coefficients.fixedCoefficients(X.getColumnsCount()), Loading.regression(X))
                .build();
        DiffuseLikelihood ll1 = DkToolkit.likelihoodComputer(true, true, false).compute(rssf1, y);
        DiffuseLikelihood ll2 = DkToolkit.likelihoodComputer(true, true, false).compute(rssf2, y);
        assertEquals(ll1.logLikelihood(), ll2.logLikelihood(), 1e-5);
    }

    public static void main(String[] arg) {
        SarimaOrders spec=SarimaOrders.airline(12);
        SarimaModel airline = SarimaModel.builder(spec)
                .theta(1, -.6)
                .btheta(1, -.8)
                .build();
        StateComponent cmp1 = SsfArima.stateComponent(airline);
        TsData s = Data.TS_PROD;
        SsfData y = new SsfData(s.getValues());
        GenericTradingDays td = GenericTradingDays.contrasts(DayClustering.TD7);
        FastMatrix X = Regression.matrix(s.getDomain(), new GenericTradingDaysVariable(td));
        ISsf rssf1 = Ssf.of(RegSsf.of(cmp1, X), RegSsf.defaultLoading(cmp1.dim(), SsfArima.defaultLoading(), X));
        CompositeSsf rssf2 = CompositeSsf.builder()
                .add(cmp1, Loading.fromPosition(0))
                .add(Coefficients.fixedCoefficients(X.getColumnsCount()), Loading.regression(X))
                .build();
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            DkToolkit.likelihood(rssf1, y, true, true);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            DkToolkit.likelihood(rssf2, y, true, true);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);

    }
}
