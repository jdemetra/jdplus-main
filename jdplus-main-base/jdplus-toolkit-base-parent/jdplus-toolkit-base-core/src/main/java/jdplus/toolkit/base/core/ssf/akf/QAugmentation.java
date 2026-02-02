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
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.State;
import jdplus.toolkit.base.core.ssf.likelihood.DiffuseLikelihood;

/**
 *
 * @author Jean Palate
 */
public interface QAugmentation {
    
    public static QAugmentation byPartialTriangularization(){
        return new QAugmentation1();
    }

    public static QAugmentation byFullTriangularization(){
        return new QAugmentation2();
    }

    public static QAugmentation normalEquation(){
        return new QAugmentation2();
    }

    public static QAugmentation defaultQ(){
        return new QAugmentation3();
    }

    void prepare(final int nd, final int nvars);

    void clear();
    
    void update(AugmentedUpdateInformation pe);

    DiffuseLikelihood likelihood(boolean scalingfactor);
    
    boolean canCollapse();

    boolean collapse(AugmentedState state);
    
    int getDegreesOfFreedom();
    
    /**
     * Computes the Cholesky factor of S (= Xl'Xl)
     * @return A Matrix. Only the lower triangular part of the matrix should be used.
     */
    FastMatrix choleskyS();
    
    /**
     * Returns S^-1 * s
     * @return 
     */
    DoubleSeq delta();
    
    /**
     * Return q - s S^-1 s 
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
    
    default boolean isWellConditioned(){
        return isWellConditioned(choleskyS().diagonal(), COND);
    }
    
    public static double COND=16;
    
    public static boolean isWellConditioned(DoubleSeq d) {
        return isWellConditioned(d, COND);
    }
    
    public static boolean isWellConditioned(DoubleSeq d, double r) {
        double max = d.max();
        double min = d.min();
        return (max / min <= r);
    }

}
