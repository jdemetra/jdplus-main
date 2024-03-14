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
public final class StringArrayFormatter implements InformationFormatter {

    @Override
    public String format(Object obj, int item, Locale locale) {
        if (item > 0) {
            return null;
        }
        String[] input = (String[]) obj;
        return switch (input.length) {
            case 0 -> "";
            case 1 -> input[0];
            default -> concatenate(input);
        };
    }

    /**
     * Replaces line separators with white spaces
     *
     * @param txt
     * @return
     */
    public static String cleanup(String txt) {
        txt = txt.replace(System.lineSeparator(), " ");
        return txt.replace('\n', ' '); // defensive approach
    }

    private String concatenate(String[] input) {
        StringBuilder builder = new StringBuilder();
        builder.append(input[0]);
        for (int i = 1; i < input.length; ++i) {
            builder.append(SEP).append(input[i]);
        }
        return builder.toString();
    }

    private static final char SEP = '/';
}
