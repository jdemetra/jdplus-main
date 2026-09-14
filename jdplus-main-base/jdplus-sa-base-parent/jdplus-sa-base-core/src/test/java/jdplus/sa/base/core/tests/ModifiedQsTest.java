/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package jdplus.sa.base.core.tests;

import java.util.Arrays;
import jdplus.toolkit.base.api.data.DoubleSeq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import jdplus.toolkit.base.api.dstats.RandomNumberGenerator;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.core.dstats.Normal;
import jdplus.toolkit.base.core.random.XorshiftRNG;
import jdplus.toolkit.base.core.timeseries.simplets.TsDataToolkit;
import org.junit.jupiter.api.Test;
import tck.demetra.data.Data;

/**
 *
 * @author Jean Palate
 */
public class ModifiedQsTest {

    public ModifiedQsTest() {
    }

    @Test
    public void testP1() {
        TsData s = Data.TS_PROD;
        s = TsDataToolkit.delta(s, 1);
        Qs test = new Qs(s.getValues(), 12);
        double v0 = test.build().getValue();
        double v1 = ModifiedQs.test(s.getValues(), 12);
        assertEquals(v0, v1);
    }

    public static void main(String[] args) {
        Normal N = new Normal();
        int M = 100000000;
        RandomNumberGenerator rng = XorshiftRNG.fromSystemNanoTime();

        double[] a = new double[M];
        for (int i = 60; i <=600; i+=60) {
            for (int j = 0; j < M; ++j) {
                double[] x = new double[i];
                for (int k = 0; k < x.length; ++k) {
                    x[k] = N.random(rng);
                }
                a[j] = ModifiedQs.test(DoubleSeq.of(x), 12);
            }
            Arrays.sort(a);
            System.out.print(i);
            System.out.print('\t');
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
