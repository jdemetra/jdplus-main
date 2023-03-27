module jdplus.toolkit.base.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    exports demetra;
    exports demetra.advanced.algebra;
    exports demetra.advanced.arima;
    exports demetra.advanced.dstats;
    exports demetra.advanced.filters;
    exports demetra.advanced.math;
    exports demetra.advanced.matrices;
    exports demetra.advanced.ssf;
    exports demetra.arima;
    exports demetra.data;
    exports demetra.design;
    exports demetra.dstats;
    exports demetra.eco;
    exports demetra.information;
    exports demetra.information.formatters;
    exports demetra.math;
    exports demetra.math.functions;
    exports demetra.math.matrices;
    exports demetra.modelling;
    exports demetra.modelling.highfreq;
    exports demetra.modelling.regular;
    exports demetra.processing;
    exports demetra.ssf;
    exports demetra.stats;
    exports demetra.time;
    exports demetra.timeseries;
    exports demetra.timeseries.calendars;
    exports demetra.timeseries.regression;
    exports demetra.timeseries.util;
    exports demetra.toolkit.dictionaries;
    exports demetra.util;
    exports demetra.util.function;

    uses demetra.advanced.dstats.Distributions.Processor;
    uses demetra.advanced.matrices.MatrixDecompositions.Processor;
    uses demetra.advanced.matrices.MatrixOperations.Processor;
    uses demetra.information.InformationExtractor;
    uses demetra.math.matrices.MatrixOperations.Computer;
    uses demetra.math.matrices.MatrixOperations.Symmetric.SymmetricComputer;
    uses demetra.math.matrices.MatrixOperations.LowerTriangular.LowerTriangularComputer;
    uses demetra.math.matrices.MatrixOperations.UpperTriangular.UpperTriangularComputer;
    uses demetra.advanced.arima.Arima.Processor;
    uses demetra.timeseries.TsProvider;
    uses demetra.advanced.math.Polynomials.Processor;
}