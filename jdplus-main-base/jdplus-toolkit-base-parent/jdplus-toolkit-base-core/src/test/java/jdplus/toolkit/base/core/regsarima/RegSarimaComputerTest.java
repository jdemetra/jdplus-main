/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.regsarima;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import tck.demetra.data.Data;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regarima.RegArimaModel;

import static org.junit.jupiter.api.Assertions.*;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import org.junit.jupiter.api.Test;

/**
 *
 * @author PALATEJ
 */
public class RegSarimaComputerTest {
    
    public RegSarimaComputerTest() {
    }

    @Test
    public void testProd() {
        assertTrue(prodAirline() != null);
    }
    
    @Test
    public void testFunction(){
        SarimaOrders spec=SarimaOrders.airline(12);
        SarimaModel arima = SarimaModel.builder(spec)
                .setDefault()
                .build();
        RegArimaModel model = RegArimaModel.<SarimaModel>builder()
                .y(DoubleSeq.of(Data.ABS63))
                .arima(arima)
                .meanCorrection(true)
                .build();
        RegArimaEstimation<SarimaModel> rslt = RegSarimaComputer.builder()
                .minimizer(LevenbergMarquardtMinimizer.builder())
                .precision(1e-9)
                .build()
                .process(model, null);
        
        double ll1 = rslt.getMax().getFunction().evaluate(rslt.getMax().getParameters()).getValue();
        double ll2 = rslt.getConcentratedLikelihood().logLikelihood();
        assertEquals(ll1, ll2, 1e-9);
    }
    
    public static RegArimaModel<SarimaModel> prodAirline(){
        SarimaOrders spec=SarimaOrders.airline(12);
        SarimaModel arima = SarimaModel.builder(spec)
                .setDefault()
                .build();
        RegArimaModel model = RegArimaModel.<SarimaModel>builder()
                .y(DoubleSeq.of(Data.PROD))
                .arima(arima)
                .meanCorrection(true)
                .build();
        RegArimaEstimation<SarimaModel> rslt = RegSarimaComputer.builder()
                .minimizer(LevenbergMarquardtMinimizer.builder())
                .precision(1e-9)
                .build()
                .process(model, null);
        return rslt.getModel();
    }
    
}
