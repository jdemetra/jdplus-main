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
package jdplus.x13.base.core.x11.filter;

import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 * The dummy filter will set the filtered series to O or 1 (multiplicative
 * case).
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class DummyFilter {

    public DoubleSeq filter(boolean mul, DoubleSeq in) {
        double c = mul ? 1.0 : 0.0;
        return DoubleSeq.onMapping(in.length(), i -> c);
    }

}
