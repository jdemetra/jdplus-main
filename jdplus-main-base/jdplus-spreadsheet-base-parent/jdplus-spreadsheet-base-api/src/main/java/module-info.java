import jdplus.spreadsheet.base.api.SpreadSheetProvider;
import jdplus.toolkit.base.api.timeseries.TsProvider;

module jdplus.spreadsheet.base.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.toolkit.base.tsp;
    requires nbbrd.spreadsheet.api;
    requires nbbrd.io.base;

    exports jdplus.spreadsheet.base.api;

    provides TsProvider
            with SpreadSheetProvider;
}