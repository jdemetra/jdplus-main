/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.regarima.tests;

import jdplus.toolkit.base.core.regsarima.RegSarimaComputerTest;
import tck.demetra.data.Data;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.regsarima.RegSarimaComputer;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class OneStepAheadForecastingTestTest {

    public OneStepAheadForecastingTestTest() {
    }

    @Test
    public void testProd() {
        RegArimaModel<SarimaModel> model = RegSarimaComputerTest.prodAirline();
        RegSarimaComputer processor = RegSarimaComputer.builder().build();
        OneStepAheadForecastingTest os = OneStepAheadForecastingTest.of(model, processor, 18);
//        System.out.println(os.inSampleMeanTest());
//        System.out.println(os.outOfSampleMeanTest());
//        System.out.println(os.sameVarianceTest());
        ec.tstoolkit.sarima.SarimaSpecification spec = new ec.tstoolkit.sarima.SarimaSpecification(12);
        spec.airline();
        ec.tstoolkit.sarima.estimation.GlsSarimaMonitor omonitor = new ec.tstoolkit.sarima.estimation.GlsSarimaMonitor();
        ec.tstoolkit.arima.estimation.RegArimaModel omodel = new ec.tstoolkit.arima.estimation.RegArimaModel(new ec.tstoolkit.sarima.SarimaModel(spec), new ec.tstoolkit.data.DataBlock(Data.PROD));
        omodel.setMeanCorrection(true);
        ec.tstoolkit.arima.estimation.RegArimaEstimation orslt = omonitor.process(omodel);

        ec.tstoolkit.modelling.arima.diagnostics.OneStepAheadForecastingTest otest=
                new ec.tstoolkit.modelling.arima.diagnostics.OneStepAheadForecastingTest(18);
        otest.test(orslt.model);
//        System.out.println(otest.inSampleMeanTest().getValue());
//        System.out.println(otest.outOfSampleMeanTest().getValue());
        assertEquals(os.outOfSampleMeanTest().getValue(), otest.outOfSampleMeanTest().getValue(), 1e-5);
        assertEquals(os.inSampleMeanTest().getValue(), otest.inSampleMeanTest().getValue(), 1e-5);
    }

}
