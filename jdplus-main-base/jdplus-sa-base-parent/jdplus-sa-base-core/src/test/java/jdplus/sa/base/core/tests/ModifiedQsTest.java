/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package jdplus.sa.base.core.tests;

import java.util.Arrays;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.dstats.RandomNumberGenerator;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.core.dstats.Normal;
import jdplus.toolkit.base.core.random.XorshiftRNG;
import jdplus.toolkit.base.core.timeseries.simplets.TsDataToolkit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
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
        assertTrue(v0 == v1);
    }

    public static void main(String[] args) {
        Normal N = new Normal();
        int M = 10000000;
        RandomNumberGenerator rng = XorshiftRNG.fromSystemNanoTime();
        for (int i = 40; i < 600; i += 40) {
            double[] a = new double[M];
            for (int j = 0; j < M; ++j) {
                double[] x = new double[i];
                for (int k = 0; k < i; ++k) {
                    x[k] = N.random(rng);
                }
                a[j]=ModifiedQs.test(DoubleSeq.of(x), 4);
            }
            Arrays.sort(a);
            System.out.print(a[(int)(M*.9)]);
            System.out.print('\t');
            System.out.print(a[(int)(M*.95)]);
            System.out.print('\t');
            System.out.print(a[(int)(M*.99)]);
            System.out.print('\t');
            System.out.println(a[(int)(M*.999)]);
        }
    }
}
