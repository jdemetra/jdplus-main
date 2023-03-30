/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.math.matrices.lapack;

import jdplus.toolkit.base.core.math.matrices.DataPointer;
import jdplus.toolkit.base.core.math.matrices.CPointer;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class GER {

    /**
     * Computes A:=alpha*x*y' + A
     *
     * @param alpha
     * @param x
     * @param y
     * @param A
     */
    public void apply(double alpha, DataPointer x, DataPointer y, FastMatrix A) {
        if (alpha == 0) {
            return;
        }
        int m = A.getRowsCount(), n = A.getColumnsCount(), lda = A.getColumnIncrement(), start = A.getStartPosition();
        int incx = x.inc();
        if (incx == 1) {
            CPointer ca = new CPointer(A.getStorage(), start);
            CPointer cx = (CPointer) x;
            for (int c = 0; c < n; ++c, ca.move(lda)) {
                double z = y.value(c);
                if (z != 0) {
                    ca.addAX(m, z * alpha, cx);
                }
            }
        } else {
            CPointer ca = new CPointer(A.getStorage(), start);
            for (int c = 0; c < n; ++c, ca.move(lda)) {
                double z = y.value(c);
                if (z != 0) {
                    ca.addAX(m, z * alpha, x);
                }
            }
        }
    }
}
