/*
 * Copyright 2025 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.toolkit.base.core.math.matrices;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class GeneralMatrixTest {

    public GeneralMatrixTest() {
    }

    @Test
    public void testSomeMethod() {
    }

    public static void main(String[] arg) {
        int m = 10, n = 15, k = 10;
        FastMatrix aa = FastMatrix.make(m, k);
        aa.set((int r, int c) -> (r + 1) + 100 * (c + 1));
        FastMatrix bb = FastMatrix.make(k, n);
        bb.set((int r, int c) -> 25 * (r + 1) - 25 * (c + 1));
        FastMatrix cc = FastMatrix.make(m, n);
        cc.set((int r, int c) -> (r + 1) + (c + 1));
        GeneralMatrix.addAB(aa, bb, cc);
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 1000000; ++i) {
            FastMatrix A = FastMatrix.make(m, k);
            FastMatrix B = FastMatrix.make(k, n);
            FastMatrix C = FastMatrix.make(m, n);
            A.set((int r, int c) -> (r + 1) + 100 * (c + 1));
            B.set((int r, int c) -> 25 * (r + 1) - 25 * (c + 1));
            C.set((int r, int c) -> (r + 1) + (c + 1));
            GeneralMatrix.addAB(A, B, C);
//            //        std::cout << C << std::endl;
            C = FastMatrix.make(m, n);
            C.set((int r, int c) -> (r + 1) + (c + 1));
            B = GeneralMatrix.transpose(B);
            GeneralMatrix.addABt(A, B, C);
            B = FastMatrix.make(k, n);
            B.set((int r, int c) -> 25 * (r + 1) - 25 * (c + 1));
            C = FastMatrix.make(m, n);
            C.set((int r, int c) -> (r + 1) + (c + 1));
            A = GeneralMatrix.transpose(A);
            GeneralMatrix.addAtB(A, B, C);
            //       std::cout << C << std::endl;
            A = FastMatrix.make(m, k);
            A.set((int r, int c) -> (r + 1) + 100 * (c + 1));
            B = FastMatrix.make(k, n);
            B.set((int r, int c) -> 25 * (r + 1) - 25 * (c + 1));
            C = FastMatrix.make(m, n);
            C.set((int r, int c) -> (r + 1) + (c + 1));
            B = GeneralMatrix.transpose(B);
            A = GeneralMatrix.transpose(A);
            GeneralMatrix.addAtBt(A, B, C);

        }
        java.lang.Runtime.getRuntime().gc();
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}
