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
package jdplus.toolkit.base.core.ssf.basic;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.ssf.ISsfLoading;

/**
 *
 * @author Jean Palate
 */
public class IntegratedDynamics implements ISsfDynamics {

    private final ISsfDynamics dynamics;
    private final ISsfLoading loading;
    private final DoubleSeq delta;

    public IntegratedDynamics(final ISsfDynamics dynamics, final ISsfLoading loading, final DoubleSeq delta) {
        this.dynamics = dynamics;
        this.loading = loading;
        this.delta = delta;
    }

    private int order() {
        return delta.length();
    }

    @Override
    public int getInnovationsDim() {
        return dynamics.getInnovationsDim();
    }

    @Override
    public void V(int pos, FastMatrix qm) {
        dynamics.V(pos, qm.dropTopLeft(order(), order()));
    }

    @Override
    public void S(int pos, FastMatrix cm) {
        dynamics.S(pos, cm.dropTopLeft(order(), 0));
    }

    @Override
    public boolean hasInnovations(int pos) {
        return dynamics.hasInnovations(pos);
    }

    @Override
    public boolean areInnovationsTimeInvariant() {
        return dynamics.areInnovationsTimeInvariant();
    }

    @Override
    public void T(int pos, FastMatrix tr) {
        int d = order();
        FastMatrix D = tr.extract(0, d, 0, d);
        D.subDiagonal(-1).set(1);
        DataBlock r0 = tr.row(0);
        r0.extract(0, d).setAY(-1, delta);
        loading.Z(pos, r0.drop(d, 0));
        dynamics.T(pos, tr.dropTopLeft(d, d));
    }

    @Override
    public void TX(int pos, DataBlock x) {
        int d = order();
        DataBlock x0 = x.extract(0, d);
        DataBlock x1 = x.drop(d, 0);
        double z = loading.ZX(pos, x1) - delta.dot(x0);
        x0.fshift(1);
        x0.set(0, z);
        dynamics.TX(pos, x1);
    }

    @Override
    public void addSU(int pos, DataBlock x, DataBlock u) {
        int d = order();
        DataBlock x1 = x.drop(d, 0);
        dynamics.addSU(pos, x1, u);
    }

    @Override
    public void addV(int pos, FastMatrix p) {
        int d = order();
        dynamics.addV(pos, p.dropTopLeft(d, d));
    }

    @Override
    public void XT(int pos, DataBlock x) {
        int d = order();
        DataBlock x0 = x.extract(0, d);
        DataBlock x1 = x.drop(d, 0);
        dynamics.XT(pos, x1);
        double w = x0.get(0);
        loading.XpZd(pos, x1, w);
        x0.bshiftAndZero();
        x0.addAY(-w, delta);
    }

    @Override
    public void XS(int pos, DataBlock x, DataBlock xs) {
        int d = order();
        xs.extract(0, d).set(0);
        DataBlock x1 = x.drop(d, 0);
        dynamics.XS(pos, x1, xs.drop(d, 0));
    }

    @Override
    public boolean isTimeInvariant() {
        return dynamics.isTimeInvariant();
    }

}
