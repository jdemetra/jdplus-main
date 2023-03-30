/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.core.tramoseats;

import tck.demetra.data.Data;
import jdplus.toolkit.base.api.processing.DefaultProcessingLog;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.sa.base.api.SaDefinition;
import jdplus.sa.base.api.SaItem;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsMoniker;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import jdplus.tramoseats.base.api.tramoseats.TramoSeats;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsDictionaries;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsSpec;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author PALATEJ
 */
public class TramoSeatsFactoryTest {

    public TramoSeatsFactoryTest() {
    }

    @Test
    public void testUpdateSpec() {
        TramoSeatsKernel ts = TramoSeatsKernel.of(TramoSeatsSpec.RSAfull, null);
        ProcessingLog log = ProcessingLog.dummy();
        TramoSeatsResults rslt = ts.process(Data.TS_PROD, log);
        assertTrue(rslt.getFinals() != null);
        TramoSeatsSpec nspec = TramoSeatsFactory.getInstance().generateSpec(TramoSeatsSpec.RSAfull, rslt);
        log = ProcessingLog.dummy();
//        System.out.println(nspec);
        ts = TramoSeatsKernel.of(nspec, null);
        TramoSeatsResults rslt2 = ts.process(Data.TS_PROD, log);
        assertTrue(rslt2.getFinals() != null);
        TramoSeatsSpec nspec2 = TramoSeatsFactory.getInstance().generateSpec(nspec, rslt2);
//        System.out.println(nspec2);
        assertEquals(rslt.getPreprocessing().getEstimation().getStatistics().getLogLikelihood(),
                rslt2.getPreprocessing().getEstimation().getStatistics().getLogLikelihood(), 1e-4);
    }
    
    @Test
    public void testProcessor(){
        ProcessingLog log=new DefaultProcessingLog();
        TramoSeatsResults rslts = (TramoSeatsResults) TramoSeatsFactory.getInstance().processor(TramoSeatsSpec.RSAfull).process(Data.TS_PROD, null, log);
    }
    
    @Test
    public void testSaItem(){
        Ts ts=Ts.builder()
                .moniker(TsMoniker.of())
                .name("prod")
                .data(Data.TS_PROD)
                .build();
        
        SaDefinition sadef=SaDefinition.builder()
                .domainSpec(TramoSeatsSpec.RSAfull)
                .ts(ts)
                .build();
        
        SaItem item=SaItem.builder()
                .name("prod")
                .definition(sadef)
                .build();
        item.process(null, false);
//        assertTrue(item.getEstimation().getQuality() == ProcQuality.Good);
    }

    public static void main(String[] args){
        testDictionaries();
    }

    static void testDictionaries() {
        Dictionary dic = TramoSeatsDictionaries.TRAMOSEATSDICTIONARY;
        Map<String, Class> xdic = TramoSeats.outputDictionary(true);
        dic.entries().forEach(entry -> {
            System.out.print(entry.display());
            System.out.print('\t');
            if (xdic.containsKey(entry.fullName())) {
                System.out.println(1);
            } else {
                System.out.println(0);
            }

        }
        );

    }
}
