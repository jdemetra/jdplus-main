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
import jdplus.toolkit.base.core.ssf.ISsfInitialization;

/**
 *
 * @author Jean Palate
 */
public class IntegratedInitialization implements ISsfInitialization {

    private final ISsfInitialization initialization;
    private final DoubleSeq delta;

    public IntegratedInitialization(final ISsfInitialization initialization, final DoubleSeq delta) {
        this.initialization = initialization;
        this.delta = delta;
    }

    private int order() {
        return delta.length();
    }

    @Override
    public int getStateDim() {
        return initialization.getStateDim() + delta.length();
    }

    @Override
    public boolean isDiffuse() {
        return true;
    }

    @Override
    public int getDiffuseDim() {
        return initialization.getDiffuseDim() + order();
    }

    @Override
    public void diffuseConstraints(FastMatrix b) {
        int d = order();
        b.extract(0, d, 0, d).diagonal().set(1);
        initialization.diffuseConstraints(b.dropTopLeft(d, d));
    }

    @Override
    public void a0(DataBlock a0) {
        initialization.a0(a0.drop(order(), 0));
    }

    @Override
    public void Pf0(FastMatrix pf0) {
        int d = order();
        initialization.Pf0(pf0.dropTopLeft(d, d));
    }

    @Override
    public void Pi0(FastMatrix pi0) {
        int d = order();
        pi0.extract(0, d, 0, d).diagonal().set(1);
        initialization.Pi0(pi0.dropTopLeft(d, d));
    }

}
