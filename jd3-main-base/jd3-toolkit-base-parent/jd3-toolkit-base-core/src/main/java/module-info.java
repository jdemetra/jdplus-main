module jdplus.toolkit.base.core {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.toolkit.base.api;

    exports jdplus.ar;
    exports jdplus.arima;
    exports jdplus.arima.estimation;
    exports jdplus.basic;
    exports jdplus.bayes;
    exports jdplus.data;
    exports jdplus.data.accumulator;
    exports jdplus.data.analysis;
    exports jdplus.data.interpolation;
    exports jdplus.data.normalizer;
    exports jdplus.data.transformation;
    exports jdplus.dstats.spi;
    exports jdplus.dstats;
    exports jdplus.eco.discrete;
    exports jdplus.math;
    exports jdplus.math.functions;
    exports jdplus.math.functions.analysis;
    exports jdplus.math.functions.bfgs;
    exports jdplus.math.functions.levmar;
    exports jdplus.math.functions.minpack;
    exports jdplus.math.functions.ssq;
    exports jdplus.math.highprecision;
    exports jdplus.math.linearfilters;
    exports jdplus.math.linearsystem;
    exports jdplus.math.matrices;
    exports jdplus.math.matrices.decomposition;
    exports jdplus.math.matrices.lapack;
    exports jdplus.math.polynomials;
    exports jdplus.math.polynomials.spi;
    exports jdplus.math.splines;
    exports jdplus.ml;
    exports jdplus.modelling;
    exports jdplus.modelling.extractors;
    exports jdplus.modelling.regression;
    exports jdplus.modelling.regular.tests;
    exports jdplus.pca;
    exports jdplus.random;
    exports jdplus.regarima;
    exports jdplus.regarima.ami;
    exports jdplus.regarima.diagnostics;
    exports jdplus.regarima.estimation;
    exports jdplus.regarima.extractors;
    exports jdplus.regarima.outlier;
    exports jdplus.regarima.tests;
    exports jdplus.regsarima;
    exports jdplus.regsarima.ami;
    exports jdplus.regsarima.internal;
    exports jdplus.regsarima.regular;
    exports jdplus.sarima;
    exports jdplus.sarima.estimation;
    exports jdplus.ssf;
    exports jdplus.ssf.akf;
    exports jdplus.ssf.arima;
    exports jdplus.ssf.array;
    exports jdplus.ssf.basic;
    exports jdplus.ssf.benchmarking;
    exports jdplus.ssf.ckms;
    exports jdplus.ssf.composite;
    exports jdplus.ssf.dk;
    exports jdplus.ssf.dk.sqrt;
    exports jdplus.ssf.likelihood;
    exports jdplus.ssf.multivariate;
    exports jdplus.ssf.sts;
    exports jdplus.ssf.sts.splines;
    exports jdplus.ssf.univariate;
    exports jdplus.ssf.utility;
    exports jdplus.stats;
    exports jdplus.stats.likelihood;
    exports jdplus.stats.linearmodel;
    exports jdplus.stats.samples;
    exports jdplus.stats.tests;
    exports jdplus.strings;
    exports jdplus.timeseries;
    exports jdplus.timeseries.calendars;
    exports jdplus.timeseries.simplets;
    exports jdplus.timeseries.simplets.analysis;
    exports jdplus.ucarima;
    exports jdplus.ucarima.estimation;

    // FIXME:
    exports internal.jdplus.dstats to jdplus.sa.base.core;
    exports internal.jdplus.arima to jdplus.x13.base.core;
    exports internal.jdplus.math.functions.gsl.integration to jdplus.experimentalsa.base.core;
    exports internal.jdplus.math.functions.riso;

    uses jdplus.arima.estimation.ArimaForecasts;
    uses jdplus.math.matrices.SymmetricMatrix.CholeskyProcessor;
    uses jdplus.modelling.regression.MovingHolidayProvider;
    uses jdplus.stats.linearmodel.Ols.Processor;
    uses jdplus.math.functions.NumericalIntegration.Processor;
    uses jdplus.arima.estimation.ArmaFilter;

    provides jdplus.arima.estimation.ArmaFilter with
            internal.jdplus.arima.AnsleyFilter,
            internal.jdplus.arima.KalmanFilter,
            internal.jdplus.arima.LjungBoxFilter;

    provides demetra.information.InformationExtractor with
            jdplus.modelling.extractors.ArimaExtractor,
            jdplus.modelling.extractors.DiffuseLikelihoodStatisticsExtractor,
            jdplus.modelling.extractors.LikelihoodStatisticsExtractor,
            jdplus.modelling.extractors.LinearModelExtractors.Default,
            jdplus.modelling.extractors.ResidualsExtractors.Dynamic,
            jdplus.modelling.extractors.ResidualsExtractors.Specific,
            jdplus.modelling.extractors.SarimaExtractor,
            jdplus.modelling.extractors.SarimaSpecExtractor,
            jdplus.modelling.extractors.UcarimaExtractor,
            jdplus.regarima.extractors.RegSarimaModelExtractors.Specific,
            jdplus.regarima.extractors.RegSarimaModelExtractors.GenericExtractor;

    provides jdplus.math.functions.NumericalIntegration.Processor with
            internal.jdplus.math.functions.gsl.integration.NumericalIntegrationProcessor;

    provides jdplus.ar.AutoRegressiveEstimation with
            internal.jdplus.ar.BurgAlgorithm,
            internal.jdplus.ar.LevinsonAlgorithm,
            internal.jdplus.ar.OlsAlgorithm;

    provides demetra.advanced.dstats.Distributions.Processor with
            jdplus.dstats.spi.DistributionsProcessor;

    provides demetra.advanced.math.Polynomials.Processor with
            jdplus.math.polynomials.spi.PolynomialsProcessor;
}