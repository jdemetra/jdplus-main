/*
 * Copyright 2022 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.sa.base.api.movingtd;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public class TimeVaryingSpec implements MovingTradingDaysSpec{
    
    public static final double DEF_DIFFAIC=0;
    
    boolean onContrast;
    boolean sameVariance;
    boolean reestimate;
    double diffAIC;
    
    public static final TimeVaryingSpec DEF_SPEC=builder().build();
    
    public static Builder builder(){
        return new Builder()
                .onContrast(false)
                .sameVariance(true)
                .reestimate(true)
                .diffAIC(DEF_DIFFAIC);
    }
}
