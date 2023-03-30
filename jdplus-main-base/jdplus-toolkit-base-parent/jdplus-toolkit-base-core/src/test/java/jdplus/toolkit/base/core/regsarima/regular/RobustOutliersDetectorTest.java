package jdplus.toolkit.base.core.regsarima.regular;

import tck.demetra.data.Data;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import org.junit.Test;

/**
 *
 * @author palatej
 */
public class RobustOutliersDetectorTest {
    
    public RobustOutliersDetectorTest() {
    }

    @Test
    public void testProd() {
        double[] data = Data.PROD.clone();
        data[125]=400;
        data[12]=6;
        TsData s=TsData.ofInternal(TsPeriod.monthly(1967, 1), data);
        
        RobustOutliersDetector tool = RobustOutliersDetector.builder()
                .build();
        DoubleSeq sc=tool.process(s.getValues(), s.getAnnualFrequency(), null);
        System.out.println(sc);
    }
    
}
