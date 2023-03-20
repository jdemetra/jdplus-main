module jdplus.x13.base.core {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.x13.base.api;
    requires jdplus.toolkit.base.core;
    requires jdplus.sa.base.core;

    exports jdplus.x11;
    exports jdplus.x11.extremevaluecorrector;
    exports jdplus.x11.filter;
    exports jdplus.x11.filter.endpoints;
    exports jdplus.x11.pseudoadd;
    exports jdplus.x13;
    exports jdplus.x13.diagnostics;
    exports jdplus.x13.extractors;
    exports jdplus.x13.regarima;
    exports jdplus.x13.spi;

    provides demetra.information.InformationExtractor with
            jdplus.x13.extractors.X13Extractor,
            jdplus.x13.extractors.MstatsExtractor,
            jdplus.x13.extractors.X13DiagnosticsExtractor,
            jdplus.x13.extractors.X11Extractor;

    provides demetra.sa.SaProcessingFactory with
            jdplus.x13.X13Factory;

    provides demetra.x13.X13.Processor with
            jdplus.x13.spi.X13Computer;

    provides demetra.regarima.RegArima.Processor with
            jdplus.x13.spi.RegArimaComputer;

    provides demetra.x11.X11.Processor with
            jdplus.x13.spi.X11Computer;
}