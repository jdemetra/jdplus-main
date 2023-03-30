/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.r.modelling;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.stats.linearmodel.LeastSquaresResults;
import jdplus.toolkit.base.core.modelling.RangeMeanTest;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class AutoModelling {
    
    /**
     * 
     * @param y
     * @param period
     * @param groupSize
     * @param trim
     * @return T-Stat of the range-mean regression
     */
    public double rangeMean(double[] y, int period, int groupSize, int trim){
        int ns=groupSize;
        if (ns == 0)
            ns=RangeMeanTest.computeDefaultGroupSize(period, y.length);
        LeastSquaresResults lsr = new RangeMeanTest()
                .groupSize(ns)
                .trim(trim)
                .process(DoubleSeq.of(y));
        return lsr.T(1);
    }
    
}
