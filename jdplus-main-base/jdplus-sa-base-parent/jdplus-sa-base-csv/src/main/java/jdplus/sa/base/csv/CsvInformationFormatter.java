/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or â€“ as soon they will be approved
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

import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.information.Explorable;
import jdplus.toolkit.base.api.information.Information;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.api.information.formatters.*;
import jdplus.toolkit.base.api.math.Complex;
import jdplus.toolkit.base.api.processing.ProcDiagnostic;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.regression.RegressionItem;
import jdplus.toolkit.base.api.util.MultiLineNameUtil;
import jdplus.toolkit.base.api.util.NamedObject;
import jdplus.toolkit.base.api.util.WildCards;
import nbbrd.picocsv.Csv;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 *
 */
public final class CsvInformationFormatter {

    private static final HashMap<Type, InformationFormatter> DICTIONARY = new HashMap<>();
    private static final String NEWLINE = System.lineSeparator();
    private static volatile Character CSV_SEPARATOR;
    private static Locale LOCALE;

    static {
        LOCALE = Locale.getDefault();
        DecimalFormat fmt = (DecimalFormat) DecimalFormat.getNumberInstance(LOCALE);
        fmt.setMaximumFractionDigits(BasicConfiguration.getFractionDigits());
        fmt.setGroupingUsed(false);
        char sep = fmt.getDecimalFormatSymbols().getDecimalSeparator();
        if (sep == ',') {
            CSV_SEPARATOR = ';';
        } else {
            CSV_SEPARATOR = ',';
        }

        DICTIONARY.put(double.class, new DoubleFormatter());
        DICTIONARY.put(int.class, new IntegerFormatter());
        DICTIONARY.put(long.class, new LongFormatter());
        DICTIONARY.put(boolean.class, new BooleanFormatter("1", "0"));
        DICTIONARY.put(Double.class, new DoubleFormatter());
        DICTIONARY.put(Integer.class, new IntegerFormatter());
        DICTIONARY.put(Long.class, new LongFormatter());
        DICTIONARY.put(Boolean.class, new BooleanFormatter("1", "0"));
        DICTIONARY.put(Complex.class, new ComplexFormatter());
        DICTIONARY.put(String.class, new StringFormatter());
        DICTIONARY.put(String[].class, new StringArrayFormatter());
        DICTIONARY.put(SarimaOrders.class, new SarimaFormatter());
        DICTIONARY.put(Parameter.class, new ParameterFormatter());
        DICTIONARY.put(TsPeriod.class, new PeriodFormatter());
        DICTIONARY.put(RegressionItem.class, new RegressionItemFormatter(true));
        DICTIONARY.put(StatisticalTest.class, new StatisticalTestFormatter());
        DICTIONARY.put(ProcDiagnostic.class, new DiagnosticFormatter());
    }

    private CsvInformationFormatter() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static char getCsvSeparator() {
        return CSV_SEPARATOR;
    }

    public static void setCsvSeparator(Character c) {
        CSV_SEPARATOR = c;
    }

    public static Locale getLocale() {
        return LOCALE;
    }

    public static void setLocale(Locale locale) {
        CsvInformationFormatter.LOCALE = locale;
    }

    public static Set<Type> formattedTypes() {
        return DICTIONARY.keySet();
    }

    // preparing the matrix:
    // for each record, for each name, we search for the length of an item, the actual items (in case of
    // wildcards) and the corresponding result
    private static class MatrixItem {

        static MatrixItem ofInformationSet(String id, InformationSet record, boolean shortname) {
            MatrixItem result = new MatrixItem();
            result.fill(id, record, shortname);
            return result;
        }

        static MatrixItem ofExplorable(String id, Explorable record, boolean shortname) {
            MatrixItem result = new MatrixItem();
            result.fill(id, record, shortname);
            return result;
        }

        private static final Object[] EMPTY = new Object[0];
        private static final String[] SEMPTY = new String[0];

        int length;
        String[] items;
        Object[] results = EMPTY;

        boolean isHomogeneous() {
            if (results.length <= 1) {
                return true;
            }
            Class c = null;
            for (int i = 0; i < results.length; ++i) {
                if (results[i] != null) {
                    if (c == null) {
                        c = results[i].getClass();
                    } else if (!results[i].getClass().equals(c)) {
                        return false;
                    }
                }
            }
            return true;
        }

        void fill(final String id, InformationSet record, boolean shortname) {
            int l = id.indexOf(':');
            String sid = id;
            if (l >= 0) {
                sid = id.substring(0, l);
                String s1 = id.substring(l + 1);
                try {
                    length = Integer.parseInt(s1);
                } catch (Exception ex) {
                    length = 1;
                }
            }
            if (WildCards.hasWildCards(sid)) {
                List<Information<Object>> sel = record.select(sid);
                if (!sel.isEmpty()) {
                    int n = sel.size();
                    results = new Object[n];
                    items = new String[n];
                    for (int i = 0; i < n; ++i) {
                        Information<Object> cur = sel.get(i);
                        results[i] = record.search(cur.getName(), Object.class);
                        items[i] = shortId(cur.getName(), shortname);
                    }
                    if (length == 0 && isHomogeneous()) {
                        updateLength();
                    }
                } else {
                    results = EMPTY;
                    items = SEMPTY;
                }
            } else {
                results = new Object[]{record.search(sid, Object.class)};
                items = new String[]{shortId(sid, shortname)};
                if (length == 0 && results[0] != null) {
                    updateLength();
                }
            }
        }

        void fill(final String id, Explorable record, boolean shortname) {
            if (record == null) {
                results = EMPTY;
                items = SEMPTY;
                return;
            }
            // we search for a pre-specified length
            int l = id.indexOf(':');
            String sid = id;
            if (l >= 0) {
                sid = id.substring(0, l);
                String s1 = id.substring(l + 1);
                try {
                    length = Integer.parseInt(s1);
                } catch (Exception ex) {
                    length = 1;
                }
            }
            // request with wild cards
            if (WildCards.hasWildCards(sid)) {
                try {
                    Map<String, Object> sel = record.searchAll(sid, Object.class);
                    if (!sel.isEmpty()) {
                        List<String> ids = new ArrayList<>();
                        List<Object> objs = new ArrayList<>();
                        sel.forEach((s, o) -> {
                            if (o != null) {
                                ids.add(shortId(s, shortname));
                                objs.add(o);
                            }
                        });
                        // update unspecified length
                        results = objs.toArray(Object[]::new);
                        items = ids.toArray(String[]::new);

                        if (length == 0 && isHomogeneous()) {
                            updateLength();
                        }
                    } else {
                        results = EMPTY;
                        items = SEMPTY;
                    }
                } catch (Exception ex) {
                    results = EMPTY;
                    items = SEMPTY;

                }
            } else {
                try {
                    results = new Object[]{record.getData(sid, Object.class)};
                    items = new String[]{shortId(sid, shortname)};
                    if (length == 0 && results[0] != null) {
                        updateLength();
                    }
                } catch (Exception ex) {
                    results = EMPTY;
                    items = SEMPTY;

                }
            }
        }

        void updateLength() {
            if (results.length == 0) {
                return;
            }
            InformationFormatter fmt = DICTIONARY.get(results[0].getClass());
            if (fmt != null) {
                length = fmt.getDefaultRepresentationLength();
            } else {
                length = 1;
            }
        }

        String shortId(String id, boolean shortname) {
            if (!shortname) {
                return id;
            } else {
                int last = id.lastIndexOf(InformationSet.SEP);
                if (last < 0) {
                    return id;
                } else {
                    return id.substring(last + 1);
                }
            }
        }

        Object search(String id) {
            for (int i = 0; i < items.length; ++i) {
                if (items[i].equals(id)) {
                    return results[i];
                }
            }
            return null;
        }
    }

    public static void format(Writer writer, List<InformationSet> records, List<String> names, boolean shortname) {
        // STEP 1: we retrieve all information for all records/names
        List<List<MatrixItem>> rows = new ArrayList<>();
        records.forEach(record -> {
            rows.add(names
                    .stream()
                    .map(name -> MatrixItem.ofInformationSet(name, record, shortname))
                    .toList()
            );
        });
        format(writer, rows, names.size(), null, false);
    }

    public static void formatResults(Writer writer, List<NamedObject<Explorable>> records, List<String> items, boolean shortColName, boolean fullRowName) {
        // STEP 1: we retrieve all information for all records/names
        // each item of the list contains a row
        List<List<MatrixItem>> rows = new ArrayList<>();
        List<String> rowHeaders = new ArrayList<>();
        records.forEach(record -> {
            rows.add(items
                    .stream()
                    .map(item -> MatrixItem.ofExplorable(item, record.getObject(), shortColName))
                    .toList()
            );
            rowHeaders.add(record.getName());
        });
        format(writer, rows, items.size(), rowHeaders, fullRowName);
    }

    private static void format(Writer writer, List<List<MatrixItem>> rows, int nameCount, List<String> rowHeaders, boolean fullRowName) {
        // STEP 2: for each name, we find the set of items/length
        List<LinkedHashMap<String, Integer>> wnames = new ArrayList<>();
        for (int nameIndex = 0; nameIndex < nameCount; nameIndex++) {
            LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
            for (List<MatrixItem> row : rows) {
                MatrixItem item = row.get(nameIndex);
                if (item.items.length > 0) {
                    for (String citem : item.items) {
                        Integer length = map.get(citem);
                        if (length == null || length < item.length) {
                            map.put(citem, item.length);
                        }
                    }
                }
            }
            wnames.add(map);
        }
        // STEP 3: write the output
        Csv.Format csvFormat = Csv.Format.DEFAULT.toBuilder().separator(NEWLINE).delimiter(CSV_SEPARATOR).build();
        try (Csv.Writer csv = Csv.Writer.of(csvFormat, Csv.WriterOptions.DEFAULT, writer, Csv.DEFAULT_CHAR_BUFFER_SIZE)) {
            // columns headers
            if (rowHeaders != null) {
                csv.writeField(null);
            }
            writeColumnsHeaders(csv, wnames);
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                if (rowHeaders != null) {
                    writeRowHeader(csv, rowHeaders.get(rowIndex), fullRowName);
                }
                writeRow(csv, rows.get(rowIndex), wnames);
            }
        } catch (IOException ex) {
            String msg = ex.getMessage();
        }
    }

    private static void writeRow(Csv.Writer writer, List<MatrixItem> row, List<LinkedHashMap<String, Integer>> wnames) throws IOException {
        for (int nameIndex = 0; nameIndex < row.size(); nameIndex++) {
            writeRowCells(writer, row.get(nameIndex), wnames.get(nameIndex));
        }
        writer.writeEndOfLine();
    }

    private static void writeRowCells(Csv.Writer writer, MatrixItem item, LinkedHashMap<String, Integer> map) throws IOException {
        for (Entry<String, Integer> cellGroup : map.entrySet()) {
            int length = cellGroup.getValue();
            Object obj = item.search(cellGroup.getKey());
            if (obj != null) {
                if (length == 1) {
                    writer.writeField(format(obj, InformationFormatter.NO_INDEX));
                } else {
                    for (int j = 1; j <= length; ++j) {
                        writer.writeField(format(obj, j));
                    }
                }
            } else {
                if (length == 0) {
                    writer.writeField(null);
                } else {
                    for (int j = 0; j < length; ++j) {
                        writer.writeField(null);
                    }
                }
            }
        }
    }

    private static void writeColumnsHeaders(Csv.Writer writer, List<LinkedHashMap<String, Integer>> wnames) throws IOException {
        for (LinkedHashMap<String, Integer> map : wnames) {
            for (Entry<String, Integer> cellGroup : map.entrySet()) {
                writer.writeField(cellGroup.getKey());
                for (int j = 1; j < cellGroup.getValue(); ++j) {
                    writer.writeField(null);
                }
            }
        }
        writer.writeEndOfLine();
    }

    private static String format(Object obj, int item) {

        try {
            InformationFormatter fmt = DICTIONARY.get(obj.getClass());
            if (fmt != null) {
                return fmt.format(obj, item, LOCALE);
            } else if (item == InformationFormatter.NO_INDEX) {
                return obj.toString();
            } else {
                return "";
            }
        } catch (Exception ex) {
            String msg = ex.getMessage();
            return "";
        }
    }

    private static void writeRowHeader(Csv.Writer writer, String txt, boolean fullRowName) throws IOException {
        if (txt == null) {
            writer.writeField(null);
            return;
        }
        if (fullRowName) {
            txt = MultiLineNameUtil.join(txt, " * ");
        } else {
            txt = MultiLineNameUtil.last(txt);
        }
        txt = StringFormatter.cleanup(txt);
        writer.writeField(txt);
    }
}
