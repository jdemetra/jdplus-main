/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.math.matrices;

import ec.tstoolkit.random.JdkRNG;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        FastMatrix Z = Y.deepClone();

        SymmetricMatrix.solveSX(S, Y, true);
        FastMatrix SY = GeneralMatrix.AB(S, Y);
        SY.sub(Z);
        assertTrue(MatrixNorms.frobeniusNorm(SY) < 1e-9);
    }

    @Test
    public void testXS() {
        FastMatrix X = FastMatrix.make(10, 5);
        JdkRNG rng = JdkRNG.newRandom(0);
        X.set((i, j) -> rng.nextDouble());
        FastMatrix S = SymmetricMatrix.XtX(X);

        FastMatrix Y = FastMatrix.make(3, 5);
        Y.set((i, j) -> rng.nextDouble());
        FastMatrix Z = Y.deepClone();

        SymmetricMatrix.solveXS(S, Y, true);
        FastMatrix YS = GeneralMatrix.AB(Y, S);
        YS.sub(Z);
        assertTrue(MatrixNorms.frobeniusNorm(YS) < 1e-9);
    }

    @Test
    public void testaddXXt() {
        FastMatrix X = FastMatrix.make(100, 50);
        JdkRNG rng = JdkRNG.newRandom(0);
        X.set((i, j) -> rng.nextDouble());
        FastMatrix S = SymmetricMatrix.XtX(X);
        FastMatrix S2 = S.deepClone();
        DataBlock x = DataBlock.make(250);
        x.set(i -> rng.nextDouble());
        DataBlock y = x.extract(0, 50);
        SymmetricMatrix.addXaXt(S, 0.5, y);
        S2.addXaXt(0.5, y);

        FastMatrix D = S.minus(S2);
        assertTrue(MatrixNorms.frobeniusNorm(D) < 1e-12);
        y = x.extract(0, 50, 5);
        SymmetricMatrix.addXaXt(S, 0.5, y);
        S2.addXaXt(0.5, y);

        D = S.minus(S2);
        assertTrue(MatrixNorms.frobeniusNorm(D) < 1e-12);
    }

    public static void main(String[] args) {
        FastMatrix X = FastMatrix.make(100, 50);
        JdkRNG rng = JdkRNG.newRandom(0);
        X.set((i, j) -> rng.nextDouble());
        FastMatrix S = SymmetricMatrix.XtX(X);
        FastMatrix S2 = S.deepClone();
        DataBlock x = DataBlock.make(50);
        x.set(i -> rng.nextDouble());
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 1000000; ++i) {
            SymmetricMatrix.addXaXt(S, 0.5, x);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 1000000; ++i) {
            S2.addXaXt(0.5, x);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}
