/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.math.matrices.decomposition;

import java.util.Random;
import jdplus.toolkit.base.core.math.matrices.GeneralMatrix;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.UpperTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.decomposition.Gauss;
import jdplus.toolkit.base.core.math.matrices.decomposition.LUDecomposition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author palatej
 */
public class GaussTest {
    
    public GaussTest() {
    }

    @Test
    public void testRandom() {
        Random rnd = new Random(0);
        int n = 10;
        FastMatrix M = FastMatrix.square(n);
        M.set((i, j) -> rnd.nextDouble());
        LUDecomposition lu = Gauss.decompose(M);
        
        FastMatrix L = lu.lu().deepClone();
        LowerTriangularMatrix.toLower(L);
        L.diagonal().set(1);
        FastMatrix U = lu.lu().deepClone();
        UpperTriangularMatrix.toUpper(U);
        
        FastMatrix LU = GeneralMatrix.AB(L, U);
        
        for (int i=0; i<n; ++i){
            assertTrue(LU.row(i).distance(M.row(lu.pivot()[i]))<1e-9);
        }
    }
    
}
