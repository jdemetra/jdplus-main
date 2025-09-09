/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package jdplus.toolkit.base.r.arima;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;

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
     
}
