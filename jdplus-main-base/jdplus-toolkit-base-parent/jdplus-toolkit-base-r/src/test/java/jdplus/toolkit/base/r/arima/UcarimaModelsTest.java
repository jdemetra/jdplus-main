/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package jdplus.toolkit.base.r.arima;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import tck.demetra.data.Data;

/**
 *
 * @author PALATEJ
 */
public class UcarimaModelsTest {
    
    public UcarimaModelsTest() {
    }

    @Test
    public void testDecomposition(){
        SarimaOrders spec = SarimaOrders.airline(6);
        SarimaModel sarima = SarimaModel.builder(spec)
                .theta(1, -.6)
                .btheta(1, -.8)
                .build();
        UcarimaModel ucm = UcarimaModels.decompose(sarima, 0, 0);
        assertNotSame(ucm, null);
//        System.out.println(ucm);
    }
     
    @Test
    public void testEstimation(){
        SarimaOrders spec = SarimaOrders.airline(12);
        SarimaModel sarima = SarimaModel.builder(spec)
                .theta(1, -.1)
                .btheta(1, -.999999)
                .build();
        UcarimaModel ucm = UcarimaModels.decompose(sarima, 0, 0);
        assertNotSame(ucm, null);
        Matrix rslt = UcarimaModels.estimate(Data.RETAIL_BOOKSTORES, ucm, true);
        assertNotSame(rslt, null);
    }
}
