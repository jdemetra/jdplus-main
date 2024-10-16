/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.sa.base.core.tests;

import tck.demetra.data.Data;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.core.timeseries.simplets.TsDataToolkit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class QsTest {
    
    public QsTest() {
    }

    @Test
    public void testP1() {
        TsData s=Data.TS_PROD;
        s=TsDataToolkit.delta(s, 1);
        Qs test=new Qs(s.getValues(),12);
        assertTrue(test.build().isSignificant(0.01));
//        System.out.println(test.build());
    }
    
    @Test
    public void testP12() {
        TsData s=Data.TS_PROD;
        s=TsDataToolkit.delta(s, 12);
        Qs test=new Qs(s.getValues(),12);
        assertFalse(test.build().isSignificant(0.01));
//        System.out.println(test.build());
//        System.out.println(test.useNegativeAutocorrelations().build());
    }
}
