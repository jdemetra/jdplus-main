/*
 * Copyright 2025 JDemetra+.
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
package jdplus.toolkit.base.core.modelling.regression;

import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.regression.TrendConstant;
import jdplus.toolkit.base.core.data.DataBlock;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Jean Palate
 */
public class TrendConstantTest {
    
    public TrendConstantTest(){
        
    }

    @Test
    public void test1() {
        
        TsPeriod start = TsPeriod.monthly(2000, 1);
        TrendConstant cnt=new TrendConstant(1,1, start.start());
        
        for (int i=0; i<50; ++i){
            TsDomain dom=TsDomain.of(start, i);
            DataBlock x = Regression.x(dom, cnt);
            assertTrue(x != null);
        }
    }
        
    @Test
    public void test2() {
        
        TsPeriod start = TsPeriod.monthly(2000, 1);
        TrendConstant cnt=new TrendConstant(1,1, start.start());
        
        for (int i=0; i<50; ++i){
            TsDomain dom=TsDomain.of(start.plus(-i), i);
            DataBlock x = Regression.x(dom, cnt);
            assertTrue(x != null);
        }
        
    }

    @Test
    public void test3() {
        
        TsPeriod start = TsPeriod.monthly(2000, 1);
        TrendConstant cnt=new TrendConstant(1,1, start.start());
        
        
        for (int i=0; i<50; ++i){
            TsDomain dom=TsDomain.of(start.plus(-i), 3*i);
            DataBlock x = Regression.x(dom, cnt);
            assertTrue(x != null);
        }
        
    }
}
