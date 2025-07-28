import internal.toolkit.base.core.ar.BurgAlgorithm;
import internal.toolkit.base.core.ar.LevinsonAlgorithm;
import internal.toolkit.base.core.ar.OlsAlgorithm;
import internal.toolkit.base.core.arima.AnsleyFilter;
import internal.toolkit.base.core.arima.KalmanFilter;
import internal.toolkit.base.core.arima.LjungBoxFilter;
import internal.toolkit.base.core.math.functions.gsl.integration.NumericalIntegrationProcessor;
import jdplus.toolkit.base.api.advanced.dstats.Distributions;
import jdplus.toolkit.base.api.advanced.math.Polynomials;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.core.ar.AutoRegressiveEstimation;
import jdplus.toolkit.base.core.arima.estimation.ArimaForecasts;
import jdplus.toolkit.base.core.arima.estimation.ArmaFilter;
import jdplus.toolkit.base.core.dstats.spi.DistributionsProcessor;
import jdplus.toolkit.base.core.math.functions.NumericalIntegration;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.math.polynomials.spi.PolynomialsProcessor;
import jdplus.toolkit.base.core.modelling.regression.MovingHolidayProvider;
import jdplus.toolkit.base.core.regarima.extractors.RegSarimaModelExtractors;
import jdplus.toolkit.base.core.stats.linearmodel.Ols;

module jdplus.toolkit.base.core {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires transitive jdplus.toolkit.base.api;

    exports jdplus.toolkit.base.core.ar;
    exports jdplus.toolkit.base.core.arima;
    exports jdplus.toolkit.base.core.arima.estimation;
    exports jdplus.toolkit.base.core.basic;
    exports jdplus.toolkit.base.core.bayes;
    exports jdplus.toolkit.base.core.data;
    exports jdplus.toolkit.base.core.data.accumulator;
    exports jdplus.toolkit.base.core.data.analysis;
    exports jdplus.toolkit.base.core.data.interpolation;
    exports jdplus.toolkit.base.core.data.normalizer;
    exports jdplus.toolkit.base.core.data.transformation;
    exports jdplus.toolkit.base.core.dstats.spi;
    exports jdplus.toolkit.base.core.dstats;
    exports jdplus.toolkit.base.core.discrete;
    exports jdplus.toolkit.base.core.math;
    exports jdplus.toolkit.base.core.math.functions;
    exports jdplus.toolkit.base.core.math.functions.analysis;
    exports jdplus.toolkit.base.core.math.functions.bfgs;
    exports jdplus.toolkit.base.core.math.functions.levmar;
    exports jdplus.toolkit.base.core.math.functions.minpack;
    exports jdplus.toolkit.base.core.math.functions.ssq;
    exports jdplus.toolkit.base.core.math.highprecision;
    exports jdplus.toolkit.base.core.math.linearfilters;
    exports jdplus.toolkit.base.core.math.linearsystem;
    exports jdplus.toolkit.base.core.math.matrices;
    exports jdplus.toolkit.base.core.math.matrices.decomposition;
    exports jdplus.toolkit.base.core.math.matrices.lapack;
    exports jdplus.toolkit.base.core.math.polynomials;
    exports jdplus.toolkit.base.core.math.polynomials.spi;
    exports jdplus.toolkit.base.core.math.splines;
    exports jdplus.toolkit.base.core.ml;
    exports jdplus.toolkit.base.core.modelling;
    exports jdplus.toolkit.base.core.modelling.extractors;
    exports jdplus.toolkit.base.core.modelling.regression;
    exports jdplus.toolkit.base.core.modelling.regular.tests;
    exports jdplus.toolkit.base.core.pca;
    exports jdplus.toolkit.base.core.random;
    exports jdplus.toolkit.base.core.regarima;
    exports jdplus.toolkit.base.core.regarima.ami;
    exports jdplus.toolkit.base.core.regarima.diagnostics;
    exports jdplus.toolkit.base.core.regarima.estimation;
    exports jdplus.toolkit.base.core.regarima.extractors;
    exports jdplus.toolkit.base.core.regarima.outlier;
    exports jdplus.toolkit.base.core.regarima.tests;
    exports jdplus.toolkit.base.core.regsarima;
    exports jdplus.toolkit.base.core.regsarima.ami;
    exports jdplus.toolkit.base.core.regsarima.internal;
    exports jdplus.toolkit.base.core.regsarima.regular;
    exports jdplus.toolkit.base.core.sarima;
    exports jdplus.toolkit.base.core.sarima.estimation;
    exports jdplus.toolkit.base.core.ssf;
    exports jdplus.toolkit.base.core.ssf.akf;
    exports jdplus.toolkit.base.core.ssf.arima;
    exports jdplus.toolkit.base.core.ssf.array;
    exports jdplus.toolkit.base.core.ssf.basic;
    exports jdplus.toolkit.base.core.ssf.benchmarking;
    exports jdplus.toolkit.base.core.ssf.ckms;
    exports jdplus.toolkit.base.core.ssf.composite;
    exports jdplus.toolkit.base.core.ssf.dk;
    exports jdplus.toolkit.base.core.ssf.dk.sqrt;
    exports jdplus.toolkit.base.core.ssf.likelihood;
    exports jdplus.toolkit.base.core.ssf.multivariate;
    exports jdplus.toolkit.base.core.ssf.sts;
    exports jdplus.toolkit.base.core.ssf.univariate;
    exports jdplus.toolkit.base.core.ssf.utility;
    exports jdplus.toolkit.base.core.stats;
    exports jdplus.toolkit.base.core.stats.likelihood;
    exports jdplus.toolkit.base.core.stats.linearmodel;
    exports jdplus.toolkit.base.core.stats.samples;
    exports jdplus.toolkit.base.core.stats.tests;
    exports jdplus.toolkit.base.core.strings;
    exports jdplus.toolkit.base.core.timeseries;
    exports jdplus.toolkit.base.core.timeseries.calendars;
    exports jdplus.toolkit.base.core.timeseries.simplets;
    exports jdplus.toolkit.base.core.timeseries.simplets.analysis;
    exports jdplus.toolkit.base.core.ucarima;
    exports jdplus.toolkit.base.core.ucarima.estimation;

    // FIXME:
    exports internal.toolkit.base.core.math.functions.riso;

    uses ArimaForecasts;
    uses SymmetricMatrix.CholeskyProcessor;
    uses MovingHolidayProvider;
    uses Ols.Processor;
    uses NumericalIntegration.Processor;
    uses ArmaFilter;

    provides ArmaFilter with
            AnsleyFilter,
            KalmanFilter,
            LjungBoxFilter;

    provides InformationExtractor with
            jdplus.toolkit.base.core.modelling.extractors.ArimaExtractor,
            jdplus.toolkit.base.core.modelling.extractors.DiffuseLikelihoodStatisticsExtractor,
            jdplus.toolkit.base.core.modelling.extractors.LikelihoodStatisticsExtractor,
            jdplus.toolkit.base.core.modelling.extractors.LinearModelExtractors.Default,
            jdplus.toolkit.base.core.modelling.extractors.ResidualsExtractors.Dynamic,
            jdplus.toolkit.base.core.modelling.extractors.ResidualsExtractors.Specific,
            jdplus.toolkit.base.core.modelling.extractors.TsResidualsExtractors.Dynamic,
            jdplus.toolkit.base.core.modelling.extractors.TsResidualsExtractors.Specific,
            jdplus.toolkit.base.core.modelling.extractors.SarimaExtractor,
            jdplus.toolkit.base.core.modelling.extractors.SarimaSpecExtractor,
            jdplus.toolkit.base.core.modelling.extractors.UcarimaExtractor,
            RegSarimaModelExtractors.Specific,
            RegSarimaModelExtractors.GenericExtractor;

    provides NumericalIntegration.Processor with
            NumericalIntegrationProcessor;

    provides AutoRegressiveEstimation with
            BurgAlgorithm,
            LevinsonAlgorithm,
            OlsAlgorithm;

    provides Distributions.Processor with
            DistributionsProcessor;

    provides Polynomials.Processor with
            PolynomialsProcessor;
}