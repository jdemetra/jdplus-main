/*
 * Copyright 2026 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
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
