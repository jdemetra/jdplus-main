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

package jdplus.toolkit.base.api.information.formatters;

import java.util.Locale;

/**
 *
 * @author Jean Palate
 */
public final class BooleanFormatter implements InformationFormatter {

    private final String strue, sfalse;
    
    public BooleanFormatter(){
        this.strue=Boolean.toString(true);
        this.sfalse=Boolean.toString(false);
    }

    public BooleanFormatter(String strue, String sfalse){
        this.strue=strue;
        this.sfalse=sfalse;
    }

    @Override
    public String format(Object obj, int item, Locale locale) {
        if (item > 0)
            return null;
        if (strue == null || sfalse == null)
            return obj.toString();
        boolean b = (Boolean)obj;
        return b ? strue : sfalse;
    }
}

