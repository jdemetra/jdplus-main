/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.arima.estimation;

import jdplus.toolkit.base.core.arima.IArimaModel;
import internal.toolkit.base.core.arima.KalmanFilter;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.api.data.DoubleSeq;


/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@FunctionalInterface
public interface ResidualsComputer {

    DoubleSeq residuals(IArimaModel arma, DoubleSeq y);
    
    public static ResidualsComputer defaultComputer() {
        return defaultComputer(new KalmanFilter(false));
    }
   
    public static ResidualsComputer defaultComputer(final ArmaFilter filter) {
        return (arma, y) -> {
            int n = y.length();
            int nf = filter.prepare(arma, n);
            DataBlock fres = DataBlock.make(nf);
            filter.apply(y, fres);
            return fres;
        };
    }
}
