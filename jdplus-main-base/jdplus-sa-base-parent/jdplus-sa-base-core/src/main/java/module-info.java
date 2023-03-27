module jdplus.sa.base.core {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.sa.base.api;
    requires jdplus.toolkit.base.core;

    exports jdplus.sa;
    exports jdplus.sa.diagnostics;
    exports jdplus.sa.extractors;
    exports jdplus.sa.modelling;
    exports jdplus.sa.regarima;
    exports jdplus.sa.spi;
    exports jdplus.sa.tests;

    provides demetra.sa.diagnostics.SeasonalityTests.Factory with
            jdplus.sa.spi.SeasonalityTestsFactory;

    provides demetra.information.InformationExtractor with
            jdplus.sa.extractors.OneStepAheadForecastingTestExtractor,
            jdplus.sa.extractors.GenericSaTestsExtractor,
            jdplus.sa.extractors.SaRegarimaExtractor,
            jdplus.sa.extractors.SaBenchmarkingExtractor,
            jdplus.sa.extractors.CombinedSeasonalityTestsExtractor;
}