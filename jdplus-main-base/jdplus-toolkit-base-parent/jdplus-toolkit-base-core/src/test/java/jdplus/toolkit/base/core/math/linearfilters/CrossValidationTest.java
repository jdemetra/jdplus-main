/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package jdplus.toolkit.base.core.math.linearfilters;

import tck.demetra.data.Data;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.analysis.DiscreteKernel;

/**
 *
 * @author PALATEJ
 */
public class CrossValidationTest {

    public CrossValidationTest() {
    }

    public static void main(String[] args){
        testNileEpanechnikov();
        testNileTriWeights();
        testNileTriCube();
        testNileHenderson();
    }
    
    public static void testNileEpanechnikov() {
        double[] cv = CrossValidation.doCrossValidation(DoubleSeq.of(Data.NILE), 2, 13, h -> LocalPolynomialFilters.of(h, 3, DiscreteKernel.epanechnikov(h)));
        for (int i = 0; i < cv.length; ++i) {
            System.out.println(cv[i]);
        }
    }

    public static void testNileTriWeights() {
        double[] cv = CrossValidation.doCrossValidation(DoubleSeq.of(Data.NILE), 2, 13, h -> LocalPolynomialFilters.of(h, 3, DiscreteKernel.triweight(h)));
        for (int i = 0; i < cv.length; ++i) {
            System.out.println(cv[i]);
        }
    }

    public static void testNileTriCube() {
        double[] cv = CrossValidation.doCrossValidation(DoubleSeq.of(Data.NILE), 2, 13, h -> LocalPolynomialFilters.of(h, 3, DiscreteKernel.tricube(h)));
        for (int i = 0; i < cv.length; ++i) {
            System.out.println(cv[i]);
        }
    }
    
    public static void testNileHenderson() {
        double[] cv = CrossValidation.doCrossValidation(DoubleSeq.of(Data.NILE), 2, 13, h -> HendersonFilters.ofLength(2*h+1));
        for (int i = 0; i < cv.length; ++i) {
            System.out.println(cv[i]);
        }
    }
    
}
