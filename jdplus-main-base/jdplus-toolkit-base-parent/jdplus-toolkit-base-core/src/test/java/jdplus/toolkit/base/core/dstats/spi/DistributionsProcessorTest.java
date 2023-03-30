/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package jdplus.toolkit.base.core.dstats.spi;

import jdplus.toolkit.base.api.advanced.dstats.Distributions;
import jdplus.toolkit.base.api.stats.ProbabilityType;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.dstats.Chi2;
import jdplus.toolkit.base.core.random.MersenneTwister;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class DistributionsProcessorTest {

    public DistributionsProcessorTest() {
    }

    @Test
    public void testNormal() {
        Distributions.Processor.Distribution normal = Distributions.normal();
        DataBlock Z = DataBlock.make(1001);
        Z.set(i -> normal.density((i - 501) * .01));
        assertTrue(!Z.isZero(0));
    }

    @Test
    public void testT() {
        Distributions.Processor.Distribution t = Distributions.t(2);
        DataBlock Z = DataBlock.make(1001);
        Z.set(i -> t.density((i - 501) * .01));
        assertTrue(!Z.isZero(0));
    }

    public static void main(String[] arg) {
        MersenneTwister rnd = MersenneTwister.fromSystemNanoTime();
        long t0 = System.currentTimeMillis();
        Chi2 d = new Chi2(16);
        for (int i = 0; i < 10000000; ++i) {
            double random = d.random(rnd);
            double p = d.getProbability(random, ProbabilityType.Lower);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        Distributions.Processor.Distribution d2 = Distributions.chi2(16);
        for (int i = 0; i < 10000000; ++i) {
            double random = d2.random();
            double p = d2.probability(random, ProbabilityType.Lower);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}
