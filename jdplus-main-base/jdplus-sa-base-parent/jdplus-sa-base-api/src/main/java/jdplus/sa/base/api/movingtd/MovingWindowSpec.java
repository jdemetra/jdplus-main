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

import jdplus.toolkit.base.api.math.linearfilters.FilterSpec;
import jdplus.toolkit.base.api.math.linearfilters.LocalPolynomialFilterSpec;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder(builderClassName="Builder", toBuilder=true)
public class MovingWindowSpec implements MovingTradingDaysSpec{
    int windowLength;
    FilterSpec filter;
    boolean reestimate;
    
    public static final MovingWindowSpec DEF_SPEC=builder().build();

    public static Builder builder(){
        return new Builder()
                .windowLength(7)
                .filter(LocalPolynomialFilterSpec.DEF_SEAS_SPEC)
                .reestimate(false);
              
    } 
}
