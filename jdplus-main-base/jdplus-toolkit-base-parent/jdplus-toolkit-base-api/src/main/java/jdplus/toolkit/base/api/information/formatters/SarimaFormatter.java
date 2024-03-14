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

import jdplus.toolkit.base.api.arima.SarimaOrders;

import java.util.Locale;

/**
 *
 * @author Jean Palate
 */
public final class SarimaFormatter implements InformationFormatter {

    @Override
    public String format(Object obj, int item, Locale locale) {

        SarimaOrders orders = (SarimaOrders) obj;
        return switch (item) {
            case 0 -> orders.toString();
            case 1 -> Integer.toString(orders.getP());
            case 2 -> Integer.toString(orders.getD());
            case 3 -> Integer.toString(orders.getQ());
            case 4 -> Integer.toString(orders.getBp());
            case 5 -> Integer.toString(orders.getBd());
            case 6 -> Integer.toString(orders.getBq());
            default -> null;
        };
    }
}
