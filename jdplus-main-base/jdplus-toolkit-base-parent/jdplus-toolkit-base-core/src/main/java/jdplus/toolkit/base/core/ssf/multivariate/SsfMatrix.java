/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.ssf.multivariate;

import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.univariate.ISsfData;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;


/**
 *
 * @author Jean Palate
 */
public class SsfMatrix implements IMultivariateSsfData {

    private final FastMatrix x;
    private final int nconstraints;

    public SsfMatrix(FastMatrix x) {
        this.x = x;
        nconstraints=0;
    }

     public SsfMatrix(FastMatrix x, int nconstraints) {
        this.x = x;
        this.nconstraints=nconstraints;
    }

   @Override
    public double get(int pos, int v) {
        return pos < x.getRowsCount() ? x.get(pos, v) : Double.NaN;
    }

    @Override
    public boolean isMissing(int pos, int v) {
        if (pos >= x.getRowsCount()) {
            return true;
        }
        double y = x.get(pos, v);
        return !Double.isFinite(y);
    }

    @Override
    public boolean isConstraint(int pos, int v) {
        return v<nconstraints;
    }

    @Override
    public int getObsCount() {
        return x.getRowsCount();
    }

    @Override
    public int getVarsCount() {
        return x.getColumnsCount();
    }


    @Override
    public DoubleSeq get(int pos) {
        return pos < x.getRowsCount() ? x.row(pos) : DataBlock.EMPTY;
    }
    
    public ISsfData asSsfData(){
        if (x.getColumnsCount() == 1)
            return new SsfData(x.column(0));
        else
            return M2uAdapter.of(this);
    }
}
