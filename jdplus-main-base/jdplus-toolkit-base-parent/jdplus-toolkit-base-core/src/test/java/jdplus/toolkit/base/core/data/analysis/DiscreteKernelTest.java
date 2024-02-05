/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.toolkit.base.core.data.analysis;

import static jdplus.toolkit.base.core.data.analysis.DiscreteKernel.biweight;
import static jdplus.toolkit.base.core.data.analysis.DiscreteKernel.henderson;
import static jdplus.toolkit.base.core.data.analysis.DiscreteKernel.triangular;
import static jdplus.toolkit.base.core.data.analysis.DiscreteKernel.tricube;
import static jdplus.toolkit.base.core.data.analysis.DiscreteKernel.triweight;
import static jdplus.toolkit.base.core.data.analysis.DiscreteKernel.uniform;

import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import java.util.function.IntToDoubleFunction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static jdplus.toolkit.base.core.data.analysis.DiscreteKernel.epanechnikov;
import static jdplus.toolkit.base.core.data.analysis.DiscreteKernel.distance;
import static jdplus.toolkit.base.core.data.analysis.DiscreteKernel.trapezoidal;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DiscreteKernelTest {

    private final int K = 6;

    public DiscreteKernelTest() {
    }

    @Test
    public void testHenderson() {
        IntToDoubleFunction kernel = DiscreteKernel.Henderson.asFunction(K);
//        System.out.println("Henderson");
        double s = 0;
        for (int i = -K; i <= K; ++i) {
            double q = kernel.applyAsDouble(i);
            //assertTrue(q>0);
            s += q;
//           System.out.println(kernel.applyAsDouble(i));
        }
        assertEquals(1, s, 1e-9);
    }

    @Test
    public void testTricube() {
        IntToDoubleFunction kernel = DiscreteKernel.Tricube.asFunction(K);
//        System.out.println("Tricube");
        double s = 0;
        for (int i = -K; i <= K; ++i) {
            double q = kernel.applyAsDouble(i);
            assertTrue(q > 0);
            s += q;
//           System.out.println(kernel.applyAsDouble(i));
        }
        assertEquals(1, s, 1e-9);
    }

    @Test
    public void testTriweight() {
        IntToDoubleFunction kernel = DiscreteKernel.Triweight.asFunction(K);
//        System.out.println("Triweight");
        double s = 0;
        for (int i = -K; i <= K; ++i) {
            double q = kernel.applyAsDouble(i);
            assertTrue(q > 0);
            s += q;
//            System.out.println(kernel.applyAsDouble(i));
        }
        assertEquals(1, s, 1e-9);
    }

    @Test
    public void testBiweight() {
        IntToDoubleFunction kernel = DiscreteKernel.Biweight.asFunction(K);
//        System.out.println("Biweight");
        double s = 0;
        for (int i = -K; i <= K; ++i) {
            double q = kernel.applyAsDouble(i);
            assertTrue(q > 0);
            s += q;
//            System.out.println(kernel.applyAsDouble(i));
        }
        assertEquals(1, s, 1e-9);
    }

    @Test
    public void testParabolic() {
        IntToDoubleFunction kernel = DiscreteKernel.Epanechnikov.asFunction(K);
//        System.out.println("Epanechnikov");
        double s = 0;
        for (int i = -K; i <= K; ++i) {
            double q = kernel.applyAsDouble(i);
            assertTrue(q > 0);
            s += q;
//            System.out.println(kernel.applyAsDouble(i));
        }
        assertEquals(1, s, 1e-9);
    }

    @Test
    public void testTriangular() {
        IntToDoubleFunction kernel = DiscreteKernel.Triangular.asFunction(K);
//        System.out.println("Triangular");
        double s = 0;
        for (int i = -K; i <= K; ++i) {
            double q = kernel.applyAsDouble(i);
            assertTrue(q > 0);
            s += q;
//            System.out.println(kernel.applyAsDouble(i));
        }
        assertEquals(1, s, 1e-9);
    }

    @Test
    public void testTrapezoidal() {
        IntToDoubleFunction kernel = DiscreteKernel.Trapezoidal.asFunction(K);
//        System.out.println("Trapezoidal");
        double s = 0;
        for (int i = -K; i <= K; ++i) {
            double q = kernel.applyAsDouble(i);
            assertTrue(q > 0);
            s += q;
//            System.out.println(kernel.applyAsDouble(i));
        }
        assertEquals(1, s, 1e-9);
    }

    @Test
    public void testTrapezoidal2() {
        IntToDoubleFunction kernel = DiscreteKernel.trapezoidal(K+15, K);
//        System.out.println("Trapezoidal");
        double s = 0;
        for (int i = -K-15; i <= K+15; ++i) {
            double q = kernel.applyAsDouble(i);
            assertTrue(q > 0);
            s += q;
//            System.out.println(kernel.applyAsDouble(i));
        }
        assertEquals(1, s, 1e-9);
    }

//    @Test
//    public void testGaussian() {
//        IntToDoubleFunction kernel = gaussian(4 * K);
////        System.out.println("Gaussian");
//        for (int i = -K; i <= K; ++i) {
//            assertTrue(kernel.applyAsDouble(i) > 0);
////            System.out.println(kernel.applyAsDouble(i));
//        }
//    }

    @Test
    public void testDistance() {
        IntToDoubleFunction[] k = new IntToDoubleFunction[8];
        k[0] = uniform(K);
        k[1] = triangular(K);
        k[2] = trapezoidal(K);
        k[3] = epanechnikov(K);
        k[4] = biweight(K);
        k[5] = triweight(K);
        k[6] = tricube(K);
        k[7] = henderson(K);
        FastMatrix D = FastMatrix.square(k.length);

        for (int i = 0; i < k.length; ++i) {
            for (int j = 0; j < k.length; ++j) {
                D.set(i, j, distance(k[i], k[j], K));
            }
        }
//       System.out.println(D);
    }
}
