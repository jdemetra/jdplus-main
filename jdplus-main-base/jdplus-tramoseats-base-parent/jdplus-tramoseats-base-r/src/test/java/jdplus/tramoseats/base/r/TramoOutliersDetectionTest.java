/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.r;

import jdplus.toolkit.base.api.timeseries.TsData;
import tck.demetra.data.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 *
 * @author PALATEJ
 */
public class TramoOutliersDetectionTest {

    public TramoOutliersDetectionTest() {
    }

    @Test
    public void testProd() {

        TramoOutliersDetection.Results rslts = TramoOutliersDetection.process(Data.TS_PROD, new int[]{0, 1, 1}, new int[]{0, 1, 1}, false, null, true, true, false, true, 4, false, false);
        String[] outliers = rslts.getData(TramoOutliersDetection.Results.BNAMES, String[].class);
//        if (outliers != null) {
//            for (int i = 0; i < outliers.length; ++i) {
//                System.out.println(outliers[i]);
//            }
//        }
        assertNull(outliers);
    }

    @Test
    public void testProdMissing() {
        double[] x=Data.PROD.clone();
        x[10]=Double.NaN;
        x[100]=Double.NaN;
        x[200]=10;
        TsData X=TsData.ofInternal(Data.TS_PROD.getStart(), x);
        TramoOutliersDetection.Results rslts = TramoOutliersDetection.process(X, new int[]{0, 1, 1}, new int[]{0, 1, 1}, false, null, true, true, false, true, 4, false, false);
        String[] outliers = rslts.getData(TramoOutliersDetection.Results.BNAMES, String[].class);
        assertEquals(1, outliers.length);
    }
}
