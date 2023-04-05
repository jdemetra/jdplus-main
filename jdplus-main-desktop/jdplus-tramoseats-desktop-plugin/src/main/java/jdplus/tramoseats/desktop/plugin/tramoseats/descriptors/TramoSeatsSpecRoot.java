/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.desktop.plugin.tramoseats.descriptors;

import jdplus.tramoseats.desktop.plugin.tramo.descriptors.TramoSpecRoot;
import jdplus.sa.base.api.benchmarking.SaBenchmarkingSpec;
import jdplus.tramoseats.base.api.seats.DecompositionSpec;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsSpec;

/**
 *
 * @author Jean Palate
 */
@lombok.Getter
class TramoSeatsSpecRoot  {
    
    public TramoSeatsSpecRoot(TramoSeatsSpec spec, boolean ro){
        tramo=new TramoSpecRoot(spec.getTramo(), ro);
        seats=spec.getSeats();
        benchmarking=spec.getBenchmarking();
    }
    
    @lombok.NonNull
    final TramoSpecRoot tramo;
    @lombok.NonNull
    DecompositionSpec seats;
    @lombok.NonNull
    SaBenchmarkingSpec benchmarking;
    

 
    TramoSeatsSpec getCore() {
        return TramoSeatsSpec.builder()
                .tramo(tramo.getCore())
                .seats(seats)
                .benchmarking(benchmarking)
                .build();
    }
    
    boolean isRo(){
        return tramo.isRo();
    }
    
    void update(DecompositionSpec nseats){
        seats=nseats;
    }
    
    void update(SaBenchmarkingSpec nbench){
        benchmarking=nbench;
    }
}
