module jdplus.toolkit.base.r {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.toolkit.base.core;
    requires jdplus.toolkit.base.protobuf;

    exports demetra.arima.r;
    exports demetra.calendar.r;
    exports demetra.math.r;
    exports demetra.modelling.r;
    exports demetra.stats.r;
    exports demetra.timeseries.r;
    exports demetra.util.r;

    provides demetra.information.InformationExtractor with
            demetra.arima.r.extensions.SarimaExtension;
}