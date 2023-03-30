/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.r.util;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.stats.RobustStandardDeviationComputer;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Outliers {
    double MAD(double[] data, double centile){
        return RobustStandardDeviationComputer.mad(centile, true).compute(DoubleSeq.of(data));
    }
}
