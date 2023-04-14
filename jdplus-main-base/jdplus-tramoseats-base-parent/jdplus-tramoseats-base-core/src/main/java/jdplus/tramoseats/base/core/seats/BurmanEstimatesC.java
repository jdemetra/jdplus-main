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
package jdplus.tramoseats.base.core.seats;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Doubles;
import jdplus.toolkit.base.core.arima.ArimaException;
import jdplus.toolkit.base.core.arima.ArimaModel;
import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.linearfilters.BackFilter;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixException;
import jdplus.toolkit.base.core.math.matrices.MatrixWindow;
import jdplus.toolkit.base.core.math.matrices.decomposition.Gauss;
import jdplus.toolkit.base.core.math.matrices.decomposition.LUDecomposition;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import jdplus.toolkit.base.core.math.polynomials.UnitRoots;
import jdplus.toolkit.base.core.ssf.arima.ExactArimaForecasts;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;
import jdplus.toolkit.base.core.ucarima.WienerKolmogorovEstimators;
import nbbrd.design.Development;

/**
 * Estimation of the components of an UCARIMA model using a variant of the
 * Burman's algorithm.</br>This class is based on the program SEATS+ developed
 * by Gianluca Caporello and Agustin Maravall -with programming support from
 * Domingo Perez and Roberto Lopez- at the Bank of Spain, and on the program
 * SEATS, previously developed by Victor Gomez and Agustin Maravall.<br>It
 * corresponds more especially to a modified version of the routine
 * <i>ESTBUR</i>
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class BurmanEstimatesC {

    private static final double[] ONE = new double[]{1};

    public static class Builder {

        private int nf, nb;
        private DoubleSeq data;
        private UcarimaModel ucm;
        private boolean bmean;
        private int mcmp;
        private double ser = 1;

        public Builder forecastsCount(int nf) {
            this.nf = nf;
            return this;
        }

        public Builder backcastsCount(int nb) {
            this.nb = nb;
            return this;
        }

        public Builder data(DoubleSeq y) {
            this.data = y;
            return this;
        }

        public Builder mean(boolean mean) {
            this.bmean = mean;
            return this;
        }

        /**
         * Index of the component associated to the mean correction
         *
         * @param cmp
         * @return
         */
        public Builder meanComponent(int cmp) {
            this.mcmp = cmp;
            return this;
        }

        public Builder innovationStdev(double ser) {
            this.ser = ser;
            return this;
        }

        public Builder ucarimaModel(UcarimaModel ucm) {
            this.ucm = ucm;
            return this;
        }

        public BurmanEstimatesC build() {
            return new BurmanEstimatesC(this);
        }
    }

//    private double[] m_data;
//    private int m_nf;
//    private WienerKolmogorovEstimators wk;
//    private Polynomial m_ar, m_ma;
//    private Polynomial[] m_g;
//    private double m_ser = 1, m_mean;
//    private int m_nparams;
//    // private int m_p, m_q;, m_r;
//    private double[][] m_e, m_f;
//    private double[] m_xb, m_xf;
//    private boolean m_bmean;
    public static Builder builder() {
        return new Builder();
    }

    private final int nfcasts, nbcasts;
    private final DoubleSeq data;
    private final UcarimaModel ucm;
    private final boolean bmean;
    private final int mcmp;
    private final double ser;
    private final WienerKolmogorovEstimators wk;

    private double[] ar, ma;
    private double[][] g;
    private double[] z;
    private DoubleSeq[] estimates, forecasts, backcasts;
    private DoubleSeq xbcasts, xfcasts;
    private LUDecomposition lu;
    private int nf;
    private double mean, meanc;

    private BurmanEstimatesC(Builder builder) {
        this.data = builder.data;
        this.bmean = builder.bmean;
        this.mcmp = builder.mcmp;
        this.ucm = builder.ucm;
        this.ser = builder.ser;
        this.nfcasts = builder.nf;
        this.nbcasts = builder.nb;

        wk = new WienerKolmogorovEstimators(ucm);
        initModel();
        extendSeries();
        for (int i = 0; i < ucm.getComponentsCount(); ++i) {
            calc(i);
        }
    }

    private void calc(final int cmp) {
        int n = data.length();
        if (cmp == mcmp && isTrendConstant()) {
            double c = meanc;
            estimates[cmp] = DoubleSeq.onMapping(n, i -> c);
            forecasts[cmp] = DoubleSeq.onMapping(nfcasts, i -> c);
            backcasts[cmp] = DoubleSeq.onMapping(nbcasts, i -> c);
        } else if (g[cmp] == null) {
            return;
        }

        // qstar is the order of the ma polynomial
        // pstar is the order of the ar polynomial
        int qstar = ma.length - 1;
        int pstar = ar.length - 1;
        if (useD1()) {
            ++qstar;
        }

        // //////////////////////////////////
        // Compute w1(t) = g(F) z(t)
//        double[] gcur = this.g[cmp];
//        int gstar = gcur.length - 1; // gstar = max(pstar, qstar). Could it be less ?
//        double[] w1 = new double[n + qstar];
//        for (int i = 0; i < n + qstar; ++i) {
//            double s = gcur[0] * z[nf + i];
//            for (int j = 1; j <= gstar; ++j) {
//                s += gcur[j] * z[nf + i + j];
//            }
//            w1[i] = s;
//        }
//        // calculation of x2
//
//        // calculation of w2=g*data, for -q<=t<n+nf , elements -q to n+nf of
//        // data w1: elt t=0 at place q
//        double[] w2 = new double[n + nf + qstar];
//        for (int i = 0; i < n + nf + qstar; ++i) {
//            double s = gcur[0] * z[nf - qstar + i];
//            for (int j = 1; j <= gstar; ++j) {
//                s += gcur[j] * z[nf - qstar + i - j];
//            }
//            w2[i] = s;
//        }
//
//        double[] ww = new double[pstar + qstar];
//        int ntmp = n + qstar - pstar;
//        for (int i = 0; i < pstar; ++i) {
//            ww[i] = w1[ntmp + i];
//        }
//
////        double[] mx = ww.length == 0 ? new double[0] : lu.solve(ww);
//        lu.solve(DataBlock.of(ww));
//
//        int nx1 = n + Math.max(2 * qstar, nf);
//        double[] x1 = new double[nx1];
//        for (int i = 0; i < pstar + qstar; ++i) {
//            x1[ntmp + i] = ww[i];
//            // backward iteration
//        }
//        for (int i = ntmp - 1; i >= 0; --i) {
//            double s = w1[i];
//            for (int j = 1; j < ma.length; ++j) {
//                s -= x1[i + j] * ma[j];
//            }
//            x1[i] = s;
//        }
//
//        // forward iteration
//        for (int i = ntmp + pstar + qstar; i < nx1; ++i) {
//            double s = 0;
//            for (int j = 1; j <= pstar; ++j) {
//                s -= ar[j] * x1[i - j];
//            }
//            x1[i] = s;
//        }
//
//        for (int i = 0; i < pstar; ++i) {
//            ww[i] = w2[pstar - i - 1];
//        }
////        mx = ww.length == 0 ? new double[0] : m_solver.solve(ww);
//        lu.solve(DataBlock.of(ww));
//
//        int nx2 = n + 2 * qstar + Math.max(nf, 2 * qstar);
//        double[] x2 = new double[nx2];
//        for (int i = 0; i < pstar + qstar; ++i) {
//            x2[pstar + qstar - 1 - i] = ww[i];
//
//            // iteration w2 start in -q, x2 in -2*q delta q
//        }
//        for (int i = pstar + qstar; i < nx2; ++i) {
//            double s = w2[i - qstar];
//            for (int j = 1; j < ma.length; ++j) {
//                s -= x2[i - j] * ma[j];
//            }
//            x2[i] = s;
//        }
//
//        double[] rslt = new double[n];
//        for (int i = 0; i < n; ++i) {
//            rslt[i] = (x1[i] + x2[i + 2 * qstar]);
//        }
//        if (cmp == 0 && useMean()) {
//            double m = correctedMean();
//            for (int i = 0; i < rslt.length; ++i) {
//                rslt[i] += m;
//            }
//        }
//        estimates[cmp] = DoubleSeq.of(rslt);
//
//        if (nfcasts > 0) {
//            double[] fcast = new double[nfcasts];
//
//            for (int i = 0; i < nfcasts; ++i) {
//                fcast[i] = (x1[n + i] + x2[n + i + 2 * qstar]);
//            }
//            if (cmp == 0 && useMean()) {
//                double m = correctedMean();
//                for (int i = 0; i < fcast.length; ++i) {
//                    fcast[i] += m;
//                }
//            }
//            forecasts[cmp] = DoubleSeq.of(fcast);
//        }
//        if (nbcasts > 0) {
//            double[] bcast = new double[nbcasts];
//
//            for (int i = 0; i < nbcasts; ++i) {
//                bcast[i] = (x1[n + i] + x2[n + i + 2 * qstar]);
//            }
//            if (cmp == 0 && useMean()) {
//                double m = correctedMean();
//                for (int i = 0; i < bcast.length; ++i) {
//                    bcast[i] += m;
//                }
//            }
//            backcasts[cmp] = DoubleSeq.of(bcast);
//        }
        int rstar = qstar + pstar;
        double[] gcur = this.g[cmp];
        int gstar = gcur.length - 1; // gstar = max(pstar, qstar). Could it be less ?
        // We want to estimate x1(t) = g(F)/Q(F) z(t) in [-2*q*, n + 2*q*[ 
        // 1. Compute w1(t) = g(F) z(t) in [-2*q*, n + q*[ (nf - g* = q*)  
        // 2. Estimate  x1(t) for t in [n+q*-p*, n+ 2* q*[ . See Burman's paper
        // 3. Compute the rest by recursion

        // //////////////////////////////////
        // 1. w1(t) = g(F) z(t) in [-2* q*, n + q*[ 
        // rem: z in [-nf, n+nf[. Starts at nf-2*q*
        double[] w1 = new double[n + 2 * nf];
        int start = nf - 2 * qstar, end = nf + n + qstar;
        for (int i = start; i < end; ++i) {
            double s = gcur[0] * z[i];
            for (int k = 1; k <= gstar; ++k) {
                s += gcur[k] * z[i + k];
            }
            w1[i] = s;
        }

        // 2. Estimate  x1(t) for t in [n + q* - p*, n+ 2* q*[
        // Q(F) x1(t) = w1(t) (t in n + q* - p*, n + q*) // p equations
        // P(B) x1(t) = 0 or m (t in n+q*, n + 2 * q*)  // q equations
        double[] ww = new double[rstar];
        for (int i = 0, j = nf + n + qstar - pstar; i < pstar; ++i, ++j) {
            ww[i] = w1[j];
        }
        lu.solve(DataBlock.of(ww));

        double[] x1 = new double[n + 2 * nf];
        start = nf + n + qstar - pstar;
        // Estimated xl
        for (int i = 0, j = start; i < rstar; ++i, ++j) {
            x1[j] = ww[i];
        }
        // backward iteration:  w = (m+MA(F))x1 <-> w(t)-m = MA(f)x1 
        end = nf - 2 * qstar;
        for (int i = start - 1; i >= end; --i) {
            double s = w1[i];
            for (int k = 1; k < ma.length; ++k) {
                s -= x1[i + k] * ma[k];
            }
            x1[i] = s / ma[0];
        }

        // symmetric computation for w2 =g(B) z(t)
        // 1. w2(t) = g(B) z(t) in [-q*, n + 2*q*[
        double[] w2 = new double[n + 2 * nf];
        start = nf - qstar;
        end = nf + n + 2 * qstar;
        for (int i = start; i < end; ++i) {
            double s = gcur[0] * z[i];
            for (int k = 1; k <= gstar; ++k) {
                s += gcur[k] * z[i - k];
            }
            w2[i] = s;
        }

        // 2. Estimate  x2(t) for t in [-2 * q*, p* - q*[
        // Q(F) x1(t) = w1(t) (t in n + q* - p*, n + q*) // p equations
        // P(B) x1(t) = 0 or m (t in n+q*, n + 2 * q*)  // q equations
        ww = new double[rstar];
        for (int i = 0, j = nf + pstar - qstar; i < pstar; ++i) {
            ww[i] = w2[--j];
        }
        lu.solve(DataBlock.of(ww));
        // ww contains estimates of the signal for t= -2q* to p*-q* (in reverse order)
        double[] x2 = new double[n + 2 * nf];
        start = nf + pstar - qstar;
        for (int i = 0, j = start; i < ww.length; ++i) {
            x2[--j] = ww[i];
        }

        // forward recursion: Q(B) w = x2
        end = nf + n + 2 * qstar;
        for (int i = start; i < end; ++i) {
            double s = w2[i];
            for (int k = 1; k < ma.length; ++k) {
                s -= x2[i - k] * ma[k];
            }
            x2[i] = s / ma[0];
        }

        int nfc = Math.max(2 * qstar, nfcasts), nbc = Math.max(2 * qstar, nbcasts);

        double[] rslt = new double[n + nfc + nbc];
        int xstart = nf - 2 * qstar, xend = nf + n + 2 * qstar;
        int del = nbc - nf;
        // x1, x2 defined in [-2*qstar, n + 2*qstar[
        // rslt define in ]-nbc, n+nbf[

        for (int i = xstart, j = xstart + del; i < xend; ++i, ++j) {
            rslt[j] = x1[i] + x2[i];
        }
        estimates[cmp] = DoubleSeq.of(rslt, nbc, n);
        double[] car = ucm.getComponent(cmp).getAr().asPolynomial().toArray();

        // complete backcasts
        for (int j = nbc - 2 * qstar - 1; j >= 0; --j) {
            double s = 0;
            for (int k = 1; k < car.length; ++k) {
                s -= car[k] * rslt[j + k];
            }
            rslt[j] = s;
        }
        // complete forecasts
        for (int j = nbc + n + 2 * qstar; j < rslt.length; ++j) {
            double s = 0;
            for (int k = 1; k < car.length; ++k) {
                s -= car[k] * rslt[j - k];
            }
            rslt[j] = s;
        }
        if (cmp == mcmp && useMean()) {
            for (int i = 0; i < rslt.length; ++i) {
                rslt[i] += meanc;
            }
        }
        if (nfcasts > 0) {
            forecasts[cmp] = DoubleSeq.of(rslt, n + nbc, nfcasts);
        }
        if (nbcasts > 0) {
            backcasts[cmp] = DoubleSeq.of(rslt, nbc - nbcasts, nbcasts);
        }
    }

    /**
     *
     * @param cmp
     * @param signal
     * @return
     */
    public DoubleSeq estimates(final int cmp, final boolean signal) {

        if (signal) {
            return estimates[cmp];
        } else {
            return data.fastOp(estimates[cmp], (a, b) -> a - b);
        }
    }

    /**
     *
     */
    private void extendSeries() {
        int q = ma.length - 1, p = ar.length - 1;
        nf = q > p ? 2 * q : p + q;

        ExactArimaForecasts fcasts = new ExactArimaForecasts();
        fcasts.prepare(wk.getUcarimaModel().getModel(), bmean);
        xfcasts = fcasts.forecasts(data, Math.max(nf, nfcasts));
        xbcasts = fcasts.backcasts(data, Math.max(nf, nbcasts));
        if (bmean) {
            mean = fcasts.getMean();
        } else {
            mean = 0;
        }
        int n = data.length();
        // z is the extended series with forecasts and backcasts
        z = new double[n + 2 * nf];
        data.copyTo(z, nf);

        xfcasts.range(0, nf).copyTo(z, nf + n);
        xbcasts.drop(xbcasts.length() - nf, 0).copyTo(z, 0);
        if (useMean()) {
            meanc = correctedMean();
            for (int i = 0; i < z.length; ++i) {
                z[i] -= meanc;
            }
        }
    }

    private IArimaModel model() {
        return wk.getUcarimaModel().getModel();
    }

    // compute mean/P(1), where P is the stationary AR 
    private double correctedMean() {
        IArimaModel arima = model();
        return mean / arima.getStationaryAr().asPolynomial().evaluateAt(1);
    }

    /**
     *
     * @param cmp
     * @param signal
     * @return
     */
    public DoubleSeq forecasts(final int cmp, final boolean signal) {
        if (signal) {
            return forecasts[cmp];
        } else {
            DoubleSeq xf = xfcasts.range(0, nfcasts);
            return forecasts[cmp].fastOp(xf, (a, b) -> a - b);
        }
    }

    /**
     *
     * @param cmp
     * @param signal
     * @return
     */
    public DoubleSeq backcasts(final int cmp, final boolean signal) {
        if (signal) {
            return backcasts[cmp];
        } else {
            int nb = xbcasts.length();
            DoubleSeq xb = xbcasts.range(nb - nbcasts, nb);
            return backcasts[cmp].fastOp(xb, (a, b) -> a - b);
        }
    }

    /**
     *
     * @return
     */
    public DoubleSeq getSeriesBackcasts() {
        return xbcasts.drop(xbcasts.length() - nbcasts, 0);
    }

    /**
     *
     * @return
     */
    public DoubleSeq getSeriesForecasts() {
        return xfcasts.range(0, nfcasts);
    }

    /**
     *
     * @return
     */
    public UcarimaModel getUcarimaModel() {
        return wk.getUcarimaModel();
    }

    /**
     *
     */
    private void initModel() {
        // cfr burman-wilson algorithm
        IArimaModel model = ucm.getModel();
        int ncmps = ucm.getComponentsCount();
        estimates = new DoubleSeq[ncmps];
        forecasts = new DoubleSeq[ncmps];
        backcasts = new DoubleSeq[ncmps];
        g = new double[ncmps][];

        Polynomial pma = model.getMa().asPolynomial();
        double v = model.getInnovationVariance();
        if (v != 1) {
            pma = pma.times(Math.sqrt(v));
        }
        Polynomial par = model.getAr().asPolynomial();
        for (int i = 0; i < ncmps; ++i) {
            ArimaModel cmp = ucm.getComponent(i);
            if (!cmp.isNull()) {
                if (!cmp.isNull()) {
                    SymmetricFilter sma = cmp.symmetricMa();
                    BackFilter umar = model.getNonStationaryAr(), ucar = cmp.getNonStationaryAr();
                    BackFilter nar = umar.divide(ucar);
                    BackFilter smar = model.getStationaryAr(), scar = cmp.getStationaryAr();
                    BackFilter.SimplifyingTool smp = new BackFilter.SimplifyingTool();
                    if (smp.simplify(smar, scar)) {
                        smar = smp.getLeft();
                        scar = smp.getRight();
                    }

                    BackFilter dar = scar;
                    nar = nar.times(smar);

                    BackFilter denom = new BackFilter(pma).times(dar);
                    SymmetricFilter c = sma.times(SymmetricFilter.convolutionOf(nar));
                    double mvar = model.getInnovationVariance();
                    if (mvar != 1) {
                        c = c.times(1 / mvar);
                    }
                    BackFilter gf = c.decompose(denom);
                    g[i] = gf.asPolynomial().toArray();
                } else {
                    g[i] = ONE;
                }
            }
        }
        if (useD1()) {
            par = par.times(UnitRoots.D1);
        }
        ma = pma.toArray();
        ar = par.toArray();
        initSolver();
    }

    private boolean useD1() {
        // we use D1 correction when there is a mean and UR in the AR part of the model
        return bmean && model().getNonStationaryArOrder() > 0;
    }

    private boolean useMean() {
        // we use the mean if there is a mean and if we don't use D1 correction
        // it appens when the model doesn't contain non stationary roots
        return bmean && model().getNonStationaryArOrder() == 0;
    }

    private boolean isTrendConstant() {
        return wk.getUcarimaModel().getComponent(mcmp).isNull();
    }

    private void initSolver() {
        int qstar = ma.length - 1;
        int pstar = ar.length - 1;

        FastMatrix M = FastMatrix.square(pstar + qstar);
        MatrixWindow top = M.top(0);
        FastMatrix M1 = top.vnext(pstar);
        for (int j = 0; j <= qstar; ++j) {
            M1.subDiagonal(j).set(ma[j]);
        }
        FastMatrix M2 = top.vnext(qstar);
        for (int j = 0; j <= pstar; ++j) {
            M2.subDiagonal(j).set(ar[pstar - j]);
        }
        lu = Gauss.decompose(M);
    }

    /**
     *
     * @return
     */
    public boolean isMeanCorrection() {
        return bmean;
    }

    /**
     *
     * @param cmp
     * @return
     */
    public DoubleSeq stdevEstimates(final int cmp) {
        if (wk.getUcarimaModel().getComponent(cmp).isNull()) {
            return Doubles.EMPTY;
        } else {
            try {
                int n = (data.length() + 1) / 2;
                double[] err = wk.totalErrorVariance(cmp, true, 0, n);
                double[] e = new double[data.length()];
                for (int i = 0; i < err.length; ++i) {
                    double x = ser * Math.sqrt(err[i]);
                    e[i] = x;
                    e[e.length - i - 1] = x;
                }
                return DoubleSeq.of(e);
            } catch (ArimaException | MatrixException err) {
                return Doubles.EMPTY;
            }
        }
    }

    /**
     *
     * @param cmp
     * @param signal
     * @return
     */
    public DoubleSeq stdevForecasts(final int cmp, final boolean signal) {
        try {
            if (wk.getUcarimaModel().getComponent(cmp).isNull() || nfcasts == 0) {
                return Doubles.EMPTY;
            }

            double[] e = wk.totalErrorVariance(cmp, signal, -nfcasts, nfcasts);
            double[] err = new double[nfcasts];
            for (int i = 0; i < nfcasts; ++i) {
                err[i] = ser * Math.sqrt(e[nfcasts - 1 - i]);
            }
            return DoubleSeq.of(err);
        } catch (ArimaException | MatrixException err) {
            return null;
        }
    }

    public DoubleSeq stdevBackcasts(final int cmp, final boolean signal) {
        if (wk.getUcarimaModel().getComponent(cmp).isNull() || nbcasts == 0) {
            return null;
        }
        try {
            double[] e = wk.totalErrorVariance(cmp, signal, -nbcasts, nbcasts);
            double[] err = new double[nbcasts];
            for (int j = nbcasts - 1; j >= 0; --j) {
                err[j] = ser * Math.sqrt(e[j]);
            }
            return DoubleSeq.of(err);
        } catch (ArimaException | MatrixException err) {
            return null;
        }

    }

}
