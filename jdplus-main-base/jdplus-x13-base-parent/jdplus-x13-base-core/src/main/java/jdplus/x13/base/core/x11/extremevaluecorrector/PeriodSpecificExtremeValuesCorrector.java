/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.base.core.x11.extremevaluecorrector;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Doubles;
import nbbrd.design.Development;
import java.util.Arrays;
import jdplus.toolkit.base.core.data.DataBlock;

/**
 * This extremvalueCorrector uses period specific Standarddeviation for the
 * detection of extremevalues, used for Calendarsigma.All or
 * Calendarsigma.Signif if Cochran false
 *
 * @author Christiane Hofer
 */
@Development(status = Development.Status.Exploratory)
public class PeriodSpecificExtremeValuesCorrector extends DefaultExtremeValuesCorrector {

    public PeriodSpecificExtremeValuesCorrector() {
        super();
    }
    private static final double EPS = 1e-15;
    private static final double EPS_STDEV = 1e-5;

    /**
     * Calculates the Standarddeviation for each period
     *
     * @param s
     *
     * @return Standarddeviation for each period
     */
    @Override
    protected double[] calcStdev(DoubleSeq s) {

        double[] stdev;

        int nstart=start;
        if (excludeFcast) {
            s = s.drop(backcastHorizon, forecastHorizon);
            nstart=(start+backcastHorizon)%period;
        }
//      one value for each period
        stdev = new double[period];
        for (int i = 0; i < period; i++) {
            //   int j = i + start > period - 1 ? i + start - period : i + start;
            int j = ((period - nstart) % period + i) % period;
            DoubleSeq sPeriod = s.extract(j, -1, period);
            stdev[i] = calcSingleStdev(sPeriod);
        }
        return stdev;
    }

    @Override
    protected DoubleSeq extremeValuesDetection(DoubleSeq cur, double[] stdev) {
        int n = cur.length();

        double[] w = new double[n];
        Arrays.fill(w, 1);
        double xbar = mul ? 1 : 0;
        for (int iPeriod = 0; iPeriod < period; iPeriod++) {
            double lv, uv;
            boolean isNullStdev = false;
            lv = stdev[iPeriod] * lsigma;
            uv = stdev[iPeriod] * usigma;
            if (Math.abs(stdev[iPeriod]) < EPS_STDEV) {
                isNullStdev = true;
            }

            int j = ((period - start) % period + iPeriod) % period;
            DataBlock dCur = DataBlock.of(cur);
            DataBlock dsPeriod = dCur.extract(j, -1, period);

            if (!isNullStdev) {
                for (int i = 0; i < dsPeriod.length(); i++) {
                    double tt = Math.abs(dsPeriod.get(i) - xbar);
                    if (tt - uv > EPS) {
                        w[i * period + j] = 0;
                    } else if (tt - lv > EPS) {
                        w[i * period + j] = (uv - tt) / (uv - lv);
                    }
                }

            }
        }
        return Doubles.of(w);
    }
}
