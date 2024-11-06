/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.base.r;

import jdplus.x13.base.api.x11.X11Spec;
import tck.demetra.data.Data;
import jdplus.x13.base.api.x13.X13Spec;
import jdplus.x13.base.core.x13.X13Output;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class X13Test {

    public X13Test() {
    }

    @Test
    public void testProd() {

        X13Output rslt = X13.fullProcess(Data.TS_PROD, "rsa0");
//        if (outliers != null) {
//            for (int i = 0; i < outliers.length; ++i) {
//                System.out.println(outliers[i]);
//            }
//        }
        byte[] sbytes = X13.toBuffer(rslt.getEstimationSpec());
        X13Spec spec = X13.specOf(sbytes);

        assertTrue(spec != null);
    }

    @Test
    public void testProd_nosa() {

        X11Spec x11 = X13Spec.RSA0.getX11().toBuilder()
                .seasonal(false)
                .build();

        X13Spec spec = X13Spec.RSA0.toBuilder()
                .x11(x11)
                .build();

        X13Output rslt = X13.fullProcess(Data.TS_PROD, spec, null);
//        if (outliers != null) {
//            for (int i = 0; i < outliers.length; ++i) {
//                System.out.println(outliers[i]);
//            }
//        }
        byte[] brslt = X13.toBuffer(rslt);

        assertTrue(brslt != null);
    }
}
