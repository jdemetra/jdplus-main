/*
 * Copyright 2022 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.toolkit.base.core.ssf.akf;

import jdplus.toolkit.base.core.ssf.likelihood.ProfileLikelihood;
import jdplus.toolkit.base.core.ssf.likelihood.MarginalLikelihood;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.LogSign;
import jdplus.toolkit.base.core.stats.likelihood.DeterminantalTerm;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.math.matrices.UpperTriangularMatrix;
import jdplus.toolkit.base.core.ssf.univariate.DefaultFilteringResults;
import jdplus.toolkit.base.core.ssf.univariate.FastFilter;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.ISsfData;
import jdplus.toolkit.base.core.ssf.univariate.OrdinaryFilter;
import jdplus.toolkit.base.core.ssf.likelihood.DiffuseLikelihood;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.linearsystem.QRLeastSquaresSolution;
import jdplus.toolkit.base.core.math.linearsystem.QRLeastSquaresSolver;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.decomposition.Householder2;
import jdplus.toolkit.base.core.math.matrices.decomposition.QRDecomposition;
import jdplus.toolkit.base.core.stats.likelihood.DiffuseConcentratedLikelihood;

/**
 * QR variant of the augmented Kalman filter. See for instance Gomez-Maravall.
 * This implementation doesn't use collapsing
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class QRFilter {

    private ISsfData o;
    private FastMatrix X, Xl;
    private DataBlock yl;
    private int n;
    // result of the regression yl=Xl*b+mu
    private QRLeastSquaresSolution ls;
    private DoubleSeq b, e;
    private double ldet, ssq;
    private static final double EPS = 1e-12;

    /**
     *
     */
    public QRFilter() {
    }

    /**
     *
     * @param ssf
     * @param data
     * @return
     */
    public boolean process(final ISsf ssf, final ISsfData data) {
        clear();
        this.o = data;
        OrdinaryFilter filter = new OrdinaryFilter();
        DefaultFilteringResults fr = DefaultFilteringResults.light();
        fr.prepare(ssf, 0, data.length());
        if (!filter.process(ssf, data, fr)) {
            return false;
        }
        DeterminantalTerm det = new DeterminantalTerm();
        DoubleSeq vars = fr.errorVariances();
        for (int i = 0; i < vars.length(); ++i) {
            double v = vars.get(i);
            if (!data.isMissing(i) && v != 0) {
                det.add(v);
            }
        }
        ldet = det.getLogDeterminant();

        // apply the filter on the diffuse effects
        X = FastMatrix.make(data.length(), ssf.getDiffuseDim());
        ssf.diffuseEffects(X);
        yl = DataBlock.of(fr.errors(true, true));
        FastFilter ffilter = new FastFilter(ssf, fr);
        n = ffilter.getOutputLength(X.getRowsCount());
        Xl = FastMatrix.make(n, X.getColumnsCount());
        for (int i = 0; i < X.getColumnsCount(); ++i) {
            ffilter.apply(X.column(i), Xl.column(i));
        }

        Householder2 hous = new Householder2();
        QRDecomposition qr = hous.decompose(Xl.deepClone());
        ls = QRLeastSquaresSolver.leastSquares(qr, yl, 1e-12);
        b = ls.getB();
        e = ls.getE();
        ssq = ls.getSsqErr();
        return true;
    }

    public MarginalLikelihood marginalLikelihood(boolean scalingFactor, boolean res) {
        DiffuseLikelihood dll = diffuseLikelihood(false, res);
        FastMatrix Q = X;
        if (X.getRowsCount() != Xl.getRowsCount()) {
            Q = FastMatrix.make(Xl.getRowsCount(), X.getColumnsCount());
            for (int i = 0, j = 0; i < o.length(); ++i) {
                if (!o.isMissing(i)) {
                    Q.row(j++).copy(X.row(i));
                }
            }
        }
        QRDecomposition qrx = new Householder2().decompose(Q);
        double mcorr = 2 * LogSign.of(qrx.rawRdiagonal()).getValue();
        int nd = UpperTriangularMatrix.rank(qrx.rawR(), EPS);

        return MarginalLikelihood.builder(n, nd)
                .ssqErr(dll.ssq())
                .logDeterminant(ldet)
                .diffuseCorrection(dll.getDiffuseCorrection())
                .marginalCorrection(mcorr)
                .residuals(dll.e())
                .concentratedScalingFactor(scalingFactor)
                .build();

    }

    public DiffuseLikelihood diffuseLikelihood(boolean scalingFactor, boolean res) {
        int nd = b.length();
        double dcorr = 2 * LogSign.of(ls.rawRDiagonal()).getValue();
        return DiffuseLikelihood.builder(n, nd)
                .ssqErr(ssq)
                .logDeterminant(ldet)
                .diffuseCorrection(dcorr)
                .concentratedScalingFactor(scalingFactor)
                .residuals(res ? e : null)
                .build();

    }

    /**
     * @param ndiffuse Number of diffuse effects that are actually considered as
     * diffuse, the other ones being considered as fixed unknown; The diffuse
     * elements must be the first ones in the diffuse constraints matrix. Not
     * used in the profile likelihood.
     * @param scalingFactor
     * @param res
     * @return
     */
    public DiffuseConcentratedLikelihood mixedLikelihood(int ndiffuse, boolean scalingFactor, boolean res) {

        double dcorr = ndiffuse == 0 ? 0 : 2 * LogSign.of(ls.rawRDiagonal().extract(0, ndiffuse)).getValue();
        FastMatrix bvar = SymmetricMatrix.UUt(UpperTriangularMatrix
                .inverse(ls.rawR()));
        return DiffuseConcentratedLikelihood.builder(n, ndiffuse, 0)
                .coefficients(b)
                .unscaledCovariance(bvar.dropTopLeft(ndiffuse, ndiffuse))
                .ssqErr(ssq)
                .logDeterminant(ldet)
                .logDiffuseDeterminant(dcorr)
                .scalingFactor(scalingFactor)
                .residuals(res ? e : null)
                .build();
    }

    public ProfileLikelihood profileLikelihood() {
        FastMatrix R = ls.rawR();
        FastMatrix bvar = SymmetricMatrix.UUt(UpperTriangularMatrix
                .inverse(R));
        bvar.mul(ssq / n);
        ProfileLikelihood pll = new ProfileLikelihood();
        pll.set(ssq, ldet, b, bvar, n);
        pll.set(e);
        return pll;
    }

    private void clear() {
        o = null;
        ldet = 0;
        X = null;
        Xl = null;
        yl = null;
        b = null;
        e = null;
        ls = null;
        ssq = 0;
        n = 0;
    }

}
