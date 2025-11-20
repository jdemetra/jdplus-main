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
package jdplus.toolkit.base.core.ssf.arima;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.ssf.ISsfInitialization;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.SsfException;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.basic.IntegratedDynamics;
import jdplus.toolkit.base.core.ssf.basic.IntegratedInitialization;
import jdplus.toolkit.base.core.ssf.basic.Loading;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;

/**
 * State array: y(t-1)...y(t-d)y*(t)...y*(t-p+1)e(t)...e(t-q)
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class SsfArima2 {

    public int dim(IArimaModel arima) {
        return arima.getNonStationaryArOrder() + arima.getMaOrder() + 1 + arima.getStationaryArOrder();
    }

    public StateComponent stateComponent(IArimaModel arima) {
        double var = arima.getInnovationVariance();
        if (var == 0) {
            throw new SsfException(SsfException.STOCH);
        }

        // case IMA
        DoubleSeq delta = arima.getNonStationaryAr().coefficients();
        DoubleSeq phi = arima.getStationaryAr().coefficients();
        DoubleSeq theta = arima.getMa().coefficients();

        if (phi.length() > 1) {
            throw new java.lang.UnsupportedOperationException();
        }

        double[] th = theta.toArray();
        int q = th.length - 1;
        int[] pos = new int[th.length];
        for (int i = 0; i <= q; ++i) {
            pos[i] = i;
        }
        ISsfLoading loading = Loading.from(pos, th);

        if (delta.length() == 1) {
            return new StateComponent(new MaInitialization(q, var), new MaDynamics(var));
        } else {
            DoubleSeq d = delta.drop(1, 0);
            IntegratedInitialization initialization = new IntegratedInitialization(new MaInitialization(q, var), d);
            IntegratedDynamics dynamics = new IntegratedDynamics(new MaDynamics(var), loading, d);
            return new StateComponent(initialization, dynamics);
        }
    }

    public Ssf ssf(IArimaModel arima) {
        double var = arima.getInnovationVariance();
        if (var == 0) {
            throw new SsfException(SsfException.STOCH);
        }

        // case IMA
        DoubleSeq delta = arima.getNonStationaryAr().coefficients();
        DoubleSeq phi = arima.getStationaryAr().coefficients();
        DoubleSeq theta = arima.getMa().coefficients();

        if (phi.length() > 1) {
            throw new java.lang.UnsupportedOperationException();
        }

        double[] th = theta.toArray();
        int q = th.length - 1;
        int[] pos = new int[th.length];
        for (int i = 0; i <= q; ++i) {
            pos[i] = i;
        }
        ISsfLoading loading = Loading.from(pos, th);

        if (delta.length() == 1) {
            return Ssf.of(new MaInitialization(q, var), new MaDynamics(var), loading);
        } else {
            DoubleSeq d = delta.drop(1, 0);
            IntegratedInitialization initialization = new IntegratedInitialization(new MaInitialization(q, var), d);
            IntegratedDynamics dynamics = new IntegratedDynamics(new MaDynamics(var), loading, d);
            return Ssf.of(initialization, dynamics, Loading.fromPosition(0));
        }
    }

    public ISsfLoading loading(IArimaModel arima) {
        double var = arima.getInnovationVariance();
        if (var == 0) {
            throw new SsfException(SsfException.STOCH);
        }

        // case IMA
        DoubleSeq delta = arima.getNonStationaryAr().coefficients();
        DoubleSeq phi = arima.getStationaryAr().coefficients();
        DoubleSeq theta = arima.getMa().coefficients();

        if (phi.length() > 1) {
            throw new java.lang.UnsupportedOperationException();
        }

        if (delta.length() == 1 && theta.length() > 1) {
            double[] th = theta.toArray();
            int q = th.length - 1;
            int[] pos = new int[th.length];
            for (int i = 0; i <= q; ++i) {
                pos[i] = i;
            }
            ISsfLoading loading = Loading.from(pos, th);
            return loading;
        } else {
            return Loading.fromPosition(0);
        }
    }

    /**
     * Contains e(t)...e(t-q)
     */
    public static class MaDynamics implements ISsfDynamics {

        final double var, se;

        MaDynamics(double var) {
            this.var = var;
            se = Math.sqrt(var);
        }

        @Override
        public int getInnovationsDim() {
            return 1;
        }

        @Override
        public void V(int pos, FastMatrix qm) {
            qm.set(0, 0, var);
        }

        @Override
        public void S(int pos, FastMatrix cm) {
            cm.set(0, 0, se);
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return true;
        }

        @Override
        public void T(int pos, FastMatrix tr) {
            tr.subDiagonal(-1).set(1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            x.fshiftAndZero();
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.add(0, se * u.get(0));
        }

        @Override
        public void addV(int pos, FastMatrix p) {
            p.add(0, 0, var);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            x.bshiftAndZero();
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            xs.mul(0, se);
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }
    }

    public static class MaInitialization implements ISsfInitialization {

        private final int q;
        private final double var;

        public MaInitialization(int q, double var) {
            this.q = q;
            this.var = var;
        }

        @Override
        public int getStateDim() {
            return q + 1;
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
        public void diffuseConstraints(FastMatrix b) {
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pf0(FastMatrix pf0) {
            pf0.diagonal().set(var);
        }

    }
}
