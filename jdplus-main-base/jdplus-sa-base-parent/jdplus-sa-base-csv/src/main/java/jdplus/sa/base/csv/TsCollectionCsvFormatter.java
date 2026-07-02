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
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDataTable;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.util.MultiLineNameUtil;
import lombok.Setter;
import nbbrd.picocsv.Csv;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 *
 * @author Kristof Bayens
 */
public class TsCollectionCsvFormatter {

    private final DecimalFormat fmt;
    private final NumberFormat ifmt;

    @Setter
    private CsvLayout presentation = CsvLayout.VTable;

    @Setter
    private boolean fullName = false;

    public TsCollectionCsvFormatter() {
        ifmt = NumberFormat.getIntegerInstance(CsvInformationFormatter.getLocale());
        ifmt.setGroupingUsed(false);
        fmt = (DecimalFormat) DecimalFormat.getNumberInstance(CsvInformationFormatter.getLocale());
        fmt.setMaximumFractionDigits(BasicConfiguration.getFractionDigits());
        fmt.setGroupingUsed(false);
    }

    public boolean write(List<TsData> coll, List<String> names, Writer writer) throws IOException {
        if (coll.isEmpty() || names.size() != coll.size()) {
            return false;
        }
        if (presentation == CsvLayout.List) {
            return writeList(coll, names, writer);
        }

        TsDataTable table = TsDataTable.of(coll);
        TsDomain domain = table.getDomain();
        if (domain.isEmpty()) {
            return false;
        }
        int ndata = domain.getLength();
        int nseries = coll.size();

        Csv.Format csvFormat = Csv.Format.DEFAULT.toBuilder()
                .separator(System.lineSeparator())
                .delimiter(CsvInformationFormatter.getCsvSeparator())
                .build();

        try (Csv.Writer csv = Csv.Writer.of(csvFormat, Csv.WriterOptions.DEFAULT, writer, Csv.DEFAULT_CHAR_BUFFER_SIZE)) {
            TsDataTable.Cursor cursor = table.cursor(TsDataTable.DistributionType.LAST);

            if (presentation == CsvLayout.VTable) {
                csv.writeField(null);
                for (int i = 0; i < nseries; ++i) {
                    csv.writeField(formatName(names.get(i)));
                }
                csv.writeEndOfLine();

                for (int j = 0; j < ndata; ++j) {
                    csv.writeField(domain.get(j).start().toLocalDate().format(DateTimeFormatter.ISO_DATE));
                    for (int i = 0; i < nseries; ++i) {
                        cursor.moveTo(j, i);
                        if (cursor.getStatus() == TsDataTable.ValueStatus.PRESENT) {
                            csv.writeField(fmt.format(cursor.getValue()));
                        } else {
                            csv.writeField(null);
                        }
                    }
                    csv.writeEndOfLine();
                }
            } else {
                csv.writeField(null);
                for (int i = 0; i < ndata; ++i) {
                    csv.writeField(domain.get(i).start().toLocalDate().format(DateTimeFormatter.ISO_DATE));
                }
                csv.writeEndOfLine();

                for (int j = 0; j < nseries; ++j) {
                    csv.writeField(formatName(names.get(j)));
                    for (int i = 0; i < ndata; ++i) {
                        cursor.moveTo(i, j);
                        if (cursor.getStatus() == TsDataTable.ValueStatus.PRESENT) {
                            csv.writeField(fmt.format(cursor.getValue()));
                        } else {
                            csv.writeField(null);
                        }
                    }
                    csv.writeEndOfLine();
                }
            }
        }
        return true;
    }

    private boolean writeList(List<TsData> coll, List<String> names, Writer writer) throws IOException {
        Csv.Format csvFormat = Csv.Format.DEFAULT.toBuilder()
                .separator(System.lineSeparator())
                .delimiter(CsvInformationFormatter.getCsvSeparator())
                .build();

        try (Csv.Writer csv = Csv.Writer.of(csvFormat, Csv.WriterOptions.DEFAULT, writer, Csv.DEFAULT_CHAR_BUFFER_SIZE)) {
            int nseries = names.size();
            for (int j = 0; j < nseries; ++j) {
                csv.writeField(formatName(names.get(j)));
                TsData cur = coll.get(j);
                if (cur != null) {
                    // header: freq, start, pos, length
                    TsPeriod start = cur.getStart();
                    csv.writeField(ifmt.format(start.annualFrequency()));
                    csv.writeField(ifmt.format(start.year()));
                    csv.writeField(ifmt.format(start.annualPosition() + 1));
                    csv.writeField(ifmt.format(cur.length()));
                    for (int i = 0; i < cur.length(); ++i) {
                        double val = cur.getValue(i);
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
