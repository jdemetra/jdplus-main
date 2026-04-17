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
package jdplus.toolkit.desktop.plugin.datatransfer.ts;

import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.timeseries.*;
import jdplus.toolkit.base.api.util.MultiLineNameUtil;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.desktop.plugin.*;
import jdplus.toolkit.desktop.plugin.actions.Configurable;
import jdplus.toolkit.desktop.plugin.beans.BeanConfigurator;
import jdplus.toolkit.desktop.plugin.beans.BeanEditor;
import jdplus.toolkit.desktop.plugin.beans.BeanHandler;
import jdplus.toolkit.desktop.plugin.datatransfer.DataTransferSpi;
import jdplus.toolkit.desktop.plugin.properties.NodePropertySetBuilder;
import jdplus.toolkit.desktop.plugin.properties.PropertySheetDialogBuilder;
import lombok.NonNull;
import nbbrd.design.SystemDependent;
import nbbrd.io.text.BooleanProperty;
import nbbrd.io.text.Parser;
import nbbrd.picocsv.Csv;
import org.jspecify.annotations.Nullable;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.ServiceProvider;

import java.awt.datatransfer.DataFlavor;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Jean Palate
 */
// FIXME: should move to jdplus-text-desktop-plugin
// FIXME: should use jdplus.toolkit.base.tsp.grid API
@ServiceProvider(service = DataTransferSpi.class, position = TxtDataTransfer.POSITION)
public final class TxtDataTransfer implements DataTransferSpi, Configurable, Persistable, ConfigEditor {

    static final int POSITION = 2000;

    @SystemDependent
    public static final Csv.Format CSV_FORMAT = Csv.Format.DEFAULT
            .toBuilder()
            .separator(System.lineSeparator())
            .delimiter('\t')
            .build();

    private static final int MIN_DATES = 2;

    // PROPERTIES
    private final NumberFormat numberFormat;
    private final DateTimeFormatter dateFormat;
    private final BeanConfigurator<InternalConfig, TxtDataTransfer> configurator;
    private InternalConfig config;

    @SystemDependent
    public TxtDataTransfer() {
        this.numberFormat = NumberFormat.getNumberInstance(Locale.getDefault(Locale.Category.FORMAT));
        this.dateFormat = DateTimeFormatter.ISO_DATE;
        this.configurator = new BeanConfigurator<>(new InternalConfigHandler(), new InternalConfigConverter(), new InternalConfigEditor());
        this.config = new InternalConfig();
    }

    @Override
    public int getPosition() {
        return POSITION;
    }

    //<editor-fold defaultstate="collapsed" desc="INamedService">
    @Override
    public @NonNull String getName() {
        return "TXT";
    }

    @Override
    public @NonNull String getDisplayName() {
        return "Tab-delimited values";
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="TssTransferHandler">
    @Override
    public @NonNull DataFlavor getDataFlavor() {
        return DataFlavor.stringFlavor;
    }

    @Override
    public boolean canExportTsCollection(@NonNull TsCollection col) {
        return config.exportTimeSeries && !col.isEmpty();
    }

    @Override
    public @NonNull Object exportTsCollection(@NonNull TsCollection col) throws IOException {
        TsCollection loaded = col.load(TsInformationType.Data, TsFactory.getDefault());
        return tsCollectionToString(loaded);
    }

    @Override
    public boolean canImportTsCollection(@NonNull Object obj) {
        return config.importTimeSeries && obj instanceof String;
    }

    @Override
    public @NonNull TsCollection importTsCollection(@NonNull Object obj) throws IOException {
        TsCollection col = tsCollectionFromString((String) obj);
        if (col == null) {
            throw new IOException("Cannot parse collection");
        }
        return col;
    }

    @Override
    public boolean canExportMatrix(@NonNull Matrix matrix) {
        return config.exportMatrix && !matrix.isEmpty();
    }

    @Override
    public @NonNull Object exportMatrix(@NonNull Matrix matrix) throws IOException {
        StringWriter stringWriter = new StringWriter();

        try (Csv.Writer csv = Csv.Writer.of(CSV_FORMAT, Csv.WriterOptions.DEFAULT, stringWriter, Csv.DEFAULT_CHAR_BUFFER_SIZE)) {
            for (int i = 0; i < matrix.getRowsCount(); i++) {
                for (int j = 0; j < matrix.getColumnsCount(); j++) {
                    csv.writeField(numberFormat.format(matrix.get(i, j)));
                }
                csv.writeEndOfLine();
            }
        }
        return stringWriter.toString();
    }

    @Override
    public boolean canImportMatrix(@NonNull Object obj) {
        return false;
    }

    @Override
    public @NonNull Matrix importMatrix(@NonNull Object obj) throws IOException, ClassCastException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean canExportTable(jdplus.toolkit.base.api.util.@NonNull Table<?> table) {
        return config.exportTable && !table.isEmpty();
    }

    @Override
    public @NonNull Object exportTable(jdplus.toolkit.base.api.util.@NonNull Table<?> table) throws IOException {
        StringWriter stringWriter = new StringWriter();

        try (Csv.Writer csv = Csv.Writer.of(CSV_FORMAT, Csv.WriterOptions.DEFAULT, stringWriter, Csv.DEFAULT_CHAR_BUFFER_SIZE)) {
            for (int i = 0; i < table.getRowsCount(); i++) {
                for (int j = 0; j < table.getColumnsCount(); j++) {
                    csv.writeField(valueToString(table.get(i, j)));
                }
                csv.writeEndOfLine();
            }
        }
        return stringWriter.toString();
    }

    @Override
    public boolean canImportTable(@NonNull Object obj) {
        return false;
    }

    @Override
    public jdplus.toolkit.base.api.util.@NonNull Table<?> importTable(@NonNull Object obj) throws IOException, ClassCastException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="IConfigurable">
    @Override
    public @NonNull Config getConfig() {
        return configurator.getConfig(this);
    }

    @Override
    public void setConfig(@NonNull Config config) {
        configurator.setConfig(this, config);
    }

    @Override
    public @NonNull Config editConfig(@NonNull Config config) {
        return configurator.editConfig(config);
    }

    @Override
    public void configure() {
        Configurable.configure(this, this);
    }
    //</editor-fold>

    private String valueToString(Object value) {
        return switch (value) {
            case null -> "";
            case LocalDate date -> date.format(dateFormat);
            case Number number -> numberFormat.format(number);
            default -> value.toString();
        };
    }

    public @NonNull String tsCollectionToString(@NonNull TsCollection col) throws IOException {
        if (col.isEmpty()) {
            return "";
        }

        StringWriter stringWriter = new StringWriter();
        TsCollectionAnalyser analyser = new TsCollectionAnalyser();
        analyser.set(col, config.beginPeriod);
        int nbdates = analyser.dates.length;
        int nseries = analyser.titles.length;

        try (Csv.Writer csv = Csv.Writer.of(CSV_FORMAT, Csv.WriterOptions.DEFAULT, stringWriter, Csv.DEFAULT_CHAR_BUFFER_SIZE)) {
            if (config.vertical) // une série par colonne
            {
                // écriture des titres des séries
                if (config.showTitle) {
                    if (config.showDates) {
                        csv.writeField(null);
                    }
                    for (int i = 0; i < nseries; i++) {
                        csv.writeField(MultiLineNameUtil.join(analyser.titles[i]));
                    }
                    csv.writeEndOfLine();
                }

                for (int i = 0; i < nbdates; i++) {
                    if (config.showDates) {
                        csv.writeField(dateFormat.format(analyser.dates[i]));
                    }
                    for (int j = 0; j < nseries; j++) {
                        double val = analyser.data.get(i, j);
                        if (!Double.isNaN(val)) {
                            csv.writeField(numberFormat.format(val));
                        } else {
                            csv.writeField(null);
                        }
                    }
                    csv.writeEndOfLine();
                }

            } // une série par ligne
            else {
                if (config.showDates) {
                    if (config.showTitle) {
                        csv.writeField(null);
                    }
                    for (int i = 0; i < nbdates; i++) {
                        csv.writeField(dateFormat.format(analyser.dates[i]));
                    }
                    csv.writeEndOfLine();
                }
                for (int i = 0; i < nseries; i++) {
                    if (config.showTitle) {
                        csv.writeField(MultiLineNameUtil.join(analyser.titles[i]));
                    }
                    for (int j = 0; j < nbdates; j++) {
                        double val = analyser.data.get(j, i);
                        if (!Double.isNaN(val)) {
                            csv.writeField(numberFormat.format(val));
                        } else {
                            csv.writeField(null);
                        }
                    }
                    csv.writeEndOfLine();
                }
            }
        }
        return stringWriter.toString();
    }

    public @Nullable TsCollection tsCollectionFromString(@NonNull String text) throws IOException {
        Parser<Number> valueParser = Parser.onNumberFormat(numberFormat);

        try {
            List<List<String>> rowsList = new ArrayList<>();
            int ncols = 0;

            Csv.ReaderOptions options = Csv.ReaderOptions.builder()
                    .lenientSeparator(true)  // Allow flexible line endings
                    .build();

            // Read all rows using picocsv
            try (Csv.Reader csv = Csv.Reader.of(CSV_FORMAT, options, new StringReader(text), Csv.DEFAULT_CHAR_BUFFER_SIZE)) {
                while (csv.readLine()) {
                    List<String> fields = new ArrayList<>();
                    while (csv.readField()) {
                        fields.add(csv.toString());
                    }
                    rowsList.add(fields);
                    if (ncols < fields.size()) {
                        ncols = fields.size();
                    }
                }
            }

            int nrows = rowsList.size();
            if (ncols < 1 || nrows < 1) {
                return null;
            }

            // Convert to array format for compatibility with existing logic
            List<String[]> rows = new ArrayList<>();
            for (List<String> row : rowsList) {
                rows.add(row.toArray(new String[0]));
            }

            // Search for the orientation, the titles and the dates
            // if vertical, m(1,0) is a date. Otherwise m(0,1)
            boolean datesAreVertical = nrows > 1 && rows.get(1).length > 0 && null != parseDate(rows.get(1)[0]);
            boolean hasTitles = rows.getFirst().length > 0 && null == parseDate(rows.getFirst()[0]);
            boolean datesAreHorizontal = rows.getFirst().length > 1 && null != parseDate(rows.getFirst()[1]);
            if (!datesAreVertical && !datesAreHorizontal) {
                return null;
            }
            LocalDate[] dates;
            String[] titles;
            FastMatrix data;
            int nr = (hasTitles || datesAreHorizontal) ? nrows - 1 : nrows;
            int nc = (hasTitles || datesAreVertical) ? ncols - 1 : ncols;
            if (datesAreVertical) {
                titles = new String[ncols - 1];
                if (hasTitles) {
                    for (int i = 0; i < titles.length; ++i) {
                        titles[i] = rows.getFirst()[i + 1];
                    }
                    data = FastMatrix.make(nrows - 1, ncols - 1);
                } else {
                    data = FastMatrix.make(nrows, ncols - 1);
                    for (int i = 0; i < titles.length; ++i) {
                        titles[i] = "s" + (i + 1);
                    }
                }
                dates = new LocalDate[data.getRowsCount()];
                for (int i = 0, j = hasTitles ? 1 : 0; i < dates.length; ++i, ++j) {
                    dates[i] = parseDate(rows.get(j)[0]);
                }
            } else {
                titles = new String[nrows - 1];
                if (hasTitles) {
                    for (int i = 0; i < titles.length; ++i) {
                        titles[i] = rows.get(i + 1)[0];
                    }
                    data = FastMatrix.make(ncols - 1, nrows - 1);
                } else {
                    data = FastMatrix.make(ncols, nrows - 1);
                    for (int i = 0; i < titles.length; ++i) {
                        titles[i] = "s" + (i + 1);
                    }
                }
                dates = new LocalDate[data.getRowsCount()];
                for (int i = 0, j = hasTitles ? 1 : 0; i < dates.length; ++i, ++j) {
                    dates[i] = parseDate(rows.getFirst()[j]);
                }
            }
            data.set(Double.NaN);

            for (int i = 0, j = (datesAreHorizontal || hasTitles) ? 1 : 0; i < nr; ++i, ++j) {
                String[] cols = rows.get(j);
                for (int k = 0, l = (datesAreVertical || hasTitles) ? 1 : 0; k < nc; ++k, ++l) {
                    if (l < cols.length) {
                        Number value = valueParser.parse(cols[l]);
                        if (value != null) {
                            if (datesAreVertical)
                                data.set(i, k, value.doubleValue());
                            else
                                data.set(k, i, value.doubleValue());
                        }
                    }
                }
            }
            int ndates = 0;
            for (LocalDate date : dates) {
                if (date != null) {
                    ++ndates;
                }
            }

            if (ndates < MIN_DATES) {
                return null;
            }
            TsCollectionAnalyser analyser = new TsCollectionAnalyser();

            analyser.data = data;
            analyser.dates = dates;
            analyser.titles = titles;
            List<Ts> result = analyser.create();

            List<Ts> nresult = new ArrayList<>();
            for (Ts s : result) {
                TsData d = s.getData();
                TsData nd = d.cleanExtremities();
                if (d != nd) {
                    nresult.add(s.toBuilder().data(nd).build());
                } else {
                    nresult.add(s);
                }
            }
            return TsCollection.of(nresult);
        } catch (Exception ex) {
            throw new IOException("Problem while retrieving data", ex);
        }
    }

    // TODO: use parsers
    private static LocalDate parseDate(String sd) {
        try {
            return LocalDate.parse(sd, DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException ignore) {
        }
        for (String fallbackFormat : FALLBACK_FORMATS) {
            try {
                return LocalDate.parse(sd, DateTimeFormatter.ofPattern(fallbackFormat, Locale.getDefault()));
            } catch (DateTimeParseException ignore) {
            }
        }
        return null;
    }

    // fallback formats; order matters!
    private static final String[] FALLBACK_FORMATS = {
            "yyyy-MM-dd",
            "yyyy MM dd",
            "yyyy.MM.dd",
            "yyyy-MMM-dd",
            "yyyy MMM dd",
            "yyyy.MMM.dd",
            "dd-MM-yyyy",
            "dd MM yyyy",
            "dd.MM.yyyy",
            "dd/MM/yyyy",
            "dd-MM-yy",
            "dd MM yy",
            "dd.MM.yy",
            "dd/MM/yy",
            "dd-MMM-yy",
            "dd MMM yy",
            "dd.MMM.yy",
            "dd/MMM/yy",
            "dd-MMM-yyyy",
            "dd MMM yyyy",
            "dd.MMM.yyyy",
            "dd/MMM/yyyy",
            "yyyy-MM-dd hh:mm:ss",
            "yyyy MM dd hh:mm:ss",
            "yyyy.MM.dd hh:mm:ss",
            "yyyy/MM/dd hh:mm:ss",
            "yyyy-MMM-dd hh:mm:ss",
            "yyyy MMM dd hh:mm:ss",
            "yyyy.MMM.dd hh:mm:ss",
            "yyyy/MMM/dd hh:mm:ss",
            "dd-MM-yyyy hh:mm:ss",
            "dd MM yyyy hh:mm:ss",
            "dd.MM.yyyy hh:mm:ss",
            "dd/MM/yyyy hh:mm:ss",
            "dd-MMM-yyyy hh:mm:ss",
            "dd MMM yyyy hh:mm:ss",
            "dd.MMM.yyyy hh:mm:ss",
            "dd/MMM/yyyy hh:mm:ss"};

    public static final class InternalConfig {

        /**
         * true : one series per column, false : one series per line
         */
        public boolean vertical = true;
        /**
         * show or not the dates
         */
        public boolean showDates = true;
        /**
         * show or not the titles of the series
         */
        public boolean showTitle = true;
        /**
         * true to set the dates at the beginning of the period, false for the
         * end of the period
         */
        public boolean beginPeriod = true;
        public boolean importTimeSeries = true;
        public boolean exportTimeSeries = true;
        public boolean importMatrix = true;
        public boolean exportMatrix = true;
        public boolean exportTable = true;

    }

    private static final class InternalConfigHandler implements BeanHandler<InternalConfig, TxtDataTransfer> {

        @Override
        public InternalConfig load(TxtDataTransfer resource) {
            return resource.config;
        }

        @Override
        public void store(TxtDataTransfer resource, InternalConfig bean) {
            resource.config = bean;
        }
    }

    private static final class InternalConfigEditor implements BeanEditor {

        @Override
        public boolean editBean(@NonNull Object bean) throws IntrospectionException {
            Sheet sheet = new Sheet();
            NodePropertySetBuilder b = new NodePropertySetBuilder();

            b.reset("tscollection").display("Time Series");
            b.withBoolean().selectField(bean, "importTimeSeries").display("Allow import").add();
            b.withBoolean().selectField(bean, "exportTimeSeries").display("Allow export").add();
            b.withBoolean().selectField(bean, "vertical").display("Vertical alignment").add();
            b.withBoolean().selectField(bean, "showDates").display("Include date headers").add();
            b.withBoolean().selectField(bean, "showTitle").display("Include title headers").add();
            b.withBoolean().selectField(bean, "beginPeriod").display("Begin period").add();
            sheet.put(b.build());

            b.reset("matrix").display("Matrix");
//            b.withBoolean().selectField(bean, "importMatrix").display("Import enabled").add();
            b.withBoolean().selectField(bean, "exportMatrix").display("Allow export").add();
            sheet.put(b.build());

            b.reset("table").display("Table");
            b.withBoolean().selectField(bean, "exportTable").display("Allow export").add();
            sheet.put(b.build());

            return new PropertySheetDialogBuilder()
                    .title("Configure Tab-delimited values")
                    .icon(DemetraIcons.CLIPBOARD_PASTE_DOCUMENT_TEXT_16)
                    .editSheet(sheet);
        }
    }

    private static final class InternalConfigConverter implements Converter<InternalConfig, Config> {

        private static final String DOMAIN = "ec.tss.datatransfer.TssTransferHandler", NAME = "TXT", VERSION = "";
        private static final BooleanProperty VERTICAL = BooleanProperty.of("vertical", true);
        private static final BooleanProperty SHOW_DATES = BooleanProperty.of("showDates", true);
        private static final BooleanProperty SHOW_TITLE = BooleanProperty.of("showTitle", true);
        private static final BooleanProperty BEGIN_PERIOD = BooleanProperty.of("beginPeriod", true);
        private static final BooleanProperty IMPORT_TS = BooleanProperty.of("importEnabled", true);
        private static final BooleanProperty EXPORT_TS = BooleanProperty.of("exportEnabled", true);
        private static final BooleanProperty IMPORT_MATRIX = BooleanProperty.of("importMatrix", true);
        private static final BooleanProperty EXPORT_MATRIX = BooleanProperty.of("exportMatrix", true);
        private static final BooleanProperty EXPORT_TABLE = BooleanProperty.of("exportTable", true);

        @Override
        public Config doForward(InternalConfig a) {
            Config.Builder b = Config.builder(DOMAIN, NAME, VERSION);
            VERTICAL.set(b::parameter, a.vertical);
            SHOW_DATES.set(b::parameter, a.showDates);
            SHOW_TITLE.set(b::parameter, a.showTitle);
            BEGIN_PERIOD.set(b::parameter, a.beginPeriod);
            IMPORT_TS.set(b::parameter, a.importTimeSeries);
            EXPORT_TS.set(b::parameter, a.exportTimeSeries);
            IMPORT_MATRIX.set(b::parameter, a.importMatrix);
            EXPORT_MATRIX.set(b::parameter, a.exportMatrix);
            EXPORT_TABLE.set(b::parameter, a.exportTable);
            return b.build();
        }

        @Override
        public InternalConfig doBackward(Config config) {
            if (!DOMAIN.equals(config.getDomain()))
                throw new IllegalArgumentException();
            if (!NAME.equals(config.getName()))
                throw new IllegalArgumentException();
            InternalConfig result = new InternalConfig();
            result.vertical = VERTICAL.get(config::getParameter);
            result.showDates = SHOW_DATES.get(config::getParameter);
            result.showTitle = SHOW_TITLE.get(config::getParameter);
            result.beginPeriod = BEGIN_PERIOD.get(config::getParameter);
            result.importTimeSeries = IMPORT_TS.get(config::getParameter);
            result.exportTimeSeries = EXPORT_TS.get(config::getParameter);
            result.importMatrix = IMPORT_MATRIX.get(config::getParameter);
            result.exportMatrix = EXPORT_MATRIX.get(config::getParameter);
            result.exportTable = EXPORT_TABLE.get(config::getParameter);
            return result;
        }
    }
}
