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


import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.helpers.ArrayBook;
import ec.util.spreadsheet.helpers.ArraySheet;
import internal.spreadsheet.base.api.SpreadsheetManager;
import jdplus.sa.base.api.SaDocument;
import jdplus.toolkit.base.api.information.Explorable;
import jdplus.toolkit.base.api.processing.Output;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDataTable;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.util.MultiLineNameUtil;
import jdplus.toolkit.base.api.util.NamedObject;
import jdplus.toolkit.base.api.util.Paths;
import nbbrd.design.VisibleForTesting;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Kristof Bayens
 */
public final class SpreadsheetOutput implements Output<SaDocument> {

    private final SpreadsheetOutputConfiguration config;
    private final SpreadsheetManager spreadsheetManager;
    private final List<Summary> summaries;
    private File folder;

    public SpreadsheetOutput(SpreadsheetOutputConfiguration config) {
        this(config, SpreadsheetManager.ofServiceLoader());
    }

    @VisibleForTesting
    SpreadsheetOutput(SpreadsheetOutputConfiguration config, SpreadsheetManager spreadsheetManager) {
        this.config = config.clone();
        this.spreadsheetManager = spreadsheetManager;
        this.summaries = new ArrayList<>();
    }

    @Override
    public void process(SaDocument document) {
        Summary summary = Summary.of(document.getName(), document.getResults(), config.getSeries());
//        if (config_.isSaveModel()) {
//            summary.setModel(document.getSpecification());
//        }
        summaries.add(summary);
    }

    @Override
    public void start(Object context) {
        summaries.clear();
        folder = Paths.folderFromContext(config.getFolder(), context);
    }

    @Override
    public void end(Object context) throws Exception {
        File ssfile = getOutputFile();

        Book.Factory factory = spreadsheetManager.getWriter(ssfile)
                .orElseThrow(() -> new IOException("Cannot find spreadsheet writer for file '" + ssfile + "'"));

        ArrayBook.Builder workbook = ArrayBook.builder();

        switch (config.getLayout()) {
            case ByComponent: {
                Map<String, List<NamedObject<TsData>>> allData = new LinkedHashMap<>();
                for (Summary summary : summaries) {
                    String name = getDisplayName(summary);
                    summary.series().forEach((k, v) -> allData.computeIfAbsent(k, ignore -> new ArrayList<>()).add(new NamedObject<>(name, v)));
                }
                for (Entry<String, List<NamedObject<TsData>>> keyValue : allData.entrySet()) {
                    String sheetName = keyValue.getKey();
                    List<String> firstHeader = new ArrayList<>();
                    List<String> secondHeader = new ArrayList<>();
                    List<TsData> table = new ArrayList<>();

                    firstHeader.add(keyValue.getKey());
                    keyValue.getValue().forEach(data -> {
                        secondHeader.add(data.getName());
                        table.add(nullToEmpty(data.getName(), data.getObject()));
                    });

                    workbook.sheet(toSheet(sheetName, firstHeader, secondHeader, table, config.isVerticalOrientation()));
                }
                break;
            }
            case BySeries: {
                for (int sheetIndex = 0; sheetIndex < summaries.size(); sheetIndex++) {
                    Summary summary = summaries.get(sheetIndex);

                    String sheetName = "Series" + sheetIndex;
                    List<String> firstHeader = new ArrayList<>();
                    List<String> secondHeader = new ArrayList<>();
                    List<TsData> table = new ArrayList<>();

                    firstHeader.add(getDisplayName(summary));
                    summary.series().forEach((k, v) -> {
                        secondHeader.add(k);
                        table.add(nullToEmpty(k, v));
                    });

                    workbook.sheet(toSheet(sheetName, firstHeader, secondHeader, table, config.isVerticalOrientation()));
                }
                break;
            }
            case OneSheet: {
                String sheetName = "Series";
                List<String> firstHeader = new ArrayList<>();
                List<String> secondHeader = new ArrayList<>();
                List<TsData> table = new ArrayList<>();

                for (Summary summary : summaries) {
                    firstHeader.add(getDisplayName(summary));
                    for (int columnIndex = 1; columnIndex < summary.series().size(); columnIndex++) {
                        firstHeader.add(null);
                    }
                    summary.series().forEach((k, v) -> {
                        secondHeader.add(k);
                        table.add(nullToEmpty(k, v));
                    });
                }

                workbook.sheet(toSheet(sheetName, firstHeader, secondHeader, table, config.isVerticalOrientation()));
                break;
            }
        }

        factory.store(ssfile, workbook.build());
    }

    @Override
    public String getName() {
        return "Spreadsheet";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private File getOutputFile() {
        return folder.toPath().resolve(config.getFileName()).toFile().getAbsoluteFile();
    }

    private String getDisplayName(Summary summary) {
        return config.isFullName() ? MultiLineNameUtil.join(summary.name(), " * ") : MultiLineNameUtil.last(summary.name());
    }

    private static TsData nullToEmpty(String k, TsData v) {
        return v != null ? v : TsData.empty("MISSING " + k);
    }

    private static ArraySheet toSheet(String sheetName, List<String> headers0, List<String> headers1, List<TsData> collection, boolean verticalOrientation) {
        TsDataTable table = TsDataTable.of(collection);
        ArraySheet.Builder result = ArraySheet.builder().name(sheetName);
        if (verticalOrientation) {
            //headers0
            result.row(0, 1, headers0);
            //headers1
            result.row(1, 1, headers1);
            //columnvalues & data
            TsDataTable.Cursor cursor = table.cursor(TsDataTable.DistributionType.FIRST);
            for (int p = 0; p < cursor.getPeriodCount(); p++) {
                List<Object> data = new ArrayList<>();
                data.add(toDate(table.getDomain().get(p)));
                for (int s = 0; s < cursor.getSeriesCount(); s++) {
                    TsDataTable.ValueStatus status = cursor.moveTo(p, s).getStatus();
                    data.add(status == TsDataTable.ValueStatus.PRESENT ? cursor.getValue() : null);
                }
                result.row(2 + p, 0, data);
            }
        } else {
            //headers0
            result.column(1, 0, headers0);
            //headers1
            result.column(1, 1, headers1);
            //columnvalues & data
            TsDataTable.Cursor cursor = table.cursor(TsDataTable.DistributionType.FIRST);
            for (int p = 0; p < cursor.getPeriodCount(); p++) {
                List<Object> data = new ArrayList<>();
                data.add(toDate(table.getDomain().get(p)));
                for (int s = 0; s < cursor.getSeriesCount(); s++) {
                    TsDataTable.ValueStatus status = cursor.moveTo(p, s).getStatus();
                    data.add(status == TsDataTable.ValueStatus.PRESENT ? cursor.getValue() : null);
                }
                result.column(0, 2 + p, data);
            }
        }
        return result.build();
    }

    private static Date toDate(TsPeriod period) {
        return Date.from(period.start().atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * @author Kristof Bayens
     */
    private record Summary(String name, LinkedHashMap<String, TsData> series) {

        public static Summary of(String name, Explorable results, List<String> items) {
            LinkedHashMap<String, TsData> series = new LinkedHashMap<>();
            for (String item : items) {
                series.put(item, results != null ? results.getData(item, TsData.class) : null);
            }
            return new Summary(name, series);
        }

    }
}
