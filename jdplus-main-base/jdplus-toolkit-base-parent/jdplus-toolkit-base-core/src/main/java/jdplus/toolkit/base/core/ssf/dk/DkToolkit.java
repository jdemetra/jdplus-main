/*
 * Copyright 2016 National Bank of Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *  
 * http://ec.europa.eu/idabc/eupl
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.toolkit.base.core.ssf.dk;

import jdplus.toolkit.base.core.stats.likelihood.DiffuseConcentratedLikelihood;
import jdplus.toolkit.base.core.ssf.likelihood.DiffuseLikelihood;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.data.DataBlockStorage;
import jdplus.toolkit.base.core.ssf.dk.sqrt.DiffuseSquareRootInitializer;
import jdplus.toolkit.base.core.ssf.ckms.CkmsDiffuseInitializer;
import jdplus.toolkit.base.core.ssf.ckms.CkmsFilter;
import jdplus.toolkit.base.core.ssf.dk.sqrt.CompositeDiffuseSquareRootFilteringResults;
import jdplus.toolkit.base.core.ssf.dk.sqrt.DefaultDiffuseSquareRootFilteringResults;
import jdplus.toolkit.base.core.ssf.dk.sqrt.DiffuseSquareRootSmoother;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.IConcentratedLikelihoodComputer;
import jdplus.toolkit.base.core.ssf.univariate.ILikelihoodComputer;
import jdplus.toolkit.base.core.ssf.univariate.ISmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.ISsfData;
import jdplus.toolkit.base.core.ssf.univariate.OrdinaryFilter;
import jdplus.toolkit.base.core.ssf.univariate.SsfRegressionModel;
import jdplus.toolkit.base.core.data.LogSign;
import jdplus.toolkit.base.core.ssf.StateInfo;
import jdplus.toolkit.base.core.ssf.StateStorage;
import jdplus.toolkit.base.core.ssf.dk.sqrt.IDiffuseSquareRootFilteringResults;
import jdplus.toolkit.base.core.ssf.likelihood.MarginalLikelihood;
import jdplus.toolkit.base.core.ssf.multivariate.IMultivariateSsf;
import jdplus.toolkit.base.core.ssf.multivariate.IMultivariateSsfData;
import jdplus.toolkit.base.core.ssf.multivariate.M2uAdapter;
import jdplus.toolkit.base.core.ssf.univariate.IFilteringResults;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.linearsystem.QRLeastSquaresSolution;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.decomposition.Householder2;
import jdplus.toolkit.base.core.math.matrices.decomposition.QRDecomposition;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.core.math.matrices.decomposition.HouseholderWithPivoting;
import jdplus.toolkit.base.core.math.linearsystem.QRLeastSquaresSolver;
import jdplus.toolkit.base.core.ssf.multivariate.ExtendedMultivariateSsfData;
import jdplus.toolkit.base.core.ssf.univariate.ExtendedSsfData;
import jdplus.toolkit.base.core.ssf.univariate.ISsfMeasurement;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class DkToolkit {

    /**
     * Diffuse likelihood (see Durbin-Koopman)
     *
     * @param ssf State space form
     * @param data Data
     * @param scalingfactor True if the likelihood is defined up to a scaling
     * factor
     * @param res True if the likelihood will contain the residuals
     * @return
     */
    public DiffuseLikelihood likelihood(ISsf ssf, ISsfData data, boolean scalingfactor, boolean res) {
        return likelihoodComputer(true, scalingfactor, res).compute(ssf, data);
    }

    /**
     * Marginal likelihood (see Franke...)
     *
     * @param ssf State space form
     * @param data Data
     * @param scalingfactor True if the likelihood is defined up to a scaling
     * factor
     * @param res True if the likelihood will contain the residuals
     * @return
     */
    public MarginalLikelihood marginalLikelihood(ISsf ssf, ISsfData data, boolean scalingfactor, boolean res) {
        return new MLLComputer(scalingfactor, res).compute(ssf, data);
    }

    public DiffuseLikelihood likelihood(IMultivariateSsf ssf, IMultivariateSsfData data, boolean scalingfactor, boolean res) {
        ISsf ussf = M2uAdapter.of(ssf);
        ISsfData udata = M2uAdapter.of(data);
        return likelihoodComputer(true, scalingfactor, res).compute(ussf, udata);
    }

    /**
     * Diffuse likelihood computer (see Durbin-Koopman)
     *
     * @param sqr True if the square root initialization is used
     * @param scalingfactor True if the likelihood is defined up to a scaling
     * factor
     * @param res True if the likelihood will contain the residuals
     * @return
     */
    public ILikelihoodComputer<DiffuseLikelihood> likelihoodComputer(boolean sqr, boolean scalingfactor, boolean res) {
        return sqr ? new LLComputer2(scalingfactor, res) : new LLComputer1(scalingfactor, res);
    }

    /**
     * Diffuse concentrated likelihood computer (see Durbin-Koopman)
     *
     * @param sqr True if the square root initialization is used
     * @param fast Fast (Ckms) processing (if possible)
     * @param scalingfactor True if the likelihood is defined up to a scaling
     * factor
     * @return
     */
    public IConcentratedLikelihoodComputer<DiffuseConcentratedLikelihood> concentratedLikelihoodComputer(boolean sqr, boolean fast, boolean scalingfactor) {
        return new CLLComputer(sqr, fast, scalingfactor);
    }

    public DefaultDiffuseFilteringResults filter(ISsf ssf, ISsfData data, boolean all) {
        DefaultDiffuseFilteringResults frslts = all
                ? DefaultDiffuseFilteringResults.full() : DefaultDiffuseFilteringResults.light();
        frslts.prepare(ssf, 0, data.length());
        DurbinKoopmanInitializer initializer = new DurbinKoopmanInitializer(frslts);
        OrdinaryFilter filter = new OrdinaryFilter(initializer);
        filter.process(ssf, data, frslts);
        return frslts;
    }

    public DefaultDiffuseSquareRootFilteringResults sqrtFilter(ISsf ssf, ISsfData data, boolean all) {
        DefaultDiffuseSquareRootFilteringResults frslts = all
                ? DefaultDiffuseSquareRootFilteringResults.full() : DefaultDiffuseSquareRootFilteringResults.light();
        frslts.prepare(ssf, 0, data.length());
        DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(frslts);
        OrdinaryFilter filter = new OrdinaryFilter(initializer);
        filter.process(ssf, data, frslts);
        return frslts;
    }

    public void sqrtFilter(ISsf ssf, ISsfData data, IFilteringResults frslts, boolean all) {
        DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(null);
        OrdinaryFilter filter = new OrdinaryFilter(initializer);
        filter.process(ssf, data, frslts);
    }

    public void sqrtFilter(ISsf ssf, ISsfData data, IDiffuseSquareRootFilteringResults frslts, boolean all) {
        DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(frslts);
        OrdinaryFilter filter = new OrdinaryFilter(initializer);
        filter.process(ssf, data, frslts);
    }

    /**
     *
     * @param ssf State space form
     * @param data Data
     * @param all Computes also the variances
     * @param rescaleVariance If true, the variances are rescaled using the
     * estimation done in the filtering phase. Otherwise, the raw variances are
     * returned.
     * @return
     */
    public DefaultSmoothingResults smooth(ISsf ssf, ISsfData data, boolean all, boolean rescaleVariance) {
        DiffuseSmoother smoother = DiffuseSmoother
                .builder(ssf)
                .calcVariance(all)
                .rescaleVariance(rescaleVariance)
                .build();
        DefaultSmoothingResults sresults = all ? DefaultSmoothingResults.full()
                : DefaultSmoothingResults.light();
        sresults.prepare(ssf.getStateDim(), 0, data.length());
        if (smoother.process(data, sresults)) {
            return sresults;
        } else {
            return null;
        }
    }

    /**
     *
     * @param ssf State space form
     * @param data Data
     * @param all Computes also the variances
     * @param rescaleVariance If true, the variances are rescaled using the
     * estimation done in the filtering phase. Otherwise, the raw variances are
     * returned.
     * @return
     */
    public StateStorage smooth(IMultivariateSsf ssf, IMultivariateSsfData data, boolean all, boolean rescaleVariance) {
        ISsf ussf = M2uAdapter.of(ssf);
        ISsfData udata = M2uAdapter.of(data);
        DefaultSmoothingResults sr = sqrtSmooth(ussf, udata, all, rescaleVariance);
        StateStorage ss = all ? StateStorage.full(StateInfo.Smoothed) : StateStorage.light(StateInfo.Smoothed);
        int m = data.getVarsCount(), n = data.getObsCount();
        ss.prepare(ussf.getStateDim(), 0, n);
        if (all) {
            for (int i = 0; i < n; ++i) {
                ss.save(i, sr.a(i * m), sr.P(i * m));
            }
        } else {
            for (int i = 0; i < n; ++i) {
                ss.save(i, sr.a(i * m), null);
            }
        }
        return ss;
    }

    /**
     *
     * @param ssf State space form
     * @param data Data
     * @param sresults Storage for the results. The variances are computed or
     * not following the properties of the storage
     * @param rescaleVariance If true, the variances are rescaled using the
     * estimation done in the filtering phase. Otherwise, the raw variances are
     * returned.
     * @return
     */
    public boolean smooth(ISsf ssf, ISsfData data, ISmoothingResults sresults, boolean rescaleVariance) {
        boolean all = sresults.hasVariances();
        DiffuseSmoother smoother = DiffuseSmoother
                .builder(ssf)
                .calcVariance(all)
                .rescaleVariance(rescaleVariance)
                .build();
        return smoother.process(data, sresults);
    }

    /**
     * Fast smoothing (using disturbance smoother)
     *
     * @param ssf
     * @param data
     * @return
     */
    public DataBlockStorage fastSmooth(ISsf ssf, ISsfData data) {
        FastStateSmoother smoother = new FastStateSmoother(ssf);
        return smoother.process(data);
    }

    public DefaultSmoothingResults sqrtSmooth(ISsf ssf, ISsfData data, boolean all, boolean rescaleVariance) {
        DiffuseSquareRootSmoother smoother = DiffuseSquareRootSmoother
                .builder(ssf)
                .calcVariance(all)
                .rescaleVariance(rescaleVariance)
                .build();
        DefaultSmoothingResults sresults = all ? DefaultSmoothingResults.full()
                : DefaultSmoothingResults.light();
        sresults.prepare(ssf.getStateDim(), 0, data.length());
        if (smoother.process(data, sresults)) {
            return sresults;
        } else {
            return null;
        }
    }

    public boolean sqrtSmooth(ISsf ssf, ISsfData data, ISmoothingResults sresults, boolean rescaleVariance) {
        boolean all = sresults.hasVariances();
        DiffuseSquareRootSmoother smoother = DiffuseSquareRootSmoother
                .builder(ssf)
                .calcVariance(all)
                .rescaleVariance(rescaleVariance)
                .build();
        return smoother.process(data, sresults);
    }

    public FastMatrix forecast(ISsf ssf, ISsfData data, int nf, boolean variance) {
        ExtendedSsfData datax = new ExtendedSsfData(data, 0, nf);
        DefaultDiffuseFilteringResults frslts = filter(ssf, datax, variance);
        FastMatrix F = FastMatrix.make(nf, variance ? 2 : 1);

        ISsfMeasurement m = ssf.measurement();
        double var = frslts.var();
        int n=data.length();
        for (int i = 0, pos=n; i < nf; ++i, ++pos) {
            DataBlock a = frslts.a(pos);
            F.set(i, 0, m.loading().ZX(pos, a));
            if (variance) {
                FastMatrix P = frslts.P(pos);
                double v = m.loading().ZVZ(pos, P);
                if (m.hasError()) {
                    v += m.error().at(pos);
                }
                F.set(i, 1, v*var);
            }
        }
        return F;
    }
    
    private static class LLComputer1 implements ILikelihoodComputer<DiffuseLikelihood> {

        private final boolean scalingfactor, res;

        LLComputer1(boolean scalingfactor, boolean res) {
            this.res = res;
            this.scalingfactor = scalingfactor;
        }

        @Override
        public DiffuseLikelihood compute(ISsf ssf, ISsfData data) {

            DiffusePredictionErrorDecomposition pe = new DiffusePredictionErrorDecomposition(res);
            if (res) {
                pe.prepare(ssf, data.length());
            }
            DurbinKoopmanInitializer initializer = new DurbinKoopmanInitializer(pe);
            OrdinaryFilter filter = new OrdinaryFilter(initializer);
            filter.process(ssf, data, pe);
            return pe.likelihood(scalingfactor);
        }

    }

    private static class LLComputer2 implements ILikelihoodComputer<DiffuseLikelihood> {

        private final boolean scalingfactor, res;

        LLComputer2(boolean scalingfactor, boolean res) {
            this.res = res;
            this.scalingfactor = scalingfactor;
        }

        @Override
        public DiffuseLikelihood compute(ISsf ssf, ISsfData data) {

            DiffusePredictionErrorDecomposition pe = new DiffusePredictionErrorDecomposition(res);
            if (res) {
                pe.prepare(ssf, data.length());
            }
            DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(pe);
            OrdinaryFilter filter = new OrdinaryFilter(initializer);
            filter.process(ssf, data, pe);
            return pe.likelihood(scalingfactor);
        }

        public MarginalLikelihood mcompute(ISsf ssf, ISsfData data, boolean scalingfactor) {

            DiffusePredictionErrorDecomposition pe = new DiffusePredictionErrorDecomposition(res);
            if (res) {
                pe.prepare(ssf, data.length());
            }
            DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(pe);
            OrdinaryFilter filter = new OrdinaryFilter(initializer);
            filter.process(ssf, data, pe);
            DiffuseLikelihood likelihood = pe.likelihood(scalingfactor);
            int collapsing = pe.getEndDiffusePosition();
            FastMatrix M = FastMatrix.make(collapsing, ssf.getDiffuseDim());
            ssf.diffuseEffects(M);
            int j = 0;
            for (int i = 0; i < collapsing; ++i) {
                if (!data.isMissing(i)) {
                    if (i > j) {
                        M.row(j).copy(M.row(i));
                    }
                    j++;
                }
            }

            QRDecomposition qr = new Householder2().decompose(M.extract(0, j, 0, M.getColumnsCount()));
            double mc = 2 * LogSign.of(qr.rawRdiagonal()).getValue();
            return MarginalLikelihood.builder(likelihood.dim(), likelihood.getD())
                    .concentratedScalingFactor(scalingfactor)
                    .diffuseCorrection(likelihood.getDiffuseCorrection())
                    .legacy(false)
                    .logDeterminant(likelihood.logDeterminant())
                    .ssqErr(likelihood.ssq())
                    .residuals(pe.errors(true, true))
                    .marginalCorrection(mc)
                    .build();
        }
    }

    private static class MLLComputer implements ILikelihoodComputer<MarginalLikelihood> {

        private final boolean res, scalingfactor;

        MLLComputer(boolean scalingfactor, boolean res) {
            this.res = res;
            this.scalingfactor = scalingfactor;
        }

        @Override
        public MarginalLikelihood compute(ISsf ssf, ISsfData data) {

            DiffusePredictionErrorDecomposition pe = new DiffusePredictionErrorDecomposition(res);
            if (res) {
                pe.prepare(ssf, data.length());
            }
            DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(pe);
            OrdinaryFilter filter = new OrdinaryFilter(initializer);
            filter.process(ssf, data, pe);
            DiffuseLikelihood likelihood = pe.likelihood(scalingfactor);
            int collapsing = pe.getEndDiffusePosition();
            FastMatrix M = FastMatrix.make(collapsing, ssf.getDiffuseDim());
            ssf.diffuseEffects(M);
            int j = 0;
            for (int i = 0; i < collapsing; ++i) {
                if (!data.isMissing(i)) {
                    if (i > j) {
                        M.row(j).copy(M.row(i));
                    }
                    j++;
                }
            }
            QRDecomposition qr = new Householder2().decompose(M.extract(0, j, 0, M.getColumnsCount()));
            double mc = 2 * LogSign.of(qr.rawRdiagonal()).getValue();
            return MarginalLikelihood.builder(likelihood.dim(), likelihood.getD())
                    .concentratedScalingFactor(scalingfactor)
                    .diffuseCorrection(likelihood.getDiffuseCorrection())
                    .legacy(false)
                    .logDeterminant(likelihood.logDeterminant())
                    .ssqErr(likelihood.ssq())
                    .residuals(pe.errors(true, true))
                    .marginalCorrection(mc)
                    .build();
        }
    }

    private static class CLLComputer implements IConcentratedLikelihoodComputer<DiffuseConcentratedLikelihood> {

        private final boolean sqr, fast, scaling;

        private CLLComputer(boolean sqr, boolean fast, boolean scaling) {
            this.sqr = sqr;
            this.fast = fast;
            this.scaling = scaling;
        }

        @Override
        public DiffuseConcentratedLikelihood compute(SsfRegressionModel model) {
            ISsfData y = model.getY();
            int n = y.length();
            DiffusePredictionErrorDecomposition pe = new DiffusePredictionErrorDecomposition(true);
            pe.prepare(model.getSsf(), n);
            FastDkFilter filter = filteringResults(model.getSsf(), y, pe);
            DiffuseLikelihood ll = pe.likelihood(scaling);
            DoubleSeq yl = pe.errors(true, true);
            int nl = yl.length();
            FastMatrix xl = xl(model, filter, nl);
            if (xl == null) {
                return DiffuseConcentratedLikelihood.builder(ll.dim(), ll.getD(), 0)
                        .ssqErr(ll.ssq())
                        .logDeterminant(ll.logDeterminant())
                        .logDiffuseDeterminant(ll.getDiffuseCorrection())
                        .residuals(yl)
                        .scalingFactor(scaling)
                        .build();
            } else {
                HouseholderWithPivoting h = new HouseholderWithPivoting();
                int ndiffuse = model.getDiffuseElements();
                QRDecomposition qr = h.decompose(xl, ndiffuse);
                QRLeastSquaresSolution ls = QRLeastSquaresSolver.leastSquares(qr, yl, 1e-9);
                DataBlock b = DataBlock.of(ls.getB());
                DataBlock res = DataBlock.of(ls.getE());
                double ssqerr = ls.getSsqErr();
                // initializing the results...
                int nobs = ll.dim();
                int d = ll.getD();
                double ldet = ll.logDeterminant(), dcorr = ll.getDiffuseCorrection();
                if (ndiffuse > 0) {
                    DoubleSeq rdiag = ls.rawRDiagonal();
                    double lregdet = 0;
                    int ndc = 0;
                    for (int i = 0; i < ndiffuse; ++i) {
                        double r = rdiag.get(i);
                        if (r != 0) {
                            lregdet += Math.log(Math.abs(r));
                            ++ndc;
                        }
                    }
                    lregdet *= 2;
                    dcorr += lregdet;
                    d += ndc;
                }
                FastMatrix bvar = ls.unscaledCovariance();
                return DiffuseConcentratedLikelihood.builder(nobs, d, ndiffuse)
                        .ssqErr(ssqerr)
                        .logDeterminant(ldet)
                        .logDiffuseDeterminant(dcorr)
                        .residuals(res)
                        .coefficients(b)
                        .unscaledCovariance(bvar)
                        .scalingFactor(scaling)
                        .build();
            }

        }

        private FastDkFilter filteringResults(ISsf ssf, ISsfData data, DiffusePredictionErrorDecomposition pe) {
            if (sqr) {
                DefaultDiffuseSquareRootFilteringResults fr = DefaultDiffuseSquareRootFilteringResults.light();
                fr.prepare(ssf, 0, data.length());
                CompositeDiffuseSquareRootFilteringResults dr = new CompositeDiffuseSquareRootFilteringResults(fr, pe);
                DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(dr);
                if (fast) {
                    CkmsDiffuseInitializer ff = new CkmsDiffuseInitializer(initializer);
                    CkmsFilter ffilter = new CkmsFilter(ff);
                    ffilter.process(ssf, data, dr);
                    return new FastDkFilter(ssf, fr, true);
                } else {
                    OrdinaryFilter filter = new OrdinaryFilter(initializer);
                    filter.process(ssf, data, dr);
                    return new FastDkFilter(ssf, fr, true);
                }
            } else {
                DefaultDiffuseFilteringResults fr = DefaultDiffuseFilteringResults.light();
                fr.prepare(ssf, 0, data.length());
                CompositeDiffuseFilteringResults dr = new CompositeDiffuseFilteringResults(fr, pe);
                DurbinKoopmanInitializer initializer = new DurbinKoopmanInitializer(dr);
                if (fast) {
                    CkmsDiffuseInitializer ff = new CkmsDiffuseInitializer(initializer);
                    CkmsFilter ffilter = new CkmsFilter(ff);
                    ffilter.process(ssf, data, dr);
                    return new FastDkFilter(ssf, fr, true);
                } else {
                    OrdinaryFilter filter = new OrdinaryFilter(initializer);
                    filter.process(ssf, data, dr);
                    return new FastDkFilter(ssf, fr, true);
                }
            }
        }

        private FastMatrix xl(SsfRegressionModel model, FastDkFilter lp, int nl) {
            Matrix x = model.getX();
            if (x == null) {
                return null;
            }
            FastMatrix xl = FastMatrix.make(nl, x.getColumnsCount());
            DataBlockIterator lcols = xl.columnsIterator();
            int i = 0;
            while (lcols.hasNext()) {
                lp.apply(x.column(i++), lcols.next());
            }
            return xl;
        }

    }

}
