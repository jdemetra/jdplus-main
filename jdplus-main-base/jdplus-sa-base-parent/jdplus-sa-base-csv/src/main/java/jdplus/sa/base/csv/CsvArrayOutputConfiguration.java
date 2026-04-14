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
 * @author Jean Palate
 */
@lombok.Data
public final class CsvArrayOutputConfiguration implements Cloneable {

    public static final CsvLayout DEFAULT_PRESENTATION = CsvLayout.List;
    public static final String DEFAULT_FILE_PREFIX = "v";
    public static final List<String> DEFAULT_ARRAYS = List.of();
    public static final boolean DEFAULT_FULL_NAME = true;
    public static final Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;

    private CsvLayout presentation = DEFAULT_PRESENTATION;
    private File folder;
    private String filePrefix = DEFAULT_FILE_PREFIX;
    private List<String> arrays = DEFAULT_ARRAYS;
    private boolean fullName = DEFAULT_FULL_NAME;
    private Charset charset = DEFAULT_CHARSET;

    @Override
    public CsvArrayOutputConfiguration clone() {
        try {
            return (CsvArrayOutputConfiguration) super.clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }
}
