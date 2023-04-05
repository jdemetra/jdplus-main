/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.desktop.plugin.x13.descriptors;

import jdplus.x13.desktop.plugin.regarima.descriptors.RegArimaSpecRoot;
import jdplus.sa.base.api.benchmarking.SaBenchmarkingSpec;
import jdplus.x13.base.api.x11.X11Spec;
import jdplus.x13.base.api.x13.X13Spec;

/**
 *
 * @author Jean Palate
 */
@lombok.Getter
class X13SpecRoot  {
    
    public X13SpecRoot(X13Spec spec, boolean ro){
        regarima=new RegArimaSpecRoot(spec.getRegArima(), ro);
        x11=spec.getX11();
        benchmarking=spec.getBenchmarking();
    }
   
    @lombok.NonNull
    final RegArimaSpecRoot regarima;
    @lombok.NonNull
    X11Spec x11;
    @lombok.NonNull
    SaBenchmarkingSpec benchmarking;
    

 
    X13Spec getCore() {
        return X13Spec.builder()
                .regArima(regarima.getCore())
                .x11(x11)
                .benchmarking(benchmarking)
                .build();
    }
    
    boolean isRo(){
        return regarima.isRo();
    }
    
    void update(X11Spec nx11){
        x11=nx11;
    }
    
    void update(SaBenchmarkingSpec nbench){
        benchmarking=nbench;
    }
}
