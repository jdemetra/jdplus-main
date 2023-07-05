/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.toolkit.base.core.math.linearfilters.advanced;

import jdplus.toolkit.base.core.data.analysis.DiscreteKernel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import jdplus.toolkit.base.api.data.DoubleSeq;
import java.util.function.DoubleUnaryOperator;
import jdplus.toolkit.base.core.math.linearfilters.FiniteFilter;
import jdplus.toolkit.base.core.math.linearfilters.IFiniteFilter;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class LocalPolynomialFiltersFactoryTest {

    public LocalPolynomialFiltersFactoryTest() {
    }

    @Test
    public void testSomeMethod() {
        for (int len = 1; len < 500; ++len) {
            SymmetricFilter lpf = LocalPolynomialFiltersFactory.of(len, 3, DiscreteKernel.henderson(len));
            SymmetricFilter hf = HendersonFilters.ofLength(2 * len + 1);
            assertTrue(lpf.coefficientsAsPolynomial().equals(hf.coefficientsAsPolynomial(), 1e-9));
        }
    }

    @Test
    public void testHigh() {
        SymmetricFilter lpf = LocalPolynomialFiltersFactory.ofDefault(25, 3, DiscreteKernel.triweight(25));
        SymmetricFilter lpf2 = LocalPolynomialFiltersFactory.ofDefault(25, 3, DiscreteKernel.biweight(25));
    }

    @Test
    public void testAsymmetric() {
        int h = 11;
        for (int i = 0; i <= h; ++i) {
            FiniteFilter f = LocalPolynomialFiltersFactory.directAsymmetricFilter(h, i, 3, DiscreteKernel.henderson(h));
            assertEquals(DoubleSeq.of(f.weightsToArray()).sum(), 1, 1e-9);
//            System.out.println(DoubleSequence.ofInternal(f.weightsToArray()));
        }
//        SymmetricFilter lp = LocalPolynomialFilterFactory.ofDefault(h, 3, DiscreteKernels.henderson(h));
//        System.out.println(DoubleSequence.ofInternal(lp.weightsToArray()));
    }

    @Test
    public void testAsymmetric2() {
        int h = 11;
        for (int i = 0; i <= h; ++i) {
            FiniteFilter f = LocalPolynomialFiltersFactory.directAsymmetricFilter(h, i, 1, DiscreteKernel.tricube(h));
            assertEquals(DoubleSeq.of(f.weightsToArray()).sum(), 1, 1e-9);
//           System.out.println(DoubleSequence.ofInternal(f.weightsToArray()));
        }
//        SymmetricFilter lp = LocalPolynomialFilterFactory.ofDefault(h, 3, DiscreteKernels.biweight(h));
//        System.out.println(DoubleSequence.ofInternal(lp.weightsToArray()));
    }

    @Test
    public void testZ() {
        FastMatrix Z = LocalPolynomialFiltersFactory.createZ(12, 3);
        FastMatrix z = LocalPolynomialFiltersFactory.z(Z, -12, 2, 0, 3);
        assertTrue(z.sum() != 0);
    }

    @Test
    public void testAsymmetric3() {
        int h = 21;
        SymmetricFilter lp = LocalPolynomialFiltersFactory.ofDefault(h, 3, DiscreteKernel.henderson(h));
        for (int i = 0; i <= h; ++i) {
            IFiniteFilter f1 = AsymmetricFiltersFactory.mmsreFilter(lp, i, 0, new double[]{.4}, DiscreteKernel.triweight(h));
            IFiniteFilter f2 = AsymmetricFiltersFactory.mmsreFilter2(lp, i, 0, new double[]{.4}, DiscreteKernel.triweight(h));
            DoubleSeq c1 = DoubleSeq.of(f1.weightsToArray());
            DoubleSeq c2 = DoubleSeq.of(f2.weightsToArray());
            assertEquals(DoubleSeq.of(f1.weightsToArray()).sum(), 1, 1e-9);
            assertTrue(c1.distance(c2) < 1e-9);
        }
        for (int i = 0; i <= h; ++i) {
            IFiniteFilter f1 = AsymmetricFiltersFactory.mmsreFilter(lp, i, 0, new double[0], DiscreteKernel.triweight(h));
            IFiniteFilter f2 = AsymmetricFiltersFactory.mmsreFilter2(lp, i, 0, new double[0], DiscreteKernel.triweight(h));
            DoubleSeq c1 = DoubleSeq.of(f1.weightsToArray());
            DoubleSeq c2 = DoubleSeq.of(f2.weightsToArray());
            assertEquals(DoubleSeq.of(f1.weightsToArray()).sum(), 1, 1e-9);
            assertTrue(c1.distance(c2) < 1e-9);
        }
        for (int i = 0; i <= h; ++i) {
            IFiniteFilter f1 = AsymmetricFiltersFactory.mmsreFilter(lp, i, 1, new double[0], DiscreteKernel.triweight(h));
            IFiniteFilter f2 = AsymmetricFiltersFactory.mmsreFilter2(lp, i, 1, new double[0], DiscreteKernel.triweight(h));
            DoubleSeq c1 = DoubleSeq.of(f1.weightsToArray());
            DoubleSeq c2 = DoubleSeq.of(f2.weightsToArray());
            assertEquals(DoubleSeq.of(f1.weightsToArray()).sum(), 1, 1e-9);
            assertTrue(c1.distance(c2) < 1e-9);
        }
        for (int i = 0; i <= h; ++i) {
            IFiniteFilter f1 = AsymmetricFiltersFactory.mmsreFilter(lp, i, 2, new double[0], DiscreteKernel.triweight(h));
            IFiniteFilter f2 = AsymmetricFiltersFactory.mmsreFilter2(lp, i, 2, new double[0], DiscreteKernel.triweight(h));
            DoubleSeq c1 = DoubleSeq.of(f1.weightsToArray());
            DoubleSeq c2 = DoubleSeq.of(f2.weightsToArray());
            assertEquals(DoubleSeq.of(f1.weightsToArray()).sum(), 1, 1e-9);
            assertTrue(c1.distance(c2) < 1e-9);
        }
    }

    public static void main(String[] args) {
        int h = 6;
        displaySymmetric(h);
        System.out.println();
//        for (int i = 0; i <= h; ++i) {
//            FiniteFilter daf = LocalPolynomialFiltersFactory.directAsymmetricFilter(h, i, 2, DiscreteKernel.henderson(h));
//            System.out.println(DoubleSeq.of(daf.weightsToArray()));
//        }
//        double D = 2.0 / .20 * Math.sqrt(1 / Math.PI);
//        SymmetricFilter lp = LocalPolynomialFiltersFactory.ofDefault(h, 3, DiscreteKernel.henderson(h));
//        for (int i = 0; i <= h; ++i) {
//            IFiniteFilter f = AsymmetricFiltersFactory.mmsreFilter(lp, i, 2, new double[0], null);
//            System.out.println(DoubleSeq.of(f.weightsToArray()));
//        }
    }
    
    public static void displaySymmetric(int h){
        SymmetricFilter sf0 = LocalPolynomialFiltersFactory.of(h, 2, DiscreteKernel.biweight(h));
        SymmetricFilter sf1 = LocalPolynomialFiltersFactory.of(h, 2, DiscreteKernel.henderson(h));
        SymmetricFilter sf2 = LocalPolynomialFiltersFactory.of(h, 2, DiscreteKernel.epanechnikov(h));
        SymmetricFilter sf3 = LocalPolynomialFiltersFactory.of(h, 2, DiscreteKernel.triweight(h));
        SymmetricFilter sf4 = LocalPolynomialFiltersFactory.of(h, 2, DiscreteKernel.tricube(h));
        System.out.println(DoubleSeq.of(sf0.weightsToArray()));
        System.out.println(DoubleSeq.of(sf1.weightsToArray()));
        System.out.println(DoubleSeq.of(sf2.weightsToArray()));
        System.out.println(DoubleSeq.of(sf3.weightsToArray()));
        System.out.println(DoubleSeq.of(sf4.weightsToArray()));
        System.out.println();
        displayGain(sf0);
        displayGain(sf1);
        displayGain(sf2);
        displayGain(sf3);
        displayGain(sf4);
    }

    public static void displayGain(IFiniteFilter f) {
        DoubleUnaryOperator p = f.gainFunction();
        double[] pw = new double[100];
        for (int k = 0; k < 100; ++k) {
            pw[k] = p.applyAsDouble(k * Math.PI / 100);
        }
        System.out.println(DoubleSeq.of(pw));
    }

    public static void displayPhase(IFiniteFilter f) {
        DoubleUnaryOperator p = f.phaseFunction();
        double[] pw = new double[100];
        for (int k = 0; k < 100; ++k) {
            pw[k] = p.applyAsDouble(k * Math.PI / 800);
        }
        System.out.println(DoubleSeq.of(pw));
    }

    @Test
    public void testTrapezoidal() {
        int h = 3;
        SymmetricFilter sf = LocalPolynomialFiltersFactory.of(h, 0, DiscreteKernel.henderson(h));
        System.out.println(sf.coefficientsAsPolynomial().coefficients());
        IFiniteFilter[] af = AsymmetricFiltersFactory.mmsreFilters(sf, 0, new double[0], null);
        for (int i=0; i<af.length; ++i){
            System.out.println(DoubleSeq.of(af[i].weightsToArray()));
        }

    }

}
