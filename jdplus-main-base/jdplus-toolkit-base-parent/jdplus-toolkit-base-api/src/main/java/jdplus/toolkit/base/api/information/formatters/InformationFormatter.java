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

import java.text.NumberFormat;
import java.util.Locale;

/**
 *
 * @author Jean Palate
 */
public interface InformationFormatter {

   public static NumberFormat df(Locale locale, int ndec) {
        NumberFormat df = NumberFormat.getNumberInstance(locale);
        df.setMaximumFractionDigits(ndec);
        df.setGroupingUsed(false);
        return df;
    }

    public static NumberFormat df3(Locale locale) {
        return df(locale, 3);
    }

    public static NumberFormat df4(Locale locale) {
        return df(locale, 4);
    }

    public static NumberFormat df6(Locale locale) {
        return df(locale, 6);
    }

    public static String format(NumberFormat fmt, double val) {
        if (Double.isInfinite(val)) {
            if (val < 0) {
                return "-inf";
            } else {
                return "inf";
            }
        } else if (Double.isNaN(val)) {
            return "nan";
        } else {
            return fmt.format(val);
        }
    }
    
    public static String format3(Locale locale, double val) {
        return format(df3(locale), val);
    }

    public static String format4(Locale locale, double val) {
        return format(df4(locale), val);
    }

    public static String format6(Locale locale, double val) {
        return format(df6(locale), val);
    }


    String format(Object obj, int item, Locale locale);
    
    default int getDefaultRepresentationLength(){
        return 1;
    }

    int NO_INDEX = 0;
}
