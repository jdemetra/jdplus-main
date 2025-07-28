import jdplus.sa.base.api.SaProcessingFactory;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.x13.base.api.regarima.RegArima;
import jdplus.x13.base.api.x11.X11;
import jdplus.x13.base.api.x13.X13;
import jdplus.x13.base.core.x13.X13Factory;

module jdplus.x13.base.core {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires transitive jdplus.x13.base.api;
    requires jdplus.toolkit.base.core;
    requires jdplus.sa.base.core;

    exports jdplus.x13.base.core.x11;
    exports jdplus.x13.base.core.x11.extremevaluecorrector;
    exports jdplus.x13.base.core.x11.filter;
    exports jdplus.x13.base.core.x11.filter.endpoints;
    exports jdplus.x13.base.core.x11.pseudoadd;
    exports jdplus.x13.base.core.x13.diagnostics;
    exports jdplus.x13.base.core.x13.extractors;
    exports jdplus.x13.base.core.x13.regarima;
    exports jdplus.x13.base.core.x13.spi;
    exports jdplus.x13.base.core.x13;

    provides InformationExtractor with
            jdplus.x13.base.core.x13.extractors.X13Extractor,
            jdplus.x13.base.core.x13.extractors.MstatsExtractor,
            jdplus.x13.base.core.x13.extractors.X13DiagnosticsExtractor,
            jdplus.x13.base.core.x13.extractors.X11Extractor;

    provides SaProcessingFactory with
            X13Factory;

    provides X13.Processor with
            jdplus.x13.base.core.x13.spi.X13Computer;

    provides RegArima.Processor with
            jdplus.x13.base.core.x13.spi.RegArimaComputer;

    provides X11.Processor with
            jdplus.x13.base.core.x13.spi.X11Computer;
}