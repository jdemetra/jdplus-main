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
package jdplus.toolkit.base.core.math.splines;

import jdplus.toolkit.base.api.data.DoubleSeq;
import tck.demetra.data.WeeklyData;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.analysis.DiscreteKernel;
import jdplus.toolkit.base.core.math.linearfilters.AsymmetricFiltersFactory;
import jdplus.toolkit.base.core.math.linearfilters.FilterUtility;
import jdplus.toolkit.base.core.math.linearfilters.IFiniteFilter;
import jdplus.toolkit.base.core.math.linearfilters.LocalPolynomialFilters;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import org.junit.Test;

/**
 *
 * @author palatej
 */
public class AdaptivePeriodicSplinesTest {

    public AdaptivePeriodicSplinesTest() {
    }

    public static void main(String[] arg) {

        double[] y = WeeklyData.US_CLAIMS;

        int q = 52;
        double[] knots = new double[q];
        double P = 365.25 / 7;
        double c = P / q;
        for (int i = 0; i < q; ++i) {
            knots[i] = i * c;
        }

        int nyears = 5;
        int ny = (int) (nyears * P + 1);
        int jump = 4;
        int nq = q / jump;
        int[] fixedKnots = new int[nq];
        for (int i = 0; i < nq; ++i) {
            fixedKnots[i] = i * jump;
        }

        DoubleSeq m = DoubleSeq.onMapping(ny, i->i - P*(int)(i/P));
        SymmetricFilter sf = LocalPolynomialFilters.of(26, 1, DiscreteKernel.uniform(26));
        IFiniteFilter[] afilters = AsymmetricFiltersFactory.mmsreFilters(sf, 0, new double[]{1}, null);
        IFiniteFilter[] lfilters = afilters.clone();
        for (int i = 0; i < lfilters.length; ++i) {
            lfilters[i] = lfilters[i].mirror();
        }
//        DoubleSeq t = FilterUtility.filter(DoubleSeq.of(y), sf, lfilters, afilters);
        DoubleSeq t = FilterUtility.filter(DoubleSeq.of(y).log(), sf, lfilters, afilters);

        DataBlock Y = DataBlock.make(ny);
//        Y.set(i -> y[i] - t.get(i));
        Y.set(i -> Math.log(y[i]) - t.get(i));
//        Y.normalize();

        long l0 = System.currentTimeMillis();
        int min = 12;

        AdaptivePeriodicSpline.Specification spec = AdaptivePeriodicSpline.Specification.builder()
                .x(m)
                .y(Y)
                .period(P)
                .knots(knots)
                .splineOrder(4)
                .maxIter(10)
                //                    .fixedKnots(fixedKnots)
                .build();

        AdaptivePeriodicSpline aspline = AdaptivePeriodicSpline.of(spec);
        AdaptivePeriodicSplines.Specification dspec = AdaptivePeriodicSplines.Specification.builder()
                .minKnots(min)
                .criterion(AdaptivePeriodicSplines.Criterion.EBIC)
                .lambda1(50)
                .build();

        AdaptivePeriodicSplines kernel = new AdaptivePeriodicSplines(dspec);
        kernel.process(aspline);
        System.out.println(kernel.best());
        long l1 = System.currentTimeMillis();
        System.out.println(l1 - l0);
        int cur = 0;
        for (AdaptivePeriodicSpline.Step result : kernel.allResults()) {
            System.out.print(result.getLambda());
            System.out.print('\t');
            System.out.print(result.getAic());
            System.out.print('\t');
            System.out.print(result.getBic());
            System.out.print('\t');
            System.out.print(result.getEbic());
            System.out.print('\t');
            System.out.print(kernel.selectedKnotsCount(cur++));
            System.out.print('\t');
            System.out.println(DoubleSeq.of(result.getS()));
        }

    }

    public static void main2(String[] arg) {
        double[] y = WeeklyData.US_CLAIMS2;

        int q = 53;
        double[] knots = new double[q];
        double P = 365.25 / 7;
        double c = P / q;
        for (int i = 0; i < q; ++i) {
            knots[i] = i * c;
        }

        int nyears = 3;
        int ny = (int) (nyears * P + 1);
        int jump = 4;
        int nq = q / jump;
        int[] fixedKnots = new int[nq];
        for (int i = 0; i < nq; ++i) {
            fixedKnots[i] = i * jump;
        }

        DoubleSeq m = DoubleSeq.onMapping(ny, i -> i - P*(int)(i/P));
        SymmetricFilter sf = LocalPolynomialFilters.of(26, 1, DiscreteKernel.uniform(26));
        IFiniteFilter[] afilters = AsymmetricFiltersFactory.mmsreFilters(sf, 0, new double[]{1}, null);
        IFiniteFilter[] lfilters = afilters.clone();
        for (int i = 0; i < lfilters.length; ++i) {
            lfilters[i] = lfilters[i].mirror();
        }
//        DoubleSeq t = FilterUtility.filter(DoubleSeq.of(y), sf, lfilters, afilters);
        DoubleSeq t = FilterUtility.filter(DoubleSeq.of(y).log(), sf, lfilters, afilters);

        DataBlock Y = DataBlock.make(ny);
//        Y.set(i -> y[i] - t.get(i));
        Y.set(i -> Math.log(y[i]) - t.get(i));
        
       AdaptivePeriodicSpline.Specification spec = AdaptivePeriodicSpline.Specification.builder()
                .x(m)
                .y(Y)
                .period(P)
                .knots(knots)
                .splineOrder(4)
                .maxIter(20)
                //                    .fixedKnots(fixedKnots)
                .build();

        AdaptivePeriodicSpline aspline = AdaptivePeriodicSpline.of(spec);
        aspline.process(50);
 
        System.out.println(aspline.S());

    }

}
