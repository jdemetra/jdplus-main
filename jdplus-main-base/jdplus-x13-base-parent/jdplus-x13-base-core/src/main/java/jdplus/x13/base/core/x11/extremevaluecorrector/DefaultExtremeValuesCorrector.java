/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.x13.base.core.x11.extremevaluecorrector;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Doubles;
import jdplus.x13.base.core.x11.X11Context;
import nbbrd.design.Development;

import java.util.Arrays;

/**
 * Default implementation for the correction of extreme values
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Beta)
public class DefaultExtremeValuesCorrector implements IExtremeValuesCorrector {

    private static final double EPS = 1e-15;
    private static final double EPS_STDEV = 1e-5;

    /**
     * Returns the averages by period
     *
     * @param s The analyzed time series
     * @param period
     *
     * @return An array of doubles (length = annual frequency), with the
     * averages for each period (months/quarters) of the year.
     */
    public static double[] periodAverages(DoubleSeq s, int period) {
        double[] outs = new double[period];
        int n = s.length();
        for (int i = 0; i < period; ++i) {
            double a = 0;
            int m = 0;
            int j = i;
            while (j < n) {
                a += s.get(j);
                ++m;
                j = j + period;
            }
            outs[i] = a / m;
        }
        return outs;
    }

    protected boolean mul;
    protected int period; // (approximated) period of the series
    protected int start; // position of the first "complete"/considered period
    protected double lsigma = 1.5, usigma = 2.5;
    protected DoubleSeq scur, sweights;
    private static final int NCYCLES = 5;// window of 5 years
    protected int forecastHorizon, backcastHorizon;
    protected boolean excludeFcast;

    /**
     * Searches the extreme values in a given series
     *
     * @param s The analysed series
     *
     */
    @Override
    public void analyse(final DoubleSeq s, X11Context context) {
        lsigma = context.getLowerSigma();
        usigma = context.getUpperSigma();
        scur = s;
        sweights = null;
        period = context.getPeriod();
        mul = context.isMultiplicative();
        forecastHorizon = context.getForecastHorizon();
        backcastHorizon = context.getBackcastHorizon();
        excludeFcast = context.isExcludefcast();
        // compute standard deviations
        double[] stdev = calcStdev(scur);
        sweights = extremeValuesDetection(scur, stdev);
        if (sweights.anyMatch(x -> x < 1)) {
            DoubleSeq corr = removeExtremes(scur, sweights);
            stdev = calcStdev(corr);
            sweights = extremeValuesDetection(scur, stdev);
        }
    }

    /**
     * Applies the detected corrections to the original series
     *
     * @param original The original series
     * @param corrections The corrections
     *
     * @return The corrected series. A new time series is always returned.
     */
    @Override
    public DoubleSeq applyCorrections(DoubleSeq original, DoubleSeq corrections) {
        double[] ns = original.toArray();
        for (int i = 0; i < ns.length; ++i) {
            double x = corrections.get(i);
            if (!Double.isNaN(x)) {
                ns[i] = x;
            }
        }
        return DoubleSeq.of(ns);
    }

    protected double[] calcStdev(DoubleSeq s) {
        int nstart=start;
        if (excludeFcast) {
            s = s.drop(backcastHorizon, forecastHorizon);
            nstart=(start+backcastHorizon)%period;
        }

        int n = s.length();
        int ny = 1 + (nstart + n - 1) / period;
        //number of full years
        int nfy = ny;
        // number of additional data for the computation of the first
        // stdev (we use 5 (=NCYCLES) complete years (cycles) + nbeg data ) 
        int nbeg; 
        if (nstart == 0){
            // full year
            nbeg=0;
        }else{
            nbeg=period-nstart;
            --nfy;
        }
        // number of observations in an incomplete year at the end of the series
        int nend = (nstart + n ) % period;
        if (nend != 0) {
            --nfy;
        }
        if (nfy < NCYCLES) {
            return new double[]{calcSingleStdev(s)};
        }

        // position of the year corresponding to the first stdev computed exactly on NCYCLES;
        // it is centred (=NCYCLES/2) and increased by 1 if the first cycle is incomplete.
        // the previous stdev are computed on NCYCLES + the incomplete year. 
        int ibeg = NCYCLES / 2;
        if (nbeg > 0) {
            ++ibeg;
        }
        
        // position of the last stdev computed exactly on NCYCLES
        // the next stdev are computed on NCYCLES + the incomplete year. 
        int iend = ibeg + nfy - NCYCLES;
        double[] stdev = new double[ny];
        int pos = nbeg, icur=ibeg;
        while (icur <= iend) {
            DoubleSeq cur = s.range(pos, pos + NCYCLES * period);
            stdev[icur++] = calcSingleStdev(cur);
            pos += period;
        }
        double e;
        // the first cycle is incomplete
        if (nbeg>0)
            e= calcSingleStdev(s.range(0, nbeg + NCYCLES * period));
        else
            e = stdev[ibeg];
        for (int i = 0; i < ibeg; ++i) {
            stdev[i] = e;
        }
        // the last cycle is incomplete...
        if (nend > 0) {
            pos -= period;
            DoubleSeq cur = s.range(pos, n);
            e = calcSingleStdev(cur);
        } else {
            e = stdev[iend];
        }
        for (int i = icur; i < stdev.length; ++i) {
            stdev[i] = e;
        }
        return stdev;
    }

    protected double calcSingleStdev(DoubleSeq data) {
        int n = data.length();
        int nm = 0;
        double e = 0;
        for (int i = 0; i < n; ++i) {
            double x = data.get(i);
            if (Double.isNaN(x)) {
                ++nm;
            } else {
                if (mul) {
                    x -= 1;
                }
                e += x * x;
            }
        }
        return Math.sqrt(e / (n - nm));
    }

    /**
     * Computes the corrections for a given series
     *
     * @param s The series being corrected
     *
     * @return A new time series is always returned. It will contain missing
     * values for the periods that should not be corrected and the actual
     * corrections for the other periods
     */
    @Override
    public DoubleSeq computeCorrections(DoubleSeq s) {
        int n = s.length();
        double[] ns = new double[n];
        double[] avgs = null;
        for (int i = 0; i < n; i++) {
            double e = sweights.get(i);
            if (e == 1.0) {
                ns[i] = Double.NaN;
            } else {
                // correct value
                double x = e * s.get(i);
                //   int[] pos = searchPositionsForOutlierCorrection(i, freq);
                int[] pos;
                pos = searchPositionsForOutlierCorrection(i, period);
                if (pos != null) {
                    for (int k = 0; k < 4; k++) {
                        x += s.get(pos[k]);
                    }
                    x *= 1.0 / (4.0 + e);
                    ns[i] = x;
                } else {
                    if (avgs == null) {
                        avgs = periodAverages(s, period);
                    }
                    ns[i] = avgs[(i) % period];
                }
            }
        }
        return DoubleSeq.of(ns);
    }

    /**
     * Gets the correction factors. The correction factors are computed on the
     * original series, using the weights of each observation. The corrections
     * will depend on the type of the decomposition (multiplicative or not).
     *
     * @return A new series is always returned
     */
    @Override
    public DoubleSeq getCorrectionFactors() {
        int n = sweights.length();
        double[] ns = new double[n];
        Arrays.fill(ns, mul ? 1 : 0);
        for (int i = 0; i < n; ++i) {
            double x = sweights.get(i);
            if (x < 1) {
                double s = scur.get(i);
                if (mul) {
                    ns[i] = s / (1 + x * (s - 1));
                } else {
                    ns[i] = s * (1 - x);
                }
            }
        }
        return DoubleSeq.of(ns);
    }

    @Override
    public DoubleSeq getObservationWeights() {
        return sweights;
    }

    /**
     *
     * @param cur The series being
     * @param stdev
     *
     * @return The weights corresponding to the series
     */
    protected DoubleSeq extremeValuesDetection(DoubleSeq cur, double[] stdev) {
        int n = cur.length();

        double[] w = new double[n];
        Arrays.fill(w, 1);

        double xbar = mul ? 1 : 0;
        int y = 0;
        int nbeg = period - start;

        int ibeg = 0, iend = nbeg;
        while (ibeg < n) {
            double lv, uv;
            boolean isNullStdev = false;
            if (y > stdev.length - 1) {
                lv = stdev[stdev.length - 1] * lsigma;
                uv = stdev[stdev.length - 1] * usigma;
                if (Math.abs(stdev[stdev.length - 1]) < EPS_STDEV) {
                    isNullStdev = true;
                }
            } else {
                lv = stdev[y] * lsigma;
                uv = stdev[y] * usigma;
                if (Math.abs(stdev[y]) < EPS_STDEV) {
                    isNullStdev = true;
                }
            }

            if (!isNullStdev) {
                for (int i = ibeg; i < iend; i++) {
                    double tt = Math.abs(cur.get(i) - xbar);
                    if (tt - uv > EPS) {
                        w[i] = 0.0;
                    } else if (tt - lv > EPS) {
                        w[i] = (uv - tt) / (uv - lv);
                    }
                }
            }
            y++;
            ibeg = iend;
            iend += period;
            if (iend > n) {
                iend = n;
            }
        }
        return Doubles.of(w);
    }

    private DoubleSeq removeExtremes(DoubleSeq in, DoubleSeq weights) {
        double[] cin = in.toArray();

        for (int i = 0; i < cin.length; ++i) {
            if (weights.get(i) == 0) {
                cin[i] = Double.NaN;
            }
        }
        return Doubles.of(cin);
    }

    private int[] searchPositionsForOutlierCorrection(int p, final int period) {
        int lp = 0, up = 0, lb = p, ub = p, k = 0;

        int[] outs = new int[4];
        // look for two positions above value
        while (lb >= period && lp != 2) {
            lb -= period;
            if (sweights.get(lb) == 1.0) {
                lp++;
                outs[k++] = lb;
            }
        }
        // look for two positions below value
        int len = sweights.length();
        while (ub < (len - period) && up != 2) {
            ub += period;
            if (sweights.get(ub) == 1.0) {
                up++;
                outs[k++] = ub;
            }
        }

        if (lp < 2) {
            while (ub < (len - period) && k < 4) {
                ub += period;
                if (sweights.get(ub) == 1.0) {
                    lp++;
                    outs[k++] = ub;
                }
            }
        } else if (up < 2) {
            while (lb >= period && k < 4) {
                lb -= period;
                if (sweights.get(lb) == 1.0) {
                    up++;
                    outs[k++] = lb;
                }
            }
        }

        if (lp + up < 4) {
            return null;
        }
        return outs;
    }

    /**
     * Gets the lower sigma value
     *
     * @return
     */
    public double getLowerSigma() {
        return lsigma;
    }

    /**
     * Gets the upper sigma value
     *
     * @return
     */
    public double getUpperSigma() {
        return usigma;
    }

    /**
     * Sets the limits for the detection of extreme values.
     *
     * @param lsig The low sigma value
     * @param usig The high sigma value
     *
     * @throws IllegalArgumentException An exception is thrown when the limits
     * are invalid (usig <= lsig or lsig <= 0.5).
     */
    @Override
    public void setSigma(double lsig, double usig) {
        if (usig <= lsig || lsig <= 0.5) {
            throw new IllegalArgumentException("Invalid sigma limits");
        }
        lsigma = lsig;
        usigma = usig;
    }

    /**
     * @return the start
     */
    public int getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    @Override
    public void setStart(int start) {
        this.start = start;
    }

}
