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

package jdplus.spreadsheet.base.api.sa;

import java.io.File;
import java.util.List;

/**
 * @author Kristof Bayens
 */
@lombok.Data
public final class SpreadsheetOutputConfiguration implements Cloneable {

    public enum SpreadsheetLayout {

        BySeries,
        ByComponent,
        OneSheet
    }

    private static final String DEFAULT_FILE_NAME = "demetra.xlsx";
    private static final List<String> DEFAULT_SERIES = List.of("y", "t", "sa", "s", "i", "ycal");

    private boolean saveModel = false;
    private boolean verticalOrientation = true;
    private SpreadsheetLayout layout = SpreadsheetLayout.BySeries;
    private File folder;
    private String fileName = DEFAULT_FILE_NAME;
    private List<String> series = DEFAULT_SERIES;
    private boolean fullName = true;

    @Override
    public SpreadsheetOutputConfiguration clone() {
        try {
            return (SpreadsheetOutputConfiguration) super.clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }
}
