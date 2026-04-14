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

import jdplus.toolkit.base.api.information.formatters.BasicConfiguration;
import jdplus.toolkit.base.api.util.MultiLineNameUtil;
import lombok.Setter;
import nbbrd.picocsv.Csv;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Kristof Bayens
 */
public class ArraysCsvFormatter {

    private final DecimalFormat fmt;
    private final NumberFormat ifmt;

    @Setter
    private CsvLayout presentation = CsvLayout.List;

    @Setter
    private boolean fullName = false;

    public ArraysCsvFormatter() {
        ifmt = NumberFormat.getIntegerInstance(Locale.getDefault());
        ifmt.setGroupingUsed(false);
        fmt = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.getDefault());
        fmt.setMaximumFractionDigits(BasicConfiguration.getFractionDigits());
        fmt.setGroupingUsed(false);
    }

    public boolean write(List<DoubleArray> coll, List<String> names, Writer writer) throws IOException {
        if (coll.isEmpty() || names.size() != coll.size()) {
            return false;
        }
        return writeList(coll, names, writer);
    }

    private boolean writeList(List<DoubleArray> coll, List<String> names, Writer writer) throws IOException {
        Csv.Format csvFormat = Csv.Format.DEFAULT.toBuilder()
                .separator(System.lineSeparator())
                .delimiter(CsvInformationFormatter.getCsvSeparator())
                .build();

        try (Csv.Writer csv = Csv.Writer.of(csvFormat, Csv.WriterOptions.DEFAULT, writer, Csv.DEFAULT_CHAR_BUFFER_SIZE)) {
            int nseries = names.size();
            for (int j = 0; j < nseries; ++j) {
                csv.writeField(formatName(names.get(j)));
                DoubleArray cur = coll.get(j);
                if (cur != null) {
                    // header: ndim, dim
                    int[] dimensions = cur.getDimensions();
                    csv.writeField(ifmt.format(dimensions.length));
                    for (int dimension : dimensions) {
                        csv.writeField(ifmt.format(dimension));
                    }
                    double[] data = cur.getData();
                    for (double val : data) {
                        if (!Double.isNaN(val)) {
                            csv.writeField(fmt.format(val));
                        } else {
                            csv.writeField(null);
                        }
                    }
                }
                csv.writeEndOfLine();
            }
        }
        return true;
    }

    private String formatName(String txt) {
        if (txt == null) {
            return null;
        }
        if (fullName) {
            return MultiLineNameUtil.join(txt, " * ");
        } else {
            return MultiLineNameUtil.last(txt);
        }
    }
}
