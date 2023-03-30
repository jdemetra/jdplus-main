/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.math.matrices.decomposition;

import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixException;
import jdplus.toolkit.base.api.data.DoubleSeq;

/**
 * A = U*S*V'
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface ISingularValueDecomposition {
    void decompose(FastMatrix A)throws MatrixException;
    
    FastMatrix U();
    
    DoubleSeq S();
    
    FastMatrix V();
    
    boolean isFullRank();
    
    int rank();
}
