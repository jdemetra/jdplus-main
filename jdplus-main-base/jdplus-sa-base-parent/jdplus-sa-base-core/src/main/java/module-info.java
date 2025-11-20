import jdplus.toolkit.base.api.information.InformationExtractor;

module jdplus.sa.base.core {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires transitive jdplus.sa.base.api;
    requires jdplus.toolkit.base.core;

    exports jdplus.sa.base.core.diagnostics;
    exports jdplus.sa.base.core.extractors;
    exports jdplus.sa.base.core.modelling;
    exports jdplus.sa.base.core.regarima;
    exports jdplus.sa.base.core.spi;
    exports jdplus.sa.base.core.tests;
    exports jdplus.sa.base.core;

    provides jdplus.sa.base.api.diagnostics.SeasonalityTests.Factory with
            jdplus.sa.base.core.spi.SeasonalityTestsFactory;

    provides InformationExtractor with
            jdplus.sa.base.core.extractors.OneStepAheadForecastingTestExtractor,
            jdplus.sa.base.core.extractors.GenericSaTestsExtractor,
            jdplus.sa.base.core.extractors.SaRegarimaExtractor,
            jdplus.sa.base.core.extractors.SaBenchmarkingExtractor,
            jdplus.sa.base.core.extractors.CombinedSeasonalityTestsExtractor;
}