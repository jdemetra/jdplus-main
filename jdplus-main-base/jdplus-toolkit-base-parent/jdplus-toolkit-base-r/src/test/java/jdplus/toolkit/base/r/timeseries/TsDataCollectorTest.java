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
package jdplus.toolkit.base.r.timeseries;

import java.time.format.DateTimeFormatter;
import tck.demetra.data.Data;
import jdplus.toolkit.base.api.timeseries.TsData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author palatej
 */
public class TsDataCollectorTest {
    
    public TsDataCollectorTest() {
    }
    
    @Test
    public void testSimple(){
        TsData s= Data.TS_PROD;
        
        String[] dates=new String[s.length()];
        for (int i=0; i<dates.length; ++i){
            dates[i]=s.getPeriod(i).start().toLocalDate().format(DateTimeFormatter.ISO_DATE);
        }
        
        TsData s2 = TsDataCollector.of(s.getValues().toArray(), dates);
        assertTrue(s.equals(s2));
    }
    
    @Test
    public void testMissing(){
        double[] vals=Data.PROD.clone();
        for (int i=15; i<200; ++i)
            vals[i]=Double.NaN;
        TsData s= TsData.ofInternal(Data.TS_PROD.getStart(), vals);
        
        String[] dates=new String[s.length()];
        for (int i=0; i<dates.length; ++i){
            dates[i]=s.getPeriod(i).start().toLocalDate().format(DateTimeFormatter.ISO_DATE);
        }
        
        TsData s2 = TsDataCollector.of(s.getValues().toArray(), dates);
        assertTrue(s.equals(s2));
    }
}
