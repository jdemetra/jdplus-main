/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package jdplus.sa.base.core.diagnostics;

import jdplus.toolkit.base.api.processing.DiagnosticsConfiguration;
import java.util.concurrent.atomic.AtomicReference;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(toBuilder=true, builderClassName="Builder")
@Development(status = Development.Status.Release)
public class CoherenceDiagnosticsConfiguration implements DiagnosticsConfiguration{

    private static final AtomicReference<CoherenceDiagnosticsConfiguration> DEFAULT
            =new AtomicReference<>(builder().build());
    
    public static void setDefault(CoherenceDiagnosticsConfiguration config){
        DEFAULT.set(config);
    }
    
    public static CoherenceDiagnosticsConfiguration getDefault(){
        return DEFAULT.get();
    }

    public static final boolean ACTIVE = true;
    private boolean active;

    public static final double TOL = 1e-3, ERR = .5, SEV = .1, BAD = .05, UNC = .01;
    public static final int SHORT = 7;

    private double tolerance;
//    private double errorThreshold;
    private double severeThreshold;
    private double badThreshold;
    private double uncertainThreshold;
    private int shortSeriesLimit;

    public static Builder builder() {
        return new Builder()
                .active(ACTIVE)
                .tolerance(TOL)
//                .errorThreshold(ERR)
                .severeThreshold(SEV)
                .badThreshold(BAD)
                .uncertainThreshold(UNC)
                .shortSeriesLimit(SHORT);
    }

    public void check() {
        if (/*errorThreshold < severeThreshold ||*/ severeThreshold < badThreshold || badThreshold < uncertainThreshold || uncertainThreshold < 0) {
            throw new IllegalArgumentException("Invalid settings in Annual totals diagnostics");
        }
    }
    
    @Override
    public DiagnosticsConfiguration activate(boolean active) {
        if (this.active == active) {
            return this;
        } else {
            return toBuilder().active(active).build();
        }
    }

}
