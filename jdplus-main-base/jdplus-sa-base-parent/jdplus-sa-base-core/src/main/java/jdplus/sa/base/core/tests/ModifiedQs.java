/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.sa.base.core.tests;

import java.util.function.IntToDoubleFunction;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.stats.AutoCovariances;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class ModifiedQs {
    
    public double P900=2.49, P950=3.83, P990=7.06, P999=11.88;

    public double test(DoubleSeq sample, int period) {
        IntToDoubleFunction acf = AutoCovariances.autoCorrelationFunction(sample, 0);
        double a1 = acf.applyAsDouble(period);
        if (a1 < 0) {
            return 0;
        }
        int n = sample.length();
        double res = a1 * a1 / (n - period);
        double a2 = acf.applyAsDouble(2 * period);
        if (a2 > 0) {
            res += a2 * a2 / (n - 2 * period);
        }
        return res* n * (n + 2);
    }
}
