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
package jdplus.toolkit.base.core.stats;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author palatej
 */
public class CombinatoricsTest {

    public CombinatoricsTest() {
    }

    @Test
    public void testLFac() {
        double s0 = 0;
        for (int i = 1; i <= 200; ++i) {
            s0 += Math.log(i);
        }
        double s1 = Combinatorics.logFactorial(200);

        assertEquals(s0, s1, 1e-9);
    }

    @Test
    public void testLchoose() {
        for (int i = 1; i < 100; ++i) {
            assertEquals(Combinatorics.logChoose(i, 1), Math.log(i), 1e-9);
        }
        for (int i = 2; i < 100; ++i) {
            assertEquals(Combinatorics.logChoose(i, 2), Math.log((i * (i - 1)) / 2), 1e-9);
        }
    }
}
