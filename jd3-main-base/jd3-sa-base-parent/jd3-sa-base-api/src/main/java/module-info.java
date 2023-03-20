module jdplus.sa.base.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.toolkit.base.api;

    exports demetra.sa;
    exports demetra.sa.benchmarking;
    exports demetra.sa.diagnostics;
    exports demetra.sa.extractors;
    exports demetra.sa.modelling;

    uses demetra.sa.SaProcessingFactory;
    uses demetra.sa.SaDiagnosticsFactory;
    uses demetra.sa.SaOutputFactory;
    uses demetra.sa.diagnostics.SeasonalityTests.Factory;

    provides demetra.information.InformationExtractor with
            demetra.sa.extractors.VarianceDecompositionExtractor,
            demetra.sa.extractors.SeriesDecompositionExtractor;
}