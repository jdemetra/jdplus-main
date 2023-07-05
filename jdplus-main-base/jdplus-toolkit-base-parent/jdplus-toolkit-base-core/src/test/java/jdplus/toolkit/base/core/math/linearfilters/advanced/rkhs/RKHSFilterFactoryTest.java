/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.math.linearfilters.advanced.rkhs;

import jdplus.toolkit.base.api.data.DoubleSeq;
import java.util.function.DoubleUnaryOperator;
import jdplus.toolkit.base.core.math.linearfilters.advanced.AsymmetricCriterion;
import jdplus.toolkit.base.core.math.linearfilters.advanced.ISymmetricFiltering;
import jdplus.toolkit.base.core.math.linearfilters.IFiniteFilter;

/**
 *
 * @author Jean Palate
 */
public class RKHSFilterFactoryTest {

    public RKHSFilterFactoryTest() {
    }

    public static void displayFilters(ISymmetricFiltering filtering, int output) {
        IFiniteFilter sf = filtering.centralFilter();
        IFiniteFilter[] af = filtering.endPointsFilters();

        switch (output) {
            case 2:
                for (int i = 0; i < af.length; ++i) {
                    displayPhase(af[i]);
                }
                System.out.println();
            case 1:
                displayGain(sf);
                for (int i = 0; i < af.length; ++i) {
                    displayGain(af[i]);
                }
                System.out.println();
            case 0:
                System.out.println(DoubleSeq.of(sf.weightsToArray()));
                for (int i = 0; i < af.length; ++i) {
                    System.out.println(DoubleSeq.of(af[i].weightsToArray()));
                }
                System.out.println();
        }
    }

    private static void displayGain(IFiniteFilter f) {
        DoubleUnaryOperator p = f.gainFunction();
        double[] pw = new double[100];
        for (int k = 0; k < 100; ++k) {
            pw[k] = p.applyAsDouble(k * Math.PI / 100);
        }
        System.out.println(DoubleSeq.of(pw));
    }

    private static void displayPhase(IFiniteFilter f) {
        DoubleUnaryOperator p = f.phaseFunction();
        double[] pw = new double[100];
        for (int k = 0; k < 100; ++k) {
            pw[k] = p.applyAsDouble(k * Math.PI / 800);
        }
        System.out.println(DoubleSeq.of(pw));
    }

    public static void main(String[] arg) {
        RKHSFilterSpec spec = new RKHSFilterSpec();
        ISymmetricFiltering filtering = RKHSFilterFactory.of(spec);
        displayFilters(filtering, 2);
        spec.setAsymmetricBandWith(AsymmetricCriterion.Timeliness);
        filtering = RKHSFilterFactory.of(spec);
        displayFilters(filtering, 2);
    }

}
