/*
 * Copyright 2025 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.toolkit.base.core.ssf.arima;

import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.core.arima.AutoCovarianceFunction;
import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.DataPointer;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.math.matrices.lapack.SYRK;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.ssf.ISsfInitialization;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.basic.Loading;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class SsfArma2 {

    public ISsfLoading defaultLoading() {
        return Loading.fromPosition(0);
    }

    public StateComponent stateComponent(IArimaModel arima) {
        if (!arima.isStationary()) {
            return null;
        }
        Data data = new Data(arima);
        Initialization initialization = new Initialization(arima, data.dim);
        Dynamics dynamics = new Dynamics(data);
        return new StateComponent(initialization, dynamics);
    }

    public Ssf ssf(IArimaModel arima) {
        return Ssf.of(stateComponent(arima), defaultLoading());
    }

    public FastMatrix unconditionalCovariance(IArimaModel arma) {

        int p = arma.getArOrder(), q = arma.getMaOrder();
        int r = Math.max(p, q + 1);
        FastMatrix V0 = FastMatrix.square(r);
        unconditionalCovariance(arma, V0);
        return V0;
    }

    private void unconditionalCovariance(IArimaModel arma, FastMatrix V0) {
        AutoCovarianceFunction acf = arma.getAutoCovarianceFunction();
        double var = arma.getInnovationVariance();
        Polynomial ar = arma.getAr().asPolynomial();
        Polynomial ma = arma.getMa().asPolynomial();
        int dim = V0.getColumnsCount();
        double[] phi = ar.coefficients().toArray(), theta = ma.coefficients().toArray();

        double[] ac = acf.values(dim);
        // first column
        DataBlock C0 = V0.column(0);
        C0.set(0, ac[0]);
        double[] lambda = lambda(phi, theta, var);
        int p = ar.degree(), q = ma.degree();
        for (int i = 2; i <= dim; ++i) {
            double c = 0;
            for (int j = i; j <= dim; ++j) {
                if (j <= p) {
                    c -= phi[j] * ac[j - i + 1];
                }
                if (j <= q + 1) {
                    c += theta[j - 1] * lambda[j - i];
                }
            }
            C0.set(i - 1, c);
        }
        recursion(phi, theta, var, V0);

    }

    double[] lambda(double[] phi, double[] theta, double var) {
        int q = theta.length - 1, p = phi.length - 1;
        double[] l = new double[q + 1];
        l[0] = var;
        for (int k = 1; k <= q; ++k) {
            double lk = theta[k] * var;
            int jmax = Math.min(p, k);
            for (int j = 1; j <= jmax; ++j) {
                lk -= phi[j] * l[k - j];
            }
            l[k] = lk;
        }
        return l;
    }

    void recursion(double[] phi, double[] theta, double v, FastMatrix V) {
        int n = V.getColumnsCount();
        for (int d = 0; d < n; ++d) {
            for (int i = d + 1, j = 1; i < n; ++j, ++i) {
                double vij = V.get(i - 1, j - 1);
                double phii = i < phi.length ? phi[i] : 0, phij = j < phi.length ? phi[j] : 0;
                double thetai = i <= theta.length ? theta[i - 1] : 0, thetaj = j <= theta.length ? theta[j - 1] : 0;
                vij += phij * (V.get(i, 0) - phii * V.get(0, 0));
                vij += phii * V.get(j, 0);
                vij -= v * thetai * thetaj;
                V.set(i, j, vij);
            }
        }
        SymmetricMatrix.fromLower(V);
    }

    static class Data {

        final int dim;
        final double var, se;
        final double[] phi, theta;

        int q() {
            return theta.length - 1;
        }

        int p() {
            return phi.length - 1;
        }

        int r() {
            return Math.max(theta.length, phi.length - 1);
        }

        Data(IArimaModel arima) {
            var = arima.getInnovationVariance();
            Polynomial ar = arima.getAr().asPolynomial();
            Polynomial ma = arima.getMa().asPolynomial();
            dim = Math.max(ar.degree(), ma.degree() + 1);
            phi = ar.coefficients().toArray();
            theta = ma.coefficients().toArray();
            se = Math.sqrt(var);
        }

    }

    static class Initialization implements ISsfInitialization {

        final int dim;
        final IArimaModel arma;

        Initialization(IArimaModel arma, int dim) {
            this.arma = arma;
            this.dim = dim;
        }

        @Override
        public int getStateDim() {
            return dim;
        }

        @Override
        public boolean isDiffuse() {
            return false;
        }

        @Override
        public int getDiffuseDim() {
            return 0;
        }

        @Override
        public void diffuseConstraints(FastMatrix fm) {
        }

        @Override
        public void a0(DataBlock db) {
            db.set(0);
        }

        @Override
        public void Pf0(FastMatrix fm) {
            unconditionalCovariance(arma, fm);
        }

        @Override
        public void Pi0(FastMatrix pi0) {
        }

    }

    static class Dynamics implements ISsfDynamics {

        final Data data;
        final FastMatrix V;

        Dynamics(Data data) {
            this.data = data;
            int n = data.dim;
            V = FastMatrix.square(n);
            int q = data.q();
            FastMatrix Vc = V.extract(0, q + 1, 0, q + 1);
            SYRK.laddaXXt(data.var, DataPointer.of(data.theta, 0), Vc);
            SymmetricMatrix.fromLower(Vc);
        }

        @Override
        public int getInnovationsDim() {
            return 1;
        }

        @Override
        public void V(int i, FastMatrix vm) {
            vm.copy(V);
        }

        @Override
        public void S(int i, FastMatrix sm) {
            double[] s = sm.getStorage();
            for (int j = 0, k=sm.getStartPosition(); j < data.theta.length; ++j, ++k) {
                s[k] = data.se * data.theta[j];
            }
        }

        @Override
        public boolean hasInnovations(int i) {
            return true;
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return true;
        }

        @Override
        public void T(int i, FastMatrix t) {
            t.subDiagonal(1).set(1);
            int p = data.p();
            DataBlock t0 = t.column(0);
            for (int j = 1; j <= p; ++j) {
                t0.set(j - 1, -data.phi[j]);
            }
        }

        @Override
        public void TX(int i, DataBlock x) {
            int p = data.p();
            double x0 = x.get(0);
            x.bshiftAndZero();
            if (p > 0 && x0 != 0) {
                DoubleSeqCursor.OnMutable cursor = x.cursor();
                for (int j = 1; j <= p; ++j) {
                    double phi = data.phi[j];
                    cursor.applyAndNext(cur -> cur - phi * x0);
                }
            }
        }

        @Override
        public void addSU(int i, DataBlock x, DataBlock u) {
            double a = u.get(0) * data.se;
            double[] px = x.getStorage();
            int q = data.q();
            for (int j = 0, k = x.getStartPosition(); j <= q; ++j, k += x.getIncrement()) {
                px[k] += a * data.theta[j];
            }
        }

        @Override
        public void addV(int i, FastMatrix fm) {
            fm.add(V);
        }

        @Override
        public void XT(int i, DataBlock x) {
            double[] px = x.getStorage();
            double x0 = 0;
            int p = data.p();
            for (int j = 1, k = x.getStartPosition(); j <= p; ++j, k += x.getIncrement()) {
                x0 -= px[k] * data.phi[j];
            }
            x.fshift(1);
            x.set(0, x0);
        }

        @Override
        public void XS(int i, DataBlock x, DataBlock sx) {
            double a = 0;
            double[] px = x.getStorage();
            int q = data.q();
            for (int j = 0, k = x.getStartPosition(); j <= q; ++j, k += x.getIncrement()) {
                a += px[k] * data.theta[j];
            }
            sx.set(0, a);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

    }

}
