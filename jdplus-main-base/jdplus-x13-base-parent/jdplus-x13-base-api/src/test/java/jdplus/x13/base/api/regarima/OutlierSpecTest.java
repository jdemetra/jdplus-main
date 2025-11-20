/*
 * Copyright 2019 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.x13.base.api.regarima;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jdplus.x13.base.api.regarima.OutlierSpec;
import jdplus.x13.base.api.regarima.SingleOutlierSpec;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Mats Maggi
 */
public class OutlierSpecTest {

    @Test
    public void testDefaultCriticalValue() {
        OutlierSpec spec = OutlierSpec.builder()
                .build();
        assertEquals(0, spec.getDefaultCriticalValue());
        spec = spec.toBuilder()
                .type(new SingleOutlierSpec("AO", 2))
                .build();
        
        assertNotNull(spec.getTypes());
        assertEquals(1, spec.getTypes().size());
        assertEquals(2, spec.getTypes().get(0).getCriticalValue());
        assertEquals("AO", spec.getTypes().get(0).getType());

        spec = spec.toBuilder()
                .defaultCriticalValue(1.5)
                .build();

        assertEquals(1.5, spec.getDefaultCriticalValue());
        assertEquals(1.5, spec.getTypes().get(0).getCriticalValue());
    }
}
