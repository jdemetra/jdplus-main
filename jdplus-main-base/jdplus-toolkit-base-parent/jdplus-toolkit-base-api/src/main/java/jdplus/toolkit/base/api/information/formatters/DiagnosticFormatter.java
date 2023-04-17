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

import jdplus.toolkit.base.api.processing.ProcDiagnostic;

import java.text.NumberFormat;
import java.util.Locale;

/**
 *
 * @author Jean Palate
 */
public final class DiagnosticFormatter implements InformationFormatter {

    private NumberFormat newFormat(Locale locale) {
        NumberFormat df4 = NumberFormat.getNumberInstance(locale);
        df4.setMaximumFractionDigits(3);
        df4.setGroupingUsed(false);
        return df4;
    }

    @Override
    public String format(Object obj, int item, Locale locale) {

        ProcDiagnostic test = (ProcDiagnostic) obj;
        if (item == 0 || item == 1) {
            return test.getQuality().toString();
        } else if (Math.abs(item) == 2) {
            return newFormat(locale).format(test.getValue());
        } else {
            return null;
        }
    }
}
