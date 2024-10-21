/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.toolkit.base.core.modelling.regular.tests;

import java.util.Arrays;
import jdplus.toolkit.base.api.dstats.RandomNumberGenerator;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.core.dstats.Normal;
import jdplus.toolkit.base.core.random.XorshiftRNG;
import tck.demetra.data.Data;
import org.junit.jupiter.api.Test;

/**
 *
 * @author PALATEJ
 */
public class CanovaHansenForTradingDaysTest {

    public CanovaHansenForTradingDaysTest() {
    }

    @Test
    public void testSomeMethod() {
        CanovaHansenForTradingDays ch = CanovaHansenForTradingDays
                .test(Data.TS_ABS_RETAIL.log())
                .differencingLags(1, 12)
                .truncationLag(15)
                .build();
        double all = ch.testAll();
//        for (int i = 0; i < 6; ++i) {
//            System.out.println(ch.test(i, 1));
//        }
//        System.out.println(ch.testDerived());
//        System.out.println(all);
    }

    public static void main(String[] args) {
        Normal N = new Normal();
        int M = 200000;
        int P = 12;
        RandomNumberGenerator rng = XorshiftRNG.fromSystemNanoTime();
        for (int i = P * 5; i <= P * 50; i += P * 5) {
            double[] a = new double[M];
            double[][] s = new double[7][];
            for (int j = 0; j < 7; ++j) {
                s[j] = new double[M];
            }
            for (int j = 0; j < M; ++j) {
                double[] x = new double[i];
                for (int k = 0; k < i; ++k) {
                    x[k] = N.random(rng);
                }
                CanovaHansenForTradingDays ch = CanovaHansenForTradingDays
                        .test(TsData.ofInternal(TsPeriod.monthly(2000, 1), x))
                        .truncationLag(15)
                        .build();
                a[j] = ch.testAll();
                for (int k = 0; k < 6; ++k) {
                    s[k][j] = ch.test(k);
                }
                s[6][j] = ch.testDerived();
            }
            Arrays.sort(a);
//            for (int j = 0; j < s.length; ++j) {
//                Arrays.sort(s[j]);
//                System.out.print(s[j][(int) (M * .9)]);
//                System.out.print('\t');
//                System.out.print(s[j][(int) (M * .95)]);
//                System.out.print('\t');
//                System.out.print(s[j][(int) (M * .99)]);
//                System.out.print('\t');
//                System.out.print(s[j][(int) (M * .999)]);
//                System.out.print('\t');
//            }
            System.out.print(a[(int) (M * .9)]);
            System.out.print('\t');
            System.out.print(a[(int) (M * .95)]);
            System.out.print('\t');
            System.out.print(a[(int) (M * .99)]);
            System.out.print('\t');
            System.out.println(a[(int) (M * .999)]);
        }
    }

}
