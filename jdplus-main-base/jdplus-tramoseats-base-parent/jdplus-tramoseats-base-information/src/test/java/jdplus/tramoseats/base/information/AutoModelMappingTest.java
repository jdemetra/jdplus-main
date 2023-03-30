/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.information;

import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.tramoseats.base.api.tramo.AutoModelSpec;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class AutoModelMappingTest {
    
    public AutoModelMappingTest() {
    }

    @Test
    public void testDefault() {
        AutoModelSpec spec1=AutoModelSpec.DEFAULT_ENABLED;
        InformationSet info = AutoModelSpecMapping.write(spec1, true);
        AutoModelSpec spec2 = AutoModelSpecMapping.read(info);
        assertEquals(spec1, spec2);
    }
    
}
