import jdplus.sa.base.api.SaOutputFactory;
import jdplus.spreadsheet.base.api.SpreadSheetProvider;
import jdplus.spreadsheet.base.api.sa.SpreadsheetOutputFactory;
import jdplus.toolkit.base.api.timeseries.TsProvider;

module jdplus.spreadsheet.base.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires jdplus.toolkit.base.tsp;
    requires jdplus.sa.base.core;
    requires nbbrd.spreadsheet.api;
    requires nbbrd.io.base;

    exports jdplus.spreadsheet.base.api;
    exports jdplus.spreadsheet.base.api.sa;

    provides SaOutputFactory
            with SpreadsheetOutputFactory;

    provides TsProvider
            with SpreadSheetProvider;
}