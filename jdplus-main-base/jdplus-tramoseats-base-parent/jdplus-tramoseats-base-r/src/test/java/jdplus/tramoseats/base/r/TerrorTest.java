/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.r;

import tck.demetra.data.Data;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.tramoseats.base.api.tramo.TramoSpec;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class TerrorTest {
    
    public TerrorTest() {
    }

    @Test
    public void testTerror0() {
        Matrix terror = Terror.process(Data.TS_PROD, TramoSpec.TR0, null, 12);
        assertTrue(terror != null);
 //       System.out.println(terror);
    }

    @Test
    public void testTerror() {
        Matrix terror = Terror.process(Data.TS_PROD, TramoSpec.TRfull, null, 12);
        assertTrue(terror != null);
 //       System.out.println(terror);
    }
    
}
