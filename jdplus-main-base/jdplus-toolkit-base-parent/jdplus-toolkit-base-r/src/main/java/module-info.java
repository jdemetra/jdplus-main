import jdplus.toolkit.base.api.information.InformationExtractor;

module jdplus.toolkit.base.r {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires jdplus.toolkit.base.core;
    requires jdplus.toolkit.base.protobuf;

    exports jdplus.toolkit.base.r.arima;
    exports jdplus.toolkit.base.r.calendar;
    exports jdplus.toolkit.base.r.math;
    exports jdplus.toolkit.base.r.modelling;
    exports jdplus.toolkit.base.r.stats;
    exports jdplus.toolkit.base.r.timeseries;
    exports jdplus.toolkit.base.r.util;

    provides InformationExtractor with
            jdplus.toolkit.base.r.arima.extensions.SarimaExtension;
}