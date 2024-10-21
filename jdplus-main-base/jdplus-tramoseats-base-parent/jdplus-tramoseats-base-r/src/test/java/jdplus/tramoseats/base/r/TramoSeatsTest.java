/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.r;

import jdplus.sa.base.api.ComponentType;
import tck.demetra.data.Data;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.arima.SarimaSpec;
import jdplus.toolkit.base.api.modelling.ComponentInformation;
import jdplus.tramoseats.base.api.seats.DecompositionSpec;
import jdplus.tramoseats.base.api.tramo.TramoSpec;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsSpec;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsOutput;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsResults;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class TramoSeatsTest {
    
    public TramoSeatsTest() {
    }
    
    @Test
    public void testProd() {
        TramoSeatsResults rslt = TramoSeats.process(Data.TS_PROD, "rsafull");
        rslt = TramoSeats.process(rslt.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value), "rsafull");
        assertTrue(rslt != null);
        assertTrue(TramoSeats.toBuffer(rslt) != null);
    }
    
    @Test
    public void testBackcast() {
        TramoSeatsSpec nspec = TramoSeatsSpec.RSAfull
                .toBuilder()
                .seats(DecompositionSpec.builder()
                        .backcastCount(18)
                        .build())
                .build();
        TramoSeatsResults rslt = TramoSeats.process(Data.TS_PROD, nspec, null);
        assertTrue(rslt != null);
        assertTrue(TramoSeats.toBuffer(rslt) != null);
    }
    
    @Test
    public void testRefresh() {
        TramoSeatsOutput rslt = TramoSeats.fullProcess(Data.TS_PROD, "RSA0");
        
        TramoSeatsSpec fspec = TramoSeats.refreshSpec(rslt.getResultSpec(), TramoSeatsSpec.RSA5, null, "Fixed");
        
        SarimaSpec arima = fspec.getTramo().getArima();
        SarimaSpec.Builder sbuilder = arima.toBuilder();
        
        SarimaSpec narima = sbuilder.btheta(new Parameter[]{Parameter.fixed(arima.getBtheta()[0].getValue() + 0.01)})
                .build();
        TramoSpec tr = fspec.getTramo().toBuilder()
                .arima(narima)
                .build();
        
        TramoSeatsSpec nspec = fspec.toBuilder()
                .tramo(tr)
                .build();
        
        TramoSeatsOutput nrslt = TramoSeats.fullProcess(Data.TS_PROD, nspec, null);
        System.out.println(rslt.getResult().getDecomposition().getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev));
//        System.out.println(nrslt.getResult().getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
    }
    
}
