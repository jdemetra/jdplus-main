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
package jdplus.toolkit.base.core.dstats;

import jdplus.toolkit.base.api.dstats.RandomNumberGenerator;
import jdplus.toolkit.base.core.random.MersenneTwister;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author palatej
 */
public class PoissonTest {
    
    public PoissonTest() {
    }

    @Test
    public void testProb() {
        Poisson p=new Poisson(10);
        double s=0;
        for (int i=0; i<100; ++i){
            s+=p.getProbability(i);
        }
        assertEquals(1, s, 1e-9);
    }
    
    @Test
    public void testRandom() {
        RandomNumberGenerator rnd=MersenneTwister.fromSystemNanoTime();
        Poisson p=new Poisson(20);
        int[] h=new int[100];
        long N=10000;
        for (long i=0; i<N; ++i){
            long random = p.random(rnd);
            if (random<h.length){
                h[(int)random]++;
            }
        }
//        double DN=N;
//        for (int i=0; i<h.length; ++i){
//            System.out.print(p.getProbability(i));
//            System.out.print('\t');
//            System.out.println(h[i]/DN);
//        }
     }
}
