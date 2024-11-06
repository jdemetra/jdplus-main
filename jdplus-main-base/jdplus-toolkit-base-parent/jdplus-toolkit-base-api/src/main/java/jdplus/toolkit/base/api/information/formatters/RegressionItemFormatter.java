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

import jdplus.toolkit.base.api.timeseries.regression.RegressionItem;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * @author Jean Palate
 */
public final class RegressionItemFormatter implements InformationFormatter {

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

    private final boolean showDesc_;

    public RegressionItemFormatter() {
        showDesc_ = false;
    }

    public RegressionItemFormatter(boolean showdesc) {
        showDesc_ = showdesc;
    }

    @Override
    public int getDefaultRepresentationLength() {
        return 3;
    }

    @Override
    public String format(Object obj, int item, Locale locale) {

        RegressionItem reg = (RegressionItem) obj;
        if (item == 0) {
            return format(reg, locale);
        }
        if (reg.getDescription() == null || !showDesc_) {
            ++item;
        }
        switch (Math.abs(item)) {
            case 1 -> {
                return StringFormatter.cleanup(reg.getDescription());
            }
            case 2 -> {
                return newFormat6(locale).format(reg.getCoefficient());
            }
            case 3 -> {
                if (reg.getStdError() == 0) {
                    return null;
                } else {
                    return newFormat4(locale).format(reg.getCoefficient() / reg.getStdError());
                }
            }
            case 4 -> {
                return newFormat4(locale).format(reg.getPvalue());
            }
            default -> {
                return null;
            }
        }
//            case 5:
//                return fmt.format(reg.getStdError());
            }

    private String format(RegressionItem reg, Locale locale) {
        StringBuilder builder = new StringBuilder();
        if (reg.getDescription() != null) {
            builder.append(reg.getDescription()).append(':');
        }
        NumberFormat df4 = newFormat4(locale);
        builder.append(df4.format(reg.getCoefficient()));
        if (reg.getStdError() != 0) {
            builder.append('[').append(
                    df4.format(reg.getCoefficient() / reg.getStdError())).append(']');
        }
        return builder.toString();

    }

}
