/*
 * Copyright 2026 JDemetra+.
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
package jdplus.toolkit.base.core.stats.samples;

import java.util.Random;
import java.util.function.DoubleSupplier;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.stats.DescriptiveStatistics;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Jean Palate
 */
public class MomentsTest {

    final DoubleSeq X;

    public MomentsTest() {
        int N = 100;
        DataBlock y = DataBlock.make(N);
        Random rnd = new Random(0);
        y.set((DoubleSupplier) rnd::nextDouble);
        X = y;
    }

    @Test
    public void testMean() {
        double m0 = Moments.mean(X);
        DescriptiveStatistics ds = DescriptiveStatistics.of(X);
        double m1 = ds.getAverage();
        assertEquals(m0, m1, 1e-15);
    }

    @Test
    public void testVariance() {
        double m0 = Moments.mean(X);
        double v0 = Moments.variance(X, m0, true);
        DescriptiveStatistics ds = DescriptiveStatistics.of(X);
        double v1 = ds.getVarDF(1);
        assertEquals(v0, v1, 1e-15);
        Sample s = Sample.build(X, false, Population.UNKNOWN);
        double v2 = s.variance();
        assertEquals(v0, v2, 1e-15);
    }

    @Test
    public void testVariance2() {
        double m0 = Moments.mean(X);
        double v0 = Moments.variance(X, m0, false);
        DescriptiveStatistics ds = DescriptiveStatistics.of(X);
        double v1 = ds.getVar();
        assertEquals(v0, v1, 1e-15);
        Sample s = Sample.build(X, false, Population.builder().mean(m0).build());
        double v2 = s.variance();
        assertEquals(v0, v2, 1e-15);
    }
}
