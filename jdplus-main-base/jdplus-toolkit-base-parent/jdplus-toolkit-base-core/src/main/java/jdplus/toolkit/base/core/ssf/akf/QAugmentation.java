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

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.LogSign;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.LowerTriangularMatrix;
import jdplus.toolkit.base.core.math.matrices.SymmetricMatrix;
import jdplus.toolkit.base.core.ssf.State;
import jdplus.toolkit.base.core.ssf.likelihood.DiffuseLikelihood;

/**
 * Q = | chol. S s' | | s q | delta = S^-1 s var(delta) : S^-1
 *
 * @author Jean Palate
 */
public interface QAugmentation {

    public static enum QType {
        NORMAL,
        PARTIAL_TRIANGULARIZATION,
        FULL_TRIANGULARIZATION,
        QR
    }
    
    public static final QType DEFAULT = QType.PARTIAL_TRIANGULARIZATION;
    public static final QType DEFAULT_COLLAPSING = QType.PARTIAL_TRIANGULARIZATION;
    public static final QType DEFAULT_NOCOLLAPSING = QType.NORMAL;

    public static QAugmentation of(QType type) {
        return switch (type) {
            case NORMAL ->
                new QAugmentation3();
            case PARTIAL_TRIANGULARIZATION ->
                new QAugmentation1();
            case FULL_TRIANGULARIZATION ->
                new QAugmentation2();
            case QR ->
                new QRAugmentation();
        };
    }

    void prepare(final int ndiffuse, final int nvars, final int nmax);

    void clear();

    void update(AugmentedUpdateInformation pe);

    boolean canCollapse();

    boolean collapse(AugmentedState state);

    default int getDegreesOfFreedom() {
        return n() - nd();
    }

    /**
     * Number of observations introduced in the augmentation
     *
     * @return
     */
    int n();

    /**
     * Number of diffuse elements (= number of regression variables)
     *
     * @return
     */
    int nd();

    /**
     * Determinantal term
     *
     * @return
     */
    double logDeterminant();

    /**
     * Computes the Cholesky factor of S (= Xl'Xl), writtend S^(1/2)
     *
     * @return A Matrix. Only the lower triangular part of the matrix should be
     * used.
     */
    FastMatrix choleskyS();

    /**
     * The covariance of delta
     *
     * @return S^-1
     */
    default FastMatrix Psi() {
        FastMatrix L = choleskyS();
        L = LowerTriangularMatrix.inverse(L);
        return SymmetricMatrix.LtL(L);
    }

    /**
     * Returns S^-1 * s
     *
     * @return
     */
    DoubleSeq delta();

    /**
     * Return q - s S^-1 s
     *
     * @return
     */
    double ssq();

    public static boolean isNotNull(DataBlock q) {
        for (int i = 0; i < q.length(); ++i) {
            if (Math.abs(q.get(i)) < State.ZERO) {
                return false;
            }
        }
        return true;
    }

    default DiffuseLikelihood likelihood(boolean scalingfactor) {
        LogSign dsl = LogSign.of(choleskyS().diagonal());
        double dcorr = 2 * dsl.getValue();
        return DiffuseLikelihood.builder(n(), nd())
                .ssqErr(ssq())
                .logDeterminant(logDeterminant())
                .diffuseCorrection(dcorr)
                .concentratedScalingFactor(scalingfactor)
                .build();
    }

    default boolean isWellConditioned() {
        return isWellConditioned(choleskyS().diagonal(), COND);
    }

    public static double COND = 16;

    public static boolean isWellConditioned(DoubleSeq d) {
        return isWellConditioned(d, COND);
    }

    public static boolean isWellConditioned(DoubleSeq d, double r) {
        double max = d.max();
        double min = d.min();
        return (max / min <= r);
    }

}
