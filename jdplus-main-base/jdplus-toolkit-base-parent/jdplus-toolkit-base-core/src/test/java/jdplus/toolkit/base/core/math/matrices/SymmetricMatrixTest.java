/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.math.matrices;

import ec.tstoolkit.random.JdkRNG;
import jdplus.toolkit.base.core.math.matrices.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author palatej
 */
public class SymmetricMatrixTest {

    public SymmetricMatrixTest() {
    }

    @Test
    public void testRandomize() {
        FastMatrix S = FastMatrix.square(10);
        SymmetricMatrix.randomize(S, null);
        assertTrue(S.isSymmetric());
    }

    @Test
    public void testCholesky() {
        FastMatrix X = FastMatrix.make(30, 15);
        JdkRNG rng = JdkRNG.newRandom(0);
        X.set((i, j) -> rng.nextDouble());
        FastMatrix S = SymmetricMatrix.XtX(X);
        FastMatrix T = S.deepClone();
        SymmetricMatrix.lcholesky(T);
        FastMatrix del = SymmetricMatrix.LLt(T).minus(S);
        assertTrue(MatrixNorms.absNorm(del) < 1e-9);
    }

    @Test
    public void testCholeskySingular() {
        FastMatrix X = FastMatrix.make(30, 15);
        JdkRNG rng = JdkRNG.newRandom(0);
        X.set((i, j) -> (i % 3 == 0) ? 0 : rng.nextDouble());
        FastMatrix S = SymmetricMatrix.XtX(X);
        FastMatrix T = S.deepClone();
        SymmetricMatrix.lcholesky(T);
        FastMatrix del = SymmetricMatrix.LLt(T).minus(S);
        assertTrue(MatrixNorms.absNorm(del) < 1e-9);
    }

    @Test
    public void testInverse() {
        FastMatrix X = FastMatrix.make(10, 5);
        JdkRNG rng = JdkRNG.newRandom(0);
        X.set((i, j) -> rng.nextDouble());
        FastMatrix S = SymmetricMatrix.XtX(X);
        FastMatrix T = S.deepClone();
        SymmetricMatrix.lcholesky(T);
        FastMatrix I = SymmetricMatrix.LtL(LowerTriangularMatrix.inverse(T));
        FastMatrix P = GeneralMatrix.AB(I, S);
        assertTrue(P.isDiagonal(1e-9) && P.diagonal().allMatch(x -> Math.abs(x - 1) < 1e-9));
    }

    @Test
    public void testXtX() {
        FastMatrix X = FastMatrix.make(2, 4);
        X.set((i, j) -> i + j * 10);
        FastMatrix M1 = SymmetricMatrix.XtX(X);
        FastMatrix M2 = GeneralMatrix.AtB(X, X);
        FastMatrix del = M1.minus(M2);
        assertTrue(MatrixNorms.absNorm(del) < 1e-9);
    }
    
    @Test
    public void testSX() {
        FastMatrix X = FastMatrix.make(10, 5);
        JdkRNG rng = JdkRNG.newRandom(0);
        X.set((i, j) -> rng.nextDouble());
        FastMatrix S = SymmetricMatrix.XtX(X);
        
        FastMatrix Y = FastMatrix.make(5, 3);
        Y.set((i, j) -> rng.nextDouble());
        FastMatrix Z=Y.deepClone();
        
        SymmetricMatrix.solveSX(S, Y, true);
        FastMatrix SY=GeneralMatrix.AB(S, Y);
        SY.sub(Z);
        assertTrue(MatrixNorms.frobeniusNorm(SY)<1e-9);
    }
    
    @Test
    public void testXS() {
        FastMatrix X = FastMatrix.make(10, 5);
        JdkRNG rng = JdkRNG.newRandom(0);
        X.set((i, j) -> rng.nextDouble());
        FastMatrix S = SymmetricMatrix.XtX(X);
        
        FastMatrix Y = FastMatrix.make(3, 5);
        Y.set((i, j) -> rng.nextDouble());
        FastMatrix Z=Y.deepClone();
        
        SymmetricMatrix.solveXS(S, Y, true);
        FastMatrix YS=GeneralMatrix.AB(Y, S);
        YS.sub(Z);
        assertTrue(MatrixNorms.frobeniusNorm(YS)<1e-9);
     }
    
}
