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

import jdplus.toolkit.base.api.stats.StatisticalTest;

import java.text.NumberFormat;
import java.util.Locale;

/**
 *
 * @author Jean Palate
 */
public final class StatisticalTestFormatter implements InformationFormatter {

    private NumberFormat newFormat6(Locale locale) {
        NumberFormat df4 = NumberFormat.getNumberInstance(locale);
        df4.setMaximumFractionDigits(6);
        df4.setGroupingUsed(false);
        return df4;
    }

    private NumberFormat newFormat4(Locale locale) {
        NumberFormat df4 = NumberFormat.getNumberInstance(locale);
        df4.setMaximumFractionDigits(4);
        df4.setGroupingUsed(false);
        return df4;
    }

    @Override
    public String format(Object obj, int item, Locale locale) {

        StatisticalTest test = (StatisticalTest)obj;
        if (item == 0)
            return newFormat6(locale).format(test.getValue());
        return switch (Math.abs(item)) {
            case 1 -> newFormat6(locale).format(test.getValue());
            case 2 -> newFormat4(locale).format(test.getPvalue());
            case 3 -> test.getDescription();
            default -> null;
        };
    }
}
