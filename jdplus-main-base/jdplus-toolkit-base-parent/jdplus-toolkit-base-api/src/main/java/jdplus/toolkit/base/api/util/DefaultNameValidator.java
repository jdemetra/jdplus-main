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
package jdplus.toolkit.base.api.util;

import lombok.NonNull;

import java.util.Arrays;

/**
 * @author Jean Palate
 */
@lombok.ToString(of = "invalidChars")
public final class DefaultNameValidator implements INameValidator {

    private static final String EMPTY_ERROR = "The name can't be empty";
    private static final String WS_ERROR = "The name can't contain leading or trailing ws";

    private final char[] invalidChars;
    private String lastError;

    public DefaultNameValidator(String invalidChars) {
        this.invalidChars = invalidChars.toCharArray();
    }

    @Override
    public boolean accept(String name) {
        if (name == null || name.isEmpty()) {
            lastError = EMPTY_ERROR;
            return false;
        }

        if (Character.isWhitespace(name.charAt(0)) || Character.isWhitespace(name.charAt(name.length() - 1))) {
            lastError = WS_ERROR;
            return false;
        }

        for (char c : invalidChars) {
            if (name.indexOf(c) >= 0) {
                lastError = error(c);
                return false;
            }
        }

        lastError = null;
        return true;
    }

    public char[] getInvalidChars() {
        return invalidChars.clone();
    }

    @Override
    public String getLastError() {
        return lastError;
    }

    private static String error(char c) {
        return "The name can't contain '" + c + "'";
    }
}
