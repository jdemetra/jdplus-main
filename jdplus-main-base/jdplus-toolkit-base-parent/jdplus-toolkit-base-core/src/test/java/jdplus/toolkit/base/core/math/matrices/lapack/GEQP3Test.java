/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.math.matrices.lapack;

import java.util.Random;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.lapack.GEQP3;
import org.junit.jupiter.api.Test;

/**
 *
 * @author palatej
 */
public class GEQP3Test {
    
    public GEQP3Test() {
    }

    @Test
    public void testRandom() {
        FastMatrix A = FastMatrix.make(100, 20);
        Random rnd = new Random(0);
        A.set((i, j) -> rnd.nextDouble());
        new GEQP3().apply(A);
    }
    
}
