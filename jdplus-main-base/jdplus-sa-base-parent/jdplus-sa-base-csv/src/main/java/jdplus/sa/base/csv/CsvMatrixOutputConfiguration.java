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
package jdplus.sa.base.csv;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 *
 */
@lombok.Data
public final class CsvMatrixOutputConfiguration implements Cloneable {

    public static final List<String> DEFAULT_ITEMS = List.of();
    public static final String DEFAULT_FILE_NAME = "demetra_m";
    public static final boolean DEFAULT_FULL_NAME = true;
    public static final boolean DEFAULT_SHORT_COLUMN_NAME = false;
    public static final Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;

    private List<String> items = DEFAULT_ITEMS;
    private File folder;
    private String fileName = DEFAULT_FILE_NAME;
    private boolean fullName = DEFAULT_FULL_NAME;
    private boolean shortColumnName = DEFAULT_SHORT_COLUMN_NAME;
    private Charset charset = DEFAULT_CHARSET;

    @Override
    public CsvMatrixOutputConfiguration clone() {
        try {
            return (CsvMatrixOutputConfiguration) super.clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }
}
