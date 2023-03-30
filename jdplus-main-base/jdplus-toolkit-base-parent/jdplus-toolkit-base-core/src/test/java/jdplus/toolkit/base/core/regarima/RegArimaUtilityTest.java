/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.regarima;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.linearfilters.BackFilter;
import jdplus.toolkit.base.core.regarima.RegArimaUtility;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author palatej
 */
public class RegArimaUtilityTest {
    
    public RegArimaUtilityTest() {
    }

    @Test
    public void testMeanRegression() {
        BackFilter d = RegArimaUtility.differencingFilter(12, 2, 1);
        double[] var = RegArimaUtility.meanRegressionVariable(d, 100);
        DataBlock out=DataBlock.make(100-d.getDegree());
        d.apply(DataBlock.of(var), out);
        assertTrue(out.isConstant(1));
    }
    
}
