import jdplus.sa.base.api.SaDiagnosticsFactory;
import jdplus.sa.base.api.SaOutputFactory;
import jdplus.sa.base.api.SaProcessingFactory;
import jdplus.toolkit.base.api.information.InformationExtractor;

module jdplus.sa.base.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires transitive jdplus.toolkit.base.api;

    exports jdplus.sa.base.api.benchmarking;
    exports jdplus.sa.base.api.diagnostics;
    exports jdplus.sa.base.api.extractors;
    exports jdplus.sa.base.api;

    uses SaProcessingFactory;
    uses SaDiagnosticsFactory;
    uses SaOutputFactory;
    uses jdplus.sa.base.api.diagnostics.SeasonalityTests.Factory;

    provides InformationExtractor with
            jdplus.sa.base.api.extractors.VarianceDecompositionExtractor,
            jdplus.sa.base.api.extractors.SeriesDecompositionExtractor;
}