module jdplus.spreadsheet.base.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.toolkit.base.tsp;
    requires nbbrd.spreadsheet.api;
    requires nbbrd.io.base;

    exports demetra.spreadsheet;

    provides demetra.timeseries.TsProvider
            with demetra.spreadsheet.SpreadSheetProvider;
}