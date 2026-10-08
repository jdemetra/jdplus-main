/*
 * Copyright 2022 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.toolkit.base.core.ssf.utility;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.linearsystem.LinearSystemSolver;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;

/**
 * Generic initialization of time invariant stationary components
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class StationaryInitialization {

    public FastMatrix of(ISsfDynamics dynamics, int dim) {
        if (!dynamics.isTimeInvariant()) {
            return null;
        }
        FastMatrix C = FastMatrix.square(dim);
        //            // We have to solve the steady state equation:
        //            // V = T V T' + Q

        FastMatrix T = FastMatrix.square(dim);
        FastMatrix Q = FastMatrix.square(dim);
        dynamics.T(0, T);
        dynamics.V(0, Q);
        solveLyapunov(T, Q, C);
        return C;
    }

    public void solveLyapunov(FastMatrix A, FastMatrix V, FastMatrix C) {
        int dim = A.getRowsCount();
        int np = (dim * (dim + 1)) / 2;
        FastMatrix M = FastMatrix.square(np);
        double[] b = new double[np];
        for (int c = 0, i = 0; c < dim; ++c) {
            for (int r = c; r < dim; ++r, ++i) {
                b[i] = V.get(r, c);
                M.set(i, i, 1);
                for (int k = 0; k < dim; ++k) {
                    double zc = A.get(c, k);
                    if (zc != 0) {
                        for (int l = 0; l < dim; ++l) {
                            double zr = A.get(r, l);
                            double z = zr * zc;
                            if (z != 0) {
                                int p = l <= k ? pos(k, l, dim) : pos(l, k, dim);
                                M.add(i, p, -z);
                            }
                        }
                    }
                }
            }
        }
        LinearSystemSolver.fastSolver().solve(M, DataBlock.of(b));
        for (int i = 0, j = 0; i < dim; i++) {
            C.column(i).drop(i, 0).copyFrom(b, j);
            j += dim - i;
        }
        SymmetricMatrix.fromLower(C);
    }

    public FastMatrix solveIterativeLyapunov(FastMatrix A, FastMatrix V, double tol, int maxIter) {
        int p = A.getRowsCount();
        FastMatrix G = FastMatrix.make(p, p);

        // Initialize G
        for (int i = 0; i < p; ++i) {
            for (int j = 0; j < p; ++j) {
                G.set(i, j, V.get(i, j));
            }
        }

        for (int iter = 0; iter < maxIter; ++iter) {
            FastMatrix GNew = SymmetricMatrix.XSXt(G, A);
            GNew.add(V);

            if (GNew.minus(G).isZero(tol)) {
                return GNew;
            }

            G = GNew;
        }

        return G;
    }

    private static int pos(int r, int c, int n) {
        return r + c * (2 * n - c - 1) / 2;
    }
}
