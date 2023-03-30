/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.core.tramo.spi;

import jdplus.tramoseats.base.api.tramo.Tramo;
import tck.demetra.data.Data;
import jdplus.toolkit.base.api.processing.ProcResults;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.tramoseats.base.api.tramo.TramoSpec;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author palatej
 */
public class TramoComputerTest {
    
    public TramoComputerTest() {
    }

    @Test
    public void testBasic() {
        List<String> items=new ArrayList<>();
        items.add("likelihoog.ll");
        ProcResults rslt = Tramo.process(Data.TS_PROD, TramoSpec.TRfull, ModellingContext.getActiveContext(), items);
        
//        System.out.println(rslt.getEstimation().getStatistics());
//        System.out.println(rslt.getDescription().getStochasticComponent());
    }
    
}
