
module jdplus.spreadsheet.base.r {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires jdplus.toolkit.base.api;
    requires jdplus.toolkit.base.core;
    requires jdplus.toolkit.base.tsp;
    requires jdplus.spreadsheet.base.api;
    requires jdplus.toolkit.base.r;

    exports jdplus.spreadsheet.base.r;
}