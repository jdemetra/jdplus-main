import jdplus.toolkit.base.api.advanced.arima.Arima;
import jdplus.toolkit.base.api.advanced.dstats.Distributions;
import jdplus.toolkit.base.api.advanced.math.Polynomials;
import jdplus.toolkit.base.api.advanced.matrices.MatrixDecompositions;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.math.matrices.MatrixOperations;
import jdplus.toolkit.base.api.timeseries.TsProvider;

module jdplus.toolkit.base.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    exports jdplus.toolkit.base.api.advanced.algebra;
    exports jdplus.toolkit.base.api.advanced.arima;
    exports jdplus.toolkit.base.api.advanced.dstats;
    exports jdplus.toolkit.base.api.advanced.filters;
    exports jdplus.toolkit.base.api.advanced.math;
    exports jdplus.toolkit.base.api.advanced.matrices;
    exports jdplus.toolkit.base.api.advanced.ssf;
    exports jdplus.toolkit.base.api.arima;
    exports jdplus.toolkit.base.api.data;
    exports jdplus.toolkit.base.api.design;
    exports jdplus.toolkit.base.api.dstats;
    exports jdplus.toolkit.base.api.eco;
    exports jdplus.toolkit.base.api.information;
    exports jdplus.toolkit.base.api.information.formatters;
    exports jdplus.toolkit.base.api.math;
    exports jdplus.toolkit.base.api.math.functions;
    exports jdplus.toolkit.base.api.math.linearfilters;
    exports jdplus.toolkit.base.api.math.matrices;
    exports jdplus.toolkit.base.api.modelling;
    exports jdplus.toolkit.base.api.modelling.highfreq;
    exports jdplus.toolkit.base.api.modelling.regular;
    exports jdplus.toolkit.base.api.processing;
    exports jdplus.toolkit.base.api.ssf;
    exports jdplus.toolkit.base.api.ssf.sts;
    exports jdplus.toolkit.base.api.stats;
    exports jdplus.toolkit.base.api.time;
    exports jdplus.toolkit.base.api.timeseries;
    exports jdplus.toolkit.base.api.timeseries.calendars;
    exports jdplus.toolkit.base.api.timeseries.regression;
    exports jdplus.toolkit.base.api.timeseries.util;
    exports jdplus.toolkit.base.api.dictionaries;
    exports jdplus.toolkit.base.api.util;
    exports jdplus.toolkit.base.api.util.function;
    exports jdplus.toolkit.base.api;

    uses Distributions.Processor;
    uses MatrixDecompositions.Processor;
    uses jdplus.toolkit.base.api.advanced.matrices.MatrixOperations.Processor;
    uses InformationExtractor;
    uses MatrixOperations.Computer;
    uses MatrixOperations.Symmetric.SymmetricComputer;
    uses MatrixOperations.LowerTriangular.LowerTriangularComputer;
    uses MatrixOperations.UpperTriangular.UpperTriangularComputer;
    uses Arima.Processor;
    uses TsProvider;
    uses Polynomials.Processor;
}